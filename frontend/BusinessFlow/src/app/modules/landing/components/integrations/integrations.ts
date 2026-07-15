import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-integrations',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './integrations.html',
  styleUrls: ['./integrations.scss']
})
export class IntegrationsComponent {
  integrations = signal([
    { name: 'Salesforce', icon: 'bi-cloud-arrow-up-fill', color: 'text-primary' },
    { name: 'Slack', icon: 'bi-slack', color: 'text-danger' },
    { name: 'Hubspot', icon: 'bi-diagram-3-fill', color: 'text-warning' },
    { name: 'Google Workspace', icon: 'bi-google', color: 'text-primary' },
    { name: 'Microsoft Teams', icon: 'bi-microsoft-teams', color: 'text-primary' },
    { name: 'QuickBooks', icon: 'bi-calculator-fill', color: 'text-success' }
  ]);
}
