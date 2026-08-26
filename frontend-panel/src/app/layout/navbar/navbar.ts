import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';

import { AuthService } from '../../core/auth/auth-service';

@Component({
  selector: 'app-navbar',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css',
})
export class Navbar {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly userName = this.authService.getUserName() ?? 'Usuario';
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

  closeMenus(): void {
    this.isMenuOpen = false;
    this.isProfileMenuOpen = false;
  }

  logout(): void {
    this.authService.logout();
    this.closeMenus();
    void this.router.navigate(['/login']);
  }
}
