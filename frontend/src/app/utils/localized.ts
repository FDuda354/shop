import {appLang} from '../services/language.service';

/**
 * Tłumaczenia DANYCH (produkty/kategorie): kolumny bazowe są polskie, pola
 * *En trzymają opcjonalną wersję angielską. Brak/pusty EN = fallback do PL.
 * Helpery czytają sygnał appLang, więc wywołane w szablonie przeliczają się
 * same po zmianie języka — wystarczy wystawić je jako pole komponentu.
 */
export function localizedName(entity: {name: string; nameEn?: string | null}): string {
  return appLang() === 'en' && entity.nameEn ? entity.nameEn : entity.name;
}

export function localizedDescription(
  entity: {description: string | null; descriptionEn?: string | null}): string | null {
  return appLang() === 'en' && entity.descriptionEn ? entity.descriptionEn : entity.description;
}

export function localizedFullDescription(
  entity: {fullDescription: string | null; fullDescriptionEn?: string | null}): string | null {
  return appLang() === 'en' && entity.fullDescriptionEn ? entity.fullDescriptionEn : entity.fullDescription;
}
