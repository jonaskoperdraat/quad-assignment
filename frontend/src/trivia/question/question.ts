import {Component, inject} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {TriviaStateService} from '../shared/trivia-state.service';
import {MatRadioButton, MatRadioGroup} from '@angular/material/radio';
import {NgClass} from '@angular/common';
import {MatProgressSpinner} from '@angular/material/progress-spinner';
import {MatCard, MatCardActions, MatCardContent, MatCardFooter} from '@angular/material/card';
import {MatChip, MatChipSet} from '@angular/material/chips';

@Component({
  selector: 'trivia-question',
  imports: [FormsModule, MatRadioButton, MatRadioGroup, NgClass, MatProgressSpinner, MatCard, MatCardFooter, MatChip, MatCardContent, MatCardActions, MatChipSet],
  template: `
    <mat-card class="question-card">
      @if(!state.active()) {
        <mat-card-content>Welcome to Quad Triva! Start by fetching the next question!</mat-card-content>
        <mat-card-actions></mat-card-actions>
      } @else {
        @if (state.question.isLoading()) {
          <mat-card-content>
            <mat-spinner />
          </mat-card-content>
        } @else if (state.question.error()) {
          <mat-card-content>
            Something went wrong while loading the question.
          </mat-card-content>
        } @else if (state.question.value(); as q) {
          <mat-card-content>
            {{ q.question }}
          </mat-card-content>
          <mat-card-actions>
            <mat-radio-group aria-labelledby="answer-radio-group" [(ngModel)]="state.selectedAnswer">
              @for (answer of q.answers; track answer) {
                <mat-radio-button [aria-label]="answer"
                                  [value]="answer"
                                  [disabled]="state.verifiedAnswer()"
                                  [ngClass]="{
                                  'correct': state.verifiedAnswer() && state.verifiedAnswer() == answer,
                                  'incorrect': state.verifiedAnswer() && state.verifiedAnswer() != answer && state.selectedAnswer() == answer
                                }">
                  {{ answer }}
                </mat-radio-button>
              }
            </mat-radio-group>
          </mat-card-actions>
          <mat-card-footer>
            <mat-chip-set>
              <mat-chip disabled>{{ q.category }}</mat-chip>
              <mat-chip disabled>{{ q.difficulty }}</mat-chip>
            </mat-chip-set>
          </mat-card-footer>
        }
      }
    </mat-card>
  `,
  styles: `
    @use '@angular/material' as mat;
    .correct, .incorrect {
      @include mat.radio-overrides((
        label-text-weight: bold
      ));
    }

    .correct {
      @include mat.radio-overrides((
        disabled-label-color: green,
        disabled-selected-icon-color: green,
        disabled-unselected-icon-color: green
      ));
    }
    .incorrect {
      @include mat.radio-overrides((
        disabled-label-color: red,
        disabled-selected-icon-color: red
      ));
    }

    .question-card {
      min-height: 24rem;
      display: flex;
      flex-direction: column;

      mat-card-content {
        flex: 1;
        display: grid;
        place-items: center;
        text-align: center;
      }

      mat-card-actions {
        flex: 0 0 auto;
        justify-content: center;
      }

      mat-card-footer {
        display: flex;
        flex: 0 0 auto;
        justify-content: right;
      }

      mat-card-footer mat-chip-set {
        float: right;
        margin-right: 0.25rem
      }
      //
      //mat-card-content {
      //  margin-block-start: 2rem;
      //  text-align: center;
      //}
      //
      mat-card-content, mat-card-actions {
        font-size: 1.2rem;
      //
        @include mat.radio-overrides((
          label-text-size: 1.2rem
        ))
      }
    }

  `
})
export class Question {
  protected readonly state = inject(TriviaStateService);
}
