import {inject} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {LanguageService} from '../services/language.service';

/**
 * Fabryka komunikatów walidacji dla signal forms. Zwrócona funkcja produkuje
 * LogicFn tłumaczony leniwie i zależny od lang() — zmiana języka podmienia
 * widoczne błędy bez przebudowy formularza. Fabrykę wywołuj w kontekście
 * wstrzykiwania (inicjalizator pola), zwrócone funkcje można wołać wszędzie.
 */
export function validationMessages(): (key: string) => () => string {
  const translate = inject(TranslateService);
  const language = inject(LanguageService);
  return (key: string) => () => {
    language.lang();
    return translate.instant(key);
  };
}
