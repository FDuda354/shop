import {Component, inject} from '@angular/core';
import {LanguageService} from '../../../services/language.service';

@Component({
  selector: 'app-language-switcher',
  standalone: false,
  templateUrl: './language-switcher.component.html',
  styleUrl: './language-switcher.component.scss',
})
export class LanguageSwitcherComponent {
  readonly languageService = inject(LanguageService);
}
