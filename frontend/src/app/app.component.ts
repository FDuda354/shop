import {Component, inject, OnInit} from '@angular/core';
import {BasketService} from './services/basket.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  standalone: false,
})
export class AppComponent implements OnInit {
  private readonly basketService = inject(BasketService);

  ngOnInit(): void {
    // Koszyk z localStorage może mieć pozycje z poprzedniej wizyty.
    this.basketService.refreshCounter();
  }
}
