import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { ClientRequest } from '../models/client.model';
import { ClientService } from './client.service';

describe('ClientService', () => {
  let service: ClientService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ClientService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('crea un cliente con POST a /api/clients', () => {
    const request: ClientRequest = {
      identificationType: 'CC',
      identificationNumber: '1020304050',
      firstNames: 'María',
      lastName: 'Gómez',
      email: 'maria@correo.com',
      birthDate: '1990-05-10',
    };

    service.create(request).subscribe();

    const call = http.expectOne('/api/clients');
    expect(call.request.method).toBe('POST');
    expect(call.request.body).toEqual(request);
    call.flush({ id: 1, ...request });
  });

  it('elimina un cliente con DELETE a /api/clients/{id}', () => {
    service.delete(7).subscribe();

    const call = http.expectOne('/api/clients/7');
    expect(call.request.method).toBe('DELETE');
    call.flush(null, { status: 204, statusText: 'No Content' });
  });
});
