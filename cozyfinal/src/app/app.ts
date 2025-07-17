import { Component } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive, Router } from '@angular/router'; // Import Router
import { CommonModule } from '@angular/common'; // For *ngIf
import { AuthService } from './services/auth'; // Import AuthService

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CommonModule], // Add CommonModule
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class AppComponent {
  title = 'cozyfinal';

  constructor(public authService: AuthService, private router: Router) {} // Inject AuthService and Router

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']); // Navigate to login page after logout
  }
}
