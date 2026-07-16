import {Component, computed, input, ResourceRef, Signal, TemplateRef} from '@angular/core';
import {Page} from '../../../../models/page';

/**
 * Generyczny wrapper na ResourceRef<T>: spinner / błąd / pusto / treść.
 * Spring Page<T> jest automatycznie rozpakowywany — do templatki treści
 * trafia page.content jako $implicit.
 */
@Component({
  selector: 'ui-content-loader',
  standalone: false,
  templateUrl: './content-loader.component.html',
  styleUrl: './content-loader.component.scss',
})
export class ContentLoaderComponent<T> {
  data = input.required<ResourceRef<T>>();
  contentTemplate = input.required<TemplateRef<{ $implicit: T }>>();
  errorTemplate = input<TemplateRef<{ $implicit: T }>>();
  emptyTemplate = input<TemplateRef<{ $implicit: T }>>();
  loaderTemplate = input<TemplateRef<{ $implicit: T }>>();

  readonly isLoading: Signal<boolean> = computed(() => {
    const status = this.data().status();
    return status === 'loading' || status === 'reloading' || status === 'idle';
  });

  readonly readyData: Signal<any | null> = computed(() => {
    if (!this.data().hasValue()) {
      return null;
    }
    const v = this.data().value();
    return this.isPage(v) ? v.content : (v as T);
  });

  readonly isEmpty: Signal<boolean> = computed(() => {
    if (!this.data().hasValue()) {
      return false;
    }
    const v = this.data().value();
    if (this.isPage(v)) return v.content.length === 0;
    if (Array.isArray(v)) return v.length === 0;
    return v == null;
  });

  private isPage(value: any): value is Page<any> {
    return value && typeof value === 'object' && 'content' in value && Array.isArray(value.content);
  }
}
