import { Routes } from '@angular/router';

// Core Components - Using your provided import style (e.g., './components/home/home')
import { HomeComponent } from './components/home/home';
import { LoginComponent } from './components/login/login';
import { RegisterComponent } from './components/register/register';
import { DashboardComponent } from './components/dashboard/dashboard';
import { UnauthorizedComponent } from './components/unauthorized/unauthorized';

// User-facing Components - Using your provided import style and component names
import { HotelListComponent } from './components/hotel-list/hotel-list'; // Your main hotel list component
import { HotelDetailComponent } from './components/hotel-detail/hotel-detail';
import { BookingFormComponent } from './components/booking-form/booking-form';
import { MyBookingsComponent } from './components/my-bookings/my-bookings';
import { UserProfileComponent } from './components/user-profile/user-profile';

// Admin Components - Using your provided import style
import { AdminDashboardComponent } from './components/admin-dashboard/admin-dashboard';
import { UserManagementComponent } from './components/admin/user-management/user-management';
import { HotelManagementComponent } from './components/admin/hotel-management/hotel-management';
import { RoomManagementComponent } from './components/admin/room-management/room-management';
import { BookingManagementComponent } from './components/admin/booking-management/booking-management';

// Functional Guards - Using your provided import style (e.g., './guards/auth-guard')
import { authGuard } from './guards/auth-guard';
import { roleGuard } from './guards/role-guard';

export const routes: Routes = [
  // Default redirect
  { path: '', redirectTo: '/home', pathMatch: 'full' },

  // Public Routes
  { path: 'home', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'unauthorized', component: UnauthorizedComponent },

  // User-facing Protected Routes (require authentication)
  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [authGuard]
  },
  {
    path: 'hotels',
    component: HotelListComponent, // This is the main list component, now handles search
    canActivate: [authGuard]
  },
  {
    path: 'hotels/:id',
    component: HotelDetailComponent,
    canActivate: [authGuard]
  },
  {
    path: 'book-room/:roomId',
    component: BookingFormComponent,
    canActivate: [authGuard]
  },
  {
    path: 'my-bookings',
    component: MyBookingsComponent,
    canActivate: [authGuard]
  },
  {
    path: 'profile',
    component: UserProfileComponent,
    canActivate: [authGuard]
  },

  // Admin Protected Routes (require ADMIN role)
  {
    path: 'admin',
    component: AdminDashboardComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] }
  },
  {
    path: 'admin/users',
    component: UserManagementComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] }
  },
  {
    path: 'admin/hotels',
    component: HotelManagementComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] }
  },
  {
    path: 'admin/rooms',
    component: RoomManagementComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] }
  },
  {
    path: 'admin/bookings',
    component: BookingManagementComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] }
  },

  // Wildcard route for 404 Not Found (redirects to home)
  { path: '**', redirectTo: '/home' }
];