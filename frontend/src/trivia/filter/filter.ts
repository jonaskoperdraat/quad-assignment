import {Component, inject, Injectable} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {Difficulty, TriviaService, Type} from '../shared/api';
import {rxResource} from '@angular/core/rxjs-interop';
import {TriviaStateService} from '../shared/trivia-state.service';
import {MatFormField, MatLabel} from '@angular/material/input';
import {MatOption, MatSelect} from '@angular/material/select';
import {finalize} from 'rxjs';
import {MatProgressBar} from '@angular/material/progress-bar';

@Component({
  selector: 'trivia-filter',
  imports: [FormsModule, MatFormField, MatLabel, MatSelect, MatOption, MatProgressBar],
  template: `

    @if (categories.isLoading()) {
      <mat-progress-bar mode="indeterminate"></mat-progress-bar>
    } @else {
      <div class="filter-form">
        <mat-form-field>
          <mat-label>Category</mat-label>
          <mat-select [(value)]="state.category" canSelectNullableOptions="true">
            <mat-option [value]="undefined"> - Any - </mat-option>
            @for (cat of categories.value(); track cat.id) {
              <mat-option [value]="cat">{{ cat.name }}</mat-option>
            }
          </mat-select>
        </mat-form-field>
        <mat-form-field>
          <mat-label>Difficulty</mat-label>
          <mat-select [(value)]="state.difficulty" canSelectNullableOptions="true">
            <mat-option [value]="undefined"> - Any - </mat-option>
            <mat-option [value]="Difficulty.Easy">Easy</mat-option>
            <mat-option [value]="Difficulty.Medium">Medium</mat-option>
            <mat-option [value]="Difficulty.Hard">Hard</mat-option>
          </mat-select>
        </mat-form-field>
        <mat-form-field>
          <mat-label>Type</mat-label>
          <mat-select [(value)]="state.type" canSelectNullableOptions="true">
            <mat-option [value]="undefined"> - Any - </mat-option>
            <mat-option [value]="Type.Boolean">Boolean</mat-option>
            <mat-option [value]="Type.Multiple">Multiple choice</mat-option>
          </mat-select>
        </mat-form-field>
      </div>
    }
  `,
  styles: `
    .filter-form {
      display: flex;
      justify-content: center;
      align-items: center;
      gap: 1rem;
      flex-wrap: wrap;
    }

    mat-form-field {
      width: 14rem;
    }

    mat-progress-bar {
      margin-inline: auto;
    }
  `
})
@Injectable({providedIn: 'root'})
export class Filter {
  protected readonly Difficulty = Difficulty;
  protected readonly Type = Type;

  readonly #api = inject(TriviaService);
  protected readonly state = inject(TriviaStateService);

  categories = rxResource({
    stream: () => this.#api.getCategories().pipe(
      finalize(() => this.state.filterInitialized())
    )
  });


}
