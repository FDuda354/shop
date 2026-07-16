import {Injectable, signal} from '@angular/core';

const THEME_KEY = 'shop.theme';
const DARK_CLASS = 'my-app-dark';

@Injectable({
  providedIn: 'root'
})
export class ThemeService {

  private readonly _dark = signal<boolean>(localStorage.getItem(THEME_KEY) === 'dark');
  readonly dark = this._dark.asReadonly();

  constructor() {
    this.apply(this._dark());
  }

  toggle(): void {
    const next = !this._dark();
    this._dark.set(next);
    localStorage.setItem(THEME_KEY, next ? 'dark' : 'light');
    this.apply(next);
  }

  private apply(dark: boolean): void {
    document.documentElement.classList.toggle(DARK_CLASS, dark);
  }
}
