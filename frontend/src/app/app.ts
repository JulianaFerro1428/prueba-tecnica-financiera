import { Component } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';

/** Marco de la aplicación: encabezado fijo y, debajo, la pantalla que indique la ruta. */
@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {}
