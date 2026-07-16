import {Component, computed, inject} from '@angular/core';
import {MenuItem} from 'primeng/api';
import {TranslateService} from '@ngx-translate/core';
import {AuthService} from '../../services/auth/auth.service';
import {ThemeService} from '../../services/theme.service';
import {LanguageService} from '../../services/language.service';

/**
 * Layout panelu admina: bez sidebara — nawigacja w headerze (p-menubar),
 * podzielona tak jak moduły backendu (produkty / kategorie / zamówienia).
 */
@Component({
  selector: 'app-full-layout',
  standalone: false,
  templateUrl: './full-layout.component.html',
  styleUrl: './full-layout.component.scss',
})
export class FullLayoutComponent {
  private readonly translate = inject(TranslateService);

  readonly authService = inject(AuthService);
  readonly themeService = inject(ThemeService);
  private readonly languageService = inject(LanguageService);

  readonly menuItems = computed<MenuItem[]>(() => {
    // instant() nie reaguje na zmianę języka — sygnał lang() wymusza przeliczenie.
    this.languageService.lang();
    return [
      {label: this.translate.instant('admin.menu.dashboard'), icon: 'pi pi-chart-line', routerLink: '/admin', routerLinkActiveOptions: {exact: true}},
      {label: this.translate.instant('admin.menu.products'), icon: 'pi pi-box', routerLink: '/admin/products'},
      {label: this.translate.instant('admin.menu.categories'), icon: 'pi pi-tags', routerLink: '/admin/categories'},
      {label: this.translate.instant('admin.menu.orders'), icon: 'pi pi-list', routerLink: '/admin/orders'},
    ];
  });
}
