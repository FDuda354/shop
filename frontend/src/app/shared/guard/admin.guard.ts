import {CanActivateFn, Router} from '@angular/router';
import {inject} from '@angular/core';
import {map} from 'rxjs/operators';
import {AuthService} from '../../services/auth/auth.service';

export const adminGuard: CanActivateFn = (_route, _state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return authService.loadMe().pipe(
    map(user => {
      if (!user) return router.createUrlTree(['/login']);
      return user.admin ? true : router.createUrlTree(['/']);
    })
  );
};
