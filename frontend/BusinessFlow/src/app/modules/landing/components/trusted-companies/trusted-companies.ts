import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-trusted-companies',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './trusted-companies.html',
  styleUrls: ['./trusted-companies.scss']
})
export class TrustedCompaniesComponent {
  companies = signal([
    { name: 'Google', color: '#4285F4' },
    { name: 'Microsoft', color: '#00a4ef' },
    { name: 'Oracle', color: '#c74634' },
    { name: 'Stripe', color: '#635BFF' },
    { name: 'OpenAI', color: '#10a37f' },
    { name: 'Amazon', color: '#FF9900' },
    { name: 'IBM', color: '#0530ad' },
    { name: 'SAP', color: '#008fd3' }
  ]);
}
