import {inject, Injectable, signal} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {PrimeNG} from 'primeng/config';
import {PL} from '../i18n/pl';
import {EN} from '../i18n/en';
import {PRIMENG_TRANSLATIONS} from '../i18n/primeng';

export type AppLanguage = 'pl' | 'en';

const LANGUAGE_KEY = 'shop.language';

// Sygnał modułowy, żeby helpery szablonowe (utils/localized.ts) mogły czytać
// język bez DI — szablony w trybie zoneless śledzą sygnały czytane w bindingu,
// więc zmiana języka odświeża je automatycznie. Pisze wyłącznie LanguageService.
const _lang = signal<AppLanguage>(
  localStorage.getItem(LANGUAGE_KEY) === 'en' ? 'en' : 'pl');

export const appLang = _lang.asReadonly();

/**
 * Język aplikacji (pl/en) trzymany w localStorage. Pipe `| translate` reaguje
 * na use() sam z siebie; kod używający `translate.instant()` w computed musi
 * dodatkowo odczytać sygnał `lang()`, żeby przeliczyć się po zmianie języka.
 */
@Injectable({
  providedIn: 'root'
})
export class LanguageService {

  private readonly translate = inject(TranslateService);
  private readonly primeng = inject(PrimeNG);

  readonly lang = appLang;

  constructor() {
    // Słowniki ładowane synchronicznie — pipe i instant() działają od startu,
    // bez http-loadera i migotania surowych kluczy.
    this.translate.setTranslation('pl', PL);
    this.translate.setTranslation('en', EN);
    this.apply(_lang());
  }

  set(lang: AppLanguage): void {
    if (lang === _lang()) {
      return;
    }
    _lang.set(lang);
    localStorage.setItem(LANGUAGE_KEY, lang);
    this.apply(lang);
  }

  private apply(lang: AppLanguage): void {
    this.translate.use(lang);
    this.primeng.setTranslation(PRIMENG_TRANSLATIONS[lang]);
    document.documentElement.lang = lang;
  }
}
