import {Component, computed, inject} from '@angular/core';
import {TriviaStateService} from '../shared/trivia-state.service';
import {DecimalPipe} from '@angular/common';
import {MatIcon} from '@angular/material/icon';
import {MatChip} from '@angular/material/chips';

@Component({
  selector: 'trivia-stats',
  imports: [
    DecimalPipe,
    MatIcon,
    MatChip
  ],
  template: `
    <div class="stats">
      <mat-chip disabled>
        <span class="chip-content" style="color: green">
          <mat-icon fontIcon="check_circle" /> {{state.answeredCorrectly()}}
        </span>
      </mat-chip>
      <mat-chip disabled>
        <span class="chip-content" style="color: red">
          <mat-icon fontIcon="cancel" /> {{state.answeredIncorrectly()}}
        </span>
      </mat-chip>
      <mat-chip disabled>
        <span class="chip-content" style="color: #666">
          <mat-icon fontIcon="percent" /> {{ percentage() * 100 | number: '1.1-1' }}
        </span>
      </mat-chip>
    </div>
  `,
  styles: `
    .stats {
      display: flex;
      justify-content: right;
      align-items: center;
      gap: 1rem;
      flex-wrap: wrap;

      .chip-content {
        display: inline-flex;
        align-items: center;
        gap: 0.35rem;
        transform: translatey(2.5px);

        mat-icon {
          font-size: 1.125rem;
          width: 1.125rem;
          height: 1.125rem;
          line-height: 1.125rem;
        }
      }

    }
  `,
})
export class Stats {
  protected readonly state = inject(TriviaStateService);

  readonly percentage = computed(() => {
    const totalAnswers = this.state.answeredCorrectly() + this.state.answeredIncorrectly();
    if (totalAnswers === 0) return 0;
    return this.state.answeredCorrectly() / totalAnswers;
  });
}
