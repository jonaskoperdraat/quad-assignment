import { bootstrapApplication } from '@angular/platform-browser';
import { triviaConfig } from './trivia/trivia.config';
import { Trivia } from './trivia/trivia';

bootstrapApplication(Trivia, triviaConfig)
  .catch((err) => console.error(err));
