import {inject, Injectable} from '@angular/core';
import {MessageService} from 'primeng/api';
import {TranslateService} from '@ngx-translate/core';

/**
 * Toasty tłumaczone przez TranslateService — komponenty podają klucze,
 * a wiadomości lądują w globalnym <p-toast key="tr"> (prawy dolny róg).
 */
@Injectable({
  providedIn: 'root'
})
export class NotificationService {

  private readonly messageService = inject(MessageService);
  private readonly translate = inject(TranslateService);

  success(titleKey: string, detailKey: string, params?: Record<string, unknown>): void {
    this.messageService.add({
      key: 'tr',
      severity: 'success',
      summary: this.translate.instant(titleKey),
      detail: this.translate.instant(detailKey, params),
      life: 5000,
    });
  }

  error(titleKey: string, detailKey: string, params?: Record<string, unknown>): void {
    this.messageService.add({
      key: 'tr',
      severity: 'error',
      summary: this.translate.instant(titleKey),
      detail: this.translate.instant(detailKey, params),
      life: 10000,
    });
  }

  warn(titleKey: string, detailKey: string, params?: Record<string, unknown>): void {
    this.messageService.add({
      key: 'tr',
      severity: 'warn',
      summary: this.translate.instant(titleKey),
      detail: this.translate.instant(detailKey, params),
      life: 5000,
    });
  }
}
