import {Component, signal} from '@angular/core';

/**
 * Layout sklepu: sidebar z kategoriami (desktop) / drawer (mobile) + header.
 */
@Component({
  selector: 'app-default-layout',
  standalone: false,
  templateUrl: './default-layout.component.html',
  styleUrl: './default-layout.component.scss',
})
export class DefaultLayoutComponent {
  readonly drawerVisible = signal(false);

  toggleDrawer(): void {
    this.drawerVisible.update(visible => !visible);
  }

  closeDrawer(): void {
    this.drawerVisible.set(false);
  }
}
