import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BookingManagement } from './booking-management';

describe('BookingManagement', () => {
  let component: BookingManagement;
  let fixture: ComponentFixture<BookingManagement>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BookingManagement]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BookingManagement);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
