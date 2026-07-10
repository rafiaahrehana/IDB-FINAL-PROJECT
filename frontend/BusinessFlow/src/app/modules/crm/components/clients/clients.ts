import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Client } from '../../models/crm.model';
import { ClientService } from '../../services/client.service';
import { Pagination } from '../../../../shared/components/pagination/pagination';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';

@Component({
  selector: 'app-clients',
  imports: [CommonModule, FormsModule, RouterLink, Pagination, Loader, EmptyState],
  templateUrl: './clients.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './clients.scss',
})
export class Clients implements OnInit {
  clients: Client[] = [];
  totalPages = 0;
  page = 0;
  loading = false;
  error = '';
  success = '';
  statusFilter = '';
  showCreate = false;
  form: any = {};

  constructor(private clientService: ClientService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.error = '';
    this.clientService.list(this.page, 20, this.statusFilter || undefined).subscribe({
      next: (res) => {
        this.clients = res.content;
        this.totalPages = res.totalPages;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load clients';
        this.loading = false;
      },
    });
  }

  create(): void {
    if (!this.form.firstName || !this.form.lastName || !this.form.email || !this.form.password) {
      this.error = 'First name, last name, email, and password are required';
      return;
    }
    this.clientService.create(this.form).subscribe({
      next: () => {
        this.showCreate = false;
        this.form = {};
        this.success = 'Client created successfully';
        this.load();
      },
      error: (err) => (this.error = err?.error?.message || 'Failed to create client'),
    });
  }

  deleteClient(client: Client): void {
    if (!confirm(`Delete client ${client.firstName} ${client.lastName}?`)) return;
    this.clientService.delete(client.id).subscribe({
      next: () => { this.success = 'Client deleted'; this.load(); },
      error: (err) => (this.error = err?.error?.message || 'Failed to delete'),
    });
  }

  tagList(client: Client): string[] {
    return client.tags
      ? client.tags
          .split(',')
          .map((t) => t.trim())
          .filter(Boolean)
      : [];
  }

  goToPage(p: number): void {
    this.page = p;
    this.load();
  }
}
