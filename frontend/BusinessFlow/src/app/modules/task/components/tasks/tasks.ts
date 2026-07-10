import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Task, TaskRequest, TaskStatus, Priority } from '../../models/task.model';
import { TaskService } from '../../services/task.service';
import { Pagination } from '../../../../shared/components/pagination/pagination';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';
import { ConfirmDialog } from '../../../../shared/components/confirm-dialog/confirm-dialog';

@Component({
  selector: 'app-tasks',
  imports: [CommonModule, FormsModule, Pagination, Loader, EmptyState, ConfirmDialog],
  templateUrl: './tasks.html',
})
export class Tasks implements OnInit {
  tasks: Task[] = [];
  totalPages = 0;
  page = 0;
  loading = false;
  saving = false;
  error = '';
  success = '';

  search = '';
  statusFilter = '';
  priorityFilter = '';

  showForm = false;
  editingId: number | null = null;
  form: TaskRequest = this.emptyForm();

  deleteTarget: Task | null = null;

  statuses: TaskStatus[] = ['PENDING', 'IN_PROGRESS', 'COMPLETED', 'BLOCKED', 'CANCELLED'];
  priorities: Priority[] = ['LOW', 'NORMAL', 'HIGH', 'URGENT'];

  constructor(private taskService: TaskService) {}

  ngOnInit(): void {
    this.load();
  }

  get displayed(): Task[] {
    const term = this.search.trim().toLowerCase();
    return this.tasks.filter((t) => {
      const matchesTerm = !term || t.title.toLowerCase().includes(term);
      const matchesStatus = !this.statusFilter || t.status === this.statusFilter;
      const matchesPriority = !this.priorityFilter || t.priority === this.priorityFilter;
      return matchesTerm && matchesStatus && matchesPriority;
    });
  }

  load(): void {
    this.loading = true;
    this.error = '';
    this.taskService.list(this.page, 20).subscribe({
      next: (res) => {
        this.tasks = res.content;
        this.totalPages = res.totalPages;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load tasks';
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

  openEdit(t: Task): void {
    this.editingId = t.id;
    this.form = {
      title: t.title,
      description: t.description,
      status: t.status,
      priority: t.priority,
      projectId: t.projectId,
      assigneeId: t.assigneeId,
      dueDate: t.dueDate ? t.dueDate.slice(0, 10) : undefined,
    };
    this.showForm = true;
  }

  save(): void {
    if (!this.form.title) return;
    this.saving = true;
    this.error = '';
    const payload = this.cleanPayload();
    const obs = this.editingId
      ? this.taskService.update(this.editingId, payload)
      : this.taskService.create(payload);
    obs.subscribe({
      next: () => {
        this.saving = false;
        this.showForm = false;
        this.success = this.editingId ? 'Task updated' : 'Task created';
        this.load();
      },
      error: (err) => {
        this.saving = false;
        this.error = err?.error?.message || 'Failed to save task';
      },
    });
  }

  confirmDelete(): void {
    if (!this.deleteTarget) return;
    this.taskService.delete(this.deleteTarget.id).subscribe({
      next: () => {
        this.deleteTarget = null;
        this.success = 'Task deleted';
        this.load();
      },
      error: () => {
        this.deleteTarget = null;
        this.error = 'Failed to delete task';
      },
    });
  }

  exportCsv(): void {
    const header = ['ID', 'Title', 'Status', 'Priority', 'Project ID', 'Assignee ID', 'Due'];
    const rows = this.displayed.map((t) => [
      t.id, t.title, t.status, t.priority, t.projectId ?? '', t.assigneeId ?? '', t.dueDate || '',
    ]);
    this.downloadCsv('tasks.csv', header, rows);
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

  private emptyForm(): TaskRequest {
    return { title: '', status: 'PENDING', priority: 'NORMAL' };
  }

  private cleanPayload(): TaskRequest {
    const payload: any = { ...this.form };
    if (!payload.description) delete payload.description;
    if (!payload.projectId) delete payload.projectId;
    if (!payload.assigneeId) delete payload.assigneeId;
    if (!payload.dueDate) delete payload.dueDate;
    return payload;
  }
}
