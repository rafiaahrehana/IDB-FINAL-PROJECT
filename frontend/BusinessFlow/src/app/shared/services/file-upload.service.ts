import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../core/services/api.service';

export interface FileUploadResult {
  fileName: string;
  fileUrl: string;
  message: string;
}

@Injectable({ providedIn: 'root' })
export class FileUploadService {
  constructor(private api: ApiService) {}

  upload(file: File): Observable<FileUploadResult> {
    const formData = new FormData();
    formData.append('file', file);
    return this.api.post<FileUploadResult>('/upload', formData);
  }
}
