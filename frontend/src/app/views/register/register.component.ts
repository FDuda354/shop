import {Component, inject, signal, ViewEncapsulation} from '@angular/core';
import {email, form, minLength, required, submit, validate} from '@angular/forms/signals';
import {firstValueFrom} from 'rxjs';
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

  async onSubmit() {
    await submit(this.registerForm, async () => {
      try {
        await firstValueFrom(this.authService.register(this.registerModel()));
        void this.router.navigate(['/']);
      } catch (err: any) {
        if (err.status === 400) {
          this.notification.error('toast.registerFailedTitle', 'toast.registerFailedDetail');
        } else if (err.status === 0) {
          this.notification.error('toast.noConnectionTitle', 'toast.noConnectionDetail');
        } else {
          this.notification.error('common.error', 'toast.serverError');
        }
      }
      return undefined;
    });
  }
}
