# Plan de pruebas funcionales

Sistema de gestión del barrio cerrado Los Aromos. Fecha de ejecución: 07/11/2026.

## Condiciones de ejecución

- Compilación y ejecución según el README (`java -cp bin Main`).
- Cada caso parte de los **datos de prueba** iniciales: antes de cada caso se borra la carpeta `datos/`. La excepción es el caso CP20, que prueba justamente la persistencia entre ejecuciones.
- Los documentos de inicio de sesión son los del README: administradora Carla (20000000), guardia Pedro (25000001), mantenimiento Jorge (25000004), vecinos Ana (30111222, titular de los lotes 1 y 2) y Luis (28555666, titular del lote 3), inquilina Sofía (35777888).
- El "resultado obtenido" es el mensaje que mostró el programa.

## Pruebas unitarias

Además de este plan, las clases con lógica de negocio tienen pruebas automáticas en `/test`:

| Prueba | Qué cubre | Comprobaciones | Resultado |
|---|---|---|---|
| PruebaPaso1y2 | Personas y personal | 7 | OK |
| PruebaPasos3y5 | Lotes, expensas, pagos, intereses, ajustes y morosidad | 35 | OK |
| PruebaPaso4 | Reclamos: estados, asignación, reapertura, escalamiento | 26 | OK |
| PruebaPasos6y7 | Equipos, órdenes de mantenimiento, visitas e ingresos | 38 | OK |

## Casos de prueba

| ID | Caso de uso | Usuario | Dato de entrada | Resultado esperado | Resultado obtenido | Estado |
|---|---|---|---|---|---|---|
| CP01 | Iniciar sesión | — | Documento 20000000 | Ingresa al menú del administrador | "Bienvenido/a, Carla Diaz." y menú de Administración | OK |
| CP02 | Iniciar sesión | — | Documento 99999999 (no existe) | Se rechaza el ingreso y se vuelve a pedir el documento | "Error: No existe un vecino con ese documento." | OK |
| CP03 | Registrar reclamo | Ana | Lote 1, tipo Seguridad, "Moto sospechosa en la calle 1" | Reclamo creado con número automático, prioridad ALTA (RN01) y estado ABIERTO | "Reclamo N° 4 registrado con prioridad ALTA." | OK |
| CP04 | Registrar reclamo | Luis | Lote 1 (no es titular ni autorizado) | Se rechaza por RN07 | "Regla de negocio: La persona no puede presentar reclamos para este lote." | OK |
| CP05 | Registrar reclamo | Ana | Lote 1, tipo Seguridad, descripción vacía | Se rechaza por falta de descripción | "Error: Falta la descripcion." | OK |
| CP06 | Asignar personal al reclamo | Carla | Reclamo 2 (Seguridad), legajo 1 (guardia Pedro) | Solo se listan los guardias; el reclamo queda ASIGNADO | Lista a Pedro y Mario; "Reclamo asignado a Pedro Lopez." | OK |
| CP07 | Asignar personal al reclamo | Carla | Reclamo 2 (Seguridad), legajo 3 (Rosa, electricidad) | Se rechaza: el empleado no puede atender ese tipo | "Regla de negocio: El empleado no puede atender este tipo de reclamo." | OK |
| CP08 | Avanzar estado del reclamo | Jorge | Reclamo 1 (EN_CURSO), "Cambie el cuerito de la canilla" | Pasa a RESUELTO y se registra en el historial | "Nuevo estado: RESUELTO" | OK |
| CP09 | Reabrir reclamo y ver historial | Ana | Reclamo 1 (resuelto hoy), motivo "Sigue perdiendo agua" | Vuelve a ABIERTO (RN03) y el historial muestra todos los cambios | "Reclamo reabierto." Historial: ABIERTO, ASIGNADO, EN_CURSO, RESUELTO, ABIERTO | OK |
| CP10 | Reabrir reclamo | Ana | Reclamo 2 (ABIERTO) | Se rechaza: solo se reabren reclamos resueltos | "Error: Solo se puede reabrir un reclamo resuelto." | OK |
| CP11 | Registrar pago | Carla | Lote 2, $100.000, TRANSFERENCIA | Se imputa al período más antiguo y baja la deuda | Deuda $1.361.250 → "Pago registrado. Deuda restante: $1261250.0" | OK |
| CP12 | Registrar pago | Carla | Lote 1 (sin deuda), $5.000 | Se rechaza el sobrepago | "Error: El importe debe ser positivo y no superar la deuda." | OK |
| CP13 | Emitir expensas | Carla | Período 2026-10 (ya emitido), $1.200.000 | Se rechaza el período duplicado | "Error: Periodo duplicado o importe menor a un centavo." | OK |
| CP14 | Emitir expensas | Carla | Período 2026-11, $1.200.000 | Se genera una expensa por lote según su coeficiente | "Expensas emitidas para 3 lotes." | OK |
| CP15 | Autorizar visita y registrar ingreso | Ana, luego Pedro | Visita puntual de Laura Gimenez (41222333) al lote 1 para hoy, 00:00 a 23:59; el guardia registra el ingreso | Visita autorizada e ingreso registrado | "Visita autorizada." / "Ingreso registrado: Laura Gimenez al lote 1." | OK |
| CP16 | Registrar ingreso | Pedro | Documento 41222333 otra vez, sin egreso | Se rechaza por RN19 | "Error: La persona tiene un ingreso sin egreso." | OK |
| CP17 | Registrar egreso | Pedro | Documento 41222333 | Se registra el egreso | "Egreso registrado." | OK |
| CP18 | Registrar ingreso | Pedro | Documento 11111111 (sin autorización) | Se rechaza el ingreso | "Ingreso rechazado: no hay autorizacion vigente." | OK |
| CP19 | Reporte de morosidad | Carla | Reportes → Morosidad por lote | Figura el lote 3 con períodos adeudados, deuda e intereses | "Lote 3 \| Titular: Luis Gomez \| Periodos adeudados: 5 \| Deuda: $2178000.0 \| Intereses: $178000.0" | OK |
| CP20 | Persistencia entre ejecuciones | Ana, luego Carla | 1.ª ejecución: Ana registra un reclamo de Convivencia en el lote 1 y sale. 2.ª ejecución: Carla abre el reporte de reclamos pendientes | La 2.ª ejecución lee el archivo y el reclamo sigue estando; el reporte sale ordenado por prioridad (Comparable) | "Se cargaron los datos guardados." El reporte muestra el N° 4 y el orden ALTA, MEDIA, BAJA, BAJA | OK |
| CP21 | Entrada inválida en el menú | Ana | Opción "abc" y luego 15 | Se informa el error y se vuelve a pedir la opción, sin cortar el programa | "Ingrese un numero." / "Opcion invalida." | OK |
| CP22 | Registrar persona autorizada a reclamar (RF03) | Ana, luego María | Ana autoriza a María Perez (42000111, nueva) en el lote 2. María reclama en el lote 2 y en el lote 1 | Puede reclamar en el lote 2 pero no en el 1 | "Maria Perez quedo autorizado a reclamar por el lote 2." / "Reclamo N° 4 registrado…" / en el lote 1: "Regla de negocio: La persona no puede presentar reclamos para este lote." | OK |
| CP23 | Asignar rol en lote (RF02) | Carla, luego Sofía | Sofía como FAMILIAR del lote 2; después Sofía intenta reclamar en el lote 2 | Queda como residente, pero residir no autoriza a reclamar (RN07) | "Sofia Martinez quedo como FAMILIAR del lote 2." / "Regla de negocio: La persona no puede presentar reclamos para este lote." | OK |
| CP24 | Dar de baja lote (RF01) | Carla | Lote 3 (con deuda y un reclamo abierto) | Se rechaza la baja | "Error: El lote tiene reclamos abiertos o deuda." | OK |

## Resumen

Se ejecutaron 24 casos y todos dieron el resultado esperado. Las 4 pruebas unitarias (106 comprobaciones) pasan.

Observación menor: en CP02 el mensaje dice "vecino" aunque el documento podría corresponder a cualquier persona del sistema.
