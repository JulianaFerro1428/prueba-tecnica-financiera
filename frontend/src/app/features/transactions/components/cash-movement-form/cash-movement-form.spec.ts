import { registerLocaleData } from '@angular/common';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import localeEsCo from '@angular/common/locales/es-CO';
import { LOCALE_ID } from '@angular/core';
import { TestBed } from '@angular/core/testing';

import { Movement } from '../../../accounts/models/movement.model';
import { CashMovementForm } from './cash-movement-form';

const DEPOSIT: Movement = {
  id: 5, reference: 'ref', accountId: 1, transactionType: 'DEPOSIT', movementType: 'CREDIT',
  amount: 50000, balanceAfter: 150000, counterpartyAccountId: null, createdAt: '2026-10-30T10:30:00',
};

describe('CashMovementForm', () => {
  let http: HttpTestingController;

  function setup() {
    registerLocaleData(localeEsCo);
    TestBed.configureTestingModule({
      imports: [CashMovementForm],
      providers: [provideHttpClient(), provideHttpClientTesting(), { provide: LOCALE_ID, useValue: 'es-CO' }],
    });
    http = TestBed.inject(HttpTestingController);
    const fixture = TestBed.createComponent(CashMovementForm);
    fixture.componentRef.setInput('accountId', 1);
    const completed: Movement[] = [];
    fixture.componentInstance.completed.subscribe((movement) => completed.push(movement));
    fixture.detectChanges();
    return { fixture, element: fixture.nativeElement as HTMLElement, completed };
  }

  function typeAmount(element: HTMLElement, value: string) {
    const input = element.querySelector<HTMLInputElement>('#amount')!;
    input.value = value;
    input.dispatchEvent(new Event('input'));
  }

  function chooseWithdrawal(element: HTMLElement) {
    element.querySelector<HTMLInputElement>('input[value="WITHDRAWAL"]')!.click();
  }

  function submit(element: HTMLElement) {
    element.querySelector<HTMLButtonElement>('button[type="submit"]')!.click();
  }

  afterEach(() => http.verify());

  it('no envía nada si falta el monto', () => {
    const { fixture, element } = setup();

    submit(element);
    fixture.detectChanges();

    expect(element.textContent).toContain('Escribe el monto');
    http.expectNone(() => true);
  });

  it('no acepta montos con más de dos decimales', () => {
    const { fixture, element } = setup();

    typeAmount(element, '100.555');
    submit(element);
    fixture.detectChanges();

    expect(element.textContent).toContain('máximo 2 decimales');
    http.expectNone(() => true);
  });

  it('consigna el monto, confirma, avisa al padre y limpia el campo', () => {
    const { fixture, element, completed } = setup();

    typeAmount(element, '50000');
    submit(element);
    const request = http.expectOne({ method: 'POST', url: '/api/accounts/1/deposits' });
    expect(request.request.body).toEqual({ amount: 50000 });
    request.flush(DEPOSIT);
    fixture.detectChanges();

    expect(completed).toEqual([DEPOSIT]);
    // El formato de moneda usa un espacio no separable entre "$" y el número.
    expect(element.textContent!.replace(/\s+/g, ' ')).toContain('Consignación de $ 50.000,00 registrada.');
    expect(element.querySelector<HTMLInputElement>('#amount')!.value).toBe('');
  });

  it('retira y muestra el motivo si el backend lo rechaza', () => {
    const { fixture, element, completed } = setup();

    chooseWithdrawal(element);
    typeAmount(element, '900000');
    fixture.detectChanges();
    expect(element.querySelector('button[type="submit"]')!.textContent).toContain('Retirar');
    submit(element);
    http.expectOne({ method: 'POST', url: '/api/accounts/1/withdrawals' }).flush(
      { detail: 'Saldo insuficiente en la cuenta 5300000001' },
      { status: 422, statusText: 'Unprocessable Content' },
    );
    fixture.detectChanges();

    expect(element.textContent).toContain('Saldo insuficiente en la cuenta 5300000001');
    expect(completed).toEqual([]);
  });
});
