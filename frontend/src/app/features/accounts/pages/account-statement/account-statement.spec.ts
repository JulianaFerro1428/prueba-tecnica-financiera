import { registerLocaleData } from '@angular/common';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import localeEsCo from '@angular/common/locales/es-CO';
import { LOCALE_ID } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';

import { Account } from '../../models/account.model';
import { Movement } from '../../models/movement.model';
import { AccountStatement } from './account-statement';

const ACCOUNT: Account = {
  id: 1, type: 'SAVINGS', number: '5300000001', status: 'ACTIVE', balance: 70000, availableBalance: 70000,
  gmfExempt: false, clientId: 7, createdAt: '2026-10-30T10:00:00', updatedAt: '2026-10-30T11:00:00',
};
const MARIA = { id: 7, firstNames: 'María', lastName: 'Gómez' };
const OTHER: Account = { ...ACCOUNT, id: 9, type: 'CHECKING', number: '3300000002' };

function movement(overrides: Partial<Movement>): Movement {
  return {
    id: 1, reference: 'ref', accountId: 1, transactionType: 'DEPOSIT', movementType: 'CREDIT',
    amount: 100000, balanceAfter: 100000, counterpartyAccountId: null, createdAt: '2026-10-30T10:30:00',
    ...overrides,
  };
}

describe('AccountStatement', () => {
  let http: HttpTestingController;

  function configure(accountId: string) {
    registerLocaleData(localeEsCo);
    TestBed.configureTestingModule({
      imports: [AccountStatement],
      providers: [
        provideHttpClient(), provideHttpClientTesting(), provideRouter([]),
        { provide: LOCALE_ID, useValue: 'es-CO' },
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: convertToParamMap({ id: accountId }) } } },
      ],
    });
    http = TestBed.inject(HttpTestingController);
  }

  afterEach(() => http.verify());

  function render(movements: Movement[]) {
    configure('1');
    const fixture = TestBed.createComponent(AccountStatement);
    fixture.detectChanges();
    http.expectOne('/api/accounts/1/statement').flush({ account: ACCOUNT, movements });
    http.expectOne('/api/accounts').flush([ACCOUNT, OTHER]);
    http.expectOne('/api/clients').flush([MARIA]);
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  it('muestra la cuenta, su dueño y el saldo', () => {
    const element = render([]);

    expect(element.textContent).toContain('5300000001');
    expect(element.textContent).toContain('María Gómez');
    expect(element.textContent).toContain('70.000');
    expect(element.textContent).toContain('Esta cuenta todavía no tiene movimientos.');
  });

  it('lista cada movimiento con su descripción, signo y saldo resultante', () => {
    const element = render([
      movement({ id: 2, transactionType: 'WITHDRAWAL', movementType: 'DEBIT', amount: 30000, balanceAfter: 70000 }),
      movement({ id: 1 }),
    ]);

    const rows = element.querySelectorAll('tbody tr');
    expect(rows).toHaveLength(2);
    expect(rows[0].textContent).toContain('Retiro');
    expect(rows[0].textContent).toContain('−');
    expect(rows[0].textContent).toContain('30.000');
    expect(rows[1].textContent).toContain('Consignación');
    expect(rows[1].textContent).toContain('+');
  });

  it('distingue la transferencia enviada de la recibida', () => {
    const element = render([
      movement({ id: 2, transactionType: 'TRANSFER', movementType: 'DEBIT', counterpartyAccountId: 9 }),
      movement({ id: 1, transactionType: 'TRANSFER', movementType: 'CREDIT', counterpartyAccountId: 9 }),
    ]);

    expect(element.textContent).toContain('Transferencia enviada');
    expect(element.textContent).toContain('Transferencia recibida');
    expect(element.textContent).toContain('A la cuenta 3300000002');
    expect(element.textContent).toContain('Desde la cuenta 3300000002');
  });

  it('muestra el error del backend si la cuenta no existe', () => {
    configure('99');
    const fixture = TestBed.createComponent(AccountStatement);
    fixture.detectChanges();
    http.expectOne('/api/accounts/99/statement').flush(
      { detail: 'No existe una cuenta con id 99' },
      { status: 404, statusText: 'Not Found' },
    );
    // forkJoin cancela las otras dos peticiones en cuanto falla el estado de cuenta.
    const others = [http.expectOne('/api/accounts'), http.expectOne('/api/clients')];
    expect(others.every((request) => request.cancelled)).toBe(true);
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain('No existe una cuenta con id 99');
  });
});
