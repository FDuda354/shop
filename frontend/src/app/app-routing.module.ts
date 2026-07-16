import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {DefaultLayoutComponent} from './layout/default/default-layout.component';
import {FullLayoutComponent} from './layout/full/full-layout.component';
import {HomeComponent} from './views/home/home.component';
import {CategoryComponent} from './views/category/category.component';
import {ProductDetailsComponent} from './views/product-details/product-details.component';
import {BasketComponent} from './views/basket/basket.component';
import {CheckoutComponent} from './views/checkout/checkout.component';
import {OrdersComponent} from './views/orders/orders.component';
import {ProfileComponent} from './views/profile/profile.component';
import {LoginComponent} from './views/login/login.component';
import {RegisterComponent} from './views/register/register.component';
import {authGuard} from './shared/guard/auth.guard';
import {adminGuard} from './shared/guard/admin.guard';

const routes: Routes = [
  {
    path: '', component: DefaultLayoutComponent, children: [
      {path: '', component: HomeComponent},
      {path: 'category/:slug', component: CategoryComponent},
      {path: 'product/:slug', component: ProductDetailsComponent},
      {path: 'basket', component: BasketComponent},
      {path: 'checkout', component: CheckoutComponent},
      {path: 'orders', component: OrdersComponent, canActivate: [authGuard]},
      {path: 'profile', component: ProfileComponent, canActivate: [authGuard]},
    ]
  },
  {
    path: 'admin',
    component: FullLayoutComponent,
    canActivate: [adminGuard],
    loadChildren: () => import('./admin/admin.module').then(m => m.AdminModule),
  },
  {path: 'login', component: LoginComponent},
  {path: 'register', component: RegisterComponent},
  {path: '**', redirectTo: ''},
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
