
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

create table rol
(
    id_rol      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        character varying,
    description character varying(200)
);

-----------
create table users
(
    id_user       UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    id_rol        UUID NOT NULL,
    first_name    character varying,
    last_name     character varying,
    email_address character varying UNIQUE,
    address       character varying,
    document_id   character varying,
    birth_date    date,
    phone_number  character varying,
    base_salary   decimal(12, 2),
    FOREIGN KEY (id_rol) REFERENCES rol (id_rol)
);


alter table users add column password character varying;


INSERT INTO rol (id_rol, name, description)
VALUES ('facbe723-85f2-4f5a-92d6-a4a4a3a5b8ca', 'ADMIN', 'ADMIN');
INSERT INTO rol (id_rol, name, description)
VALUES ('a71e243b-e901-4e6e-b521-85ff39ac2f3e', 'CLIENT', 'CLIENT');
INSERT INTO rol (id_rol, name, description)
VALUES ('beaed8b3-7090-4c58-a3d5-7578ce4f1b6a', 'ASSESSOR', 'ASSESSOR');
