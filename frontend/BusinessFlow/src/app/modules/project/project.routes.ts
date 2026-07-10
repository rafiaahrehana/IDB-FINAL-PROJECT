import { Routes } from '@angular/router';
import { Projects } from './components/projects/projects';

export const PROJECT_ROUTES: Routes = [
  { path: '', component: Projects },
  { path: '**', redirectTo: '' }
];
