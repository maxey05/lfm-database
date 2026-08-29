CREATE TABLE church_satellite (
    id     bigserial    PRIMARY KEY,
    name   varchar(100) NOT NULL UNIQUE,
    active boolean      NOT NULL DEFAULT true
);

CREATE TABLE app_user (
    id            bigserial    PRIMARY KEY,
    username      varchar(60)  NOT NULL,
    password_hash varchar(100) NOT NULL,
    full_name     varchar(150) NOT NULL,
    role          varchar(20)  NOT NULL,
    enabled       boolean      NOT NULL DEFAULT true,
    created_at    timestamptz  NOT NULL DEFAULT now()
);

CREATE TABLE person (
    id                    bigserial    PRIMARY KEY,
    first_name            varchar(100) NOT NULL,
    middle_name           varchar(100),
    last_name             varchar(100) NOT NULL,
    nickname              varchar(60),
    email                 varchar(255),
    contact_number        varchar(25),
    facebook_name         varchar(150),
    date_of_birth         date,
    gender                varchar(20),
    civil_status          varchar(20),
    church_satellite_id   bigint       REFERENCES church_satellite (id),
    in_dgroup             boolean      NOT NULL DEFAULT false,
    dgroup_leader_name    varchar(150),
    dgroup_leader_id      bigint       REFERENCES person (id),
    dgroup_leader_contact varchar(25),
    lfm_group_leader_name varchar(150),
    lfm_group_leader_id   bigint       REFERENCES person (id),
    source                varchar(20)  NOT NULL DEFAULT 'MANUAL',
    archived              boolean      NOT NULL DEFAULT false,
    created_at            timestamptz  NOT NULL DEFAULT now(),
    updated_at            timestamptz  NOT NULL DEFAULT now(),
    created_by            bigint       REFERENCES app_user (id),
    updated_by            bigint       REFERENCES app_user (id),
    CONSTRAINT uq_person_email      UNIQUE (email),
    CONSTRAINT ck_person_email_case CHECK (email = lower(email))
);

CREATE TABLE form_submission_log (
    id                 bigserial   PRIMARY KEY,
    google_response_id varchar(120) NOT NULL UNIQUE,
    received_at        timestamptz NOT NULL DEFAULT now(),
    raw_payload        jsonb,
    status             varchar(20) NOT NULL,
    error_message      text,
    person_id          bigint      REFERENCES person (id)
);

CREATE INDEX idx_person_last_name  ON person (last_name);
CREATE INDEX idx_person_dob        ON person (date_of_birth);
CREATE INDEX idx_person_satellite  ON person (church_satellite_id);
CREATE INDEX idx_person_created_at ON person (created_at);