import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SupportAgent } from '../../models/support.model';
import { AgentService } from '../../services/agent.service';
import { Pagination } from '../../../../shared/components/pagination/pagination';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';

@Component({
  selector: 'app-agents',
  imports: [CommonModule, FormsModule, Pagination, Loader, EmptyState],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './agents.html',
})
export class Agents implements OnInit {
  agents: SupportAgent[] = [];
  totalPages = 0;
  page = 0;
  loading = false;
  error = '';
  success = '';
  showForm = false;
  form: any = {};

  constructor(private agentService: AgentService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.cdr.markForCheck();
    this.agentService.list(this.page).subscribe({
      next: (res) => {
        this.agents = res.content;
        this.totalPages = res.totalPages;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.error = 'Failed to load agents';
        this.loading = false;
        this.cdr.markForCheck();
      },
    });
  }

  save(): void {
    this.agentService.create(this.form).subscribe({
      next: () => {
        this.showForm = false;
        this.form = {};
        this.success = 'Agent created';
        this.load();
      },
      error: (err) => (this.error = err?.error?.message || 'Failed'),
    });
  }

  toggleAccepting(a: SupportAgent): void {
    this.agentService.setAccepting(a.id, !a.acceptingTickets).subscribe({
      next: () => this.load(),
      error: (err) => (this.error = err?.error?.message || 'Failed'),
    });
  }

  statusClass(s: string): string {
    return (
      {
        ACTIVE: 'text-bg-success',
        INACTIVE: 'text-bg-secondary',
        ON_BREAK: 'text-bg-warning',
        OFFLINE: 'text-bg-dark',
      }[s] || 'text-bg-secondary'
    );
  }

  goToPage(p: number): void {
    this.page = p;
    this.load();
  }
}
