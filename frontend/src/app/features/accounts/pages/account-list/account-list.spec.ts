import { registerLocaleData } from '@angular/common';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import localeEsCo from '@angular/common/locales/es-CO';
import { LOCALE_ID } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { Account } from '../../models/account.model';
import { AccountList } from './account-list';

const SAVINGS: Account = {
  id: 1, type: 'SAVINGS', number: '5300000001', status: 'ACTIVE', balance: 150000, availableBalance: 150000,
  gmfExempt: false, clientId: 7, createdAt: '2026-10-30T10:00:00', updatedAt: '2026-10-30T10:00:00',
};
const MARIA = { id: 7, firstNames: 'María', lastName: 'Gómez' };

describe('AccountList', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    registerLocaleData(localeEsCo);
    TestBed.configureTestingModule({
      imports: [AccountList],
      providers: [
        provideHttpClient(), provideHttpClientTesting(), provideRouter([]),
        { provide: LOCALE_ID, useValue: 'es-CO' },
      ],
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  function render(accounts: Account[]) {
    const fixture = TestBed.createComponent(AccountList);
    fixture.detectChanges();
    http.expectOne('/api/accounts').flush(accounts);
    http.expectOne('/api/clients').flush([MARIA]);
    fixture.detectChanges();
    return { fixture, element: fixture.nativeElement as HTMLElement };
  }

  function clickButton(root: HTMLElement, label: string) {
    const button = Array.from(root.querySelectorAll('button')).find((b) => b.textContent?.trim() === label);
    if (!button) {
      throw new Error(`No se encontró el botón "${label}"`);
    }
    button.click();
  }

  it('muestra la cuenta con su dueño, saldo y estado', () => {
    const { element } = render([SAVINGS]);

    expect(element.textContent).toContain('5300000001');
    expect(element.textContent).toContain('María Gómez');
    expect(element.textContent).toContain('150.000');
    expect(element.textContent).toContain('Activa');
  });

  it('inactiva una cuenta y actualiza su fila', () => {
    const { fixture, element } = render([SAVINGS]);

    clickButton(element, 'Inactivar');
    http.expectOne({ method: 'PATCH', url: '/api/accounts/1/deactivate' }).flush({ ...SAVINGS, status: 'INACTIVE' });
    fixture.detectChanges();

    expect(element.querySelector('.status')?.textContent).toContain('Inactiva');
    expect(element.textContent).toContain('Activar');
  });

  it('pide confirmación para cancelar y muestra el motivo si el backend la rechaza', () => {
    const { fixture, element } = render([SAVINGS]);

    clickButton(element, 'Cancelar');
    fixture.detectChanges();
    clickButton(element, 'Sí, cancelar');
    http.expectOne({ method: 'PATCH', url: '/api/accounts/1/cancel' }).flush(
      { detail: 'La cuenta 5300000001 tiene saldo $150000.00 y solo se puede cancelar con saldo $0' },
      { status: 422, statusText: 'Unprocessable Content' },
    );
    fixture.detectChanges();

    expect(element.querySelector('[role="alert"]')?.textContent).toContain('solo se puede cancelar con saldo $0');
  });
});
