import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SupportAgent, SupportContextSwitch } from '../../models/support.model';
import { ContextSwitchService } from '../../services/context-switch.service';
import { AgentService } from '../../services/agent.service';
import { CompanyService } from '../../../platform-admin/services/company.service';
import { Company } from '../../../platform-admin/models/platform-admin.model';
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

  // Start-switch form (POST /support/context/switch)
  showStart = false;
  starting = false;
  startForm: { supportAgentId?: number; viewedCompanyId?: number; purpose: string } = { purpose: '' };
  agents: SupportAgent[] = [];
  companies: Company[] = [];

  constructor(
    private contextSwitchService: ContextSwitchService,
    private agentService: AgentService,
    private companyService: CompanyService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.loadActive();
  }

  openStart(): void {
    this.showStart = true;
    this.startForm = { purpose: '' };
    if (!this.agents.length) {
      this.agentService.list(0).subscribe({ next: (r) => (this.agents = r.content) });
    }
    if (!this.companies.length) {
      // Support roles are authorized to list companies (see CompanyController)
      this.companyService.list(0, 100).subscribe({ next: (r) => (this.companies = r.content) });
    }
  }

  startSwitch(): void {
    if (!this.startForm.supportAgentId || !this.startForm.viewedCompanyId) {
      this.error = 'Agent and company are required';
      return;
    }
    this.starting = true;
    this.error = '';
    this.contextSwitchService.switchContext({
      supportAgentId: this.startForm.supportAgentId,
      viewedCompanyId: this.startForm.viewedCompanyId,
      purpose: this.startForm.purpose || undefined,
    }).subscribe({
      next: () => {
        this.success = 'Context switch started';
        this.showStart = false;
        this.starting = false;
        this.loadActive();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to start switch';
        this.starting = false;
      },
    });
  }

  loadActive(): void {
    this.loading = true;
    this.cdr.markForCheck();
    this.contextSwitchService.active().subscribe({
      next: (res) => {
        this.active = res;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.error = 'Failed to load active context switches';
        this.loading = false;
        this.cdr.markForCheck();
      },
    });
  }

  loadHistory(): void {
    if (!this.agentIdFilter) return;
    this.loading = true;
    this.cdr.markForCheck();
    this.contextSwitchService.historyForAgent(this.agentIdFilter, this.page, 20).subscribe({
      next: (res) => {
        this.history = res.content;
        this.totalPages = res.totalPages;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.error = 'Failed to load history for that agent';
        this.loading = false;
        this.cdr.markForCheck();
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
