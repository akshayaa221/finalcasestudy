import { Component, OnInit } from '@angular/core';
import { AuthService, DecodedToken } from '@app-services/auth'; // Import AuthService and DecodedToken
import { UserService, User } from '@app-services/user'; // Import UserService and User interface
import { CommonModule } from '@angular/common'; // For *ngIf
import { FormsModule } from '@angular/forms'; // For ngModel in the form
import { HttpErrorResponse } from '@angular/common/http'; // For error typing
import { Router } from '@angular/router'; // Import Router for redirection

@Component({
  selector: 'app-user-profile',
  standalone: true,
  imports: [CommonModule, FormsModule], // Add CommonModule and FormsModule
  templateUrl: './user-profile.html',
  styleUrl: './user-profile.scss'
})
export class UserProfileComponent implements OnInit {
  currentUser: DecodedToken | null = null;
  userProfile: User | null = null; // Full user profile data
  updatedUser: User | null = null; // A copy for editing
  originalPassword = ''; // To store the original password for comparison if updating
  newPassword = '';
  confirmNewPassword = '';

  isLoading: boolean = true;
  errorMessage: string | null = null;
  successMessage: string | null = null;
  isEditing: boolean = false; // To toggle edit mode

  constructor(
    private authService: AuthService,
    private userService: UserService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.currentUserValue;
    if (this.currentUser && this.currentUser.userId) {
      this.loadUserProfile(this.currentUser.userId);
    } else {
      this.errorMessage = 'You must be logged in to view your profile.';
      this.isLoading = false;
      this.router.navigate(['/login']); // Redirect to login if not authenticated
    }
  }

  loadUserProfile(userId: number): void {
    this.isLoading = true;
    this.errorMessage = null;
    this.userService.getUserById(userId).subscribe({
      next: (data: User) => {
        this.userProfile = data;
        // Create a copy for editing, exclude password for security
        this.updatedUser = { ...data, password: '' };
        this.isLoading = false;
        console.log('User profile loaded:', this.userProfile);
      },
      error: (err: HttpErrorResponse) => {
        console.error('Failed to load user profile:', err);
        this.errorMessage = `Failed to load profile: ${err.error?.message || 'Server error'}`;
        this.isLoading = false;
      }
    });
  }

  toggleEditMode(): void {
    this.isEditing = !this.isEditing;
    this.errorMessage = null;
    this.successMessage = null;
    if (this.isEditing) {
      // Reset password fields when entering edit mode
      this.newPassword = '';
      this.confirmNewPassword = '';
    } else {
      // If exiting edit mode without saving, revert changes in updatedUser
      if (this.userProfile) {
        this.updatedUser = { ...this.userProfile, password: '' };
      }
    }
  }

  saveProfile(): void {
    this.errorMessage = null;
    this.successMessage = null;

    if (!this.updatedUser || !this.currentUser || !this.currentUser.userId) {
      this.errorMessage = 'User data is missing.';
      return;
    }

    // Handle password update logic
    if (this.newPassword) {
      if (this.newPassword !== this.confirmNewPassword) {
        this.errorMessage = 'New passwords do not match.';
        return;
      }
      // Assign new password to updatedUser for sending to backend
      this.updatedUser.password = this.newPassword;
    } else {
      // Ensure password is not sent if not being updated
      delete this.updatedUser.password;
    }

    this.userService.updateUser(this.currentUser.userId, this.updatedUser).subscribe({
      next: (response: User) => {
        this.successMessage = 'Profile updated successfully!';
        console.log('Profile updated:', response);
        this.loadUserProfile(this.currentUser!.userId!); // Reload to show latest data
        this.isEditing = false; // Exit edit mode
        this.newPassword = ''; // Clear password fields
        this.confirmNewPassword = '';
      },
      error: (err: HttpErrorResponse) => {
        console.error('Failed to update profile:', err);
        this.errorMessage = `Failed to update profile: ${err.error?.message || 'Server error'}`;
        // If password update failed, clear password fields for security
        this.newPassword = '';
        this.confirmNewPassword = '';
      }
    });
  }
}