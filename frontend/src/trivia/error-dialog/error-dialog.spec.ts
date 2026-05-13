import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TriviaErrorDialog } from './error-dialog';

describe('ErrorDialog', () => {
  let component: TriviaErrorDialog;
  let fixture: ComponentFixture<TriviaErrorDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TriviaErrorDialog],
    }).compileComponents();

    fixture = TestBed.createComponent(TriviaErrorDialog);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
