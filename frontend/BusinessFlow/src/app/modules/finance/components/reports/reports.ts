import { Component, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProfitLossReport, BalanceSheetReport, TrialBalanceReport } from '../../models/finance.model';
import { FinancialReportService } from '../../services/financial-report.service';

type ReportType = 'PROFIT_LOSS' | 'BALANCE_SHEET' | 'TRIAL_BALANCE';

@Component({
  selector: 'app-finance-reports',
  imports: [CommonModule, FormsModule],
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

  loading = false;
  error = '';

  constructor(private reportService: FinancialReportService, private cdr: ChangeDetectorRef) {}

  generate(): void {
    this.error = '';
    this.profitLoss = undefined;
    this.balanceSheet = undefined;
    this.trialBalance = undefined;

    if (this.reportType === 'PROFIT_LOSS') {
      if (!this.startDate || !this.endDate) {
        this.error = 'Please select a start and end date';
        this.cdr.markForCheck();
        return;
      }
      this.loading = true;
      this.cdr.markForCheck();
      this.reportService.profitLoss(this.startDate, this.endDate).subscribe({
        next: (r) => { this.profitLoss = r; this.loading = false; this.cdr.markForCheck(); },
        error: () => { this.error = 'Failed to generate report'; this.loading = false; this.cdr.markForCheck(); },
      });
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
    } else {
      this.reportService.trialBalance(this.asOfDate).subscribe({
        next: (r) => { this.trialBalance = r; this.loading = false; this.cdr.markForCheck(); },
        error: () => { this.error = 'Failed to generate report'; this.loading = false; this.cdr.markForCheck(); },
      });
    }
  }
}
