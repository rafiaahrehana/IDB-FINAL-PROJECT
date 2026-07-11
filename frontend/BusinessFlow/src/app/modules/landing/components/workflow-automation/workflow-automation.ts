import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-workflow-automation',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './workflow-automation.html',
  styleUrls: ['./workflow-automation.scss']
})
export class WorkflowAutomationComponent {
  steps = [
    { label: 'Request', icon: 'bi-box-arrow-in-right', color: 'primary' },
    { label: 'Approval', icon: 'bi-check2-circle', color: 'warning' },
    { label: 'Task', icon: 'bi-list-task', color: 'primary' },
    { label: 'Review', icon: 'bi-search', color: 'accent' },
    { label: 'Invoice', icon: 'bi-receipt', color: 'primary' },
    { label: 'Completed', icon: 'bi-flag', color: 'success' }
  ];
}
