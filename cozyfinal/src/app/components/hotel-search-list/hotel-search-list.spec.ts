import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HotelSearchList } from './hotel-search-list';

describe('HotelSearchList', () => {
  let component: HotelSearchList;
  let fixture: ComponentFixture<HotelSearchList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HotelSearchList]
    })
    .compileComponents();

    fixture = TestBed.createComponent(HotelSearchList);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
