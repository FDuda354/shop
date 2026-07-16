import {Component, inject, Injector, signal} from '@angular/core';
import {form, minLength, required, submit, validate} from '@angular/forms/signals';
import {firstValueFrom} from 'rxjs';
import {rxResource} from '@angular/core/rxjs-interop';
import {AuthService} from '../../services/auth/auth.service';
import {UserService} from '../../services/user.service';
import {NotificationService} from '../../services/notification.service';
import {imageUrl} from '../../utils/image-url';
import {validationMessages} from '../../utils/validation-message';

@Component({
  selector: 'app-profile',
  standalone: false,
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.scss',
})
export class ProfileComponent {
  private readonly userService = inject(UserService);
  private readonly notification = inject(NotificationService);
  private readonly injector = inject(Injector);
  private readonly msg = validationMessages();

  readonly authService = inject(AuthService);
  readonly imageUrl = imageUrl;
  readonly uploadingAvatar = signal(false);

  readonly profileImage = rxResource<{image: string | null}, number | undefined>({
    params: () => this.authService.currentUser()?.id,
    stream: ({params}) => this.userService.getProfileImage(params!),
  });

  readonly passwordModel = signal({password: '', repeatPassword: ''});

  readonly passwordForm = signal(this.buildForm());

  private buildForm() {
    return form(this.passwordModel, (f) => {
      required(f.password, {message: this.msg('validation.passwordRequired')});
      minLength(f.password, 8, {message: this.msg('validation.passwordMinLength')});
      required(f.repeatPassword, {message: this.msg('validation.passwordRepeat')});
      validate(f.repeatPassword, ({value}) =>
        value() !== '' && value() !== this.passwordModel().password
          ? {kind: 'passwordMismatch', message: this.msg('validation.passwordMismatch')()}
          : undefined);
    }, {injector: this.injector});
  }

  onAvatarSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    this.uploadingAvatar.set(true);
    this.userService.uploadProfileImage(file).subscribe({
      next: response => {
        this.userService.updateProfileImage(response.fileName).subscribe({
          next: () => {
            this.uploadingAvatar.set(false);
            this.profileImage.reload();
            this.notification.success('common.saved', 'toast.avatarUpdated');
          },
          error: () => {
            this.uploadingAvatar.set(false);
            this.notification.error('common.error', 'toast.avatarSaveError');
          },
        });
      },
      error: () => {
        this.uploadingAvatar.set(false);
        this.notification.error('common.error', 'toast.imageUploadError');
      },
    });
    input.value = '';
  }

  async onPasswordSubmit() {
    await submit(this.passwordForm(), async () => {
      try {
        await firstValueFrom(this.userService.changePassword(
          this.passwordModel().password,
          this.passwordModel().repeatPassword,
        ));
        this.passwordModel.set({password: '', repeatPassword: ''});
        this.passwordForm.set(this.buildForm());
        this.notification.success('common.saved', 'toast.passwordChanged');
      } catch (_err) {
        this.notification.error('common.error', 'toast.passwordChangeError');
      }
      return undefined;
    });
  }
}
