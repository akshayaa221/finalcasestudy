import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth';

export const roleGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // First, ensure the user is logged in
  if (!authService.isLoggedIn()) {
    console.log('RoleGuard: User not logged in, redirecting to /login.');
    router.navigate(['/login']);
    return false;
  }

  // Get the roles required for this route from the route's data
  // Example: data: { roles: ['ADMIN', 'HOTEL_OWNER'] }
  const requiredRoles = route.data['roles'] as string[];

  if (!requiredRoles || requiredRoles.length === 0) {
    // If no roles are specified, allow access (assuming AuthGuard already passed)
    console.log('RoleGuard: No specific roles required, allowing access.');
    return true;
  }

  const userRoles = authService.getRoles();
  console.log('RoleGuard: User Roles:', userRoles, 'Required Roles:', requiredRoles);

  // Check if the user has at least one of the required roles
  const hasRequiredRole = requiredRoles.some(role => userRoles.includes(role));

  if (hasRequiredRole) {
    console.log('RoleGuard: User has required role, allowing access.');
    return true;
  } else {
    console.log('RoleGuard: User does NOT have required role, redirecting to /unauthorized or home.');
    // Redirect to an unauthorized page or home
    router.navigate(['/unauthorized']); // We'll create this component later
    return false;
  }
};