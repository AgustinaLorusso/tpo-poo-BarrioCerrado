
# Gestión de Barrio Cerrado - Los Aromos

## Implementacion de pasos 3 y 5

Clases de lotes, residencias, barrio, presupuesto, expensas, pagos y ajustes.
Ver [uso, supuestos y ejecucion de pruebas](docs/Pasos3y5.md).
La prueba ejecutable es `test/PruebaPasos3y5.java`.

Trabajo Práctico Obligatorio — Programación Orientada a Objetos.
Propuesta A: sistema para gestionar reclamos, expensas, luminarias y cámaras, e ingreso de visitas de un barrio cerrado de 150 lotes.

## Integrantes

| Agustina Lorusso Marin | 1239089

| Thomas Delli Antoni | 1122801

| Joaquina Dias Rato | 1200817

## Estructura del repositorio

```
/docs       Enunciado del tp, Desarrollo teorico
/docs/uml   Diagramas 
/src        Código 
/test       Pruebas unitarias 
```
## Etapa 1: Entrega 25/10/26 

-Entrega tp punto 1 a 6. (Diagrama de caso de uso, UML, identificacion de patrones GRASP y principios SOLID)
-----------------------------------

## Etapa 2: Entrega 04/11/26 
-Codigo 

-Datos de prueba





## Cómo ejecutar

Requiere Java 11 o superior. Desde la raíz del repositorio:

```
javac -encoding UTF-8 -d bin $(find src test -name '*.java')
java -cp bin Main
```

Pruebas: `java -cp bin PruebaPaso1y2`, `java -cp bin PruebaPasos3y5`, `java -cp bin PruebaPaso4` y `java -cp bin PruebaPasos6y7`.

### Persistencia

Los datos se guardan en `datos/barrio.dat` al cerrar cada sesión y al salir del sistema, y se leen al iniciar.
Si el archivo no existe (primera ejecución) se cargan los datos de prueba. Para volver a los datos de prueba, borrar la carpeta `datos/`.

## Inicio de sesión

Al arrancar, el sistema pide el documento y muestra el menú según el rol. Con 0 se sale.

| Rol | Documento para probar |
|---|---|
| Administrador | 20000000 (Carla Diaz) |
| Guardia de portería | 25000001 (Pedro Lopez) |
| Guardia de ronda | 25000002 (Mario Sosa) |
| Mantenimiento | 25000003 (Rosa Ruiz, electricidad) · 25000004 (Jorge Diaz, plomería) |
| Vecino | 30111222 (Ana Perez) · 30333444 (Juan Perez) · 28555666 (Luis Gomez) · 35777888 (Sofia Martinez) |

## Datos de prueba

El `Main` arranca con estos datos cargados:

| Tipo | Datos |
|---|---|
| Administrador | Carla Diaz (doc 20000000) |
| Vecinos | Ana Perez (30111222), titular de los lotes 1 y 2 · Juan Perez (30333444), familiar del lote 1 y autorizado a reclamar · Luis Gomez (28555666), titular del lote 3 · Sofia Martinez (35777888), inquilina del lote 3 |
| Personal | Legajo 1: Pedro Lopez (25000001), guardia de portería · Legajo 2: Mario Sosa (25000002), guardia de ronda · Legajo 3: Rosa Ruiz (25000003), mantenimiento (electricidad) · Legajo 4: Jorge Diaz (25000004), mantenimiento (plomería) |
| Lotes | 1 (300 m²), 2 (500 m²), 3 (400 m²) |
| Expensas | Junio a octubre 2026, presupuesto $1.200.000 por mes. Lote 1 pagó todo, lote 2 pagó la mitad, lote 3 no pagó nada y está moroso |
| Reclamos | N° 1: mantenimiento del lote 1, en curso con Jorge · N° 2: seguridad del lote 1, abierto · N° 3: convivencia del lote 3, asignado a Mario |
| Sectores y equipos | Acceso principal: CAM-1 · Calle 1: LUZ-1 (con falla, sin orden) y LUZ-2 · Plaza central: CAM-2 (en reparación, orden asignada a Rosa) y LUZ-3 |
| Visitas | Marta Lopez (40123456), puntual para hoy al lote 1 · Carlos Jardinero (40654321), recurrente de lunes a viernes de 8 a 18 al lote 2 |
