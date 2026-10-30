import { registerLocaleData } from '@angular/common';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import localeEsCo from '@angular/common/locales/es-CO';
import { LOCALE_ID } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { Account } from '../../../accounts/models/account.model';
import { TransferForm } from './transfer-form';

const SOURCE: Account = {
  id: 1, type: 'SAVINGS', number: '5300000001', status: 'ACTIVE', balance: 150000, availableBalance: 150000,
  gmfExempt: false, clientId: 7, createdAt: '2026-10-30T10:00:00', updatedAt: '2026-10-30T10:00:00',
};
const TARGET: Account = { ...SOURCE, id: 2, type: 'CHECKING', number: '3300000002', balance: 0, availableBalance: 0 };
const INACTIVE: Account = { ...SOURCE, id: 3, number: '5300000003', status: 'INACTIVE' };
const MARIA = { id: 7, firstNames: 'María', lastName: 'Gómez' };

describe('TransferForm', () => {
  let http: HttpTestingController;

  function setup() {
    registerLocaleData(localeEsCo);
    TestBed.configureTestingModule({
      imports: [TransferForm],
      providers: [
        provideHttpClient(), provideHttpClientTesting(), provideRouter([]),
        { provide: LOCALE_ID, useValue: 'es-CO' },
      ],
    });
    http = TestBed.inject(HttpTestingController);
    const fixture = TestBed.createComponent(TransferForm);
    fixture.detectChanges();
    flushAccounts([SOURCE, TARGET, INACTIVE]);
    fixture.detectChanges();
    return { fixture, element: fixture.nativeElement as HTMLElement };
  }

  function flushAccounts(accounts: Account[]) {
    http.expectOne('/api/accounts').flush(accounts);
    http.expectOne('/api/clients').flush([MARIA]);
  }

  function choose(element: HTMLElement, control: string, value: string) {
    const select = element.querySelector<HTMLSelectElement>(`[formControlName="${control}"]`)!;
    select.value = value;
    select.dispatchEvent(new Event('change'));
  }

  function typeAmount(element: HTMLElement, value: string) {
    const input = element.querySelector<HTMLInputElement>('#amount')!;
    input.value = value;
    input.dispatchEvent(new Event('input'));
  }

  function submit(element: HTMLElement) {
    element.querySelector<HTMLButtonElement>('button[type="submit"]')!.click();
  }

  afterEach(() => http.verify());

  it('solo ofrece cuentas activas, con su dueño y saldo', () => {
    const { element } = setup();

    const options = Array.from(element.querySelectorAll('#source option')).map((o) => o.textContent!.trim());
    expect(options.some((o) => o.includes('5300000001') && o.includes('María Gómez') && o.includes('150.000'))).toBe(true);
    expect(options.some((o) => o.includes('5300000003'))).toBe(false);
  });

  it('no envía nada si faltan datos', () => {
    const { fixture, element } = setup();

    submit(element);
    fixture.detectChanges();

    expect(element.textContent).toContain('Elige la cuenta de origen');
    expect(element.textContent).toContain('Elige la cuenta de destino');
    expect(element.textContent).toContain('Escribe el monto');
    http.expectNone('/api/transfers');
  });

  it('no permite transferir a la misma cuenta', () => {
    const { fixture, element } = setup();

    choose(element, 'sourceAccountId', '1');
    choose(element, 'targetAccountId', '1');
    typeAmount(element, '50000');
    submit(element);
    fixture.detectChanges();

    expect(element.textContent).toContain('La cuenta de destino debe ser distinta a la de origen');
    http.expectNone('/api/transfers');
  });

  it('transfiere, confirma con las dos cuentas y recarga los saldos', () => {
    const { fixture, element } = setup();

    choose(element, 'sourceAccountId', '1');
    choose(element, 'targetAccountId', '2');
    typeAmount(element, '50000');
    submit(element);
    const request = http.expectOne({ method: 'POST', url: '/api/transfers' });
    expect(request.request.body).toEqual({ sourceAccountId: 1, targetAccountId: 2, amount: 50000 });
    request.flush({ reference: 'ref', debit: {}, credit: {} });
    flushAccounts([{ ...SOURCE, balance: 100000 }, { ...TARGET, balance: 50000 }]);
    fixture.detectChanges();

    // El formato de moneda usa un espacio no separable entre "$" y el número.
    expect(element.textContent!.replace(/\s+/g, ' ')).toContain(
      'Transferencia de $ 50.000,00 de la cuenta 5300000001 a la cuenta 3300000002 registrada.',
    );
    expect(element.querySelector<HTMLInputElement>('#amount')!.value).toBe('');
  });

  it('muestra el motivo si el backend rechaza la transferencia', () => {
    const { fixture, element } = setup();

    choose(element, 'sourceAccountId', '2');
    choose(element, 'targetAccountId', '1');
    typeAmount(element, '50000');
    submit(element);
    http.expectOne('/api/transfers').flush(
      { detail: 'Saldo insuficiente en la cuenta 3300000002' },
      { status: 422, statusText: 'Unprocessable Content' },
    );
    fixture.detectChanges();

    expect(element.textContent).toContain('Saldo insuficiente en la cuenta 3300000002');
  });
});
