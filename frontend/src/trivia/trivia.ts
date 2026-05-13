import {Component, effect, inject, signal} from '@angular/core';
import {Filter} from './filter/filter';
import {Controls} from './controls/controls';
import {Question} from './question/question';
import {Stats} from './stats/stats';
import {TriviaStateService} from './shared/trivia-state.service';
import {MatDialog} from '@angular/material/dialog';
import {TriviaErrorDialog, TriviaErrorDialogData, TriviaErrorDialogAction} from './error-dialog/error-dialog';
import {HttpErrorResponse} from '@angular/common/http';

@Component({
  selector: 'trivia',
  imports: [Filter, Question, Controls, Stats],
  template: `
    <main class="app-shell mat-elevation-z4">
      <h1>Quad Trivia App</h1>
      <trivia-filter></trivia-filter>
      <trivia-question></trivia-question>
      <trivia-controls></trivia-controls>
      <trivia-stats></trivia-stats>
    </main>
  `,
  styles: `
    :host {
      min-height: 100vh;
      display: grid;
      place-items: center;
      padding: 2rem;
      box-sizing: border-box;
    }

    .app-shell {
      width: min(100%, 56rem);
      padding: 2rem;
      border-radius: 1.5rem;
      background: var(--mat-sys-surface);
      color: var(--mat-sys-on-surface);
    }

    h1 {
      text-align: center;
    }
  `
})
export class Trivia {
  private readonly state = inject(TriviaStateService);
  private readonly dialog = inject(MatDialog);

  constructor() {
    effect(() => {
      const error = this.state.question.error();

      if (!error) {
        return
      }

      this.openQuestionErrorDialog(error);
    })
  }

  private openQuestionErrorDialog(error: unknown) {
    const data = this.toDialogData(error);

    console.log('Opening error dialog with data:', data);

    const ref = this.dialog.open<TriviaErrorDialog, TriviaErrorDialogData, TriviaErrorDialogAction>(
      TriviaErrorDialog,
      {
        data,
        disableClose: true
      }
    );

    ref.afterClosed().subscribe((action) => {
      if (action === 'retry') {
        this.state.retryQuestion();
      }

      if (action === 'cancel') {
        this.state.cancelQuestion();
      }

      if (action === 'reset-session') {
        this.state.resetSession();
      }
    });
  }

  private toDialogData(error: unknown): TriviaErrorDialogData {
    if (error instanceof HttpErrorResponse) {
      const apiError = error.error as Partial<{
        code: string;
        message: string;
        detail?: string;
      }> | undefined

      // Too many requests --> retry
      if (error.status === 429) {
        return {
          title: 'Too many requests',
          message: apiError?.message ?? 'The server is temporarily unable to handle the request due to a high volume of requests.',
          detail: apiError?.detail,
          primaryAction: {
            label: 'Try again',
            value: 'retry',
            cooldownMs: 5000
          }
        }
      }

      // Session empty --> reset session | change filter (= close)
      if (error.status === 409) {
        return {
          title: 'No matching questions left',
          message: apiError?.message || 'For the current filter, you have exhausted the questions.',
          detail: apiError?.detail || 'You can either reset the session or change the filter.',
          primaryAction: {
            label: 'Reset session',
            value: 'reset-session'
          },
          secondaryAction: {
            label: 'Change filter',
            value: 'cancel'
          }


        }
      }
      return {
        title: 'Could not load question',
        message: apiError?.message ?? `The server returned HTTP ${error.status}`,
        primaryAction: {
          label: 'Try again',
          value: 'retry'
        }
      }
    }

    return {
      title: 'Something went wrong',
      message: 'An unexpected error occurred while loading the question.',
      primaryAction: {
        label: 'Try again',
        value: 'retry'
      }
    }

  }
}
