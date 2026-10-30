import { registerLocaleData } from '@angular/common';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import localeEsCo from '@angular/common/locales/es-CO';
import { LOCALE_ID } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { Account } from '../../../accounts/models/account.model';
import { Dashboard } from './dashboard';

function account(id: number, type: Account['type'], status: Account['status'], balance: number): Account {
  return {
    id, type, status, balance, availableBalance: balance, gmfExempt: false, clientId: 1,
    number: (type === 'SAVINGS' ? '53' : '33') + String(id).padStart(8, '0'),
    createdAt: '2026-10-30T10:00:00', updatedAt: '2026-10-30T10:00:00',
  };
}

describe('Dashboard', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    registerLocaleData(localeEsCo);
    TestBed.configureTestingModule({
      imports: [Dashboard],
      providers: [
        provideHttpClient(), provideHttpClientTesting(), provideRouter([]),
        { provide: LOCALE_ID, useValue: 'es-CO' },
      ],
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  function renderWith(accounts: Account[]) {
    const fixture = TestBed.createComponent(Dashboard);
    fixture.detectChanges();
    http.expectOne('/api/clients').flush([]);
    http.expectOne('/api/accounts').flush(accounts);
    fixture.detectChanges();
    return fixture;
  }

  it('muestra el dinero total y cuántas cuentas hay', () => {
    const fixture = renderWith([account(1, 'SAVINGS', 'ACTIVE', 100000), account(2, 'CHECKING', 'ACTIVE', 50000)]);
    const element = fixture.nativeElement as HTMLElement;

    expect(element.querySelector('.total')?.textContent).toContain('150.000');
    expect(element.querySelector('.donut-center strong')?.textContent).toBe('2');
  });

  it('filtra la lista de cuentas por estado al pulsar un chip', () => {
    const fixture = renderWith([account(1, 'SAVINGS', 'ACTIVE', 100000), account(2, 'CHECKING', 'INACTIVE', 50000)]);
    const element = fixture.nativeElement as HTMLElement;
    const inactiveChip = Array.from(element.querySelectorAll<HTMLButtonElement>('.chip'))
      .find((chip) => chip.textContent?.trim() === 'Inactiva');

    inactiveChip?.click();
    fixture.detectChanges();

    const numbers = Array.from(element.querySelectorAll('.top-list .number')).map((n) => n.textContent);
    expect(numbers).toEqual(['3300000002']);
  });
});
