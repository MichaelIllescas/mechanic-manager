import { inject } from '@angular/core';
import { CanMatchFn, Router } from '@angular/router';

import { AuthService } from '../auth/auth-service';

/** Deja entrar a la pantalla de cambio solo a quien tiene una contraseña temporal pendiente. */
export const passwordChangeGuard: CanMatchFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (!auth.isAuthenticated()) {
    return router.createUrlTree(['/login']);
  }

  if (auth.mustChangePassword()) {
    return true;
  }

  return auth.isAdmin()
    ? router.createUrlTree(['/admin/usuarios'])
    : router.createUrlTree(['/dashboard']);
};
