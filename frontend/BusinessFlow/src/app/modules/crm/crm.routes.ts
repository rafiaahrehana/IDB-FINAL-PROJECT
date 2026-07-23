import { Routes } from '@angular/router';
import { Leads } from './components/leads/leads';
import { PipelineBoard } from './components/pipeline-board/pipeline-board';
import { PipelineReports } from './components/pipeline-reports/pipeline-reports';
import { Clients } from './components/clients/clients';
import { ClientDetail } from './components/client-detail/client-detail';

export const CRM_ROUTES: Routes = [
  { path: 'leads', component: Leads, data: { requiredPermission: 'LEAD_VIEW' } },
  { path: 'pipeline', component: PipelineBoard, data: { requiredPermission: 'OPPORTUNITY_VIEW' } },
  { path: 'pipeline/reports', component: PipelineReports, data: { requiredPermission: 'OPPORTUNITY_VIEW' } },
  { path: 'clients', component: Clients, data: { requiredPermission: 'CLIENT_VIEW' } },
  { path: 'clients/:id', component: ClientDetail, data: { requiredPermission: 'CLIENT_VIEW' } },
  { path: '', redirectTo: 'pipeline', pathMatch: 'full' }
];
