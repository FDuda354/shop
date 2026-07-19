import {Component, inject, signal, ViewEncapsulation} from '@angular/core';
import {form, required} from '@angular/forms/signals';
import {HttpErrorResponse} from '@angular/common/http';
import {finalize} from 'rxjs';
import {Router} from '@angular/router';
import {AuthService} from '../../services/auth/auth.service';
import {NotificationService} from '../../services/notification.service';
import {validationMessages} from '../../utils/validation-message';

@Component({
  selector: 'app-login',
  standalone: false,
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
  encapsulation: ViewEncapsulation.None,
})
export class LoginComponent {

  private readonly authService = inject(AuthService);
  private readonly notification = inject(NotificationService);
  private readonly router = inject(Router);
  private readonly msg = validationMessages();

  readonly loginModel = signal({username: '', password: ''});

  readonly passwordMasked = signal(true);
  readonly submitting = signal(false);

  readonly loginForm = form(this.loginModel, (f) => {
    required(f.username, {message: this.msg('validation.emailRequired')});
    required(f.password, {message: this.msg('validation.passwordRequired')});
  });

  onSubmit() {
    this.loginForm().markAsTouched();
    if (this.loginForm().invalid()) {
      return;
    }
    this.submitting.set(true);
    this.authService.login(this.loginModel())
      .pipe(finalize(() => this.submitting.set(false)))
      .subscribe({
        next: (user) => {
          void this.router.navigate([user.admin ? '/admin' : '/']);
        },
        error: (err: HttpErrorResponse) => {
          if (err.status === 0) {
            this.notification.error('toast.noConnectionTitle', 'toast.noConnectionDetail');
          } else if (err.status === 401) {
            this.notification.error('toast.badCredentialsTitle', 'toast.badCredentialsDetail');
          } else if (err.status === 429) {
            this.notification.error('toast.tooManyAttemptsTitle', 'toast.tooManyAttemptsDetail');
          } else {
            this.notification.error('common.error', 'toast.serverError');
          }
        },
      });
  }
}
