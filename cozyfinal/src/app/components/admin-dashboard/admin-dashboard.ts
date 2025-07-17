import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common'; // For *ngIf, *ngFor
import { RouterLink } from '@angular/router'; // For navigation links

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink], // Add CommonModule and RouterLink
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.scss'
})
export class AdminDashboardComponent implements OnInit {
  // No specific data fetching for the dashboard itself,
  // it's more of a navigation hub.
  // Data fetching will happen in sub-components (e.g., UserManagementComponent).

  constructor() {}

  ngOnInit(): void {
    console.log('Admin Dashboard loaded.');
  }

  // Placeholder for future admin actions or data summaries
  // For example, you might have methods here to fetch counts of users, hotels, bookings etc.
  // or to navigate to specific management pages.
}