import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { Client } from '../../models/client.model';
import { ClientList } from './client-list';

const MARIA: Client = {
  id: 1,
  identificationType: 'CC',
  identificationNumber: '1020304050',
  firstNames: 'María',
  lastName: 'Gómez',
  email: 'maria@correo.com',
  birthDate: '1990-05-10',
  createdAt: '2026-10-30T10:00:00',
  updatedAt: '2026-10-30T10:00:00',
};

describe('ClientList', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ClientList],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  function render() {
    const fixture = TestBed.createComponent(ClientList);
    fixture.detectChanges();
    return fixture;
  }

  it('muestra los clientes que devuelve el backend', () => {
    const fixture = render();
    http.expectOne('/api/clients').flush([MARIA]);
    fixture.detectChanges();

    const text = (fixture.nativeElement as HTMLElement).textContent;
    expect(text).toContain('María Gómez');
    expect(text).toContain('10/05/1990');
  });

  it('muestra un mensaje cuando no hay clientes', () => {
    const fixture = render();
    http.expectOne('/api/clients').flush([]);
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Todavía no hay clientes');
  });

  it('muestra el motivo del backend cuando no se puede eliminar', () => {
    const fixture = render();
    http.expectOne('/api/clients').flush([MARIA]);
    fixture.detectChanges();
    const element = fixture.nativeElement as HTMLElement;

    clickButton(element, 'Eliminar');
    fixture.detectChanges();
    clickButton(element, 'Sí, eliminar');
    http.expectOne({ method: 'DELETE', url: '/api/clients/1' }).flush(
      { detail: 'No se puede eliminar el cliente 1 porque tiene cuentas asociadas' },
      { status: 422, statusText: 'Unprocessable Content' },
    );
    fixture.detectChanges();

    expect(element.querySelector('[role="alert"]')?.textContent).toContain('tiene cuentas asociadas');
    expect(element.textContent).toContain('María Gómez');
  });
});

function clickButton(root: HTMLElement, label: string): void {
  const button = Array.from(root.querySelectorAll('button')).find((b) => b.textContent?.trim() === label);
  if (!button) {
    throw new Error(`No se encontró el botón "${label}"`);
  }
  button.click();
}
