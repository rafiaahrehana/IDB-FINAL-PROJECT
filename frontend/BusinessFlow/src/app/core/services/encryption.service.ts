import { Injectable } from '@angular/core';
import * as CryptoJS from 'crypto-js';

@Injectable({ providedIn: 'root' })
export class EncryptionService {
  private readonly secretKey = 'BusinessOS-2024-SecureKey!@#$%^&*()';

  encrypt(data: string): string {
    return CryptoJS.AES.encrypt(data, this.secretKey).toString();
  }

  decrypt(encryptedData: string): string {
    const bytes = CryptoJS.AES.decrypt(encryptedData, this.secretKey);
    return bytes.toString(CryptoJS.enc.Utf8);
  }

  encryptObject<T extends Record<string, any>>(obj: T): string {
    return this.encrypt(JSON.stringify(obj));
  }

  decryptObject<T>(encryptedData: string): T | null {
    try {
      const decrypted = this.decrypt(encryptedData);
      return JSON.parse(decrypted) as T;
    } catch {
      return null;
    }
  }

  hash(data: string): string {
    return CryptoJS.SHA256(data).toString();
  }

  generateToken(): string {
    return CryptoJS.lib.WordArray.random(32).toString();
  }

  encryptToken(token: string): string {
    return this.encrypt(token);
  }

  decryptToken(encryptedToken: string): string {
    return this.decrypt(encryptedToken);
  }
}
