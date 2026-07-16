import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {AdminDashboardComponent} from './dashboard/admin-dashboard.component';
import {AdminProductsComponent} from './products/admin-products.component';
import {AdminManageProductComponent} from './products/manage-product/admin-manage-product.component';
import {AdminCategoriesComponent} from './categories/admin-categories.component';
import {AdminOrdersComponent} from './orders/admin-orders.component';
import {AdminOrderDetailsComponent} from './orders/order-details/admin-order-details.component';

const routes: Routes = [
  {path: '', component: AdminDashboardComponent},
  {path: 'products', component: AdminProductsComponent},
  {path: 'product/new', component: AdminManageProductComponent},
  {path: 'product/:id', component: AdminManageProductComponent},
  {path: 'categories', component: AdminCategoriesComponent},
  {path: 'orders', component: AdminOrdersComponent},
  {path: 'order/:id', component: AdminOrderDetailsComponent},
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class AdminRoutingModule {
}
