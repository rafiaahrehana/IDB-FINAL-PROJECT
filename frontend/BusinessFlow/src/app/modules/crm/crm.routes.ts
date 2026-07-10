import { Routes } from '@angular/router';
import { Leads } from './components/leads/leads';
import { PipelineBoard } from './components/pipeline-board/pipeline-board';
import { Clients } from './components/clients/clients';
import { ClientDetail } from './components/client-detail/client-detail';

export const CRM_ROUTES: Routes = [
  { path: 'leads', component: Leads },
  { path: 'pipeline', component: PipelineBoard },
  { path: 'clients', component: Clients },
  { path: 'clients/:id', component: ClientDetail },
  { path: '', redirectTo: 'pipeline', pathMatch: 'full' }
];
