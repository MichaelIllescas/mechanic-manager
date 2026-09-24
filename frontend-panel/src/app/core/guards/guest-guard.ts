import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AuthService } from '../auth/auth-service';

export const guestGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    return true;
  }

  if (authService.mustChangePassword()) {
    return router.createUrlTree(['/cambiar-password']);
  }

  return authService.isAdmin()
    ? router.createUrlTree(['/admin/usuarios'])
    : router.createUrlTree(['/dashboard']);
};
