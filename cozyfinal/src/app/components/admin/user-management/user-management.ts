    import { Component, OnInit } from '@angular/core';
    import { UserService, User } from '@app-services/user'; // <-- CHANGED TO USE ALIAS
    import { CommonModule } from '@angular/common';
    import { HttpErrorResponse } from '@angular/common/http';

    @Component({
      selector: 'app-user-management',
      standalone: true,
      imports: [CommonModule],
      templateUrl: './user-management.html',
      styleUrl: './user-management.scss'
    })
    export class UserManagementComponent implements OnInit {
      users: User[] = [];
      isLoading: boolean = true;
      errorMessage: string | null = null;
      successMessage: string | null = null;

      constructor(private userService: UserService) {}

      ngOnInit(): void {
        this.loadUsers();
      }

      loadUsers(): void {
        this.isLoading = true;
        this.errorMessage = null;
        this.userService.getAllUsers().subscribe({
          next: (data: User[]) => {
            this.users = data;
            this.isLoading = false;
            console.log('Users loaded:', this.users);
          },
          error: (err: HttpErrorResponse) => {
            console.error('Failed to load users:', err);
            if (err.status === 403) {
              this.errorMessage = 'Access Denied: You do not have permission to view users. (Requires ADMIN role)';
            } else if (err.error && err.error.message) {
              this.errorMessage = `Failed to load users: ${err.error.message}`;
            } else {
              this.errorMessage = 'Failed to load users. Please try again later.';
            }
            this.isLoading = false;
          }
        });
      }

      // Placeholder for future methods:
      // editUser(userId: number): void { /* ... */ }
      // deleteUser(userId: number): void { /* ... */ }
      // createUser(): void { /* ... */ }
    }
    