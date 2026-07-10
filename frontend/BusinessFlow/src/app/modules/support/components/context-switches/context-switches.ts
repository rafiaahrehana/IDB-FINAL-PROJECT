import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SupportContextSwitch } from '../../models/support.model';
import { ContextSwitchService } from '../../services/context-switch.service';
import { Pagination } from '../../../../shared/components/pagination/pagination';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';

@Component({
  selector: 'app-context-switches',
  imports: [CommonModule, FormsModule, Pagination, Loader, EmptyState],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './context-switches.html',
})
export class ContextSwitches implements OnInit {
  active: SupportContextSwitch[] = [];
  history: SupportContextSwitch[] = [];
  totalPages = 0;
  page = 0;
  loading = false;
  error = '';
  success = '';

  agentIdFilter?: number;

  constructor(private contextSwitchService: ContextSwitchService) {}

  ngOnInit(): void {
    this.loadActive();
  }

  loadActive(): void {
    this.loading = true;
    this.contextSwitchService.active().subscribe({
      next: (res) => {
        this.active = res;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load active context switches';
        this.loading = false;
      },
    });
  }

  loadHistory(): void {
    if (!this.agentIdFilter) return;
    this.loading = true;
    this.contextSwitchService.historyForAgent(this.agentIdFilter, this.page, 20).subscribe({
      next: (res) => {
        this.history = res.content;
        this.totalPages = res.totalPages;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load history for that agent';
        this.loading = false;
      },
    });
  }

  goToPage(p: number): void {
    this.page = p;
    this.loadHistory();
  }

  endSwitch(cs: SupportContextSwitch): void {
    this.contextSwitchService.endContextSwitch(cs.id).subscribe({
      next: () => {
        this.success = 'Context switch ended';
        this.loadActive();
        if (this.agentIdFilter) this.loadHistory();
      },
      error: (err) => (this.error = err?.error?.message || 'Failed to end context switch'),
    });
  }
}
