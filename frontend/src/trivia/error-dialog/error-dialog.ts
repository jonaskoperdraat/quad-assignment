import {Component, computed, inject, signal} from '@angular/core';
import {TriviaStateService} from '../shared/trivia-state.service';
import {
  MAT_DIALOG_DATA,
  MatDialogActions,
  MatDialogClose,
  MatDialogContent,
  MatDialogTitle
} from '@angular/material/dialog';
import {MatButton} from '@angular/material/button';

export type TriviaErrorDialogAction = 'reset-session' | 'retry' | 'cancel'

export interface TriviaErrorDialogButton {
  label: string;
  value: TriviaErrorDialogAction;
  cooldownMs?: number;
}

export interface TriviaErrorDialogData {
  title: string
  message: string
  detail?: string
  primaryAction: {
    label: string,
    value: TriviaErrorDialogAction,
    cooldownMs?: number
  },
  secondaryAction?: {
    label: string,
    value: TriviaErrorDialogAction
  }
}

@Component({
  selector: 'trivia-controls',
  imports: [
    MatDialogContent,
    MatDialogActions,
    MatDialogClose,
    MatDialogTitle,
    MatButton
  ],
  template: `
    <h2 mat-dialog-title>{{ data.title }}</h2>

    <mat-dialog-content>
      <p>
        {{ data.message }}
      </p>
      @if (data.detail) {
        <p>
            {{ data.detail }}
        </p>
      }
    </mat-dialog-content>

    <mat-dialog-actions align="end">

      @if (data.secondaryAction; as btn) {
        <button matButton="outlined" [mat-dialog-close]="btn.value">
          {{ btn.label }}
        </button>
      }

      @if (data.primaryAction; as btn) {
        <button matButton="outlined"
                class="cooldown-button"
                [style.--cooldown-progress.%]="progress()"
                [disabled]="disabled()"
                [mat-dialog-close]="btn.value">
          <span class="cooldown-button-label">
              {{ label() }}
          </span>
        </button>
      }
    </mat-dialog-actions>
  `,
  styles: `
    .cooldown-button {
      position: relative;
      overflow: hidden;
    }

    .cooldown-button::before {
      content: '';
      position: absolute;
      inset: 0;
      width: var(--cooldown-progress, 0%);
      background: color-mix(in srgb, var(--mat-sys-primary) 20%, transparent);
      transition: width 100ms linear;
      pointer-events: none;
    }

    .cooldown-button-label {
      position: relative;
      z-index: 1;
    }
  `,
})
export class TriviaErrorDialog {
  protected readonly data = inject<TriviaErrorDialogData>(MAT_DIALOG_DATA);

  protected readonly remainingMs = signal(this.data.primaryAction?.cooldownMs ?? 0)

  protected readonly progress = computed(() =>
    this.calcProgress(this.data.primaryAction.cooldownMs, this.remainingMs())
  );

  protected readonly disabled = computed(() => this.remainingMs() > 0);

  protected readonly label = computed(() =>
    this.calcLabel(this.data.primaryAction.label, this.remainingMs())
  );

  constructor() {
    this.startCooldown(this.data.primaryAction?.cooldownMs, this.remainingMs);
  }

  private startCooldown(
    cooldownMs: number | undefined,
    remainingMs: ReturnType<typeof signal<number>>
  ): void {
    if (!cooldownMs || cooldownMs <= 0) {
      return;
    }

    const interval  = window.setInterval(() => {
      remainingMs.update(remaining => {
        const next = Math.max(remaining - 100, 0);

        if (next === 0) {
          window.clearInterval(interval);
        }

        return next;
      })
    }, 100);
  }

  private calcProgress(cooldownMs: number | undefined, remainingMs: number): number {
    if (!cooldownMs || cooldownMs <= 0) {
      return 100;
    }

    return 100 - Math.round((remainingMs / cooldownMs) * 100);
  }

  private calcLabel(label: string, remainingMs: number): string {
    if (remainingMs <= 0) {
      return label;
    }

    return `${label} (${Math.round(remainingMs / 1000)}s)`;
  }

}
