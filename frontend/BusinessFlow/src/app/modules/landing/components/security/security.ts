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
    { title: 'JWT Authentication', icon: 'bi-key-fill', border: 'rgba(59, 130, 246, 0.25)', hover: 'rgba(59, 130, 246, 0.8)', bg: 'rgba(59, 130, 246, 0.04)', iconBg: 'linear-gradient(135deg, #3b82f6, #1d4ed8)' },
    { title: 'Role Based Access', icon: 'bi-person-badge-fill', border: 'rgba(16, 185, 129, 0.25)', hover: 'rgba(16, 185, 129, 0.8)', bg: 'rgba(16, 185, 129, 0.04)', iconBg: 'linear-gradient(135deg, #10b981, #059669)' },
    { title: 'Audit Logs', icon: 'bi-journal-check', border: 'rgba(245, 158, 11, 0.25)', hover: 'rgba(245, 158, 11, 0.8)', bg: 'rgba(245, 158, 11, 0.04)', iconBg: 'linear-gradient(135deg, #f59e0b, #d97706)' },
    { title: 'Encrypted Storage', icon: 'bi-hdd-network-fill', border: 'rgba(139, 92, 246, 0.25)', hover: 'rgba(139, 92, 246, 0.8)', bg: 'rgba(139, 92, 246, 0.04)', iconBg: 'linear-gradient(135deg, #8b5cf6, #7c3aed)' },
    { title: 'Tenant Isolation', icon: 'bi-building-lock', border: 'rgba(244, 63, 94, 0.25)', hover: 'rgba(244, 63, 94, 0.8)', bg: 'rgba(244, 63, 94, 0.04)', iconBg: 'linear-gradient(135deg, #f43f5e, #e11d48)' },
    { title: 'Secure APIs', icon: 'bi-shield-shaded', border: 'rgba(6, 182, 212, 0.25)', hover: 'rgba(6, 182, 212, 0.8)', bg: 'rgba(6, 182, 212, 0.04)', iconBg: 'linear-gradient(135deg, #06b6d4, #0891b2)' }
  ]);
}
