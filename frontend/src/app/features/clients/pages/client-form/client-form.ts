import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { apiErrorMessage } from '../../../../core/http/api-error';
import { ClientRequest, IDENTIFICATION_TYPE_LABELS, IdentificationType } from '../../models/client.model';
import { ClientService } from '../../services/client.service';

type FieldName = keyof ClientRequest;

/** Mismos mensajes que el backend, para que el trabajador vea el error antes de enviar. */
const MESSAGES: Record<FieldName, Record<string, string>> = {
  identificationType: { required: 'El tipo de identificación es obligatorio' },
  identificationNumber: {
    required: 'El número de identificación es obligatorio',
    pattern: 'El número de identificación debe tener entre 3 y 20 letras o dígitos',
  },
  firstNames: { required: 'Los nombres son obligatorios', minlength: 'Los nombres deben tener al menos 2 caracteres' },
  lastName: { required: 'El apellido es obligatorio', minlength: 'El apellido debe tener al menos 2 caracteres' },
  email: { required: 'El correo es obligatorio', pattern: 'El correo debe tener el formato usuario@dominio.com' },
  birthDate: { required: 'La fecha de nacimiento es obligatoria' },
};

/** Formulario para registrar un cliente (/clients/new) o editarlo (/clients/:id/edit). */
@Component({
  selector: 'app-client-form',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './client-form.html',
  styleUrl: './client-form.css',
})
export class ClientForm {
  private readonly clientService = inject(ClientService);
  private readonly router = inject(Router);
  private readonly clientId = Number(inject(ActivatedRoute).snapshot.paramMap.get('id')) || null;

  protected readonly isEdit = this.clientId !== null;
  protected readonly typeOptions = Object.entries(IDENTIFICATION_TYPE_LABELS) as [IdentificationType, string][];
  protected readonly today = new Date().toISOString().slice(0, 10);
  protected readonly saving = signal(false);
  protected readonly formError = signal('');

  protected readonly form = inject(FormBuilder).nonNullable.group({
    identificationType: ['' as IdentificationType | '', Validators.required],
    identificationNumber: ['', [Validators.required, Validators.pattern(/^[A-Za-z0-9]{3,20}$/)]],
    firstNames: ['', [Validators.required, Validators.minLength(2)]],
    lastName: ['', [Validators.required, Validators.minLength(2)]],
    email: ['', [Validators.required, Validators.pattern(/^[^@\s]+@[^@\s]+\.[^@\s]+$/)]],
    birthDate: ['', Validators.required],
  });

  constructor() {
    if (this.clientId !== null) {
      this.clientService.getById(this.clientId).subscribe({
        next: (client) => this.form.patchValue(client),
        error: (error) => this.formError.set(apiErrorMessage(error)),
      });
    }
  }

  /** Texto del primer error de un campo, solo después de que el usuario lo tocó. */
  protected errorFor(name: FieldName): string | null {
    const control = this.form.controls[name];
    if (!control.touched || !control.errors) {
      return null;
    }
    // "server" trae el mensaje que envió el backend; los demás errores usan los mensajes locales.
    const key = Object.keys(control.errors)[0];
    return key === 'server' ? control.errors[key] : (MESSAGES[name][key] ?? null);
  }

  protected save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.formError.set('');
    const request = this.form.getRawValue() as ClientRequest;
    const call = this.clientId === null
      ? this.clientService.create(request)
      : this.clientService.update(this.clientId, request);

    call.subscribe({
      next: (client) => {
        const verb = this.isEdit ? 'actualizado' : 'registrado';
        this.router.navigate(['/clients'], {
          state: { notice: `Cliente ${client.firstNames} ${client.lastName} ${verb}.` },
        });
      },
      error: (error) => {
        this.saving.set(false);
        this.showServerErrors(error);
      },
    });
  }

  /** El backend puede devolver errores por campo (400) o una regla de negocio (409/422). */
  private showServerErrors(error: unknown): void {
    const fieldErrors = error instanceof HttpErrorResponse ? error.error?.errors : null;
    if (fieldErrors) {
      for (const [name, message] of Object.entries(fieldErrors)) {
        this.form.get(name)?.setErrors({ server: message });
      }
    }
    this.formError.set(apiErrorMessage(error));
  }
}
