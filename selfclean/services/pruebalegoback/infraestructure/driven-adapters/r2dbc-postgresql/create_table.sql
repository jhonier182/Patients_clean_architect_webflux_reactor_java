CREATE TABLE patients (
    id VARCHAR(255) PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    document_number VARCHAR(255) NOT NULL,
    document_type VARCHAR(50) NOT NULL,
    birth_date DATE NOT NULL,
    address TEXT,
    phone VARCHAR(20),
    email VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    admission_date TIMESTAMP NOT NULL,
    active BOOLEAN NOT NULL
);

-- Índices para mejorar rendimiento
CREATE INDEX idx_patients_document_number ON patients(document_number);
CREATE INDEX idx_patients_city ON patients(city);
CREATE INDEX idx_patients_active ON patients(active);
CREATE INDEX idx_patients_admission_date ON patients(admission_date);
