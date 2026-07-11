import { Component, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { SupportMessage } from '../../models/support.model';
import { MessageService } from '../../services/message.service';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';

@Component({
  selector: 'app-messages',
  imports: [CommonModule, FormsModule, Loader, EmptyState],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './messages.html',
})
export class Messages {
  ticketId?: number;
  external: SupportMessage[] = [];
  internal: SupportMessage[] = [];
  showInternal = false;
  loading = false;
  error = '';

  newMessage = '';
  isInternal = false;

  constructor(private messageService: MessageService, private route: ActivatedRoute, private cdr: ChangeDetectorRef) {
    const qp = this.route.snapshot.queryParamMap.get('ticketId');
    if (qp) {
      this.ticketId = Number(qp);
      this.load();
    }
  }

  load(): void {
    if (!this.ticketId) return;
    this.loading = true;
    this.cdr.markForCheck();
    this.error = '';
    this.messageService.getExternalMessages(this.ticketId).subscribe({
      next: (res) => {
        this.external = res;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.error = 'Failed to load messages for that ticket';
        this.loading = false;
        this.cdr.markForCheck();
      },
    });
    this.messageService.getInternalNotes(this.ticketId).subscribe({ next: (res) => (this.internal = res) });
  }

  send(): void {
    if (!this.ticketId || !this.newMessage.trim()) return;
    this.messageService
      .create({ ticketId: this.ticketId, message: this.newMessage.trim(), isInternal: this.isInternal })
      .subscribe({
        next: () => {
          this.newMessage = '';
          this.load();
        },
        error: (err) => (this.error = err?.error?.message || 'Failed to send message'),
      });
  }

  delete(m: SupportMessage): void {
    this.messageService.delete(m.id).subscribe({
      next: () => this.load(),
      error: (err) => (this.error = err?.error?.message || 'Failed to delete message'),
    });
  }
}
