import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Announcement, AnnouncementRequest, ANNOUNCEMENT_AUDIENCES, Department } from '../../models/hrm.model';
import { AnnouncementService } from '../../services/announcement.service';
import { DepartmentService } from '../../services/department.service';
import { Pagination } from '../../../../shared/components/pagination/pagination';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';
import { ConfirmDialog } from '../../../../shared/components/confirm-dialog/confirm-dialog';

@Component({
  selector: 'app-announcements',
  imports: [CommonModule, FormsModule, Pagination, Loader, EmptyState, ConfirmDialog],
  templateUrl: './announcements.html',
})
export class Announcements implements OnInit {
  announcements: Announcement[] = [];
  departments: Department[] = [];
  totalPages = 0;
  page = 0;
  loading = false;
  saving = false;
  error = '';
  success = '';

  showForm = false;
  form: AnnouncementRequest = this.emptyForm();

  deleteTarget: Announcement | null = null;

  audiences = ANNOUNCEMENT_AUDIENCES;

  constructor(
    private announcementService: AnnouncementService,
    private departmentService: DepartmentService
  ) {}

  ngOnInit(): void {
    this.load();
    this.loadDepartments();
  }

  load(): void {
    this.loading = true;
    this.error = '';
    this.announcementService.list(this.page, 20).subscribe({
      next: (res) => {
        this.announcements = res.content;
        this.totalPages = res.totalPages;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load announcements';
        this.loading = false;
      }
    });
  }

  loadDepartments(): void {
    this.departmentService.listActive().subscribe({
      next: (d) => this.departments = d,
      error: () => this.departments = []
    });
  }

  openCreate(): void {
    this.form = this.emptyForm();
    this.showForm = true;
  }

  save(): void {
    this.saving = true;
    this.error = '';
    const payload = this.cleanPayload();
    this.announcementService.create(payload).subscribe({
      next: () => {
        this.saving = false;
        this.showForm = false;
        this.success = 'Announcement created successfully';
        this.load();
      },
      error: (err) => {
        this.saving = false;
        this.error = err?.error?.message || 'Failed to create announcement';
      }
    });
  }

  publish(a: Announcement): void {
    this.announcementService.publish(a.id).subscribe({
      next: () => {
        this.success = 'Announcement published successfully';
        this.load();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to publish announcement';
      }
    });
  }

  confirmDelete(): void {
    if (!this.deleteTarget) return;
    this.announcementService.delete(this.deleteTarget.id).subscribe({
      next: () => {
        this.deleteTarget = null;
        this.success = 'Announcement deleted successfully';
        this.load();
      },
      error: () => {
        this.deleteTarget = null;
        this.error = 'Failed to delete announcement';
      }
    });
  }

  goToPage(p: number): void {
    this.page = p;
    this.load();
  }

  private emptyForm(): AnnouncementRequest {
    return {
      title: '',
      body: '',
      audience: 'ALL',
      priority: 1,
      notifyAll: false,
    };
  }

  private cleanPayload(): AnnouncementRequest {
    const payload: any = { ...this.form };
    if (!payload.targetDepartmentId) delete payload.targetDepartmentId;
    if (!payload.expiresAt) delete payload.expiresAt;
    if (!payload.attachmentUrl) delete payload.attachmentUrl;
    return payload;
  }
}
