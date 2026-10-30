import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router, convertToParamMap, provideRouter } from '@angular/router';

import { ClientForm } from './client-form';

const VALID_CLIENT = {
  identificationType: 'CC',
  identificationNumber: '1020304050',
  firstNames: 'María',
  lastName: 'Gómez',
  email: 'maria@correo.com',
  birthDate: '1990-05-10',
};

describe('ClientForm', () => {
  let http: HttpTestingController;

  function setup(clientId: string | null) {
    TestBed.configureTestingModule({
      imports: [ClientForm],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: convertToParamMap(clientId ? { id: clientId } : {}) } } },
      ],
    });
    http = TestBed.inject(HttpTestingController);
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    const fixture = TestBed.createComponent(ClientForm);
    fixture.detectChanges();
    return { fixture, element: fixture.nativeElement as HTMLElement, navigate };
  }

  function fill(element: HTMLElement, values: Record<string, string>) {
    for (const [name, value] of Object.entries(values)) {
      const field = element.querySelector<HTMLInputElement | HTMLSelectElement>(`[formControlName="${name}"]`)!;
      field.value = value;
      field.dispatchEvent(new Event(field instanceof HTMLSelectElement ? 'change' : 'input'));
    }
  }

  function submit(element: HTMLElement) {
    element.querySelector<HTMLButtonElement>('button[type="submit"]')!.click();
  }

  afterEach(() => http.verify());

  it('no envía nada y muestra los errores si el formulario está vacío', () => {
    const { fixture, element } = setup(null);

    submit(element);
    fixture.detectChanges();

    expect(element.textContent).toContain('Los nombres son obligatorios');
    http.expectNone('/api/clients');
  });

  it('registra un cliente nuevo y vuelve a la lista', () => {
    const { fixture, element, navigate } = setup(null);

    fill(element, VALID_CLIENT);
    submit(element);
    const call = http.expectOne({ method: 'POST', url: '/api/clients' });
    expect(call.request.body).toEqual(VALID_CLIENT);
    call.flush({ id: 1, ...VALID_CLIENT });
    fixture.detectChanges();

    expect(navigate).toHaveBeenCalledWith(['/clients'], expect.anything());
  });

  it('en modo edición carga el cliente y guarda con PUT', () => {
    const { fixture, element } = setup('1');
    http.expectOne('/api/clients/1').flush({ id: 1, ...VALID_CLIENT });
    fixture.detectChanges();

    expect(element.querySelector<HTMLInputElement>('[formControlName="firstNames"]')!.value).toBe('María');
    fill(element, { lastName: 'Gómez Pérez' });
    submit(element);

    const call = http.expectOne({ method: 'PUT', url: '/api/clients/1' });
    expect(call.request.body.lastName).toBe('Gómez Pérez');
    call.flush({ id: 1, ...VALID_CLIENT, lastName: 'Gómez Pérez' });
  });

  it('muestra el motivo del backend cuando rechaza al cliente', () => {
    const { fixture, element } = setup(null);

    fill(element, { ...VALID_CLIENT, birthDate: '2015-01-01' });
    submit(element);
    http.expectOne('/api/clients').flush(
      { detail: 'El cliente debe ser mayor de edad' },
      { status: 422, statusText: 'Unprocessable Content' },
    );
    fixture.detectChanges();

    expect(element.querySelector('[role="alert"]')?.textContent).toContain('mayor de edad');
  });
});
