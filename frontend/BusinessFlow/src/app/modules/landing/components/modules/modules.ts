import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-modules',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './modules.html',
  styleUrls: ['./modules.scss']
})
export class ModulesComponent {
  modulesList = signal([
    { title: 'CRM', icon: 'bi-person-lines-fill', desc: 'Track sales, pipelines, and customer interactions.' },
    { title: 'HRM', icon: 'bi-people-fill', desc: 'Manage employees, payroll, and time off.' },
    { title: 'Finance', icon: 'bi-wallet-fill', desc: 'Accounting, ledgers, and automated invoicing.' },
    { title: 'Inventory', icon: 'bi-box-seam', desc: 'Stock control and multi-warehouse management.' },
    { title: 'Procurement', icon: 'bi-cart-check-fill', desc: 'Purchase orders and vendor management.' },
    { title: 'Service Desk', icon: 'bi-headset', desc: 'ITSM, ticketing, and SLA tracking.' },
    { title: 'Support', icon: 'bi-chat-left-dots-fill', desc: 'Omnichannel customer service portal.' },
    { title: 'AI Assistant', icon: 'bi-stars', desc: 'Generative AI for drafting and insights.' },
    { title: 'Document Management', icon: 'bi-file-earmark-text-fill', desc: 'Secure cloud storage and version control.' },
    { title: 'Workflow Engine', icon: 'bi-arrow-repeat', desc: 'Custom state machines and approvals.' },
    { title: 'Analytics', icon: 'bi-bar-chart-fill', desc: 'Custom dashboards and real-time reports.' },
    { title: 'Notifications', icon: 'bi-bell-fill', desc: 'Push, email, and SMS alerts engine.' }
  ]);
}
