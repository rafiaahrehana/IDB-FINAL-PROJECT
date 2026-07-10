import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Meeting, MeetingRequest } from '../../models/meeting.model';
import { MeetingService } from '../../services/meeting.service';
import { Pagination } from '../../../../shared/components/pagination/pagination';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';
import { ConfirmDialog } from '../../../../shared/components/confirm-dialog/confirm-dialog';

@Component({
  selector: 'app-meetings',
  imports: [CommonModule, FormsModule, Pagination, Loader, EmptyState, ConfirmDialog],
  templateUrl: './meetings.html',
})
export class Meetings implements OnInit {
  meetings: Meeting[] = [];
  totalPages = 0;
  page = 0;
  loading = false;
  saving = false;
  error = '';
  success = '';

  search = '';

  showForm = false;
  editingId: number | null = null;
  form: MeetingRequest = this.emptyForm();

  deleteTarget: Meeting | null = null;

  constructor(private meetingService: MeetingService) {}

  ngOnInit(): void {
    this.load();
  }

  get displayed(): Meeting[] {
    const term = this.search.trim().toLowerCase();
    return this.meetings.filter((m) =>
      !term || m.title.toLowerCase().includes(term) ||
      (!!m.location && m.location.toLowerCase().includes(term)));
  }

  load(): void {
    this.loading = true;
    this.error = '';
    this.meetingService.list(this.page, 20).subscribe({
      next: (res) => {
        this.meetings = res.content;
        this.totalPages = res.totalPages;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load meetings';
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

  openEdit(m: Meeting): void {
    this.editingId = m.id;
    this.form = {
      title: m.title,
      description: m.description,
      organizerId: m.organizerId,
      startTime: m.startTime ? m.startTime.slice(0, 16) : undefined,
      endTime: m.endTime ? m.endTime.slice(0, 16) : undefined,
      location: m.location,
    };
    this.showForm = true;
  }

  save(): void {
    if (!this.form.title || !this.form.startTime) return;
    this.saving = true;
    this.error = '';
    const payload = this.cleanPayload();
    const obs = this.editingId
      ? this.meetingService.update(this.editingId, payload)
      : this.meetingService.create(payload);
    obs.subscribe({
      next: () => {
        this.saving = false;
        this.showForm = false;
        this.success = this.editingId ? 'Meeting updated' : 'Meeting created';
        this.load();
      },
      error: (err) => {
        this.saving = false;
        this.error = err?.error?.message || 'Failed to save meeting';
      },
    });
  }

  confirmDelete(): void {
    if (!this.deleteTarget) return;
    this.meetingService.delete(this.deleteTarget.id).subscribe({
      next: () => {
        this.deleteTarget = null;
        this.success = 'Meeting deleted';
        this.load();
      },
      error: () => {
        this.deleteTarget = null;
        this.error = 'Failed to delete meeting';
      },
    });
  }

  exportCsv(): void {
    const header = ['ID', 'Title', 'Start', 'End', 'Location', 'Organizer ID'];
    const rows = this.displayed.map((m) => [
      m.id, m.title, m.startTime || '', m.endTime || '', m.location || '', m.organizerId ?? '',
    ]);
    this.downloadCsv('meetings.csv', header, rows);
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

  private emptyForm(): MeetingRequest {
    return { title: '', startTime: '' };
  }

  private cleanPayload(): MeetingRequest {
    const payload: any = { ...this.form };
    if (!payload.description) delete payload.description;
    if (!payload.organizerId) delete payload.organizerId;
    if (!payload.endTime) delete payload.endTime;
    if (!payload.location) delete payload.location;
    return payload;
  }
}
