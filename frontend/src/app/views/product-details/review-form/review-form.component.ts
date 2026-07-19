import {Component, inject, Injector, input, output, signal} from '@angular/core';
import {form, maxLength, minLength, required} from '@angular/forms/signals';
import {finalize} from 'rxjs';
import {ReviewService} from '../../../services/review.service';
import {NotificationService} from '../../../services/notification.service';
import {validationMessages} from '../../../utils/validation-message';

@Component({
  selector: 'app-review-form',
  standalone: false,
  templateUrl: './review-form.component.html',
  styleUrl: './review-form.component.scss',
})
export class ReviewFormComponent {
  private readonly reviewService = inject(ReviewService);
  private readonly notification = inject(NotificationService);
  private readonly injector = inject(Injector);
  private readonly msg = validationMessages();

  productId = input.required<number>();
  reviewAdded = output<void>();

  readonly reviewModel = signal({authorName: '', content: ''});

  readonly submitting = signal(false);

  // Signal forms w 22.0 nie mają publicznego resetu touched — po sukcesie
  // budujemy świeży formularz, żeby wyczyszczone pola nie świeciły błędami.
  readonly reviewForm = signal(this.buildForm());

  private buildForm() {
    return form(this.reviewModel, (f) => {
      required(f.authorName, {message: this.msg('validation.reviewAuthorRequired')});
      minLength(f.authorName, 2, {message: this.msg('validation.reviewAuthorMinLength')});
      maxLength(f.authorName, 60, {message: this.msg('validation.reviewAuthorMaxLength')});
      required(f.content, {message: this.msg('validation.reviewContentRequired')});
      minLength(f.content, 2, {message: this.msg('validation.reviewContentMinLength')});
      maxLength(f.content, 600, {message: this.msg('validation.reviewContentMaxLength')});
    }, {injector: this.injector});
  }

  onSubmit() {
    this.reviewForm()().markAsTouched();
    if (this.reviewForm()().invalid()) {
      return;
    }
    this.submitting.set(true);
    this.reviewService.addReview({
      authorName: this.reviewModel().authorName,
      content: this.reviewModel().content,
      productId: this.productId(),
    })
      .pipe(finalize(() => this.submitting.set(false)))
      .subscribe({
        next: () => {
          this.reviewModel.set({authorName: '', content: ''});
          this.reviewForm.set(this.buildForm());
          this.notification.success('toast.reviewThanks', 'toast.reviewAdded');
          this.reviewAdded.emit();
        },
        error: () => {
          this.notification.error('common.error', 'toast.reviewError');
        },
      });
  }
}
