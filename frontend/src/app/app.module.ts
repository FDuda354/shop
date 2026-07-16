import {inject, NgModule, provideAppInitializer, provideZonelessChangeDetection} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {provideHttpClient, withInterceptors} from '@angular/common/http';
import {firstValueFrom} from 'rxjs';
import {ConfirmationService, MessageService} from 'primeng/api';
import {providePrimeNG} from 'primeng/config';
import {provideSignalFormsConfig} from '@angular/forms/signals';
import {definePreset} from '@primeuix/themes';
import Aura from '@primeuix/themes/aura';
import {provideTranslateService} from '@ngx-translate/core';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {SharedModule} from './shared/shared.module';
import {httpInterceptorFn} from './services/auth/http-interceptor.service';
import {AuthService} from './services/auth/auth.service';
import {LanguageService} from './services/language.service';
import {DefaultLayoutComponent} from './layout/default/default-layout.component';
import {FullLayoutComponent} from './layout/full/full-layout.component';
import {HeaderBarComponent} from './layout/header-bar/header-bar.component';
import {SideBarComponent} from './layout/side-bar/side-bar.component';
import {HomeComponent} from './views/home/home.component';
import {CategoryComponent} from './views/category/category.component';
import {ProductDetailsComponent} from './views/product-details/product-details.component';
import {ReviewFormComponent} from './views/product-details/review-form/review-form.component';
import {BasketComponent} from './views/basket/basket.component';
import {CheckoutComponent} from './views/checkout/checkout.component';
import {OrdersComponent} from './views/orders/orders.component';
import {ProfileComponent} from './views/profile/profile.component';
import {LoginComponent} from './views/login/login.component';
import {RegisterComponent} from './views/register/register.component';

// Klucz PrimeNG wstrzykiwany w build time przez esbuild --define
// (npm scripts / gradle przekazują env PRIMEUI_LICENSE — patrz debtor).
declare const PRIMEUI_LICENSE: string;

// Preset motywu: kolor przewodni emerald z palety Prime, treść na slate
// (struktura 1:1 z debtora, tylko paleta primary podmieniona).
const ShopPreset = definePreset(Aura, {
  semantic: {
    primary: {
      50: '{emerald.50}',
      100: '{emerald.100}',
      200: '{emerald.200}',
      300: '{emerald.300}',
      400: '{emerald.400}',
      500: '{emerald.500}',
      600: '{emerald.600}',
      700: '{emerald.700}',
      800: '{emerald.800}',
      900: '{emerald.900}',
      950: '{emerald.950}'
    },
    colorScheme: {
      light: {
        primary: {
          color: '{primary.600}',
          contrastColor: '{surface.0}',
          hoverColor: '{primary.700}',
          activeColor: '{primary.800}'
        },
        highlight: {
          background: '{primary.600}',
          focusBackground: '{primary.700}',
          color: '{surface.0}',
          focusColor: '{surface.0}'
        },
        content: {
          background: '{slate.50}',
          hoverBackground: '{slate.100}',
          borderColor: '{slate.300}',
          color: '{slate.700}',
          hoverColor: '{slate.200}'
        }
      },
      dark: {
        primary: {
          color: '{emerald.400}',
          contrastColor: '{slate.900}',
          hoverColor: '{emerald.300}',
          activeColor: '{emerald.200}'
        },
        highlight: {
          background: '{emerald.400}',
          focusBackground: '{emerald.300}',
          color: '{slate.900}',
          focusColor: '{slate.900}'
        },
        content: {
          background: '{slate.900}',
          hoverBackground: '{slate.800}',
          borderColor: '{slate.700}',
          color: '{slate.100}',
          hoverColor: '{slate.700}'
        }
      }
    }
  }
});

@NgModule({
  declarations: [
    AppComponent,
    DefaultLayoutComponent,
    FullLayoutComponent,
    HeaderBarComponent,
    SideBarComponent,
    HomeComponent,
    CategoryComponent,
    ProductDetailsComponent,
    ReviewFormComponent,
    BasketComponent,
    CheckoutComponent,
    OrdersComponent,
    ProfileComponent,
    LoginComponent,
    RegisterComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    SharedModule,
  ],
  providers: [
    provideZonelessChangeDetection(),
    ConfirmationService,
    MessageService,
    provideHttpClient(withInterceptors([httpInterceptorFn])),
    provideAppInitializer(() => firstValueFrom(inject(AuthService).loadMe())),
    provideTranslateService(),
    // Instancjonuje LanguageService przed pierwszym renderem: ładuje słowniki
    // pl/en i aplikuje zapamiętany język (ngx-translate + PrimeNG + <html lang>).
    provideAppInitializer(() => void inject(LanguageService)),
    // Signal forms bindują invalid do inputów PrimeNG od pierwszego renderu —
    // klasa field-touched pozwala CSS-owi pokazywać błąd dopiero po dotknięciu.
    provideSignalFormsConfig({
      classes: {
        'field-touched': field => field.state().touched(),
      },
    }),
    providePrimeNG({
      license: typeof PRIMEUI_LICENSE !== 'undefined' ? PRIMEUI_LICENSE : '',
      theme: {
        preset: ShopPreset,
        options: {
          darkModeSelector: '.my-app-dark'
        }
      }
      // Tłumaczenia wbudowanych tekstów PrimeNG aplikuje LanguageService
      // (i18n/primeng.ts) — statyczny blok tutaj nie przeżyłby zmiany języka.
    })
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
