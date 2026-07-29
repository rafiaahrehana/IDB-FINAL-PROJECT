import { Component, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChartConfiguration } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';
import { ProfitLossReport, BalanceSheetReport, TrialBalanceReport, AgeingReport, CashFlowReport, ApAgeingReport, AccountLedgerReport, ChartOfAccount } from '../../models/finance.model';
import { FinancialReportService } from '../../services/financial-report.service';
import { VendorBillService } from '../../services/vendor.service';
import { CoaService } from '../../services/coa.service';

import { BosCurrencyPipe } from '../../../../shared/pipes/bos-currency.pipe';
type ReportType = 'PROFIT_LOSS' | 'BALANCE_SHEET' | 'TRIAL_BALANCE' | 'AGEING' | 'AP_AGEING' | 'CASH_FLOW' | 'ACCOUNT_LEDGER';

// Validated categorical slots (see dataviz skill palette) - fixed order, never
// reassigned per value. Aqua falls below 3:1 contrast on a light surface, so
// both series carry direct value labels (the "relief rule") rather than relying
// on color alone.
const SERIES_1_BLUE = '#2a78d6';
const SERIES_2_AQUA = '#1baf7a';
const STATUS_GOOD = '#0ca30c';
const STATUS_CRITICAL = '#d03b3b';

@Component({
  selector: 'app-finance-reports',
  imports: [BosCurrencyPipe, CommonModule, FormsModule, BaseChartDirective],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './reports.html',
})
export class Reports {
  reportType: ReportType = 'PROFIT_LOSS';
  startDate = '';
  endDate = '';
  asOfDate = '';

  profitLoss?: ProfitLossReport;
  balanceSheet?: BalanceSheetReport;
  trialBalance?: TrialBalanceReport;
  ageing?: AgeingReport;
  apAgeing?: ApAgeingReport;
  cashFlow?: CashFlowReport;
  accountLedger?: AccountLedgerReport;

  // For the Account Ledger account picker (lazy-loaded on first use)
  accounts: ChartOfAccount[] = [];
  ledgerAccountId: number | null = null;

  loading = false;
  error = '';

  plChartData?: ChartConfiguration<'bar'>['data'];
  plChartOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { display: false } },
    scales: { y: { beginAtZero: true } },
  };

  tbChartData?: ChartConfiguration<'bar'>['data'];
  tbChartOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { position: 'top' } },
    scales: { y: { beginAtZero: true } },
  };

  ageingChartData?: ChartConfiguration<'bar'>['data'];
  ageingChartOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { display: false } },
    scales: { y: { beginAtZero: true } },
  };

  cashFlowChartData?: ChartConfiguration<'bar'>['data'];
  cashFlowChartOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { position: 'top' } },
    scales: { y: { beginAtZero: true } },
  };

  constructor(
    private reportService: FinancialReportService,
    private vendorBillService: VendorBillService,
    private coaService: CoaService,
    private cdr: ChangeDetectorRef,
  ) {}

  usesDateRange(): boolean {
    return this.reportType === 'PROFIT_LOSS' || this.reportType === 'CASH_FLOW' || this.reportType === 'ACCOUNT_LEDGER';
  }

  onReportTypeChange(): void {
    if (this.reportType === 'ACCOUNT_LEDGER' && !this.accounts.length) {
      this.coaService.list(0, 200).subscribe({
        next: (res) => { this.accounts = res.content; this.cdr.markForCheck(); },
        error: () => { this.error = 'Failed to load accounts'; this.cdr.markForCheck(); },
      });
    }
  }

  generate(): void {
    this.error = '';
    this.profitLoss = undefined;
    this.balanceSheet = undefined;
    this.trialBalance = undefined;
    this.ageing = undefined;
    this.apAgeing = undefined;
    this.cashFlow = undefined;
    this.accountLedger = undefined;
    this.plChartData = undefined;
    this.tbChartData = undefined;
    this.ageingChartData = undefined;
    this.cashFlowChartData = undefined;

    if (this.usesDateRange()) {
      if (!this.startDate || !this.endDate) {
        this.error = 'Please select a start and end date';
        this.cdr.markForCheck();
        return;
      }
      if (this.reportType === 'ACCOUNT_LEDGER') {
        if (!this.ledgerAccountId) {
          this.error = 'Please select an account';
          this.cdr.markForCheck();
          return;
        }
        this.loading = true;
        this.cdr.markForCheck();
        this.reportService.accountLedger(this.ledgerAccountId, this.startDate, this.endDate).subscribe({
          next: (r) => { this.accountLedger = r; this.loading = false; this.cdr.markForCheck(); },
          error: () => { this.error = 'Failed to generate report'; this.loading = false; this.cdr.markForCheck(); },
        });
        return;
      }
      this.loading = true;
      this.cdr.markForCheck();
      if (this.reportType === 'PROFIT_LOSS') {
        this.reportService.profitLoss(this.startDate, this.endDate).subscribe({
          next: (r) => { this.profitLoss = r; this.buildProfitLossChart(r); this.loading = false; this.cdr.markForCheck(); },
          error: () => { this.error = 'Failed to generate report'; this.loading = false; this.cdr.markForCheck(); },
        });
      } else {
        this.reportService.cashFlow(this.startDate, this.endDate).subscribe({
          next: (r) => { this.cashFlow = r; this.buildCashFlowChart(r); this.loading = false; this.cdr.markForCheck(); },
          error: () => { this.error = 'Failed to generate report'; this.loading = false; this.cdr.markForCheck(); },
        });
      }
      return;
    }

    if (!this.asOfDate) {
      this.error = 'Please select an as-of date';
      this.cdr.markForCheck();
      return;
    }
    this.loading = true;
    this.cdr.markForCheck();
    if (this.reportType === 'BALANCE_SHEET') {
      this.reportService.balanceSheet(this.asOfDate).subscribe({
        next: (r) => { this.balanceSheet = r; this.loading = false; this.cdr.markForCheck(); },
        error: () => { this.error = 'Failed to generate report'; this.loading = false; this.cdr.markForCheck(); },
      });
    } else if (this.reportType === 'TRIAL_BALANCE') {
      this.reportService.trialBalance(this.asOfDate).subscribe({
        next: (r) => { this.trialBalance = r; this.buildTrialBalanceChart(r); this.loading = false; this.cdr.markForCheck(); },
        error: () => { this.error = 'Failed to generate report'; this.loading = false; this.cdr.markForCheck(); },
      });
    } else if (this.reportType === 'AP_AGEING') {
      this.vendorBillService.apAgeing(this.asOfDate).subscribe({
        next: (r) => { this.apAgeing = r; this.buildApAgeingChart(r); this.loading = false; this.cdr.markForCheck(); },
        error: () => { this.error = 'Failed to generate report'; this.loading = false; this.cdr.markForCheck(); },
      });
    } else {
      this.reportService.ageing(this.asOfDate).subscribe({
        next: (r) => { this.ageing = r; this.buildAgeingChart(r); this.loading = false; this.cdr.markForCheck(); },
        error: () => { this.error = 'Failed to generate report'; this.loading = false; this.cdr.markForCheck(); },
      });
    }
  }

  get netProfitClass(): string {
    if (!this.profitLoss) return '';
    return this.profitLoss.netProfit >= 0 ? 'text-success' : 'text-danger';
  }

  netProfitColor(): string {
    if (!this.profitLoss) return STATUS_GOOD;
    return this.profitLoss.netProfit >= 0 ? STATUS_GOOD : STATUS_CRITICAL;
  }

  get netChangeClass(): string {
    if (!this.cashFlow) return '';
    return this.cashFlow.netChange >= 0 ? 'text-success' : 'text-danger';
  }

  private buildProfitLossChart(r: ProfitLossReport): void {
    this.plChartData = {
      labels: ['Revenue', 'Expense'],
      datasets: [{
        data: [r.totalRevenue, r.totalExpense],
        backgroundColor: [SERIES_1_BLUE, SERIES_2_AQUA],
        borderRadius: 4,
        maxBarThickness: 80,
      }],
    };
  }

  private buildTrialBalanceChart(r: TrialBalanceReport): void {
    this.tbChartData = {
      labels: r.accounts.map((a) => a.accountCode),
      datasets: [
        { label: 'Debit', data: r.accounts.map((a) => a.debitBalance), backgroundColor: SERIES_1_BLUE, borderRadius: 3 },
        { label: 'Credit', data: r.accounts.map((a) => a.creditBalance), backgroundColor: SERIES_2_AQUA, borderRadius: 3 },
      ],
    };
  }

  // Aging buckets are ordered by severity (not distinct identities), so a single
  // hue is correct here rather than the categorical palette.
  private buildAgeingChart(r: AgeingReport): void {
    this.ageingChartData = {
      labels: ['Current', '1-30 days', '31-60 days', '61-90 days', '90+ days'],
      datasets: [{
        data: [r.current, r.days1to30, r.days31to60, r.days61to90, r.over90],
        backgroundColor: SERIES_1_BLUE,
        borderRadius: 4,
        maxBarThickness: 60,
      }],
    };
  }

  private buildApAgeingChart(r: ApAgeingReport): void {
    this.ageingChartData = {
      labels: ['Current', '1-30 days', '31-60 days', '61-90 days', '90+ days'],
      datasets: [{
        data: [r.current, r.days1to30, r.days31to60, r.days61to90, r.over90],
        backgroundColor: SERIES_2_AQUA,
        borderRadius: 4,
        maxBarThickness: 60,
      }],
    };
  }

  private buildCashFlowChart(r: CashFlowReport): void {
    this.cashFlowChartData = {
      labels: r.lines.map((l) => l.category),
      datasets: [
        { label: 'Inflow', data: r.lines.map((l) => l.inflow), backgroundColor: SERIES_1_BLUE, borderRadius: 3 },
        { label: 'Outflow', data: r.lines.map((l) => l.outflow), backgroundColor: SERIES_2_AQUA, borderRadius: 3 },
      ],
    };
  }
}
