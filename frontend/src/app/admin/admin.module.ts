import {NgModule} from '@angular/core';
import {ChartModule} from 'primeng/chart';
import {SharedModule} from '../shared/shared.module';
import {AdminRoutingModule} from './admin-routing.module';
import {AdminDashboardComponent} from './dashboard/admin-dashboard.component';
import {AdminProductsComponent} from './products/admin-products.component';
import {AdminManageProductComponent} from './products/manage-product/admin-manage-product.component';
import {AdminCategoriesComponent} from './categories/admin-categories.component';
import {AdminOrdersComponent} from './orders/admin-orders.component';
import {AdminOrderDetailsComponent} from './orders/order-details/admin-order-details.component';

@NgModule({
  declarations: [
    AdminDashboardComponent,
    AdminProductsComponent,
    AdminManageProductComponent,
    AdminCategoriesComponent,
    AdminOrdersComponent,
    AdminOrderDetailsComponent,
  ],
  imports: [
    SharedModule,
    AdminRoutingModule,
    // ChartModule tylko tutaj — ciągnie chart.js (~200 kB), który ma trafić
    // do lazy chunka admina, a nie do bundla wejściowego sklepu.
    ChartModule,
  ],
})
export class AdminModule {
}
