-- Script SQL para crear la tabla de pacientes en PostgreSQL
-- Ejecutar este script en la base de datos 'clinica_db'

CREATE TABLE IF NOT EXISTS patients (
    id VARCHAR(255) PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    document_number VARCHAR(255) NOT NULL,
    document_type VARCHAR(50) NOT NULL,
    birth_date DATE NOT NULL,
    address TEXT,
    phone VARCHAR(20),
    email VARCHAR(255),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    admission_date TIMESTAMP NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Crear índices para mejorar el rendimiento
CREATE INDEX IF NOT EXISTS idx_patients_document_number ON patients(document_number);
CREATE INDEX IF NOT EXISTS idx_patients_city ON patients(city);
CREATE INDEX IF NOT EXISTS idx_patients_active ON patients(active);
CREATE INDEX IF NOT EXISTS idx_patients_email ON patients(email);

-- Comentarios en la tabla
COMMENT ON TABLE patients IS 'Tabla de pacientes de la clínica';
COMMENT ON COLUMN patients.id IS 'Identificador único del paciente';
COMMENT ON COLUMN patients.first_name IS 'Nombre del paciente';
COMMENT ON COLUMN patients.last_name IS 'Apellido del paciente';
COMMENT ON COLUMN patients.document_number IS 'Número de documento de identidad';
COMMENT ON COLUMN patients.document_type IS 'Tipo de documento (CC, CE, TI, etc.)';
COMMENT ON COLUMN patients.birth_date IS 'Fecha de nacimiento del paciente';
COMMENT ON COLUMN patients.address IS 'Dirección de residencia';
COMMENT ON COLUMN patients.phone IS 'Número de teléfono';
COMMENT ON COLUMN patients.email IS 'Correo electrónico';
COMMENT ON COLUMN patients.city IS 'Ciudad de residencia';
COMMENT ON COLUMN patients.state IS 'Estado/Departamento de residencia';
COMMENT ON COLUMN patients.admission_date IS 'Fecha de ingreso a la clínica';
COMMENT ON COLUMN patients.active IS 'Indica si el paciente está activo';
