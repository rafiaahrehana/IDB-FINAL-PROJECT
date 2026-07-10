import { Routes } from '@angular/router';
import { Tasks } from './components/tasks/tasks';

export const TASK_ROUTES: Routes = [
  { path: '', component: Tasks },
  { path: '**', redirectTo: '' }
];
