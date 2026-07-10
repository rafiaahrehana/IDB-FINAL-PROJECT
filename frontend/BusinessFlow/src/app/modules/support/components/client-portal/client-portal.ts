import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SupportTicket } from '../../models/support.model';
import { SupportMessage } from '../../models/support.model';
import { TicketService } from '../../services/ticket.service';
import { MessageService } from '../../services/message.service';
import { ApiService } from '../../../../core/services/api.service';
import { Pagination } from '../../../../shared/components/pagination/pagination';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';

@Component({
  selector: 'app-client-portal',
  imports: [CommonModule, FormsModule, Pagination, Loader, EmptyState],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './client-portal.html',
})
export class ClientPortal implements OnInit {
  tickets: SupportTicket[] = [];
  totalPages = 0;
  page = 0;
  loading = false;
  error = '';
  success = '';

  view: 'list' | 'create' | 'detail' = 'list';
  selected?: SupportTicket;
  messages: SupportMessage[] = [];
  newMessage = '';

  form: any = {};
  categories: any[] = [];

  // Satisfaction rating
  satisfactionRating = 0;
  satisfactionFeedback = '';

  priorities = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

  constructor(
    private ticketService: TicketService,
    private messageService: MessageService,
    private api: ApiService,
  ) {}

  ngOnInit(): void {
    this.loadTickets();
    this.api.get<any>('/support/categories/active').subscribe({ next: (r) => (this.categories = r), error: () => {} });
  }

  loadTickets(): void {
    this.loading = true;
    this.ticketService.myTickets(undefined, this.page).subscribe({
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

  showCreate(): void {
    this.view = 'create';
    this.form = { priority: 'MEDIUM' };
    this.error = '';
  }

  showDetail(ticket: SupportTicket): void {
    this.selected = ticket;
    this.view = 'detail';
    this.satisfactionRating = ticket.satisfactionRating || 0;
    this.satisfactionFeedback = ticket.satisfactionFeedback || '';
    this.loadMessages(ticket.id);
  }

  goBack(): void {
    this.view = 'list';
    this.selected = undefined;
    this.messages = [];
    this.error = '';
    this.success = '';
  }

  create(): void {
    if (!this.form.title || !this.form.priority || !this.form.description) {
      this.error = 'Title, priority, and description are required';
      return;
    }
    this.ticketService.create(this.form).subscribe({
      next: (ticket) => {
        this.success = 'Ticket created successfully';
        this.form = {};
        this.view = 'detail';
        this.selected = ticket;
        this.loadMessages(ticket.id);
        this.loadTickets();
      },
      error: (err) => (this.error = err?.error?.message || 'Failed to create ticket'),
    });
  }

  loadMessages(ticketId: number): void {
    this.messageService.getExternalMessages(ticketId).subscribe({
      next: (msgs) => (this.messages = msgs),
      error: () => (this.messages = []),
    });
  }

  sendMessage(): void {
    if (!this.selected || !this.newMessage.trim()) return;
    this.messageService.create({ ticketId: this.selected.id, message: this.newMessage.trim() }).subscribe({
      next: () => {
        this.newMessage = '';
        this.loadMessages(this.selected!.id);
      },
      error: (err) => (this.error = err?.error?.message || 'Failed to send message'),
    });
  }

  reopen(): void {
    if (!this.selected) return;
    const reason = prompt('Reason for reopening:');
    if (!reason) return;
    this.ticketService.reopen(this.selected.id, reason).subscribe({
      next: () => {
        this.success = 'Ticket reopened';
        this.refreshSelected();
      },
      error: (err) => (this.error = err?.error?.message || 'Failed'),
    });
  }

  submitSatisfaction(): void {
    if (!this.selected || !this.satisfactionRating) return;
    this.ticketService.recordSatisfaction(this.selected.id, this.satisfactionRating, this.satisfactionFeedback).subscribe({
      next: () => {
        this.success = 'Thank you for your feedback!';
        this.refreshSelected();
      },
      error: (err) => (this.error = err?.error?.message || 'Failed'),
    });
  }

  private refreshSelected(): void {
    if (!this.selected) return;
    this.ticketService.getById(this.selected.id).subscribe({ next: (t) => (this.selected = t) });
  }

  statusClass(s: string): string {
    return { NEW: 'text-bg-secondary', OPEN: 'text-bg-primary', IN_PROGRESS: 'text-bg-info', RESOLVED: 'text-bg-success', CLOSED: 'text-bg-dark', REOPENED: 'text-bg-warning', WAITING: 'text-bg-warning' }[s] || 'text-bg-light';
  }

  priorityClass(p: string): string {
    return { CRITICAL: 'text-bg-danger', HIGH: 'text-bg-warning', MEDIUM: 'text-bg-info', LOW: 'text-bg-light' }[p] || 'text-bg-secondary';
  }

  goToPage(p: number): void {
    this.page = p;
    this.loadTickets();
  }
}
