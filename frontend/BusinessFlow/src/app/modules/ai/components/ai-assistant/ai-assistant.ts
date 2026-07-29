import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AiService, AiGenerateResponse } from '../../../../core/services/ai.service';
import { AnnouncementService } from '../../../hrm/services/announcement.service';
import { AnnouncementDraftResponse, HolidayDraftResponse } from '../../../hrm/models/hrm.model';
import { LeavePolicyService } from '../../../hrm/services/leave-policy.service';
import { HolidayService } from '../../../hrm/services/holiday.service';
import { extractErrorMessage } from '../../../../core/utils/http-error.util';
import { Loader } from '../../../../shared/components/loader/loader';

@Component({
  selector: 'app-ai-assistant',
  imports: [CommonModule, FormsModule, Loader],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './ai-assistant.html',
})
export class AiAssistant implements OnInit {
  features = [
    { value: 'GENERAL', label: 'General Assistant' },
    { value: 'CRM_LEAD_SUMMARY', label: 'CRM Lead Summary' },
    { value: 'CRM_ACTIVITY_SUMMARY', label: 'CRM Activity Summary' },
    { value: 'INVOICE_SUMMARY', label: 'Invoice Summary' },
    { value: 'SERVICE_REQUEST_SUMMARY', label: 'Service Request Summary' },
    { value: 'EMPLOYMENT_LETTER', label: 'Employment Letter' },
    { value: 'LEAVE_POLICY', label: 'Leave Policy Draft' },
    { value: 'PERFORMANCE_REVIEW', label: 'Performance Review Draft' },
    { value: 'ANNOUNCEMENT_DRAFT', label: 'Announcement Draft' },
    { value: 'HOLIDAY_DRAFT', label: 'Holiday Draft' },
    { value: 'WORKFLOW_SUGGESTION', label: 'Workflow Suggestion' },
  ];
  feature = 'GENERAL';
  prompt = '';
  result?: AiGenerateResponse;
  history: AiGenerateResponse[] = [];
  generating = false;
  loadingHistory = false;
  error = '';

  // ANNOUNCEMENT_DRAFT is special-cased: it goes through the same
  // company-context-aware endpoint the Announcements page uses (real company
  // name + date injected, structured title/body), and the result can be
  // saved as a real Announcement instead of only appearing here as chat text.
  announcementDraft?: AnnouncementDraftResponse;
  savingAnnouncement = false;
  announcementSaved = false;

  // LEAVE_POLICY is special-cased the same way: it goes through the
  // company-context-aware endpoint (real annual/sick entitlements pulled
  // from the company's configured leave policies), instead of the generic
  // raw-prompt endpoint which has no knowledge of the company's actual policy.
  leavePolicyDraft?: { document: string };

  // HOLIDAY_DRAFT mirrors ANNOUNCEMENT_DRAFT exactly: AI returns structured
  // {name, date, type, description}, reviewed here, then saved as a real
  // Holiday record via the same create() endpoint the Holidays page uses.
  holidayDraft?: HolidayDraftResponse;
  savingHoliday = false;
  holidaySaved = false;

  constructor(
    private aiService: AiService,
    private announcementService: AnnouncementService,
    private leavePolicyService: LeavePolicyService,
    private holidayService: HolidayService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.loadHistory();
  }

  generate(): void {
    if (!this.prompt.trim()) return;
    this.generating = true;
    this.error = '';
    this.result = undefined;
    this.announcementDraft = undefined;
    this.announcementSaved = false;
    this.leavePolicyDraft = undefined;
    this.holidayDraft = undefined;
    this.holidaySaved = false;
    this.cdr.markForCheck();

    if (this.feature === 'ANNOUNCEMENT_DRAFT') {
      this.announcementService.draftWithAi(this.prompt.trim()).subscribe({
        next: (draft) => {
          this.announcementDraft = draft;
          this.generating = false;
          this.cdr.markForCheck();
        },
        error: (err) => {
          this.error = extractErrorMessage(err, 'Generation failed — check your AI provider configuration');
          this.generating = false;
          this.cdr.markForCheck();
        },
      });
      return;
    }

    if (this.feature === 'HOLIDAY_DRAFT') {
      this.holidayService.draftWithAi(this.prompt.trim()).subscribe({
        next: (draft) => {
          this.holidayDraft = draft;
          this.generating = false;
          this.cdr.markForCheck();
        },
        error: (err) => {
          this.error = extractErrorMessage(err, 'Generation failed — check your AI provider configuration');
          this.generating = false;
          this.cdr.markForCheck();
        },
      });
      return;
    }

    if (this.feature === 'LEAVE_POLICY') {
      this.leavePolicyService.draftWithAi(false, this.prompt.trim()).subscribe({
        next: (draft) => {
          this.leavePolicyDraft = draft;
          this.generating = false;
          this.cdr.markForCheck();
        },
        error: (err) => {
          this.error = extractErrorMessage(err, 'Generation failed — configure an Annual leave policy for Full-time employees first');
          this.generating = false;
          this.cdr.markForCheck();
        },
      });
      return;
    }

    this.aiService.generate(this.feature, this.prompt.trim()).subscribe({
      next: (res) => {
        this.result = res;
        this.generating = false;
        this.loadHistory();
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.error =
          err?.error?.message || 'Generation failed — check your AI provider configuration';
        this.generating = false;
        this.cdr.markForCheck();
      },
    });
  }

  saveAsAnnouncement(): void {
    if (!this.announcementDraft || this.savingAnnouncement) return;
    this.savingAnnouncement = true;
    this.error = '';
    this.cdr.markForCheck();
    this.announcementService.create({
      title: this.announcementDraft.title,
      body: this.announcementDraft.body,
      audience: 'ALL',
      priority: 1,
    }).subscribe({
      next: () => {
        this.savingAnnouncement = false;
        this.announcementSaved = true;
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.error = extractErrorMessage(err, 'Failed to save announcement');
        this.savingAnnouncement = false;
        this.cdr.markForCheck();
      },
    });
  }

  saveAsHoliday(): void {
    if (!this.holidayDraft || this.savingHoliday) return;
    this.savingHoliday = true;
    this.error = '';
    this.cdr.markForCheck();
    this.holidayService.create({
      name: this.holidayDraft.name,
      holidayDate: this.holidayDraft.date,
      holidayType: this.holidayDraft.type,
      description: this.holidayDraft.description,
    }).subscribe({
      next: () => {
        this.savingHoliday = false;
        this.holidaySaved = true;
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.error = extractErrorMessage(err, 'Failed to save holiday');
        this.savingHoliday = false;
        this.cdr.markForCheck();
      },
    });
  }

  loadHistory(): void {
    this.loadingHistory = true;
    this.cdr.markForCheck();
    this.aiService.conversations().subscribe({
      next: (res) => {
        this.history = res.content;
        this.loadingHistory = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.loadingHistory = false;
        this.cdr.markForCheck();
      },
    });
  }

  reuse(item: AiGenerateResponse): void {
    this.feature = item.feature;
    this.result = item;
    this.cdr.markForCheck();
  }
}
