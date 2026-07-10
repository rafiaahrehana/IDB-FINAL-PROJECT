import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { SupportTicket, SupportAgent } from '../../models/support.model';
import { TicketService } from '../../services/ticket.service';
import { AgentService } from '../../services/agent.service';
import { ApiService } from '../../../../core/services/api.service';
import { Pagination } from '../../../../shared/components/pagination/pagination';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';

@Component({
  selector: 'app-tickets',
  imports: [CommonModule, FormsModule, RouterLink, Pagination, Loader, EmptyState],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './tickets.html',
})
export class Tickets implements OnInit {
  tickets: SupportTicket[] = [];
  agents: SupportAgent[] = [];
  categories: any[] = [];
  totalPages = 0;
  page = 0;
  loading = false;
  error = '';
  success = '';
  statusFilter = '';
  showSlaOnly = false;

  selected?: SupportTicket;
  assignAgentId?: number;
  resolutionNotes = '';
  reopenReason = '';
  showCreate = false;
  form: any = {};

  statuses = ['NEW', 'OPEN', 'IN_PROGRESS', 'WAITING', 'ON_HOLD', 'RESOLVED', 'CLOSED'];

  constructor(
    private ticketService: TicketService,
    private agentService: AgentService,
    private api: ApiService,
  ) {}

  ngOnInit(): void {
    this.load();
    this.agentService.available().subscribe({ next: (r) => (this.agents = r) });
    this.api.get<any>('/support/categories/active').subscribe({ next: (r) => (this.categories = r), error: () => {} });
  }

  load(): void {
    this.loading = true;
    if (this.showSlaOnly) {
      // Backend returns a plain list for SLA-breached tickets, not a page
      this.ticketService.slaBreached().subscribe({
        next: (res) => {
          this.tickets = res;
          this.totalPages = 1;
          this.loading = false;
        },
        error: () => {
          this.error = 'Failed to load tickets';
          this.loading = false;
        },
      });
      return;
    }
    const obs = this.statusFilter
      ? this.ticketService.listByStatus(this.statusFilter, this.page)
      : this.ticketService.list(this.page);
    obs.subscribe({
      next: (res) => {
        this.tickets = res.content;
        this.totalPages = res.totalPages;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load tickets';
        this.loading = false;
      },
    });
  }

  view(t: SupportTicket): void {
    this.ticketService.getById(t.id).subscribe({
      next: (ticket) => {
        this.selected = ticket;
        this.resolutionNotes = '';
      },
    });
  }

  create(): void {
    this.ticketService.create(this.form).subscribe({
      next: () => {
        this.showCreate = false;
        this.form = {};
        this.success = 'Ticket created';
        this.load();
      },
      error: (err) => (this.error = err?.error?.message || 'Failed'),
    });
  }

  // The backend endpoints for assign/resolve/close/reopen return no body (ResponseEntity<Void>),
  // so we refresh the selected ticket from the server after each action.
  private refreshSelected(): void {
    if (!this.selected) return;
    this.ticketService.getById(this.selected.id).subscribe({ next: (t) => (this.selected = t) });
  }

  assign(): void {
    if (!this.selected || !this.assignAgentId) return;
    this.ticketService.assign(this.selected.id, this.assignAgentId).subscribe({
      next: () => {
        this.success = 'Assigned';
        this.refreshSelected();
        this.load();
      },
      error: (err) => (this.error = err?.error?.message || 'Failed'),
    });
  }

  resolve(): void {
    if (!this.selected) return;
    this.ticketService.resolve(this.selected.id, this.resolutionNotes).subscribe({
      next: () => {
        this.success = 'Resolved';
        this.refreshSelected();
        this.load();
      },
      error: (err) => (this.error = err?.error?.message || 'Failed'),
    });
  }

  close(): void {
    if (!this.selected) return;
    this.ticketService.close(this.selected.id).subscribe({
      next: () => {
        this.success = 'Closed';
        this.refreshSelected();
        this.load();
      },
      error: (err) => (this.error = err?.error?.message || 'Failed'),
    });
  }

  reopen(): void {
    if (!this.selected || !this.reopenReason.trim()) return;
    this.ticketService.reopen(this.selected.id, this.reopenReason.trim()).subscribe({
      next: () => {
        this.success = 'Reopened';
        this.reopenReason = '';
        this.refreshSelected();
        this.load();
      },
      error: (err) => (this.error = err?.error?.message || 'Failed'),
    });
  }

  statusClass(s: string): string {
    return (
      {
        NEW: 'text-bg-secondary',
        OPEN: 'text-bg-primary',
        IN_PROGRESS: 'text-bg-info',
        RESOLVED: 'text-bg-success',
        CLOSED: 'text-bg-dark',
        WAITING: 'text-bg-warning',
      }[s] || 'text-bg-light'
    );
  }

  priorityClass(p: string): string {
    return (
      {
        CRITICAL: 'text-bg-danger',
        HIGH: 'text-bg-warning',
        MEDIUM: 'text-bg-info',
        LOW: 'text-bg-light',
      }[p] || 'text-bg-secondary'
    );
  }

  goToPage(p: number): void {
    this.page = p;
    this.load();
  }
}
