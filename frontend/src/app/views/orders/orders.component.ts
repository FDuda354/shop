import {Component, inject} from '@angular/core';
import {rxResource} from '@angular/core/rxjs-interop';
import {OrderService} from '../../services/order.service';
import {OrderForUser} from '../../models/order';
import {orderStatusSeverity} from '../../utils/order-status';

@Component({
  selector: 'app-orders',
  standalone: false,
  templateUrl: './orders.component.html',
  styleUrl: './orders.component.scss',
})
export class OrdersComponent {
  private readonly orderService = inject(OrderService);

  readonly orderStatusSeverity = orderStatusSeverity;

  readonly orders = rxResource<OrderForUser[], undefined>({
    stream: () => this.orderService.getMyOrders(),
  });
}
