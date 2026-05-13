import {computed, inject, Injectable, signal} from '@angular/core';
import {Category, Difficulty, TriviaService, Type} from './api';
import {rxResource} from '@angular/core/rxjs-interop';

type QuestionParms = {
  category: Category | undefined;
  difficulty: Difficulty | undefined;
  type: Type | undefined;
};

/**
 * A shared service that holds the state of the trivia app.
 * Communication between components is mostly done via signals.
 */
@Injectable({ providedIn: 'root'})
export class TriviaStateService {

  readonly #api = inject(TriviaService)

  // Filter properties
  readonly category = signal<Category | undefined>(undefined);
  readonly difficulty = signal<Difficulty | undefined>(undefined);
  readonly type = signal<Type | undefined>(undefined);

  // Initialization
  readonly initialized = computed<boolean>(() => !this.#filterInitializing())
  readonly #filterInitializing = signal(true);
  filterInitialized(): void {
    this.#filterInitializing.set(false)
  }

  // Question retrieval
  readonly #questionParams = signal<QuestionParms | undefined>(undefined);
  readonly active = signal(false);
  readonly question = rxResource({
    params: () => this.#questionParams(),
    stream: ({ params }) => {
      return this.#api.getQuestion(params.type, params.difficulty, params.category?.id, undefined)
    }
  });
  readonly hasQuestion = computed(() => this.question.hasValue());
  readonly questionNeedsResolution = computed(() =>
    this.hasQuestion() && (!this.selectedAnswer() || !this.verifiedAnswer())
  );

  // Answer verification
  readonly selectedAnswer = signal<string | undefined>(undefined);
  readonly checkingAnswer = signal(false);
  readonly verifiedAnswer = signal<string | undefined>(undefined);

  // Statistics
  readonly answeredCorrectly = signal(0);
  readonly answeredIncorrectly = signal(0);

  nextQuestion() {
    this.selectedAnswer.set(undefined);
    this.verifiedAnswer.set(undefined);
    this.active.set(true);

    this.#questionParams.set({
      category: this.category(),
      difficulty: this.difficulty(),
      type: this.type()
    })
  }

  checkAnswer() {
    const answer = this.selectedAnswer();
    if (answer) {
      this.checkingAnswer.set(true);
      this.#api.checkAnswer({questionId: this.question.value()!.id, answer})
        .subscribe(({
          isCorrect,
          correctAnswer
                    }) => {
          this.verifiedAnswer.set(isCorrect ? answer : correctAnswer);
          this.answeredCorrectly.set(isCorrect ? this.answeredCorrectly() + 1 : this.answeredCorrectly());
          this.answeredIncorrectly.set(isCorrect ? this.answeredIncorrectly() : this.answeredIncorrectly() + 1);
          this.checkingAnswer.set(false);
        });
    }
  }

  cancelQuestion() {
    this.#questionParams.set(undefined);
  }

  retryQuestion() {
    this.selectedAnswer.set(undefined)
    this.verifiedAnswer.set(undefined)
    this.question.reload();
  }

  resetSession() {
    this.#api.sessionReset(undefined).subscribe({
      next: () => {
        this.selectedAnswer.set(undefined);
        this.verifiedAnswer.set(undefined);
        this.nextQuestion();
      },
      error: error => {
        console.error('Failed to reset session', error);
      },
    });
  }

}
