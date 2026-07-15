import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SettingsTab } from '../tabs/settings-tab/settings-tab';
import { BlogTab } from '../tabs/blog-tab/blog-tab';
import { PagesTab } from '../tabs/pages-tab/pages-tab';
import { FaqsTab } from '../tabs/faqs-tab/faqs-tab';
import { NavTab } from '../tabs/nav-tab/nav-tab';
import { TeamTab } from '../tabs/team-tab/team-tab';
import { TestimonialsTab } from '../tabs/testimonials-tab/testimonials-tab';
import { ProjectsTab } from '../tabs/projects-tab/projects-tab';
import { PricingTab } from '../tabs/pricing-tab/pricing-tab';

type Tab = 'settings' | 'blog' | 'pages' | 'faqs' | 'nav' | 'team' | 'testimonials' | 'projects' | 'pricing';

@Component({
  selector: 'app-website-management',
  imports: [
    CommonModule, SettingsTab, BlogTab, PagesTab, FaqsTab, NavTab,
    TeamTab, TestimonialsTab, ProjectsTab, PricingTab,
  ],
  templateUrl: './website-management.html',
})
export class WebsiteManagement {
  tab: Tab = 'settings';

  tabs: { key: Tab; label: string; icon: string }[] = [
    { key: 'settings', label: 'Branding & Theme', icon: 'bi-brush' },
    { key: 'blog', label: 'Blog', icon: 'bi-journal-text' },
    { key: 'pages', label: 'Pages', icon: 'bi-file-earmark-text' },
    { key: 'faqs', label: 'FAQs', icon: 'bi-question-circle' },
    { key: 'nav', label: 'Navigation', icon: 'bi-list' },
    { key: 'team', label: 'Team', icon: 'bi-people' },
    { key: 'testimonials', label: 'Testimonials', icon: 'bi-chat-quote' },
    { key: 'projects', label: 'Projects', icon: 'bi-kanban' },
    { key: 'pricing', label: 'Pricing', icon: 'bi-tags' },
  ];

  setTab(t: Tab): void {
    this.tab = t;
  }
}
