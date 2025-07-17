import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { UserService, User, UserRole } from '../../services/user';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule, CommonModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: './register.scss'
})
export class RegisterComponent {
  user: User = {
    id: undefined,
    username: '',
    email: '',
    password: '',
    name: '',
    gender: '',
    contactNumber: '',
    address: '',
    roles: ['GUEST']
  };
  confirmPassword: string = '';
  errorMessage: string | null = null;
  successMessage: string | null = null;
  isRegistering: boolean = false;

  roles: UserRole[] = ['GUEST', 'HOTEL_OWNER'];

  constructor(
    private userService: UserService,
    private router: Router
  ) {}

  onSubmit(): void { // <-- FIXED: Method name is onSubmit
    this.errorMessage = null;
    this.successMessage = null;
    this.isRegistering = true;

    if (this.user.password !== this.confirmPassword) {
      this.errorMessage = 'Passwords do not match.';
      this.isRegistering = false;
      return;
    }

    this.userService.createUser(this.user).subscribe({
      next: (response: User) => {
        this.successMessage = 'Registration successful! You can now log in.';
        this.isRegistering = false;
        console.log('Registration successful:', response);
        this.router.navigate(['/login']);
      },
      error: (err: any) => {
        console.error('Registration failed:', err);
        if (err && err.error && err.error.message) {
          this.errorMessage = err.error.message;
        } else if (err && err.error) {
          this.errorMessage = err.error;
        } else {
          this.errorMessage = 'Registration failed. Please try again.';
        }
        this.isRegistering = false;
      }
    });
  }
}
