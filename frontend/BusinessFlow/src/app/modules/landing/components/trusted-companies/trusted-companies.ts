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
  companies = signal(['Google', 'Microsoft', 'Oracle', 'Stripe', 'OpenAI', 'Amazon', 'IBM', 'SAP']);
}
