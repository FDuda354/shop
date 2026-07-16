import {Component, inject, signal, ViewEncapsulation} from '@angular/core';
import {form, required, submit} from '@angular/forms/signals';
import {firstValueFrom} from 'rxjs';
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

  readonly loginForm = form(this.loginModel, (f) => {
    required(f.username, {message: this.msg('validation.emailRequired')});
    required(f.password, {message: this.msg('validation.passwordRequired')});
  });

  async onSubmit() {
    await submit(this.loginForm, async () => {
      try {
        const user = await firstValueFrom(this.authService.login(this.loginModel()));
        void this.router.navigate([user.admin ? '/admin' : '/']);
      } catch (err: any) {
        if (err.status === 0) {
          this.notification.error('toast.noConnectionTitle', 'toast.noConnectionDetail');
        } else if (err.status === 401) {
          this.notification.error('toast.badCredentialsTitle', 'toast.badCredentialsDetail');
        } else if (err.status === 429) {
          this.notification.error('toast.tooManyAttemptsTitle', 'toast.tooManyAttemptsDetail');
        } else {
          this.notification.error('common.error', 'toast.serverError');
        }
      }
      return undefined;
    });
  }
}
