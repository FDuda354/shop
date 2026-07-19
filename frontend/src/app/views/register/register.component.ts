import {Component, inject, signal, ViewEncapsulation} from '@angular/core';
import {email, form, minLength, required, validate} from '@angular/forms/signals';
import {HttpErrorResponse} from '@angular/common/http';
import {finalize} from 'rxjs';
import {Router} from '@angular/router';
import {AuthService} from '../../services/auth/auth.service';
import {NotificationService} from '../../services/notification.service';
import {validationMessages} from '../../utils/validation-message';

@Component({
  selector: 'app-register',
  standalone: false,
  templateUrl: './register.component.html',
  styleUrls: ['../login/login.component.scss'],
  encapsulation: ViewEncapsulation.None,
})
export class RegisterComponent {

  private readonly authService = inject(AuthService);
  private readonly notification = inject(NotificationService);
  private readonly router = inject(Router);
  private readonly msg = validationMessages();

  readonly registerModel = signal({username: '', password: '', confirmPassword: ''});

  readonly passwordMasked = signal(true);
  readonly confirmPasswordMasked = signal(true);
  readonly submitting = signal(false);

  readonly registerForm = form(this.registerModel, (f) => {
    required(f.username, {message: this.msg('validation.emailRequired')});
    email(f.username, {message: this.msg('validation.emailInvalid')});
    required(f.password, {message: this.msg('validation.passwordRequired')});
    minLength(f.password, 8, {message: this.msg('validation.passwordMinLength')});
    required(f.confirmPassword, {message: this.msg('validation.passwordRepeat')});
    validate(f.confirmPassword, ({value}) =>
      value() !== '' && value() !== this.registerModel().password
        ? {kind: 'passwordMismatch', message: this.msg('validation.passwordMismatch')()}
        : undefined);
  });

  onSubmit() {
    this.registerForm().markAsTouched();
    if (this.registerForm().invalid()) {
      return;
    }
    this.submitting.set(true);
    this.authService.register(this.registerModel())
      .pipe(finalize(() => this.submitting.set(false)))
      .subscribe({
        next: () => {
          void this.router.navigate(['/']);
        },
        error: (err: HttpErrorResponse) => {
          if (err.status === 400) {
            this.notification.error('toast.registerFailedTitle', 'toast.registerFailedDetail');
          } else if (err.status === 0) {
            this.notification.error('toast.noConnectionTitle', 'toast.noConnectionDetail');
          } else {
            this.notification.error('common.error', 'toast.serverError');
          }
        },
      });
  }
}
