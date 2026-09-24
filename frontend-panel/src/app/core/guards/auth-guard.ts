import { inject } from '@angular/core';
import { CanMatchFn, Router } from '@angular/router';

import { AuthService } from '../auth/auth-service';

export const authGuard: CanMatchFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (!auth.isAuthenticated()) {
    return router.createUrlTree(['/login']);
  }

  if (auth.mustChangePassword()) {
    return router.createUrlTree(['/cambiar-password']);
  }

  return auth.isAdmin()
    ? router.createUrlTree(['/admin/usuarios'])
    : true;
};
