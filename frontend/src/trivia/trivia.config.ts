import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import {provideHttpClient} from '@angular/common/http';
import {provideApi} from './shared/api';

export const triviaConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideHttpClient(),
    provideApi('http://localhost:4200/api')
  ]
};
