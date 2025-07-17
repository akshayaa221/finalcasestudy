import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService, DecodedToken } from '../../services/auth';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss'
})
export class DashboardComponent implements OnInit {
  currentUser: DecodedToken | null = null; // <<< IMPORTANT: Declare and initialize currentUser

  constructor(public authService: AuthService) {}

  ngOnInit(): void {
    // <<< IMPORTANT: Assign the value from AuthService to currentUser
    this.currentUser = this.authService.currentUserValue;
  }
}
