import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-security',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './security.html',
  styleUrls: ['./security.scss']
})
export class SecurityComponent {
  features = signal([
    { title: 'JWT Authentication', icon: 'bi-key-fill' },
    { title: 'Role Based Access', icon: 'bi-person-badge-fill' },
    { title: 'Audit Logs', icon: 'bi-journal-check' },
    { title: 'Encrypted Storage', icon: 'bi-hdd-network-fill' },
    { title: 'Tenant Isolation', icon: 'bi-building-lock' },
    { title: 'Secure APIs', icon: 'bi-shield-shaded' }
  ]);
}
