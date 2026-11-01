/* =====================================================================
   TPO POO (UADE) - Guardería de Mascotas - Grupo 4
   Script 1: crea la base de datos, el usuario que usa el programa y las tablas.
   Se ejecuta una sola vez en SQL Server Management Studio (SSMS): abrir el archivo y apretar "Ejecutar" (F5).
   Si se vuelve a ejecutar, borra las tablas y las crea de nuevo (se pierden los datos).
   ===================================================================== */

IF DB_ID('GuarderiaMascotas') IS NULL
    CREATE DATABASE GuarderiaMascotas;
GO

/* Usuario de SQL Server que usa el programa (ver conexion.txt).
   Requiere que el servidor acepte "SQL Server and Windows Authentication mode" (ver LEEME). */
IF NOT EXISTS (SELECT 1 FROM sys.server_principals WHERE name = 'guarderia')
    CREATE LOGIN guarderia WITH PASSWORD = 'Guarderia2026!', CHECK_POLICY = OFF, DEFAULT_DATABASE = GuarderiaMascotas;
GO

USE GuarderiaMascotas;
GO

IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = 'guarderia')
BEGIN
    CREATE USER guarderia FOR LOGIN guarderia;
    ALTER ROLE db_owner ADD MEMBER guarderia;
END
GO

/* Se borran en orden inverso a las claves foráneas */
IF OBJECT_ID('sucursal') IS NOT NULL AND OBJECT_ID('FK_sucursal_encargado') IS NOT NULL
    ALTER TABLE sucursal DROP CONSTRAINT FK_sucursal_encargado;
DROP TABLE IF EXISTS log_sistema;
DROP TABLE IF EXISTS registro_diario;
DROP TABLE IF EXISTS indicacion;
DROP TABLE IF EXISTS servicio_contratado;
DROP TABLE IF EXISTS pago;
DROP TABLE IF EXISTS reserva;
DROP TABLE IF EXISTS vacuna;
DROP TABLE IF EXISTS mascota;
DROP TABLE IF EXISTS cliente;
DROP TABLE IF EXISTS alojamiento;
DROP TABLE IF EXISTS empleado;
DROP TABLE IF EXISTS sucursal;
GO

CREATE TABLE sucursal (
    id            INT           NOT NULL PRIMARY KEY,
    nombre        NVARCHAR(100) NOT NULL,
    direccion     NVARCHAR(150) NOT NULL,
    telefono      NVARCHAR(30)  NULL,
    id_encargado  INT           NULL          -- FK a empleado: se agrega al final (las dos tablas se apuntan)
);

CREATE TABLE empleado (
    id            INT           NOT NULL PRIMARY KEY,
    nombre        NVARCHAR(60)  NOT NULL,
    apellido      NVARCHAR(60)  NOT NULL,
    dni           NVARCHAR(15)  NOT NULL UNIQUE,
    telefono      NVARCHAR(30)  NULL,
    email         NVARCHAR(100) NULL,
    legajo        NVARCHAR(20)  NOT NULL UNIQUE,
    fecha_ingreso DATE          NOT NULL,
    id_sucursal   INT           NOT NULL REFERENCES sucursal(id)
);

ALTER TABLE sucursal ADD CONSTRAINT FK_sucursal_encargado FOREIGN KEY (id_encargado) REFERENCES empleado(id);

CREATE TABLE cliente (
    id            INT           NOT NULL PRIMARY KEY,
    nombre        NVARCHAR(60)  NOT NULL,
    apellido      NVARCHAR(60)  NOT NULL,
    dni           NVARCHAR(15)  NOT NULL UNIQUE,
    telefono      NVARCHAR(30)  NULL,
    email         NVARCHAR(100) NULL,
    direccion     NVARCHAR(150) NULL
);

CREATE TABLE alojamiento (
    id                INT           NOT NULL PRIMARY KEY,
    codigo            NVARCHAR(20)  NOT NULL,
    especie           NVARCHAR(10)  NOT NULL,     -- PERRO, GATO, CONEJO, AVE (enum Especie)
    categoria         NVARCHAR(10)  NOT NULL,     -- ESTANDAR, PREMIUM (enum Categoria)
    precio_base_noche DECIMAL(12,2) NOT NULL,
    activo            BIT           NOT NULL,
    id_sucursal       INT           NOT NULL REFERENCES sucursal(id),
    CONSTRAINT UQ_alojamiento_codigo UNIQUE (id_sucursal, codigo)
);

/* Una sola tabla para toda la jerarquía Mascota: la columna "especie" dice qué subclase es */
CREATE TABLE mascota (
    id                   INT           NOT NULL PRIMARY KEY,
    codigo_collar        NVARCHAR(20)  NOT NULL UNIQUE,   -- identificador del collar (COL-00001)
    especie              NVARCHAR(10)  NOT NULL,
    nombre               NVARCHAR(60)  NOT NULL,
    fecha_nacimiento     DATE          NOT NULL,
    peso_kg              DECIMAL(6,2)  NOT NULL,
    observaciones        NVARCHAR(300) NULL,
    fecha_ultimo_control DATE          NULL,
    id_cliente           INT           NOT NULL REFERENCES cliente(id),
    raza                 NVARCHAR(60)  NULL,              -- raza (perro, gato, conejo) o tipo de ave
    castrado             BIT           NOT NULL DEFAULT 0 -- solo perro y gato
);

CREATE TABLE vacuna (
    id                INT IDENTITY(1,1) PRIMARY KEY,
    id_mascota        INT           NOT NULL REFERENCES mascota(id),
    nombre            NVARCHAR(60)  NOT NULL,
    fecha_aplicacion  DATE          NOT NULL,
    fecha_vencimiento DATE          NOT NULL
);

CREATE TABLE reserva (
    id             INT          NOT NULL PRIMARY KEY,
    id_mascota     INT          NOT NULL REFERENCES mascota(id),
    id_alojamiento INT          NOT NULL REFERENCES alojamiento(id),
    fecha_desde    DATE         NOT NULL,
    fecha_hasta    DATE         NOT NULL,
    estado         NVARCHAR(15) NOT NULL       -- enum EstadoReserva
);

CREATE TABLE pago (
    id          INT IDENTITY(1,1) PRIMARY KEY,
    id_reserva  INT           NOT NULL REFERENCES reserva(id),
    fecha       DATE          NOT NULL,
    monto       DECIMAL(12,2) NOT NULL,
    medio       NVARCHAR(30)  NOT NULL
);

CREATE TABLE servicio_contratado (
    id                   INT           NOT NULL PRIMARY KEY,
    id_reserva           INT           NOT NULL REFERENCES reserva(id),
    tipo                 NVARCHAR(20)  NOT NULL,   -- enum TipoServicio
    precio               DECIMAL(12,2) NOT NULL,
    fecha_solicitud      DATE          NOT NULL,
    realizado            BIT           NOT NULL DEFAULT 0,
    fecha_hora_realizado DATETIME2(0)  NULL,
    id_empleado          INT           NULL REFERENCES empleado(id)   -- quién lo hizo
);

CREATE TABLE indicacion (
    id          INT           NOT NULL PRIMARY KEY,
    id_reserva  INT           NOT NULL REFERENCES reserva(id),
    tipo        NVARCHAR(20)  NOT NULL,   -- enum TipoIndicacion
    descripcion NVARCHAR(300) NOT NULL,
    horario     NVARCHAR(100) NULL
);

CREATE TABLE registro_diario (
    id            INT IDENTITY(1,1) PRIMARY KEY,
    id_reserva    INT           NOT NULL REFERENCES reserva(id),
    fecha_hora    DATETIME2(0)  NOT NULL,
    tipo          NVARCHAR(20)  NOT NULL,   -- enum TipoRegistro
    descripcion   NVARCHAR(300) NOT NULL,
    id_empleado   INT           NOT NULL REFERENCES empleado(id),   -- quién lo hizo
    id_indicacion INT           NULL REFERENCES indicacion(id)      -- indicación del dueño que cumple
);

CREATE TABLE log_sistema (
    id          INT IDENTITY(1,1) PRIMARY KEY,
    fecha_hora  DATETIME2(0)  NOT NULL,
    usuario     NVARCHAR(100) NOT NULL,
    accion      NVARCHAR(20)  NOT NULL,   -- enum AccionLog
    detalle     NVARCHAR(500) NOT NULL
);
GO

PRINT 'Base GuarderiaMascotas y tablas creadas. Ahora ejecutar 2_datos_de_ejemplo.sql';
