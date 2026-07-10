import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Project, ProjectRequest, ProjectStatus, Priority } from '../../models/project.model';
import { ProjectService } from '../../services/project.service';
import { Pagination } from '../../../../shared/components/pagination/pagination';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';
import { ConfirmDialog } from '../../../../shared/components/confirm-dialog/confirm-dialog';

@Component({
  selector: 'app-projects',
  imports: [CommonModule, FormsModule, Pagination, Loader, EmptyState, ConfirmDialog],
  templateUrl: './projects.html',
})
export class Projects implements OnInit {
  projects: Project[] = [];
  totalPages = 0;
  page = 0;
  loading = false;
  saving = false;
  error = '';
  success = '';

  search = '';
  statusFilter = '';

  showForm = false;
  editingId: number | null = null;
  form: ProjectRequest = this.emptyForm();

  deleteTarget: Project | null = null;

  statuses: ProjectStatus[] = ['PLANNING', 'ACTIVE', 'ON_HOLD', 'COMPLETED', 'CANCELLED'];
  priorities: Priority[] = ['LOW', 'NORMAL', 'HIGH', 'URGENT'];

  constructor(private projectService: ProjectService) {}

  ngOnInit(): void {
    this.load();
  }

  get displayed(): Project[] {
    const term = this.search.trim().toLowerCase();
    return this.projects.filter((p) => {
      const matchesTerm = !term || p.name.toLowerCase().includes(term);
      const matchesStatus = !this.statusFilter || p.status === this.statusFilter;
      return matchesTerm && matchesStatus;
    });
  }

  load(): void {
    this.loading = true;
    this.error = '';
    this.projectService.list(this.page, 20).subscribe({
      next: (res) => {
        this.projects = res.content;
        this.totalPages = res.totalPages;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load projects';
        this.loading = false;
      },
    });
  }

  goToPage(p: number): void {
    this.page = p;
    this.load();
  }

  openCreate(): void {
    this.editingId = null;
    this.form = this.emptyForm();
    this.showForm = true;
  }

  openEdit(p: Project): void {
    this.editingId = p.id;
    this.form = {
      name: p.name,
      description: p.description,
      status: p.status,
      priority: p.priority,
      ownerId: p.ownerId,
      startDate: p.startDate ? p.startDate.slice(0, 10) : undefined,
      endDate: p.endDate ? p.endDate.slice(0, 10) : undefined,
      progress: p.progress,
      budget: p.budget,
    };
    this.showForm = true;
  }

  save(): void {
    if (!this.form.name) return;
    this.saving = true;
    this.error = '';
    const payload = this.cleanPayload();
    const obs = this.editingId
      ? this.projectService.update(this.editingId, payload)
      : this.projectService.create(payload);
    obs.subscribe({
      next: () => {
        this.saving = false;
        this.showForm = false;
        this.success = this.editingId ? 'Project updated' : 'Project created';
        this.load();
      },
      error: (err) => {
        this.saving = false;
        this.error = err?.error?.message || 'Failed to save project';
      },
    });
  }

  confirmDelete(): void {
    if (!this.deleteTarget) return;
    this.projectService.delete(this.deleteTarget.id).subscribe({
      next: () => {
        this.deleteTarget = null;
        this.success = 'Project deleted';
        this.load();
      },
      error: () => {
        this.deleteTarget = null;
        this.error = 'Failed to delete project';
      },
    });
  }

  exportCsv(): void {
    const header = ['ID', 'Name', 'Status', 'Priority', 'Progress', 'Start', 'End', 'Budget'];
    const rows = this.displayed.map((p) => [
      p.id, p.name, p.status, p.priority, p.progress,
      p.startDate || '', p.endDate || '', p.budget ?? '',
    ]);
    this.downloadCsv('projects.csv', header, rows);
  }

  private downloadCsv(filename: string, header: string[], rows: (string | number)[][]): void {
    const escape = (v: string | number) => `"${String(v).replace(/"/g, '""')}"`;
    const csv = [header.map(escape).join(','), ...rows.map((r) => r.map(escape).join(','))].join('\n');
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    a.click();
    URL.revokeObjectURL(url);
  }

  private emptyForm(): ProjectRequest {
    return { name: '', status: 'PLANNING', priority: 'NORMAL', progress: 0 };
  }

  private cleanPayload(): ProjectRequest {
    const payload: any = { ...this.form };
    if (!payload.description) delete payload.description;
    if (!payload.ownerId) delete payload.ownerId;
    if (!payload.startDate) delete payload.startDate;
    if (!payload.endDate) delete payload.endDate;
    if (payload.budget === undefined || payload.budget === null) delete payload.budget;
    return payload;
  }
}
