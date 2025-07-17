import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HotelManagement } from './hotel-management';

describe('HotelManagement', () => {
  let component: HotelManagement;
  let fixture: ComponentFixture<HotelManagement>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HotelManagement]
    })
    .compileComponents();

    fixture = TestBed.createComponent(HotelManagement);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
