import {CanActivateFn, Router} from '@angular/router';
import {inject} from '@angular/core';
import {map} from 'rxjs/operators';
import {AuthService} from '../../services/auth/auth.service';

export const authGuard: CanActivateFn = (_route, _state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return authService.loadMe().pipe(
    map(user => user ? true : router.createUrlTree(['/login']))
  );
};
