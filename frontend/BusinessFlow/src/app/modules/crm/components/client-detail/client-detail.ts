import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Client, ClientContact, CrmActivity, Opportunity } from '../../models/crm.model';
import { ClientService } from '../../services/client.service';
import { ContactService } from '../../services/contact.service';
import { ActivityService } from '../../services/activity.service';
import { OpportunityService } from '../../services/opportunity.service';

@Component({
  selector: 'app-client-detail',
  imports: [CommonModule, FormsModule],
  templateUrl: './client-detail.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './client-detail.scss',
})
export class ClientDetail implements OnInit {
  clientId!: number;
  client?: Client;
  contacts: ClientContact[] = [];
  activities: CrmActivity[] = [];
  opportunities: Opportunity[] = [];
  error = '';

  showContactForm = false;
  newContact: Partial<ClientContact> = {};

  newActivity: Partial<CrmActivity> = { type: 'NOTE' };

  constructor(
    private route: ActivatedRoute,
    private clientService: ClientService,
    private contactService: ContactService,
    private activityService: ActivityService,
    private opportunityService: OpportunityService,
  ) {}

  ngOnInit(): void {
    this.clientId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadAll();
  }

  loadAll(): void {
    this.clientService.getById(this.clientId).subscribe({
      next: (c) => (this.client = c),
      error: () => (this.error = 'Failed to load account'),
    });
    this.loadContacts();
    this.loadTimeline();
    this.opportunityService.list(0, 50, { clientId: this.clientId }).subscribe({
      next: (res) => (this.opportunities = res.content),
    });
  }

  loadContacts(): void {
    this.contactService.listByClient(this.clientId).subscribe({
      next: (list) => (this.contacts = list),
    });
  }

  loadTimeline(): void {
    this.activityService.timeline({ clientId: this.clientId }, 0, 30).subscribe({
      next: (res) => (this.activities = res.content),
    });
  }

  saveContact(): void {
    if (!this.newContact.fullName?.trim()) return;
    this.contactService.create(this.clientId, this.newContact).subscribe({
      next: () => {
        this.newContact = {};
        this.showContactForm = false;
        this.loadContacts();
      },
      error: (err) => (this.error = err?.error?.message || 'Failed to add contact'),
    });
  }

  makePrimary(contact: ClientContact): void {
    this.contactService.markPrimary(this.clientId, contact.id).subscribe({
      next: () => this.loadContacts(),
    });
  }

  logActivity(): void {
    if (!this.newActivity.subject?.trim()) return;
    this.activityService.log({ ...this.newActivity, clientId: this.clientId }).subscribe({
      next: () => {
        this.newActivity = { type: 'NOTE' };
        this.loadTimeline();
      },
      error: (err) => (this.error = err?.error?.message || 'Failed to log activity'),
    });
  }

  activityIcon(type: string): string {
    const icons: Record<string, string> = {
      CALL: 'bi-telephone',
      MEETING: 'bi-calendar-event',
      EMAIL: 'bi-envelope',
      NOTE: 'bi-journal-text',
      TASK: 'bi-check2-square',
      FOLLOW_UP: 'bi-bell',
      STAGE_CHANGE: 'bi-graph-up-arrow',
      STATUS_CHANGE: 'bi-arrow-repeat',
      DOCUMENT: 'bi-file-earmark',
    };
    return icons[type] || 'bi-dot';
  }
}
