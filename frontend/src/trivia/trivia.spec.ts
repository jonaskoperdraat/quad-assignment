import { TestBed } from '@angular/core/testing';
import { Trivia } from './trivia';

describe('Trivia', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Trivia],
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(Trivia);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should render title', async () => {
    const fixture = TestBed.createComponent(Trivia);
    await fixture.whenStable();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('h1')?.textContent).toContain('Hello, frontend');
  });
});
