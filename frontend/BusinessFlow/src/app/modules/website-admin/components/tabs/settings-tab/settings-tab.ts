import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { WebsiteAdminService } from '../../../services/website-admin.service';
import { WebsiteSettings } from '../../../models/website-admin.model';
import { Loader } from '../../../../../shared/components/loader/loader';
import { FileUpload } from '../../../../../shared/components/file-upload/file-upload';
import { FileUploadResult } from '../../../../../shared/services/file-upload.service';

function emptySettings(): WebsiteSettings {
  return { heroImages: [], socialLinks: [], stats: [] };
}

@Component({
  selector: 'app-settings-tab',
  imports: [CommonModule, FormsModule, Loader, FileUpload],
  templateUrl: './settings-tab.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SettingsTab implements OnInit {
  loading = true;
  saving = false;
  error = '';
  success = '';

  form: WebsiteSettings = emptySettings();
  newHeroImageUrl = '';
  newSocialLink = { platform: '', url: '', icon: '' };
  newStat = { value: '', label: '', icon: '' };

  constructor(private service: WebsiteAdminService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.service.getSettings().subscribe({
      next: (s) => {
        this.form = { ...emptySettings(), ...s, heroImages: s.heroImages || [], socialLinks: s.socialLinks || [], stats: s.stats || [] };
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => { this.loading = false; this.cdr.markForCheck(); },
    });
  }

  save(): void {
    this.saving = true;
    this.error = '';
    this.success = '';
    this.cdr.markForCheck();
    this.service.saveSettings(this.form).subscribe({
      next: (s) => {
        this.form = { ...emptySettings(), ...s, heroImages: s.heroImages || [], socialLinks: s.socialLinks || [], stats: s.stats || [] };
        this.success = 'Settings saved';
        this.saving = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to save settings';
        this.saving = false;
        this.cdr.markForCheck();
      },
    });
  }

  onLogoUploaded(result: FileUploadResult): void {
    this.form.logoUrl = result.fileUrl;
  }

  onFaviconUploaded(result: FileUploadResult): void {
    this.form.faviconUrl = result.fileUrl;
  }

  onHeroImageUploaded(result: FileUploadResult): void {
    this.form.heroImageUrl = result.fileUrl;
  }

  onOgImageUploaded(result: FileUploadResult): void {
    this.form.ogImage = result.fileUrl;
  }

  onHeroGalleryImageUploaded(result: FileUploadResult): void {
    this.form.heroImages.push(result.fileUrl);
  }

  addHeroImageUrl(): void {
    if (!this.newHeroImageUrl.trim()) return;
    this.form.heroImages.push(this.newHeroImageUrl.trim());
    this.newHeroImageUrl = '';
  }

  removeHeroImage(index: number): void {
    this.form.heroImages.splice(index, 1);
  }

  addSocialLink(): void {
    if (!this.newSocialLink.platform.trim() || !this.newSocialLink.url.trim()) return;
    this.form.socialLinks.push({ ...this.newSocialLink });
    this.newSocialLink = { platform: '', url: '', icon: '' };
  }

  removeSocialLink(index: number): void {
    this.form.socialLinks.splice(index, 1);
  }

  addStat(): void {
    if (!this.newStat.value.trim() || !this.newStat.label.trim()) return;
    this.form.stats.push({ ...this.newStat });
    this.newStat = { value: '', label: '', icon: '' };
  }

  removeStat(index: number): void {
    this.form.stats.splice(index, 1);
  }
}
