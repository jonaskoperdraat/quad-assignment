import {Component, inject} from '@angular/core';
import {TriviaStateService} from '../shared/trivia-state.service';
import {MatButton} from '@angular/material/button';

@Component({
  selector: 'trivia-controls',
  imports: [
    MatButton
  ],
  template: `
    <div class="controls">
        <button matButton="outlined"
                (click)="state.checkAnswer()"
                [disabled]="state.checkingAnswer()
                  || !state.selectedAnswer()
                  || state.verifiedAnswer()">
          Check answer</button>

        <button matButton="outlined"
                (click)="state.nextQuestion()"
                [disabled]="!this.state.initialized()
                  || this.state.checkingAnswer()
                  || state.question.error()
                  || state.questionNeedsResolution()">
          Next question</button>
    </div>
  `,
  styles: `
  .controls {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 1rem;
    flex-wrap: wrap;
    margin-block-start: 1.5rem;
  }`,
})
export class Controls {
  protected readonly state = inject(TriviaStateService);

}
