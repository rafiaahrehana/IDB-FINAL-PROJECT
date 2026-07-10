import { Routes } from '@angular/router';
import { Requests } from './components/requests/requests';
import { RequestDetail } from './components/request-detail/request-detail';
import { Approvals } from './components/approvals/approvals';
import { KnowledgeBase } from './components/knowledge-base/knowledge-base';
import { Categories } from './components/categories/categories';
import { Services } from './components/services/services';
import { Packages } from './components/packages/packages';
import { Workflows } from './components/workflows/workflows';
import { Templates } from './components/templates/templates';
import { Reviews } from './components/reviews/reviews';
import { ServiceFormFields } from './components/service-form-fields/service-form-fields';

// NOTE: 'quotations' route/files were deleted outright (see below) - the standalone
// QuotationController was removed server-side, there is no independent list/CRUD
// endpoint anymore. Quotation data now lives as embedded fields on ServiceRequestResponse,
// accessed via /service-requests/{id}/quotation(/accept|/reject). Quotation submit/
// accept/reject is now available directly on the Request Detail page via
// ServiceRequestService. The old Quotations page/service/model
// (components/quotations, quotation.service.ts, Quotation/QuotationRequest interfaces)
// called dead endpoints and have been deleted.
export const SERVICEDESK_ROUTES: Routes = [
  { path: 'requests', component: Requests },
  { path: 'requests/:id', component: RequestDetail },
  { path: 'approvals', component: Approvals },
  { path: 'kb', component: KnowledgeBase },
  { path: 'categories', component: Categories },
  { path: 'services', component: Services },
  { path: 'services/:id/form-fields', component: ServiceFormFields },
  { path: 'packages', component: Packages },
  { path: 'workflows', component: Workflows },
  { path: 'templates', component: Templates },
  { path: 'reviews', component: Reviews },
  { path: '', redirectTo: 'requests', pathMatch: 'full' }
];
