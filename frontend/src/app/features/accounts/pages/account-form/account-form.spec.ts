import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';

import { AccountForm } from './account-form';

const MARIA = { id: 7, identificationType: 'CC', identificationNumber: '1020304050', firstNames: 'María', lastName: 'Gómez' };

describe('AccountForm', () => {
  let http: HttpTestingController;

  function setup(clients: object[] = [MARIA]) {
    TestBed.configureTestingModule({
      imports: [AccountForm],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    http = TestBed.inject(HttpTestingController);
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    const fixture = TestBed.createComponent(AccountForm);
    fixture.detectChanges();
    http.expectOne('/api/clients').flush(clients);
    fixture.detectChanges();
    return { fixture, element: fixture.nativeElement as HTMLElement, navigate };
  }

  function submit(element: HTMLElement) {
    element.querySelector<HTMLButtonElement>('button[type="submit"]')!.click();
  }

  function chooseClient(element: HTMLElement, id: number) {
    const select = element.querySelector<HTMLSelectElement>('[formControlName="clientId"]')!;
    select.value = String(id);
    select.dispatchEvent(new Event('change'));
  }

  afterEach(() => http.verify());

  it('no envía nada si falta el cliente o el tipo de cuenta', () => {
    const { fixture, element } = setup();

    submit(element);
    fixture.detectChanges();

    expect(element.textContent).toContain('Selecciona el cliente');
    expect(element.textContent).toContain('Selecciona el tipo de cuenta');
    http.expectNone('/api/accounts');
  });

  it('abre la cuenta y vuelve a la lista con el número generado', () => {
    const { element, navigate } = setup();

    chooseClient(element, 7);
    element.querySelector<HTMLInputElement>('input[value="SAVINGS"]')!.click();
    element.querySelector<HTMLInputElement>('[formControlName="gmfExempt"]')!.click();
    submit(element);

    const call = http.expectOne({ method: 'POST', url: '/api/accounts' });
    expect(call.request.body).toEqual({ clientId: 7, type: 'SAVINGS', gmfExempt: true });
    call.flush({ id: 1, number: '5300000001', type: 'SAVINGS', clientId: 7 });

    expect(navigate).toHaveBeenCalledWith(['/accounts'], {
      state: { notice: 'Cuenta de ahorros 5300000001 abierta para María Gómez.' },
    });
  });

  it('invita a registrar un cliente si todavía no hay ninguno', () => {
    const { element } = setup([]);

    expect(element.textContent).toContain('Primero registra un cliente');
  });
});
