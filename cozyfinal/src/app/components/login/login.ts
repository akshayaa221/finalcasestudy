import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router'; // Added RouterLink for potential future use in template
import { AuthService, LoginCredentials } from '../../services/auth';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, CommonModule, RouterLink], // Added RouterLink
  templateUrl: './login.html', // <-- FIXED: Changed to .html
  styleUrl: './login.scss' // <-- FIXED: Changed to .scss
})
export class LoginComponent implements OnInit {
  loginForm: LoginCredentials = { // <-- FIXED: Use loginForm consistently
    email: '',
    password: ''
  };
  isLoading: boolean = false; // For loading state, if needed
  errorMessage: string | null = null;

  constructor(
    private authService: AuthService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    // Redirect if already logged in
    if (this.authService.isLoggedIn()) {
      this.router.navigate(['/dashboard']);
    }
  }

  onLogin(): void { // <-- FIXED: Method name is onLogin
    this.isLoading = true;
    this.errorMessage = null;

    this.authService.login(this.loginForm).subscribe({ // <-- FIXED: Use loginForm
      next: (response) => {
        this.isLoading = false;
        console.log('Login Successful:', response);
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.isLoading = false;
        console.error('Login error:', err);
        // Extract more specific error message if available from backend
        if (err && err.error && err.error.message) {
          this.errorMessage = err.error.message;
        } else if (typeof err.error === 'string') {
          this.errorMessage = err.error; // Backend might return plain string error
        }
        else {
          this.errorMessage = 'Login failed. Please check your credentials.';
        }
      }
    });
  }
}
