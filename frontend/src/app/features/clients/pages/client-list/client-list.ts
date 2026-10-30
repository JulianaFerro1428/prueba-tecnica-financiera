import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { apiErrorMessage } from '../../../../core/http/api-error';
import { Client } from '../../models/client.model';
import { ClientService } from '../../services/client.service';

type LoadState = 'loading' | 'ready' | 'error';

interface Notice {
  kind: 'success' | 'error';
  text: string;
}

/** Pantalla con la lista de clientes. Eliminar pide confirmación en la misma fila. */
@Component({
  selector: 'app-client-list',
  imports: [DatePipe, RouterLink],
  templateUrl: './client-list.html',
  styleUrl: './client-list.css',
})
export class ClientList {
  private readonly clientService = inject(ClientService);

  protected readonly clients = signal<Client[]>([]);
  protected readonly loadState = signal<LoadState>('loading');
  protected readonly loadError = signal('');
  protected readonly notice = signal<Notice | null>(null);
  protected readonly confirmingId = signal<number | null>(null);

  constructor() {
    // El formulario envía aquí el mensaje de éxito ("Cliente ... registrado.") al volver a la lista.
    const savedNotice = inject(Router).currentNavigation()?.extras.state?.['notice'];
    if (typeof savedNotice === 'string') {
      this.notice.set({ kind: 'success', text: savedNotice });
    }
    this.load();
  }

  protected load(): void {
    this.loadState.set('loading');
    this.clientService.getAll().subscribe({
      next: (clients) => {
        this.clients.set(clients);
        this.loadState.set('ready');
      },
      error: (error) => {
        this.loadError.set(apiErrorMessage(error));
        this.loadState.set('error');
      },
    });
  }

  protected askToDelete(client: Client): void {
    this.notice.set(null);
    this.confirmingId.set(client.id);
  }

  protected cancelDelete(): void {
    this.confirmingId.set(null);
  }

  protected delete(client: Client): void {
    this.clientService.delete(client.id).subscribe({
      next: () => {
        this.clients.update((list) => list.filter((c) => c.id !== client.id));
        this.confirmingId.set(null);
        this.notice.set({ kind: 'success', text: `Cliente ${client.firstNames} ${client.lastName} eliminado.` });
      },
      error: (error) => {
        // Ej.: el backend responde 422 si el cliente todavía tiene cuentas.
        this.confirmingId.set(null);
        this.notice.set({ kind: 'error', text: apiErrorMessage(error) });
      },
    });
  }
}
