import { Routes } from '@angular/router';
import { Meetings } from './components/meetings/meetings';

export const MEETING_ROUTES: Routes = [
  { path: '', component: Meetings },
  { path: '**', redirectTo: '' }
];
