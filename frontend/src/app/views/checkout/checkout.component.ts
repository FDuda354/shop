import {Component, computed, effect, inject, Injector, signal, untracked} from '@angular/core';
import {Router} from '@angular/router';
import {rxResource} from '@angular/core/rxjs-interop';
import {email, form, maxLength, required, submit} from '@angular/forms/signals';
import {firstValueFrom} from 'rxjs';
import {TranslateService} from '@ngx-translate/core';
import {OrderService} from '../../services/order.service';
import {BasketService} from '../../services/basket.service';
import {AuthService} from '../../services/auth/auth.service';
import {NotificationService} from '../../services/notification.service';
import {LanguageService} from '../../services/language.service';
import {validationMessages} from '../../utils/validation-message';
import {localizedName} from '../../utils/localized';
import {InitOrder, OrderSummary} from '../../models/order';
import {BasketSummary} from '../../models/basket';

@Component({
  selector: 'app-checkout',
  standalone: false,
  templateUrl: './checkout.component.html',
  styleUrl: './checkout.component.scss',
})
export class CheckoutComponent {
  private readonly router = inject(Router);
  private readonly orderService = inject(OrderService);
  private readonly basketService = inject(BasketService);
  private readonly authService = inject(AuthService);
  private readonly notification = inject(NotificationService);
  private readonly translate = inject(TranslateService);
  private readonly languageService = inject(LanguageService);
  private readonly injector = inject(Injector);
  private readonly msg = validationMessages();

  readonly localizedName = localizedName;

  readonly initOrder = rxResource<InitOrder, undefined>({
    stream: () => this.orderService.initOrder(),
  });

  readonly basket = rxResource<BasketSummary, number | undefined>({
    params: () => this.basketService.basketId() > 0 ? this.basketService.basketId() : undefined,
    stream: ({params}) => this.basketService.getBasket(params!),
  });

  readonly shipmentId = signal<number | null>(null);
  readonly paymentId = signal<number | null>(null);
  readonly confirmation = signal<OrderSummary | null>(null);

  // Brak koszyka (id 0) lub koszyk bez pozycji — zamówienia nie da się złożyć.
  readonly basketMissing = computed(() => this.basketService.basketId() <= 0);
  readonly basketEmpty = computed(() =>
    this.basketMissing() || (this.basket.hasValue() && this.basket.value().items.length === 0));

  // Nazwy dostaw/płatności przychodzą z API po angielsku (dane z bazy) —
  // etykiety tłumaczymy po polu `type`, z fallbackiem na surową nazwę.
  readonly shipmentOptions = computed(() => {
    this.languageService.lang();
    if (!this.initOrder.hasValue()) return [];
    return this.initOrder.value().shipments.map(shipment =>
      ({...shipment, label: this.typeLabel('checkout.shipmentType.' + shipment.type, shipment.name)}));
  });

  readonly paymentOptions = computed(() => {
    this.languageService.lang();
    if (!this.initOrder.hasValue()) return [];
    return this.initOrder.value().payments.map(payment =>
      ({...payment, label: this.typeLabel('checkout.paymentType.' + payment.type, payment.name)}));
  });

  readonly selectedShipment = computed(() =>
    this.shipmentOptions().find(shipment => shipment.id === this.shipmentId()) ?? null);

  readonly totalValue = computed(() => {
    const basketValue = this.basket.hasValue() ? this.basket.value().summary.grossValue : 0;
    const shipmentPrice = this.selectedShipment()?.price ?? 0;
    return Number(basketValue) + Number(shipmentPrice);
  });

  readonly addressModel = signal({
    firstName: '',
    lastName: '',
    street: '',
    zipCode: '',
    city: '',
    // Zalogowany użytkownik nie musi przepisywać swojego e-maila (username = e-mail).
    email: this.authService.currentUser()?.username ?? '',
    phone: '',
  });

  readonly addressForm = form(this.addressModel, (f) => {
    required(f.firstName, {message: this.msg('validation.firstNameRequired')});
    maxLength(f.firstName, 30, {message: this.msg('validation.firstNameTooLong')});
    required(f.lastName, {message: this.msg('validation.lastNameRequired')});
    maxLength(f.lastName, 30, {message: this.msg('validation.lastNameTooLong')});
    required(f.street, {message: this.msg('validation.streetRequired')});
    maxLength(f.street, 60, {message: this.msg('validation.streetTooLong')});
    required(f.zipCode, {message: this.msg('validation.zipCodeRequired')});
    maxLength(f.zipCode, 10, {message: this.msg('validation.zipCodeTooLong')});
    required(f.city, {message: this.msg('validation.cityRequired')});
    maxLength(f.city, 30, {message: this.msg('validation.cityTooLong')});
    required(f.email, {message: this.msg('validation.emailRequired')});
    email(f.email, {message: this.msg('validation.emailInvalid')});
    required(f.phone, {message: this.msg('validation.phoneRequired')});
    maxLength(f.phone, 20, {message: this.msg('validation.phoneTooLong')});
  }, {injector: this.injector});

  constructor() {
    // Po załadowaniu initOrder ustaw domyślną dostawę/płatność (defaultShipment/defaultPayment).
    effect(() => {
      if (!this.initOrder.hasValue()) return;
      const init = this.initOrder.value();
      untracked(() => {
        if (this.shipmentId() == null) {
          this.shipmentId.set(init.shipments.find(s => s.defaultShipment)?.id ?? init.shipments[0]?.id ?? null);
        }
        if (this.paymentId() == null) {
          this.paymentId.set(init.payments.find(p => p.defaultPayment)?.id ?? init.payments[0]?.id ?? null);
        }
      });
    });
  }

  async onSubmit() {
    if (this.basketEmpty()) {
      this.notification.error('toast.emptyBasketTitle', 'toast.emptyBasketDetail');
      return;
    }
    if (this.shipmentId() == null || this.paymentId() == null) {
      this.notification.error('toast.missingDeliveryTitle', 'toast.missingDeliveryDetail');
      return;
    }
    await submit(this.addressForm, async () => {
      try {
        const summary = await firstValueFrom(this.orderService.createOrder({
          ...this.addressModel(),
          basketId: this.basketService.basketId(),
          shipmentId: this.shipmentId()!,
          paymentId: this.paymentId()!,
        }));
        this.confirmation.set(summary);
        this.basketService.clearBasket();
      } catch (_err) {
        this.notification.error('common.error', 'toast.orderError');
      }
      return undefined;
    });
  }

  goToShop(): void {
    void this.router.navigate(['/']);
  }

  // instant() zwraca klucz, gdy brakuje tłumaczenia — wtedy pokazujemy nazwę z bazy.
  private typeLabel(key: string, fallback: string): string {
    const translated = this.translate.instant(key);
    return translated === key ? fallback : translated;
  }
}
