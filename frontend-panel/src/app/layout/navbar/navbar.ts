import { Component, computed, inject, input } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../../core/auth/auth-service';
import { NotificationService } from '../../core/notifications/notification-service';

export interface NavbarLink {
  label: string;
  path: string;
  exact?: boolean;
}

const APP_LINKS: NavbarLink[] = [
  { label: 'Inicio', path: '/dashboard', exact: true },
  { label: 'Clientes', path: '/customers' },
  { label: 'Vehículos', path: '/vehicles' },
  { label: 'Turnos', path: '/appointments' },
  { label: 'Presupuestos', path: '/quotes' },
  { label: 'Servicios', path: '/servicios' },
  { label: 'Insumos', path: '/insumos' },
  { label: 'Historial', path: '/history' },
];

const ADMIN_LINKS: NavbarLink[] = [{ label: 'Usuarios', path: '/admin/usuarios' }];

@Component({
  selector: 'app-navbar',
  imports: [RouterLink],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css',
})
export class Navbar {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly notifications = inject(NotificationService);

  readonly variant = input<'app' | 'admin'>('app');

  readonly links = computed(() => (this.variant() === 'admin' ? ADMIN_LINKS : APP_LINKS));
  readonly homePath = computed(() => (this.variant() === 'admin' ? '/admin/usuarios' : '/dashboard'));
  readonly userName = this.auth.getUserName() ?? 'Usuario';
  readonly userInitial = this.userName.trim().charAt(0).toUpperCase() || 'U';

  isMenuOpen = false;
  isProfileMenuOpen = false;

  toggleMenu(): void {
    this.isMenuOpen = !this.isMenuOpen;
    this.isProfileMenuOpen = false;
  }

  toggleProfileMenu(): void {
    this.isProfileMenuOpen = !this.isProfileMenuOpen;
  }

  navigateTo(event: MouseEvent, path: string): void {
    event.preventDefault();
    void this.router.navigateByUrl(path).then(() => this.closeMenus());
  }

  isLinkActive(link: NavbarLink): boolean {
    return this.router.isActive(link.path, {
      paths: link.exact ? 'exact' : 'subset',
      queryParams: 'ignored',
      fragment: 'ignored',
      matrixParams: 'ignored',
    });
  }

  closeMenus(): void {
    this.isMenuOpen = false;
    this.isProfileMenuOpen = false;
  }

  logout(): void {
    this.closeMenus();
    this.auth.logout();
    void this.router.navigate(['/login']);
    this.notifications.success('Cerraste sesión correctamente');
  }
}
