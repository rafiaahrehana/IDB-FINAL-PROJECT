import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-dashboard-preview',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard-preview.html',
  styleUrls: ['./dashboard-preview.scss']
})
export class DashboardPreviewComponent {
  tabs = signal(['CRM', 'HRM', 'Finance', 'Inventory', 'Support']);
  activeTab = signal('CRM');

  setActiveTab(tab: string) {
    this.activeTab.set(tab);
  }
}
