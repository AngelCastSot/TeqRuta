# TeqRuta - Traslados en Terreno (TEQMED SpA)

Aplicación móvil Android desarrollada para la **Evaluación Parcial 2** de la asignatura
**Desarrollo de Aplicaciones Móviles (DSY1105)** - Duoc UC, Escuela de Informática y Telecomunicaciones.

**Integrantes**

| Nombre | Rol en el proyecto |
|---|---|
| _[Nombre integrante 1]_ | _[por completar]_ |
| _[Nombre integrante 2]_ | _[por completar]_ |

**Docente:** _[por completar]_ | **Sección:** _[por completar]_ | **Fecha:** _[por completar]_

---

## 1. Descripción del caso

TEQMED SpA es una empresa de servicio técnico de equipamiento médico con clientes en clínicas, hospitales y
centros de diálisis de la Región del Biobío (Concepción, Chillán, Los Ángeles y Temuco). Sus técnicos consumen
combustible, pagan peajes y acumulan kilometraje, pero hoy lo anotan en una libreta y lo declaran a fin de mes,
lo que genera diferencias entre lo declarado y lo autorizado.

**TeqRuta** permite al técnico registrar cada traslado en el momento en que ocurre, **sin conexión a internet**
(modo *offline-first* con Room/SQLite), calcular automáticamente su costo en pesos chilenos y detectar
desviaciones de kilometraje respecto de la distancia oficial del destino.

## 2. Reglas de negocio implementadas

1. **Vehículo del día:** el técnico confirma su vehículo (Empresa o Particular), viendo patente, combustible y rendimiento (km/L).
2. **Registro de traslado:** origen, destino (clínica/hospital con distancia oficial), odómetro inicial y final, peajes en ruta (FilterChip) y peaje extra.
3. **Costo:** `(kmFinal - kmInicial) * tarifa + peajes`, con tarifa fija de $120/km (Empresa) o $180/km (Particular).
4. **Desviación:** `D_decl = kmFinal - kmInicial`. Si `|D_decl - D_ofic| / D_ofic > 0.15` el estado es `OBSERVADO_DESVIACION`; si no, queda `PENDIENTE_SYNC`. Un traslado pendiente puede pasar a `APROBADO`.

## 3. Arquitectura (MVVM clásico)

```
UI (Jetpack Compose)  ->  ViewModel (StateFlow)  ->  Repository  ->  DAO (Room / SQLite)
 ui/screen/*               ui/TrasladoViewModel       data/TrasladoRepository   data/TrasladoDao
```

| Capa | Archivos | Responsabilidad |
|---|---|---|
| Data | `Traslado`, `Destino`, `Vehiculo`, `TrasladoDao`, `TrasladoRepository`, `AppDatabase` | Entidades Room, consultas y acceso a la base local |
| ViewModel | `TrasladoViewModel`, `TrasladoFormState`, `ViewModelFactory` | Estado con `StateFlow`, **toda la validación** y la lógica de negocio |
| UI | `LoginScreen`, `TrasladoListScreen`, `TrasladoFormScreen`, `Componentes` | Solo muestra el estado y envía eventos (sin lógica de validación) |
| Utilidades | `Utilidades.kt` | Formato de pesos, peajes, lectura de GPS con respaldo simulado |

La inyección de dependencias es manual con `ViewModelFactory` (sin Hilt ni Dagger).

## 4. Requisitos de la rúbrica cumplidos

| Requisito | Cómo se cumple |
|---|---|
| Diseño Material 3 | `Scaffold`, `TopAppBar` con insignia "Modo Terreno Offline - Room Activo", `NavigationBar`, `ElevatedCard`, `FilterChip`, paleta de alto contraste |
| Formularios validados | `isError`, `supportingText`, `leadingIcon` y `trailingIcon` (Error / CheckCircle) en cada `OutlinedTextField` |
| Validación desacoplada | Todas las reglas están en `TrasladoViewModel` y se publican en `TrasladoFormState` por `StateFlow` |
| Animaciones | `AnimatedVisibility` (errores, resumen, alertas), `animateColorAsState` (estado de la tarjeta), `animateContentSize` (tarjetas expandibles) |
| Persistencia Room | Tablas `traslados` y `destinos`, destinos pre-cargados, `fallbackToDestructiveMigration()` |
| Recursos nativos | **GPS puntual** (`LocationManager`, con coordenadas simuladas de respaldo) y **cámara** (`TakePicturePreview`), ambos con permisos en tiempo de ejecución |

Validaciones: correo corporativo `@teqmed.cl`, contraseña de al menos 6 caracteres, campos obligatorios completos,
`odómetro inicial > 0` y `odómetro final > odómetro inicial`, peaje extra mayor o igual a 0.

## 5. Instrucciones de compilación

1. Abrir la carpeta del proyecto con **Android Studio** (versión reciente, compatible con AGP 8.13).
2. Esperar la sincronización de Gradle (**File > Sync Project with Gradle Files**).
3. Seleccionar un emulador o dispositivo con **Android 7.0 (API 24) o superior**.
4. Ejecutar con **Run > Run 'app'**.
5. Ingresar con cualquier correo `nombre@teqmed.cl` y una contraseña de 6 o más caracteres.

**Uso en el emulador:** para la cámara, habilitar la cámara virtual (Device Manager > Edit > Camera). Si no hay GPS
configurado, la app usa coordenadas simuladas de Concepción y lo indica en pantalla.

## 6. Estructura del proyecto

```
app/src/main/java/com/example/sqlite/
  MainActivity.kt
  data/   Traslado, Destino, Vehiculo, TrasladoDao, TrasladoRepository, AppDatabase
  ui/     TrasladoViewModel, TrasladoFormState, ViewModelFactory, Utilidades
  ui/screen/  LoginScreen, TrasladoListScreen, TrasladoFormScreen, Componentes
  ui/theme/   Color, Theme, Type
```

## 7. Alcance y limitaciones

- La sincronización con un backend (Flask/Retrofit/WorkManager), los roles y el cálculo centralizado del SRS quedan fuera de esta evaluación; el estado `PENDIENTE_SYNC` deja preparado ese paso.
- Las distancias oficiales, los montos de peajes y los vehículos son datos referenciales de ejemplo.
- La autenticación valida solo el formato de las credenciales (no hay servidor).
