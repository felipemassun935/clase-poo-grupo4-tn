/* =====================================================================
   Script 2: datos de ejemplo para probar el sistema y para la defensa.
   Borra todo lo que haya en las tablas y vuelve a cargar los datos de ejemplo.
   Las fechas de reservas, pagos, registros, vacunas y controles se calculan desde HOY (@hoy),
   así los casos de prueba del LEEME funcionan cualquier día que se ejecute.
   ===================================================================== */
USE GuarderiaMascotas;
GO

DECLARE @hoy  DATE = CAST(GETDATE() AS DATE);
DECLARE @hoy0 DATETIME2(0) = CAST(@hoy AS DATETIME2(0));   -- hoy a las 00:00

DELETE FROM log_sistema;
DELETE FROM registro_diario;
DELETE FROM indicacion;
DELETE FROM servicio_contratado;
DELETE FROM pago;
DELETE FROM reserva;
DELETE FROM vacuna;
DELETE FROM mascota;
DELETE FROM cliente;
DELETE FROM alojamiento;
UPDATE sucursal SET id_encargado = NULL;
DELETE FROM empleado;
DELETE FROM sucursal;

-- Sucursales (el encargado se asigna después de cargar los empleados)
INSERT INTO sucursal (id, nombre, direccion, telefono) VALUES (1, N'Patitas Felices - Belgrano', N'Av. Cabildo 1234, CABA', N'11-4785-0000');
INSERT INTO sucursal (id, nombre, direccion, telefono) VALUES (2, N'Patitas Felices - Caballito', N'Av. Rivadavia 5200, CABA', N'11-4902-0000');

-- Empleados (legajos 101, 102, 103 en Belgrano; 201, 202 en Caballito)
INSERT INTO empleado (id, nombre, apellido, dni, telefono, email, legajo, fecha_ingreso, id_sucursal) VALUES (1, N'Carla', N'López', N'20111000', N'11-4000-0001', N'carla@patitas.com', N'101', '2022-03-01', 1);
INSERT INTO empleado (id, nombre, apellido, dni, telefono, email, legajo, fecha_ingreso, id_sucursal) VALUES (2, N'Diego', N'Pérez', N'21222000', N'11-4000-0002', N'diego@patitas.com', N'102', '2023-05-15', 1);
INSERT INTO empleado (id, nombre, apellido, dni, telefono, email, legajo, fecha_ingreso, id_sucursal) VALUES (3, N'Valeria', N'Sosa', N'22333000', N'11-4000-0003', N'valeria@patitas.com', N'103', '2024-02-01', 1);
INSERT INTO empleado (id, nombre, apellido, dni, telefono, email, legajo, fecha_ingreso, id_sucursal) VALUES (4, N'Marcos', N'Ibarra', N'23444000', N'11-4000-0004', N'marcos@patitas.com', N'201', '2021-08-10', 2);
INSERT INTO empleado (id, nombre, apellido, dni, telefono, email, legajo, fecha_ingreso, id_sucursal) VALUES (5, N'Julieta', N'Romero', N'24555000', N'11-4000-0005', N'julieta@patitas.com', N'202', '2025-01-20', 2);
UPDATE sucursal SET id_encargado = 1 WHERE id = 1;
UPDATE sucursal SET id_encargado = 4 WHERE id = 2;

-- Clientes
INSERT INTO cliente (id, nombre, apellido, dni, telefono, email, direccion) VALUES (1, N'Lucía', N'Fernández', N'30111222', N'11-5555-1001', N'lucia.fernandez@mail.com', N'Juramento 2100, CABA');
INSERT INTO cliente (id, nombre, apellido, dni, telefono, email, direccion) VALUES (2, N'Martín', N'Gómez', N'28999333', N'11-5555-1002', N'martin.gomez@mail.com', N'Av. Cabildo 3300, CABA');
INSERT INTO cliente (id, nombre, apellido, dni, telefono, email, direccion) VALUES (3, N'Sofía', N'Ruiz', N'35444555', N'11-5555-1003', N'sofi.ruiz@mail.com', N'Monroe 1500, CABA');
INSERT INTO cliente (id, nombre, apellido, dni, telefono, email, direccion) VALUES (4, N'Tomás', N'Benítez', N'33666777', N'11-5555-1004', N'tomas.benitez@mail.com', N'Av. Acoyte 400, CABA');

-- Alojamientos
INSERT INTO alojamiento (id, codigo, especie, categoria, precio_base_noche, activo, id_sucursal) VALUES (1, N'B-P01', 'PERRO', 'ESTANDAR', 20000.00, 1, 1);
INSERT INTO alojamiento (id, codigo, especie, categoria, precio_base_noche, activo, id_sucursal) VALUES (2, N'B-P02', 'PERRO', 'PREMIUM', 20000.00, 1, 1);
INSERT INTO alojamiento (id, codigo, especie, categoria, precio_base_noche, activo, id_sucursal) VALUES (3, N'B-G01', 'GATO', 'ESTANDAR', 15000.00, 1, 1);
INSERT INTO alojamiento (id, codigo, especie, categoria, precio_base_noche, activo, id_sucursal) VALUES (4, N'B-C01', 'CONEJO', 'ESTANDAR', 10000.00, 1, 1);
INSERT INTO alojamiento (id, codigo, especie, categoria, precio_base_noche, activo, id_sucursal) VALUES (5, N'B-A01', 'AVE', 'ESTANDAR', 8000.00, 1, 1);
INSERT INTO alojamiento (id, codigo, especie, categoria, precio_base_noche, activo, id_sucursal) VALUES (6, N'B-P03', 'PERRO', 'ESTANDAR', 20000.00, 0, 1);
INSERT INTO alojamiento (id, codigo, especie, categoria, precio_base_noche, activo, id_sucursal) VALUES (7, N'C-P01', 'PERRO', 'ESTANDAR', 18000.00, 1, 2);
INSERT INTO alojamiento (id, codigo, especie, categoria, precio_base_noche, activo, id_sucursal) VALUES (8, N'C-G01', 'GATO', 'PREMIUM', 15000.00, 1, 2);
INSERT INTO alojamiento (id, codigo, especie, categoria, precio_base_noche, activo, id_sucursal) VALUES (9, N'C-C01', 'CONEJO', 'PREMIUM', 10000.00, 1, 2);
INSERT INTO alojamiento (id, codigo, especie, categoria, precio_base_noche, activo, id_sucursal) VALUES (10, N'C-A01', 'AVE', 'ESTANDAR', 8000.00, 1, 2);

-- Mascotas (codigo_collar = COL- + id con 5 dígitos)
INSERT INTO mascota (id, codigo_collar, especie, nombre, fecha_nacimiento, peso_kg, observaciones, fecha_ultimo_control, id_cliente, raza, castrado) VALUES (1, 'COL-00001', 'PERRO', N'Rocco', '2021-03-10', 28.5, N'Le tiene miedo a los truenos', DATEADD(DAY, -229, @hoy), 1, N'Labrador', 0);
INSERT INTO mascota (id, codigo_collar, especie, nombre, fecha_nacimiento, peso_kg, observaciones, fecha_ultimo_control, id_cliente, raza, castrado) VALUES (2, 'COL-00002', 'GATO', N'Mishi', '2022-07-22', 4.2, NULL, DATEADD(DAY, -274, @hoy), 1, N'Siamés', 1);
INSERT INTO mascota (id, codigo_collar, especie, nombre, fecha_nacimiento, peso_kg, observaciones, fecha_ultimo_control, id_cliente, raza, castrado) VALUES (3, 'COL-00003', 'PERRO', N'Toby', '2019-11-05', 9.0, N'Come alimento light', DATEADD(DAY, -248, @hoy), 2, N'Caniche', 0);
INSERT INTO mascota (id, codigo_collar, especie, nombre, fecha_nacimiento, peso_kg, observaciones, fecha_ultimo_control, id_cliente, raza, castrado) VALUES (4, 'COL-00004', 'CONEJO', N'Copito', '2023-09-01', 1.8, NULL, DATEADD(DAY, -248, @hoy), 3, N'Enano holandés', 0);
INSERT INTO mascota (id, codigo_collar, especie, nombre, fecha_nacimiento, peso_kg, observaciones, fecha_ultimo_control, id_cliente, raza, castrado) VALUES (5, 'COL-00005', 'AVE', N'Pepe', '2020-06-12', 0.3, N'Habla mucho', DATEADD(DAY, -137, @hoy), 3, N'Loro', 0);
INSERT INTO mascota (id, codigo_collar, especie, nombre, fecha_nacimiento, peso_kg, observaciones, fecha_ultimo_control, id_cliente, raza, castrado) VALUES (6, 'COL-00006', 'CONEJO', N'Nube', '2024-04-20', 2.1, NULL, DATEADD(DAY, -57, @hoy), 4, N'Cabeza de león', 0);
INSERT INTO mascota (id, codigo_collar, especie, nombre, fecha_nacimiento, peso_kg, observaciones, fecha_ultimo_control, id_cliente, raza, castrado) VALUES (7, 'COL-00007', 'PERRO', N'Luna', '2022-12-01', 15.0, NULL, DATEADD(DAY, -189, @hoy), 4, N'Mestizo', 1);
INSERT INTO mascota (id, codigo_collar, especie, nombre, fecha_nacimiento, peso_kg, observaciones, fecha_ultimo_control, id_cliente, raza, castrado) VALUES (8, 'COL-00008', 'GATO', N'Garfield', '2018-05-05', 6.5, N'Es mayor, toma medicación para la artrosis', DATEADD(DAY, -107, @hoy), 2, N'Persa', 1);

-- Vacunas (Toby tiene la Antirrábica vencida)
INSERT INTO vacuna (id_mascota, nombre, fecha_aplicacion, fecha_vencimiento) VALUES (1, N'Antirrábica', DATEADD(DAY, -229, @hoy), DATEADD(DAY, 136, @hoy));
INSERT INTO vacuna (id_mascota, nombre, fecha_aplicacion, fecha_vencimiento) VALUES (1, N'Séxtuple', DATEADD(DAY, -229, @hoy), DATEADD(DAY, 136, @hoy));
INSERT INTO vacuna (id_mascota, nombre, fecha_aplicacion, fecha_vencimiento) VALUES (2, N'Antirrábica', DATEADD(DAY, -274, @hoy), DATEADD(DAY, 91, @hoy));
INSERT INTO vacuna (id_mascota, nombre, fecha_aplicacion, fecha_vencimiento) VALUES (2, N'Triple felina', DATEADD(DAY, -274, @hoy), DATEADD(DAY, 91, @hoy));
INSERT INTO vacuna (id_mascota, nombre, fecha_aplicacion, fecha_vencimiento) VALUES (3, N'Antirrábica', DATEADD(DAY, -533, @hoy), DATEADD(DAY, -168, @hoy));
INSERT INTO vacuna (id_mascota, nombre, fecha_aplicacion, fecha_vencimiento) VALUES (3, N'Séxtuple', DATEADD(DAY, -248, @hoy), DATEADD(DAY, 117, @hoy));
INSERT INTO vacuna (id_mascota, nombre, fecha_aplicacion, fecha_vencimiento) VALUES (7, N'Antirrábica', DATEADD(DAY, -189, @hoy), DATEADD(DAY, 176, @hoy));
INSERT INTO vacuna (id_mascota, nombre, fecha_aplicacion, fecha_vencimiento) VALUES (7, N'Séxtuple', DATEADD(DAY, -189, @hoy), DATEADD(DAY, 176, @hoy));
INSERT INTO vacuna (id_mascota, nombre, fecha_aplicacion, fecha_vencimiento) VALUES (8, N'Antirrábica', DATEADD(DAY, -107, @hoy), DATEADD(DAY, 258, @hoy));
INSERT INTO vacuna (id_mascota, nombre, fecha_aplicacion, fecha_vencimiento) VALUES (8, N'Triple felina', DATEADD(DAY, -107, @hoy), DATEADD(DAY, 258, @hoy));

-- Reservas
INSERT INTO reserva (id, id_mascota, id_alojamiento, fecha_desde, fecha_hasta, estado) VALUES (1, 1, 1, DATEADD(DAY, -2, @hoy), DATEADD(DAY, 3, @hoy), 'EN_CURSO');
INSERT INTO reserva (id, id_mascota, id_alojamiento, fecha_desde, fecha_hasta, estado) VALUES (2, 5, 5, DATEADD(DAY, -15, @hoy), DATEADD(DAY, -11, @hoy), 'FINALIZADA');
INSERT INTO reserva (id, id_mascota, id_alojamiento, fecha_desde, fecha_hasta, estado) VALUES (3, 3, 2, DATEADD(DAY, 0, @hoy), DATEADD(DAY, 4, @hoy), 'CONFIRMADA');
INSERT INTO reserva (id, id_mascota, id_alojamiento, fecha_desde, fecha_hasta, estado) VALUES (4, 4, 4, DATEADD(DAY, 0, @hoy), DATEADD(DAY, 2, @hoy), 'CONFIRMADA');
INSERT INTO reserva (id, id_mascota, id_alojamiento, fecha_desde, fecha_hasta, estado) VALUES (5, 2, 3, DATEADD(DAY, 0, @hoy), DATEADD(DAY, 3, @hoy), 'CONFIRMADA');
INSERT INTO reserva (id, id_mascota, id_alojamiento, fecha_desde, fecha_hasta, estado) VALUES (6, 7, 7, DATEADD(DAY, 9, @hoy), DATEADD(DAY, 14, @hoy), 'PENDIENTE');
INSERT INTO reserva (id, id_mascota, id_alojamiento, fecha_desde, fecha_hasta, estado) VALUES (7, 6, 9, DATEADD(DAY, -6, @hoy), DATEADD(DAY, -4, @hoy), 'CANCELADA');
INSERT INTO reserva (id, id_mascota, id_alojamiento, fecha_desde, fecha_hasta, estado) VALUES (8, 8, 8, DATEADD(DAY, -1, @hoy), DATEADD(DAY, 6, @hoy), 'EN_CURSO');

-- Pagos
INSERT INTO pago (id_reserva, fecha, monto, medio) VALUES (1, DATEADD(DAY, -6, @hoy), 30000.00, N'Transferencia');
INSERT INTO pago (id_reserva, fecha, monto, medio) VALUES (2, DATEADD(DAY, -15, @hoy), 32000.00, N'Efectivo');
INSERT INTO pago (id_reserva, fecha, monto, medio) VALUES (8, DATEADD(DAY, -4, @hoy), 50000.00, N'Tarjeta de débito');

-- Indicaciones del dueño
INSERT INTO indicacion (id, id_reserva, tipo, descripcion, horario) VALUES (1, 1, 'ALIMENTACION', N'Balanceado para perros grandes, 300 g. Sin snacks', N'08:00 y 20:00');
INSERT INTO indicacion (id, id_reserva, tipo, descripcion, horario) VALUES (2, 1, 'MEDICACION', N'Antiparasitario, 1 comprimido', N'Una sola vez, el segundo día');
INSERT INTO indicacion (id, id_reserva, tipo, descripcion, horario) VALUES (3, 1, 'CUIDADO_ESPECIAL', N'Le tiene miedo a los truenos: si hay tormenta, dejarlo en la cucha cerrada', NULL);
INSERT INTO indicacion (id, id_reserva, tipo, descripcion, horario) VALUES (4, 8, 'MEDICACION', N'Meloxicam 0,5 ml para la artrosis', N'Con la cena');
INSERT INTO indicacion (id, id_reserva, tipo, descripcion, horario) VALUES (5, 3, 'ALIMENTACION', N'Alimento light, 150 g', N'Mañana y noche');

-- Registros de la bitácora (los de comida y medicación cumplen una indicación)
INSERT INTO registro_diario (id_reserva, fecha_hora, tipo, descripcion, id_empleado, id_indicacion) VALUES (1, DATEADD(MINUTE, -1680, @hoy0), 'COMIDA', N'Comió toda la ración de balanceado', 2, 1);
INSERT INTO registro_diario (id_reserva, fecha_hora, tipo, descripcion, id_empleado, id_indicacion) VALUES (1, DATEADD(MINUTE, -870, @hoy0), 'MEDICACION', N'Antiparasitario, 1 comprimido', 3, 2);
INSERT INTO registro_diario (id_reserva, fecha_hora, tipo, descripcion, id_empleado, id_indicacion) VALUES (1, DATEADD(MINUTE, -375, @hoy0), 'INCIDENTE', N'Se raspó una pata jugando en el patio', 2, NULL);
INSERT INTO registro_diario (id_reserva, fecha_hora, tipo, descripcion, id_empleado, id_indicacion) VALUES (8, DATEADD(MINUTE, -180, @hoy0), 'MEDICACION', N'Medicación para la artrosis con la cena', 5, 4);
INSERT INTO registro_diario (id_reserva, fecha_hora, tipo, descripcion, id_empleado, id_indicacion) VALUES (1, DATEADD(MINUTE, -780, @hoy0), 'SERVICIO', N'Se realizó: Baño', 2, NULL);
INSERT INTO registro_diario (id_reserva, fecha_hora, tipo, descripcion, id_empleado, id_indicacion) VALUES (8, DATEADD(MINUTE, -450, @hoy0), 'SERVICIO', N'Se realizó: Corte de pelo', 5, NULL);
INSERT INTO registro_diario (id_reserva, fecha_hora, tipo, descripcion, id_empleado, id_indicacion) VALUES (1, DATEADD(MINUTE, -330, @hoy0), 'PASEO', N'Paseo de 40 minutos por la plaza', 3, NULL);

-- Servicios contratados: Rocco tiene el baño hecho y el corte de uñas pendiente; Garfield el corte de pelo hecho;
-- Toby (todavía no ingresó) tiene un baño contratado
INSERT INTO servicio_contratado (id, id_reserva, tipo, precio, fecha_solicitud, realizado, fecha_hora_realizado, id_empleado) VALUES (1, 1, 'BANIO', 9000.00, DATEADD(DAY, -6, @hoy), 1, DATEADD(MINUTE, -780, @hoy0), 2);
INSERT INTO servicio_contratado (id, id_reserva, tipo, precio, fecha_solicitud, realizado, fecha_hora_realizado, id_empleado) VALUES (2, 1, 'CORTE_UNIAS', 5000.00, DATEADD(DAY, -2, @hoy), 0, NULL, NULL);
INSERT INTO servicio_contratado (id, id_reserva, tipo, precio, fecha_solicitud, realizado, fecha_hora_realizado, id_empleado) VALUES (3, 8, 'CORTE_PELO', 14000.00, DATEADD(DAY, -4, @hoy), 1, DATEADD(MINUTE, -450, @hoy0), 5);
INSERT INTO servicio_contratado (id, id_reserva, tipo, precio, fecha_solicitud, realizado, fecha_hora_realizado, id_empleado) VALUES (4, 3, 'BANIO', 9000.00, DATEADD(DAY, -4, @hoy), 0, NULL, NULL);

-- Algunas líneas de log para que la pantalla no arranque vacía
INSERT INTO log_sistema (fecha_hora, usuario, accion, detalle) VALUES (DATEADD(MINUTE, -1740, @hoy0), N'Carla López (legajo 101)', 'CHECK_IN', N'Check-in de la reserva 1 (Rocco, COL-00001) en B-P01');
INSERT INTO log_sistema (fecha_hora, usuario, accion, detalle) VALUES (DATEADD(MINUTE, -780, @hoy0), N'Diego Pérez (legajo 102)', 'SERVICIO', N'Reserva 1: Baño realizado por Diego Pérez');
INSERT INTO log_sistema (fecha_hora, usuario, accion, detalle) VALUES (DATEADD(MINUTE, -450, @hoy0), N'Julieta Romero (legajo 202)', 'SERVICIO', N'Reserva 8: Corte de pelo realizado por Julieta Romero');
INSERT INTO log_sistema (fecha_hora, usuario, accion, detalle) VALUES (DATEADD(MINUTE, -375, @hoy0), N'Diego Pérez (legajo 102)', 'REGISTRO', N'Reserva 1: Incidente - Se raspó una pata jugando en el patio');
GO

PRINT 'Datos de ejemplo cargados.';
