import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OfferLetter, OfferLetterRequest, LetterType, LETTER_TYPES, Employee } from '../../models/hrm.model';
import { OfferLetterService } from '../../services/offer-letter.service';
import { EmployeeService } from '../../services/employee.service';
import { Pagination } from '../../../../shared/components/pagination/pagination';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';
import { ConfirmDialog } from '../../../../shared/components/confirm-dialog/confirm-dialog';

@Component({
  selector: 'app-offer-letters',
  imports: [CommonModule, FormsModule, Pagination, Loader, EmptyState, ConfirmDialog],
  templateUrl: './offer-letters.html',
})
export class OfferLetters implements OnInit {
  letters: OfferLetter[] = [];
  employees: Employee[] = [];
  totalPages = 0;
  page = 0;
  loading = false;
  saving = false;
  error = '';
  success = '';

  showForm = false;
  isEdit = false;
  selectedId: number | null = null;
  form: OfferLetterRequest = this.emptyForm();

  deleteTarget: OfferLetter | null = null;

  letterTypes = LETTER_TYPES;

  constructor(
    private letterService: OfferLetterService,
    private employeeService: EmployeeService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.load();
    this.loadEmployees();
  }

  load(): void {
    this.loading = true;
    this.cdr.markForCheck();
    this.error = '';
    this.letterService.list(this.page, 20).subscribe({
      next: (res) => {
        this.letters = res.content;
        this.totalPages = res.totalPages;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.error = 'Failed to load offer/official letters';
        this.loading = false;
        this.cdr.markForCheck();
      }
    });
  }

  loadEmployees(): void {
    this.employeeService.list(0, 100).subscribe({
      next: (res) => this.employees = res.content,
      error: () => this.employees = []
    });
  }

  openCreate(): void {
    this.form = this.emptyForm();
    this.isEdit = false;
    this.showForm = true;
  }

  save(): void {
    this.saving = true;
    this.cdr.markForCheck();
    this.error = '';
    const payload = this.cleanPayload();
    this.letterService.create(payload).subscribe({
      next: () => {
        this.saving = false;
        this.cdr.markForCheck();
        this.showForm = false;
        this.success = 'Letter record created successfully';
        this.load();
      },
      error: (err) => {
        this.saving = false;
        this.cdr.markForCheck();
        this.error = err?.error?.message || 'Failed to create letter';
      }
    });
  }

  issue(l: OfferLetter): void {
    this.letterService.issue(l.id).subscribe({
      next: () => {
        this.success = 'Letter marked as issued';
        this.load();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to issue letter';
      }
    });
  }

  confirmDelete(): void {
    if (!this.deleteTarget) return;
    this.letterService.delete(this.deleteTarget.id).subscribe({
      next: () => {
        this.deleteTarget = null;
        this.success = 'Letter record deleted';
        this.load();
      },
      error: () => {
        this.deleteTarget = null;
        this.error = 'Failed to delete letter';
      }
    });
  }

  goToPage(p: number): void {
    this.page = p;
    this.load();
  }

  private emptyForm(): OfferLetterRequest {
    return {
      employeeId: undefined,
      letterType: 'OFFER',
      referenceNumber: '',
      issueDate: '',
      content: '',
      signedBy: '',
    };
  }

  private cleanPayload(): OfferLetterRequest {
    const payload: any = { ...this.form };
    return payload;
  }
}
