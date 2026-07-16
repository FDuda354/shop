import {Translation} from 'primeng/api';

/**
 * Tłumaczenia wbudowanych tekstów PrimeNG (datepicker, confirm, upload).
 * Aplikowane w runtime przez LanguageService.setTranslation() przy zmianie
 * języka — oba obiekty mają ten sam zestaw kluczy, żeby przełączenie
 * pl→en→pl nadpisywało wszystko w obie strony.
 */
export const PRIMENG_TRANSLATIONS: Record<'pl' | 'en', Translation> = {
  pl: {
    accept: 'Tak',
    reject: 'Nie',
    choose: 'Wybierz',
    upload: 'Prześlij',
    cancel: 'Anuluj',
    firstDayOfWeek: 1,
    dayNames: ['Niedziela', 'Poniedziałek', 'Wtorek', 'Środa', 'Czwartek', 'Piątek', 'Sobota'],
    dayNamesShort: ['Nie', 'Pon', 'Wto', 'Śro', 'Czw', 'Pią', 'Sob'],
    dayNamesMin: ['Nie', 'Pon', 'Wt', 'Śr', 'Czw', 'Pt', 'Sob'],
    monthNames: [
      'Styczeń', 'Luty', 'Marzec', 'Kwiecień', 'Maj', 'Czerwiec',
      'Lipiec', 'Sierpień', 'Wrzesień', 'Październik', 'Listopad', 'Grudzień'
    ],
    monthNamesShort: [
      'Sty', 'Lut', 'Mar', 'Kwi', 'Maj', 'Cze',
      'Lip', 'Sie', 'Wrz', 'Paź', 'Lis', 'Gru'
    ],
    today: 'Dzisiaj',
    clear: 'Wyczyść',
    dateFormat: 'dd.mm.yy',
    weekHeader: 'Tydz',
    weak: 'Słabe',
    medium: 'Średnie',
    strong: 'Silne',
    passwordPrompt: 'Wpisz hasło',
    emptyMessage: 'Brak wyników',
    emptyFilterMessage: 'Brak wyników',
    emptySearchMessage: 'Brak wyników',
    aria: {
      close: 'Zamknij',
      previous: 'Poprzedni',
      next: 'Następny',
      navigation: 'Nawigacja',
      listLabel: 'Lista opcji',
      pageLabel: 'Strona {page}',
      firstPageLabel: 'Pierwsza strona',
      lastPageLabel: 'Ostatnia strona',
      nextPageLabel: 'Następna strona',
      prevPageLabel: 'Poprzednia strona',
      previousPageLabel: 'Poprzednia strona',
      rowsPerPageLabel: 'Wierszy na stronę',
      jumpToPageDropdownLabel: 'Wybór strony',
      jumpToPageInputLabel: 'Numer strony',
    }
  },
  en: {
    accept: 'Yes',
    reject: 'No',
    choose: 'Choose',
    upload: 'Upload',
    cancel: 'Cancel',
    firstDayOfWeek: 0,
    dayNames: ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'],
    dayNamesShort: ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'],
    dayNamesMin: ['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa'],
    monthNames: [
      'January', 'February', 'March', 'April', 'May', 'June',
      'July', 'August', 'September', 'October', 'November', 'December'
    ],
    monthNamesShort: [
      'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
      'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'
    ],
    today: 'Today',
    clear: 'Clear',
    dateFormat: 'mm/dd/yy',
    weekHeader: 'Wk',
    weak: 'Weak',
    medium: 'Medium',
    strong: 'Strong',
    passwordPrompt: 'Enter a password',
    emptyMessage: 'No results found',
    emptyFilterMessage: 'No results found',
    emptySearchMessage: 'No results found',
    aria: {
      close: 'Close',
      previous: 'Previous',
      next: 'Next',
      navigation: 'Navigation',
      listLabel: 'Option List',
      pageLabel: 'Page {page}',
      firstPageLabel: 'First Page',
      lastPageLabel: 'Last Page',
      nextPageLabel: 'Next Page',
      prevPageLabel: 'Previous Page',
      previousPageLabel: 'Previous Page',
      rowsPerPageLabel: 'Rows per page',
      jumpToPageDropdownLabel: 'Jump to Page Dropdown',
      jumpToPageInputLabel: 'Jump to Page Input',
    }
  },
};
