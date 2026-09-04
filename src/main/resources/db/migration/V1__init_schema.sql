-- ============================================================
-- HOSPITAL MANAGEMENT DATABASE
-- PostgreSQL
-- ============================================================

-- ============================================================
-- 1. USERS
-- ============================================================

CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,

                       role VARCHAR(50) NOT NULL,

                       email VARCHAR(150) NOT NULL,
                       password VARCHAR(255) NOT NULL,

                       document_id VARCHAR(50) NOT NULL,

                       first_name VARCHAR(100) NOT NULL,
                       last_name VARCHAR(100) NOT NULL,

                       phone VARCHAR(20),

                       is_active BOOLEAN NOT NULL DEFAULT TRUE,

                       created_at TIMESTAMP WITHOUT TIME ZONE
        NOT NULL DEFAULT CURRENT_TIMESTAMP,

                       updated_at TIMESTAMP WITHOUT TIME ZONE,

                       CONSTRAINT uk_users_email
                           UNIQUE (email),

                       CONSTRAINT uk_users_document_id
                           UNIQUE (document_id)
);

CREATE INDEX idx_users_role
    ON users(role);

CREATE INDEX idx_users_is_active
    ON users(is_active);

CREATE INDEX idx_users_last_name
    ON users(last_name);

-- ============================================================
-- 2. DEPARTMENTS
-- ============================================================

CREATE TABLE departments (
                             id BIGSERIAL PRIMARY KEY,

                             department_type VARCHAR(255) NOT NULL,

                             location VARCHAR(100) NOT NULL,

                             phone_extension VARCHAR(10),

                             description VARCHAR(500),

                             is_active BOOLEAN NOT NULL DEFAULT TRUE,

                             created_at TIMESTAMP WITHOUT TIME ZONE
        NOT NULL DEFAULT CURRENT_TIMESTAMP,

                             updated_at TIMESTAMP WITHOUT TIME ZONE,

                             CONSTRAINT uk_departments_type
                                 UNIQUE (department_type)
);

CREATE INDEX idx_departments_is_active
    ON departments(is_active);

CREATE INDEX idx_departments_location
    ON departments(location);

-- ============================================================
-- 3. PATIENTS
-- ============================================================

CREATE TABLE patients (
                          id BIGSERIAL PRIMARY KEY,

                          user_id BIGINT NOT NULL,

                          blood_type VARCHAR(20),

                          birth_date DATE NOT NULL,

                          emergency_contact_name VARCHAR(150),
                          emergency_contact_phone VARCHAR(20),
                          emergency_contact_relationship VARCHAR(50),

                          allergies TEXT,

                          has_health_insurance BOOLEAN
                              NOT NULL DEFAULT FALSE,

                          health_insurance_provider VARCHAR(100),

                          health_insurance_number VARCHAR(50),

                          medical_history TEXT,

                          created_at TIMESTAMP WITHOUT TIME ZONE
        NOT NULL DEFAULT CURRENT_TIMESTAMP,

                          updated_at TIMESTAMP WITHOUT TIME ZONE,

                          CONSTRAINT uk_patients_user
                              UNIQUE (user_id),

                          CONSTRAINT fk_patient_user
                              FOREIGN KEY (user_id)
                                  REFERENCES users(id)
                                  ON DELETE CASCADE
);

CREATE INDEX idx_patients_blood_type
    ON patients(blood_type);

CREATE INDEX idx_patients_birth_date
    ON patients(birth_date);

CREATE INDEX idx_patients_health_insurance
    ON patients(has_health_insurance);

-- ============================================================
-- 4. DOCTORS
-- ============================================================

CREATE TABLE doctors (
                         id BIGSERIAL PRIMARY KEY,

                         user_id BIGINT NOT NULL,

                         department_id BIGINT NOT NULL,

                         medical_license_number VARCHAR(50) NOT NULL,

                         specialty VARCHAR(255) NOT NULL,

                         years_of_experience INTEGER
                             NOT NULL DEFAULT 0,

                         biography TEXT,

                         created_at TIMESTAMP WITHOUT TIME ZONE
        NOT NULL DEFAULT CURRENT_TIMESTAMP,

                         updated_at TIMESTAMP WITHOUT TIME ZONE,

                         CONSTRAINT uk_doctors_user
                             UNIQUE (user_id),

                         CONSTRAINT uk_doctors_medical_license
                             UNIQUE (medical_license_number),

                         CONSTRAINT chk_doctors_experience
                             CHECK (years_of_experience >= 0),

                         CONSTRAINT fk_doctor_user
                             FOREIGN KEY (user_id)
                                 REFERENCES users(id)
                                 ON DELETE CASCADE,

                         CONSTRAINT fk_doctor_department
                             FOREIGN KEY (department_id)
                                 REFERENCES departments(id)
                                 ON DELETE RESTRICT
);

CREATE INDEX idx_doctors_department
    ON doctors(department_id);

CREATE INDEX idx_doctors_specialty
    ON doctors(specialty);

-- ============================================================
-- 5. NURSES
-- ============================================================

CREATE TABLE nurses (
                        id BIGSERIAL PRIMARY KEY,

                        user_id BIGINT NOT NULL,

                        department_id BIGINT NOT NULL,

                        license_number VARCHAR(50) NOT NULL,

                        specialty VARCHAR(255)
                            NOT NULL DEFAULT 'GENERAL',

                        shift_type VARCHAR(255) NOT NULL,

                        years_of_experience INTEGER
                            NOT NULL DEFAULT 0,

                        hire_date DATE,

                        biography TEXT,

                        emergency_contact_name VARCHAR(150),
                        emergency_contact_phone VARCHAR(20),
                        emergency_contact_relationship VARCHAR(50),

                        max_patients_per_shift INTEGER
                            NOT NULL DEFAULT 5,

                        is_charge_nurse BOOLEAN
                            NOT NULL DEFAULT FALSE,

                        vacation_days_available INTEGER
                                     DEFAULT 30,

                        created_at TIMESTAMP WITHOUT TIME ZONE
        NOT NULL DEFAULT CURRENT_TIMESTAMP,

                        updated_at TIMESTAMP WITHOUT TIME ZONE,

                        CONSTRAINT uk_nurses_user
                            UNIQUE (user_id),

                        CONSTRAINT uk_nurses_license
                            UNIQUE (license_number),

                        CONSTRAINT chk_nurses_experience
                            CHECK (
                                years_of_experience
                                    BETWEEN 0 AND 60
                                ),

                        CONSTRAINT chk_nurses_max_patients
                            CHECK (
                                max_patients_per_shift
                                    BETWEEN 0 AND 20
                                ),

                        CONSTRAINT chk_nurses_vacation_days
                            CHECK (
                                vacation_days_available IS NULL
                                    OR vacation_days_available
                                    BETWEEN 0 AND 60
                                ),

                        CONSTRAINT fk_nurse_user
                            FOREIGN KEY (user_id)
                                REFERENCES users(id)
                                ON DELETE CASCADE,

                        CONSTRAINT fk_nurse_department
                            FOREIGN KEY (department_id)
                                REFERENCES departments(id)
                                ON DELETE RESTRICT
);

CREATE INDEX idx_nurses_department
    ON nurses(department_id);

CREATE INDEX idx_nurses_specialty
    ON nurses(specialty);

CREATE INDEX idx_nurses_shift
    ON nurses(shift_type);

CREATE INDEX idx_nurses_charge
    ON nurses(is_charge_nurse);

-- ============================================================
-- 6. RECEPTIONISTS
-- ============================================================

CREATE TABLE receptionists (
                               id BIGSERIAL PRIMARY KEY,

                               user_id BIGINT NOT NULL,

                               department_id BIGINT NOT NULL,

                               desk_number VARCHAR(20),

                               shift_type VARCHAR(255) NOT NULL,

                               created_at TIMESTAMP WITHOUT TIME ZONE
        NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               updated_at TIMESTAMP WITHOUT TIME ZONE,

                               CONSTRAINT uk_receptionists_user
                                   UNIQUE (user_id),

                               CONSTRAINT fk_receptionist_user
                                   FOREIGN KEY (user_id)
                                       REFERENCES users(id)
                                       ON DELETE CASCADE,

                               CONSTRAINT fk_receptionist_department
                                   FOREIGN KEY (department_id)
                                       REFERENCES departments(id)
                                       ON DELETE RESTRICT
);

CREATE INDEX idx_receptionists_department
    ON receptionists(department_id);

CREATE INDEX idx_receptionists_shift
    ON receptionists(shift_type);

CREATE INDEX idx_receptionists_desk
    ON receptionists(desk_number);

-- ============================================================
-- 7. ADMINS
-- ============================================================

CREATE TABLE admin (
                       id BIGSERIAL PRIMARY KEY,

                       user_id BIGINT NOT NULL,

                       department_id BIGINT,

                       admin_level VARCHAR(30)
                                      NOT NULL DEFAULT 'DEPARTMENT_ADMIN',

                       last_login TIMESTAMP WITHOUT TIME ZONE,

                       is_super_admin BOOLEAN
                                      NOT NULL DEFAULT FALSE,

                       created_at TIMESTAMP WITHOUT TIME ZONE
        NOT NULL DEFAULT CURRENT_TIMESTAMP,

                       updated_at TIMESTAMP WITHOUT TIME ZONE,

                       CONSTRAINT uk_admin_user
                           UNIQUE (user_id),

                       CONSTRAINT chk_admin_department
                           CHECK (
                               admin_level <> 'DEPARTMENT_ADMIN'
                                   OR department_id IS NOT NULL
                               ),

                       CONSTRAINT chk_admin_super_status
                           CHECK (
                               (
                                   admin_level = 'SUPER_ADMIN'
                                       AND is_super_admin = TRUE
                                   )
                                   OR
                               (
                                   admin_level <> 'SUPER_ADMIN'
                                       AND is_super_admin = FALSE
                                   )
                               ),

                       CONSTRAINT fk_admin_user
                           FOREIGN KEY (user_id)
                               REFERENCES users(id)
                               ON DELETE CASCADE,

                       CONSTRAINT fk_admin_department
                           FOREIGN KEY (department_id)
                               REFERENCES departments(id)
                               ON DELETE SET NULL
);

CREATE INDEX idx_admin_department
    ON admin(department_id);

CREATE INDEX idx_admin_level
    ON admin(admin_level);

CREATE INDEX idx_admin_super
    ON admin(is_super_admin);

CREATE INDEX idx_admin_last_login
    ON admin(last_login);

-- ============================================================
-- 8. ADMIN PERMISSIONS
-- ============================================================

CREATE TABLE admin_permissions (
                                   admin_id BIGINT NOT NULL,

                                   permission VARCHAR(50) NOT NULL,

                                   CONSTRAINT uk_admin_permission
                                       UNIQUE (
                                               admin_id,
                                               permission
                                           ),

                                   CONSTRAINT fk_admin_permissions_admin
                                       FOREIGN KEY (admin_id)
                                           REFERENCES admin(id)
                                           ON DELETE CASCADE
);

CREATE INDEX idx_admin_permissions_permission
    ON admin_permissions(permission);

-- ============================================================
-- 9. WARDS
-- ============================================================

CREATE TABLE wards (
                       id BIGSERIAL PRIMARY KEY,

                       name VARCHAR(100) NOT NULL,

                       description VARCHAR(500),

                       is_active BOOLEAN
                                         NOT NULL DEFAULT TRUE,

                       department_id BIGINT NOT NULL,

                       created_at TIMESTAMP WITHOUT TIME ZONE
        NOT NULL DEFAULT CURRENT_TIMESTAMP,

                       updated_at TIMESTAMP WITHOUT TIME ZONE,

                       CONSTRAINT uk_ward_name_department
                           UNIQUE (
                                   name,
                                   department_id
                               ),

                       CONSTRAINT fk_ward_department
                           FOREIGN KEY (department_id)
                               REFERENCES departments(id)
                               ON DELETE RESTRICT
);

CREATE INDEX idx_wards_department
    ON wards(department_id);

CREATE INDEX idx_wards_is_active
    ON wards(is_active);

CREATE INDEX idx_wards_name
    ON wards(name);

-- ============================================================
-- 10. ROOMS
-- ============================================================

CREATE TABLE rooms (
                       id BIGSERIAL PRIMARY KEY,

                       number VARCHAR(20) NOT NULL,

                       floor INTEGER NOT NULL,

                       room_type VARCHAR(255) NOT NULL,

                       room_status VARCHAR(255)
                           NOT NULL DEFAULT 'AVAILABLE',

                       capacity INTEGER NOT NULL,

                       ward_id BIGINT NOT NULL,

                       notes TEXT,

                       created_at TIMESTAMP WITHOUT TIME ZONE
        NOT NULL DEFAULT CURRENT_TIMESTAMP,

                       updated_at TIMESTAMP WITHOUT TIME ZONE,

                       CONSTRAINT uk_rooms_number
                           UNIQUE (number),

                       CONSTRAINT chk_rooms_capacity
                           CHECK (capacity > 0),

                       CONSTRAINT fk_room_ward
                           FOREIGN KEY (ward_id)
                               REFERENCES wards(id)
                               ON DELETE RESTRICT
);

CREATE INDEX idx_rooms_ward
    ON rooms(ward_id);

CREATE INDEX idx_rooms_type
    ON rooms(room_type);

CREATE INDEX idx_rooms_status
    ON rooms(room_status);

CREATE INDEX idx_rooms_floor
    ON rooms(floor);

-- ============================================================
-- 11. BEDS
-- ============================================================

CREATE TABLE beds (
                      id BIGSERIAL PRIMARY KEY,

                      bed_number VARCHAR(20) NOT NULL,

                      room_id BIGINT NOT NULL,

                      status VARCHAR(255)
                                             NOT NULL DEFAULT 'AVAILABLE',

                      notes TEXT,

                      created_at TIMESTAMP WITHOUT TIME ZONE
        NOT NULL DEFAULT CURRENT_TIMESTAMP,

                      updated_at TIMESTAMP WITHOUT TIME ZONE,

                      CONSTRAINT uk_bed_number_room
                          UNIQUE (
                                  bed_number,
                                  room_id
                              ),

                      CONSTRAINT fk_bed_room
                          FOREIGN KEY (room_id)
                              REFERENCES rooms(id)
                              ON DELETE RESTRICT
);

CREATE INDEX idx_beds_room
    ON beds(room_id);

CREATE INDEX idx_beds_status
    ON beds(status);

CREATE INDEX idx_beds_room_status
    ON beds(
            room_id,
            status
        );

-- ============================================================
-- 12. ADMISSIONS
-- ============================================================

CREATE TABLE admissions (
                            id BIGSERIAL PRIMARY KEY,

                            patient_id BIGINT NOT NULL,

                            bed_id BIGINT NOT NULL,

                            attending_doctor_id BIGINT NOT NULL,

                            status VARCHAR(255)
                                NOT NULL DEFAULT 'ACTIVE',

                            admission_reason VARCHAR(250) NOT NULL,

                            admitted_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,

                            discharged_at TIMESTAMP WITHOUT TIME ZONE,

                            notes TEXT,

                            created_at TIMESTAMP WITHOUT TIME ZONE
        NOT NULL DEFAULT CURRENT_TIMESTAMP,

                            updated_at TIMESTAMP WITHOUT TIME ZONE,

                            CONSTRAINT fk_admission_patient
                                FOREIGN KEY (patient_id)
                                    REFERENCES patients(id)
                                    ON DELETE RESTRICT,

                            CONSTRAINT fk_admission_bed
                                FOREIGN KEY (bed_id)
                                    REFERENCES beds(id)
                                    ON DELETE RESTRICT,

                            CONSTRAINT fk_admission_doctor
                                FOREIGN KEY (attending_doctor_id)
                                    REFERENCES doctors(id)
                                    ON DELETE RESTRICT,

                            CONSTRAINT chk_admission_dates
                                CHECK (
                                    discharged_at IS NULL
                                        OR discharged_at >= admitted_at
                                    )
);

CREATE INDEX idx_admissions_patient
    ON admissions(patient_id);

CREATE INDEX idx_admissions_bed
    ON admissions(bed_id);

CREATE INDEX idx_admissions_doctor
    ON admissions(attending_doctor_id);

CREATE INDEX idx_admissions_status
    ON admissions(status);

CREATE INDEX idx_admissions_admitted_at
    ON admissions(admitted_at);

CREATE INDEX idx_admissions_patient_status
    ON admissions(
                  patient_id,
                  status
        );

CREATE INDEX idx_admissions_bed_status
    ON admissions(
                  bed_id,
                  status
        );

-- Una cama solamente puede tener un ingreso activo.
CREATE UNIQUE INDEX uk_admission_active_bed
    ON admissions(bed_id)
    WHERE status = 'ACTIVE';

-- Un paciente solamente puede tener un ingreso activo.
CREATE UNIQUE INDEX uk_admission_active_patient
    ON admissions(patient_id)
    WHERE status = 'ACTIVE';

-- ============================================================
-- 13. APPOINTMENTS
-- ============================================================

CREATE TABLE appointments (
                              id BIGSERIAL PRIMARY KEY,

                              doctor_id BIGINT NOT NULL,

                              patient_id BIGINT NOT NULL,

                              room_id BIGINT,

                              date_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,

                              reason VARCHAR(100) NOT NULL,

                              notes TEXT,

                              status VARCHAR(255)
                                  NOT NULL DEFAULT 'SCHEDULED',

                              cancellation_reason VARCHAR(200),

                              cancelled_at TIMESTAMP WITHOUT TIME ZONE,

                              confirmed_at TIMESTAMP WITHOUT TIME ZONE,

                              completed_at TIMESTAMP WITHOUT TIME ZONE,

                              created_at TIMESTAMP WITHOUT TIME ZONE
        NOT NULL DEFAULT CURRENT_TIMESTAMP,

                              updated_at TIMESTAMP WITHOUT TIME ZONE,

                              CONSTRAINT fk_appointment_doctor
                                  FOREIGN KEY (doctor_id)
                                      REFERENCES doctors(id)
                                      ON DELETE RESTRICT,

                              CONSTRAINT fk_appointment_patient
                                  FOREIGN KEY (patient_id)
                                      REFERENCES patients(id)
                                      ON DELETE RESTRICT,

                              CONSTRAINT fk_appointment_room
                                  FOREIGN KEY (room_id)
                                      REFERENCES rooms(id)
                                      ON DELETE SET NULL
);

CREATE INDEX idx_appointments_doctor
    ON appointments(doctor_id);

CREATE INDEX idx_appointments_patient
    ON appointments(patient_id);

CREATE INDEX idx_appointments_room
    ON appointments(room_id);

CREATE INDEX idx_appointments_status
    ON appointments(status);

CREATE INDEX idx_appointments_date_time
    ON appointments(date_time);

CREATE INDEX idx_appointments_doctor_date
    ON appointments(
                    doctor_id,
                    date_time
        );

CREATE INDEX idx_appointments_patient_date
    ON appointments(
                    patient_id,
                    date_time
        );

CREATE INDEX idx_appointments_room_date
    ON appointments(
                    room_id,
                    date_time
        );

-- Evita que un doctor tenga dos citas activas
-- exactamente en la misma fecha y hora.
CREATE UNIQUE INDEX uk_appointment_doctor_datetime_active
    ON appointments(
                    doctor_id,
                    date_time
        )
    WHERE status IN (
        'SCHEDULED',
        'CONFIRMED'
    );

-- Evita que un paciente tenga dos citas activas
-- exactamente en la misma fecha y hora.
CREATE UNIQUE INDEX uk_appointment_patient_datetime_active
    ON appointments(
                    patient_id,
                    date_time
        )
    WHERE status IN (
        'SCHEDULED',
        'CONFIRMED'
    );

-- Evita reservar una habitación para dos citas activas
-- exactamente en la misma fecha y hora.
CREATE UNIQUE INDEX uk_appointment_room_datetime_active
    ON appointments(
                    room_id,
                    date_time
        )
    WHERE room_id IS NOT NULL
      AND status IN (
          'SCHEDULED',
          'CONFIRMED'
      );