
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



