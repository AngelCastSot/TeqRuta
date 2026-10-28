# Commits sugeridos y tablero Trello - TeqRuta

## 10 mensajes de commit (repartidos entre los 2 integrantes)

**Integrante A: datos, lógica y arquitectura**

1. `feat(data): crear entidades Traslado y Destino con campos de GPS, peajes, estado y foto`
2. `feat(data): agregar TrasladoDao, TrasladoRepository y pre-carga de destinos en Room`
3. `refactor(db): subir AppDatabase a version 3 con fallbackToDestructiveMigration`
4. `feat(viewmodel): implementar TrasladoViewModel con StateFlow, tarifas y calculo de costo`
5. `feat(viewmodel): validar login y formulario en el ViewModel y calcular desviacion mayor a 15%`

**Integrante B: interfaz y recursos nativos**

6. `feat(ui): crear tema Material 3 de alto contraste y componente CampoTexto con iconos de error y exito`
7. `feat(ui): implementar LoginScreen con validacion de correo @teqmed.cl y clave de 6 caracteres`
8. `feat(ui): agregar TrasladoListScreen con NavigationBar, resumen de gasto y vehiculo del dia`
9. `feat(native): integrar GPS puntual y camara con permisos en tiempo de ejecucion en TrasladoFormScreen`
10. `docs: agregar README formal, animaciones finales y fix de estados de tarjetas`

> Sugerencia: hacer cada commit desde su propia rama (`feature/data`, `feature/ui`) y unir con Pull Request.

---

## Tablero Trello (tarjetas para copiar)

**Listas:** `Backlog` | `En desarrollo` | `En revisión` | `Terminado`

### Tarjeta 1 - Modelo de datos en Room
- **Responsable:** Integrante A | **Etiqueta:** Datos
- [ ] Entidad `Traslado` con 21 campos (GPS, peajes, estado, foto)
- [ ] Entidad `Destino` y catálogo inicial
- [ ] `TrasladoDao` con consultas de traslados y destinos
- [ ] `AppDatabase` versión 3 con migración destructiva

### Tarjeta 2 - Repositorio y pre-carga
- **Responsable:** Integrante A | **Etiqueta:** Datos
- [ ] `TrasladoRepository` con agregar, actualizar y eliminar
- [ ] Pre-carga de 8 destinos reales si la tabla está vacía

### Tarjeta 3 - Lógica de negocio en el ViewModel
- **Responsable:** Integrante A | **Etiqueta:** Lógica
- [ ] Cálculo de costo ($120/km Empresa, $180/km Particular)
- [ ] Desviación `|D_decl - D_ofic| / D_ofic > 0.15`
- [ ] Estados `PENDIENTE_SYNC`, `OBSERVADO_DESVIACION`, `APROBADO`
- [ ] Gasto total y cantidad de traslados con `StateFlow`

### Tarjeta 4 - Validaciones desacopladas
- **Responsable:** Integrante A | **Etiqueta:** Lógica
- [ ] Login: `@teqmed.cl` y clave de 6 o más caracteres
- [ ] Formulario: obligatorios, odómetro inicial > 0, final > inicial, peaje extra >= 0
- [ ] `TrasladoFormState` con `Campo` (valor, error, válido)

### Tarjeta 5 - Tema y componentes Material 3
- **Responsable:** Integrante B | **Etiqueta:** UI
- [ ] Paleta de alto contraste
- [ ] `CampoTexto` con `isError`, `supportingText`, `leadingIcon`, `trailingIcon`
- [ ] Insignia "Modo Terreno Offline - Room Activo" en la `TopAppBar`

### Tarjeta 6 - Pantalla de Login
- **Responsable:** Integrante B | **Etiqueta:** UI
- [ ] Campos de correo y contraseña con feedback visual
- [ ] Navegación al listado al ingresar

### Tarjeta 7 - Pantalla de listado
- **Responsable:** Integrante B | **Etiqueta:** UI
- [ ] Resumen superior (gasto CLP y cantidad)
- [ ] Confirmación del vehículo del día
- [ ] Tarjetas expandibles con color por estado y alerta de desviación
- [ ] `NavigationBar` (Traslados / Registrar)

### Tarjeta 8 - Formulario de registro
- **Responsable:** Integrante B | **Etiqueta:** UI
- [ ] Selección de ciudad y destino, `FilterChip` de peajes
- [ ] Resumen animado de costo y desviación
- [ ] Botón guardar con validación completa

### Tarjeta 9 - Recursos nativos
- **Responsable:** Ambos | **Etiqueta:** Nativo
- [ ] Permiso y captura de GPS de salida y llegada (con respaldo simulado)
- [ ] Permiso de cámara y miniatura del comprobante
- [ ] Permisos declarados en `AndroidManifest.xml`

### Tarjeta 10 - Animaciones
- **Responsable:** Ambos | **Etiqueta:** UI
- [ ] `AnimatedVisibility` en errores, resumen y alertas
- [ ] `animateColorAsState` en tarjetas
- [ ] `animateContentSize` en el listado

### Tarjeta 11 - Pruebas y documentación
- **Responsable:** Ambos | **Etiqueta:** Calidad
- [ ] Probar en emulador (cámara y GPS)
- [ ] README con nombres de los integrantes
- [ ] Ensayo de la defensa oral
