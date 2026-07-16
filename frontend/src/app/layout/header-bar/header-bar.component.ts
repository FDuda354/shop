import {Component, computed, inject, output} from '@angular/core';
import {Router} from '@angular/router';
import {MenuItem} from 'primeng/api';
import {TranslateService} from '@ngx-translate/core';
import {AuthService} from '../../services/auth/auth.service';
import {BasketService} from '../../services/basket.service';
import {ThemeService} from '../../services/theme.service';
import {LanguageService} from '../../services/language.service';

@Component({
  selector: 'app-header-bar',
  standalone: false,
  templateUrl: './header-bar.component.html',
  styleUrl: './header-bar.component.scss',
})
export class HeaderBarComponent {
  private readonly router = inject(Router);
  private readonly translate = inject(TranslateService);

  readonly authService = inject(AuthService);
  readonly basketService = inject(BasketService);
  readonly themeService = inject(ThemeService);
  private readonly languageService = inject(LanguageService);

  menuToggle = output<void>();

  readonly userMenuItems = computed<MenuItem[]>(() => {
    // instant() nie reaguje na zmianę języka — sygnał lang() wymusza przeliczenie.
    this.languageService.lang();
    const user = this.authService.currentUser();
    if (!user) return [];
    const items: MenuItem[] = [
      {label: this.translate.instant('header.myOrders'), icon: 'pi pi-list', routerLink: '/orders'},
      {label: this.translate.instant('header.profile'), icon: 'pi pi-user', routerLink: '/profile'},
    ];
    if (user.admin) {
      items.push({label: this.translate.instant('header.adminPanel'), icon: 'pi pi-cog', routerLink: '/admin'});
    }
    items.push({separator: true});
    items.push({label: this.translate.instant('header.logout'), icon: 'pi pi-sign-out', command: () => this.authService.logout()});
    return items;
  });

  goToBasket(): void {
    void this.router.navigate(['/basket']);
  }

  goToLogin(): void {
    void this.router.navigate(['/login']);
  }
}
