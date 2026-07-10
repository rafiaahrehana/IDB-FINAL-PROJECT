import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Lead } from '../../models/crm.model';
import { LeadService } from '../../services/lead.service';
import { OpportunityService } from '../../services/opportunity.service';
import { Pagination } from '../../../../shared/components/pagination/pagination';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';

@Component({
  selector: 'app-leads',
  imports: [CommonModule, FormsModule, Pagination, Loader, EmptyState],
  templateUrl: './leads.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './leads.scss',
})
export class Leads implements OnInit {
  leads: Lead[] = [];
  totalPages = 0;
  page = 0;
  loading = false;
  error = '';
  success = '';
  statusFilter = '';
  keyword = '';

  constructor(
    private leadService: LeadService,
    private opportunityService: OpportunityService,
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.error = '';
    const trimmedKeyword = this.keyword.trim();
    // Backend's plain listAll endpoint does not accept a keyword param - it has to
    // go through the dedicated /search endpoint instead.
    const obs = trimmedKeyword
      ? this.leadService.search(trimmedKeyword, this.page, 20)
      : this.leadService.list(this.page, 20, this.statusFilter ? { status: this.statusFilter } : undefined);
    obs.subscribe({
      next: (res) => {
        this.leads = res.content;
        this.totalPages = res.totalPages;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load leads';
        this.loading = false;
      },
    });
  }

  convert(lead: Lead): void {
    this.leadService.convert(lead.id).subscribe({
      next: () => {
        this.success = `Lead "${lead.contactName}" converted to client`;
        this.error = '';
        this.load();
      },
      error: (err) => (this.error = err?.error?.message || 'Failed to convert lead'),
    });
  }

  createOpportunity(lead: Lead): void {
    if (!lead.converted) {
      this.error = 'Convert this lead to a client first, then create the opportunity.';
      return;
    }
    const payload = {
      name: (lead.companyName || lead.contactName) + ' - Deal',
      amount: lead.estimatedValue,
      expectedCloseDate: lead.expectedCloseDate,
    };
    this.opportunityService.createFromLead(lead.id, payload as any).subscribe({
      next: (opp) => {
        this.success = `Opportunity "${opp.name}" created`;
        this.error = '';
      },
      error: (err) => (this.error = err?.error?.message || 'Failed to create opportunity'),
    });
  }

  goToPage(p: number): void {
    this.page = p;
    this.load();
  }
}
