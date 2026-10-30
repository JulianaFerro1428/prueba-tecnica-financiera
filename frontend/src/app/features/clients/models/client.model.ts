/** Tipos de identificación que acepta el backend. */
export type IdentificationType = 'CC' | 'CE' | 'PA';

export const IDENTIFICATION_TYPE_LABELS: Record<IdentificationType, string> = {
  CC: 'Cédula de ciudadanía',
  CE: 'Cédula de extranjería',
  PA: 'Pasaporte',
};

/** Cliente tal como lo devuelve GET /api/clients. Las fechas llegan como texto ISO. */
export interface Client {
  id: number;
  identificationType: IdentificationType;
  identificationNumber: string;
  firstNames: string;
  lastName: string;
  email: string;
  birthDate: string;
  createdAt: string;
  updatedAt: string;
}

/** Datos que se envían para crear o actualizar un cliente. */
export interface ClientRequest {
  identificationType: IdentificationType;
  identificationNumber: string;
  firstNames: string;
  lastName: string;
  email: string;
  birthDate: string;
}
