# Frontend (Angular)

Interfaz para los trabajadores del banco: clientes, cuentas, movimientos y estado de cuenta.

## Ejecutar en local

Requiere el backend corriendo en `http://localhost:8081`.

```bash
npm install
npm start          # http://localhost:4300 (el 4200 suele estar ocupado)
```

`proxy.conf.json` reenvía las peticiones `/api/...` al backend, así el navegador
solo habla con un origen y no se necesita configurar CORS.

## Pruebas y build

```bash
npm test
npm run build
```
