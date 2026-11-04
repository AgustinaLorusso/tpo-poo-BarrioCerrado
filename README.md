
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

Pruebas: `java -cp bin PruebaPasos3y5`, `java -cp bin PruebaPaso4` y `java -cp bin PruebaPasos6y7`.

## Datos de prueba

El `Main` arranca con estos datos cargados:

| Tipo | Datos |
|---|---|
| Administrador | Carla Diaz (doc 20000000) |
| Vecinos | Ana Perez (30111222), titular de los lotes 1 y 2 · Juan Perez (30333444), familiar del lote 1 y autorizado a reclamar · Luis Gomez (28555666), titular del lote 3 · Sofia Martinez (35777888), inquilina del lote 3 |
| Personal | Legajo 1: Pedro Lopez, guardia de portería · Legajo 2: Mario Sosa, guardia de ronda · Legajo 3: Rosa Ruiz, mantenimiento (electricidad) · Legajo 4: Jorge Diaz, mantenimiento (plomería) |
| Lotes | 1 (300 m²), 2 (500 m²), 3 (400 m²) |
| Expensas | Junio a octubre 2026, presupuesto $1.200.000 por mes. Lote 1 pagó todo, lote 2 pagó la mitad, lote 3 no pagó nada y está moroso |
| Reclamos | N° 1: mantenimiento del lote 1, en curso con Jorge · N° 2: seguridad del lote 1, abierto · N° 3: convivencia del lote 3, asignado a Mario |
| Sectores y equipos | Acceso principal: CAM-1 · Calle 1: LUZ-1 (con falla, sin orden) y LUZ-2 · Plaza central: CAM-2 (en reparación, orden asignada a Rosa) y LUZ-3 |
| Visitas | Marta Lopez (40123456), puntual para hoy al lote 1 · Carlos Jardinero (40654321), recurrente de lunes a viernes de 8 a 18 al lote 2 |
