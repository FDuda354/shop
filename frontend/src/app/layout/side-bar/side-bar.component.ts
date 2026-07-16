import {Component, inject, output} from '@angular/core';
import {rxResource} from '@angular/core/rxjs-interop';
import {Category} from '../../models/category';
import {CategoryService} from '../../services/category.service';
import {localizedName} from '../../utils/localized';

/**
 * Nawigacja po kategoriach — na desktopie żyje w sidebarze DefaultLayoutu,
 * na mobile ta sama instancja renderuje się w p-drawer.
 */
@Component({
  selector: 'app-side-bar',
  standalone: false,
  templateUrl: './side-bar.component.html',
  styleUrl: './side-bar.component.scss',
})
export class SideBarComponent {
  private readonly categoryService = inject(CategoryService);

  navigated = output<void>();

  readonly localizedName = localizedName;

  readonly categories = rxResource<Category[], undefined>({
    stream: () => this.categoryService.getCategories(),
  });
}
