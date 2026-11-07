# Gestión de Barrio Cerrado - Los Aromos

Trabajo Práctico Integrador — Programación Orientada a Objetos (UADE).
Propuesta A: gestión de un barrio cerrado.

## Descripción del sistema

Aplicación de consola en Java para administrar el barrio cerrado Los Aromos, de hasta 150 lotes. Cada usuario inicia sesión con su documento y ve un menú según su rol.

- **Administrador:**
  - alta, baja y modificación de vecinos y lotes, y asignación de roles en cada lote (titular, familiar o inquilino);
  - emisión de expensas a partir del presupuesto mensual, registro de pagos y asientos de ajuste;
  - asignación de reclamos al personal y órdenes de mantenimiento;
  - reportes de reclamos pendientes, morosidad y equipos fuera de servicio;
  - procesos automáticos: escalamiento y cierre de reclamos, y aviso de cámaras que superan el tiempo máximo sin funcionar.
- **Vecino:**
  - registro, reapertura y seguimiento de reclamos, con comentarios e historial;
  - consulta del estado de cuenta;
  - aviso de fallas en cámaras y luminarias;
  - autorización de visitas puntuales y de proveedores recurrentes;
  - el titular, además, autoriza a otras personas a reclamar por su lote.
- **Guardia:** consulta de autorizaciones, registro de ingresos y egresos de visitas y atención de reclamos de seguridad.
- **Mantenimiento:** atención de reclamos según su especialidad y cierre de órdenes de mantenimiento.

Las expensas se reparten según la superficie de cada lote. Las vencidas generan un interés del 3 % mensual, y un lote con más de tres períodos impagos queda moroso. Los datos se guardan en un archivo y se conservan entre ejecuciones.

## Integrantes

| Integrante | Legajo |
|---|---|
| Agustina Lorusso Marin | 1239089 |
| Thomas Delli Antoni | 1122801 |
| Joaquina Dias Rato | 1200817 |

## Requisitos

- Java (JDK) 17 o superior. Se puede verificar con `java -version` y `javac -version`.
- No usa bibliotecas externas ni Maven.

## Instalación y ejecución

1. Clonar el repositorio:
   ```
   git clone https://github.com/AgustinaLorusso/tpo-poo-BarrioCerrado.git
   cd tpo-poo-BarrioCerrado
   ```
2. Compilar desde la raíz del repositorio.

   macOS / Linux:
   ```
   javac -encoding UTF-8 -d bin $(find src test -name '*.java')
   ```
   Windows (PowerShell):
   ```
   javac -encoding UTF-8 -d bin (Get-ChildItem src,test -Recurse -Filter *.java).FullName
   ```
3. Ejecutar el programa:
   ```
   java -cp bin Main
   ```
   El sistema pide el documento para iniciar sesión (ver [Datos de prueba](#datos-de-prueba)). Con 0 se cierra la sesión o se sale.
4. Ejecutar las pruebas unitarias:
   ```
   java -cp bin PruebaPaso1y2
   java -cp bin PruebaPasos3y5
   java -cp bin PruebaPaso4
   java -cp bin PruebaPasos6y7
   ```

### Persistencia

- Los datos se guardan en `datos/barrio.dat` al cerrar cada sesión y al salir del sistema, y se leen al iniciar.
- En la primera ejecución, cuando el archivo todavía no existe, se cargan los datos de prueba.
- Para volver a los datos de prueba, borrar la carpeta `datos/`. Conviene hacerlo antes de una demostración, porque algunos datos de prueba, como la visita de Marta, son para el día en que se cargaron.

## Datos de prueba

### Documentos para iniciar sesión

| Rol | Documento |
|---|---|
| Administrador | 20000000 (Carla Diaz) |
| Guardia de portería | 25000001 (Pedro Lopez) |
| Guardia de ronda | 25000002 (Mario Sosa) |
| Mantenimiento | 25000003 (Rosa Ruiz, electricidad) · 25000004 (Jorge Diaz, plomería) |
| Vecino | 30111222 (Ana Perez) · 30333444 (Juan Perez) · 28555666 (Luis Gomez) · 35777888 (Sofia Martinez) |

### Datos cargados

| Tipo | Datos |
|---|---|
| Administrador | Carla Diaz (doc 20000000) |
| Vecinos | Ana Perez (30111222), titular de los lotes 1 y 2 · Juan Perez (30333444), familiar del lote 1 y autorizado a reclamar · Luis Gomez (28555666), titular del lote 3 · Sofia Martinez (35777888), inquilina del lote 3 |
| Personal | Legajo 1: Pedro Lopez (25000001), guardia de portería · Legajo 2: Mario Sosa (25000002), guardia de ronda · Legajo 3: Rosa Ruiz (25000003), mantenimiento (electricidad) · Legajo 4: Jorge Diaz (25000004), mantenimiento (plomería) |
| Lotes | 1 (300 m²), 2 (500 m²), 3 (400 m²) |
| Expensas | Junio a octubre 2026, presupuesto de $1.200.000 por mes. El lote 1 pagó todo, el lote 2 pagó la mitad y el lote 3 no pagó nada y está moroso |
| Reclamos | N° 1: mantenimiento del lote 1, en curso con Jorge · N° 2: seguridad del lote 1, abierto · N° 3: convivencia del lote 3, asignado a Mario |
| Sectores y equipos | Acceso principal: CAM-1 · Calle 1: LUZ-1 (con falla, sin orden) y LUZ-2 · Plaza central: CAM-2 (en reparación, orden asignada a Rosa) y LUZ-3 |
| Visitas | Marta Lopez (40123456), puntual para el día de la carga al lote 1 · Carlos Jardinero (40654321), recurrente de lunes a viernes de 8 a 18 al lote 2 |

## Estructura del repositorio

```
/docs             Plan de pruebas funcionales e informe final
/docs/diagramas   Diagramas (archivos draw.io)
/src              Código fuente (paquetes modelo.*, persistencia y Main)
/test             Pruebas unitarias
```

## Entregas

| Etapa | Fecha | Contenido | Tag |
|---|---|---|---|
| Etapa 1 | 25/10/26 | Casos de uso, diagrama de clases, diagramas de secuencia, principios SOLID y patrones GRASP | `etapa-1` |
| Etapa 2 | 04/11/26 | Código, pruebas unitarias, plan de pruebas e informe final | `etapa-2` |
