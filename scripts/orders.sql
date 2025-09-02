CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
-------------
create table status
(
    id_status   UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        varchar(200),
    description varchar(200)
);
create table type_loan
(
    id_type_loan         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name                 varchar(200),
    minimum_amount       decimal(12, 2),
    maximum_amount       decimal(12, 2),
    interest_rate        decimal(12, 2),
    automatic_validation boolean default false
);

create table orders
(
    id_order      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    amount         decimal(12, 2),
    term_months   integer,
    document_id   varchar(15),
    email_address varchar(200),
    id_status     uuid,
    id_type_loan  uuid,
    FOREIGN KEY (id_status) REFERENCES status (id_status),
    FOREIGN KEY (id_type_loan) REFERENCES type_loan (id_type_loan)
);


---STATUS
INSERT INTO status (id_status,name, description)
VALUES ('f8820448-a6ef-4d0d-beb8-130a71dc3fda','Pendiente de revisión', 'Pendiente de revisión');
INSERT INTO status (id_status, name, description)
VALUES ('f7820448-a6ef-4d0d-beb8-130a71dc3fda', 'En revision', 'En revision');
---TYPE LOAN
INSERT INTO type_loan (id_type_loan, name, minimum_amount, maximum_amount, interest_rate, automatic_validation)
VALUES ('f7820448-a6ef-4d0d-beb8-130a71dc3fd4', 'Prestamo 1', 0, 5000, 0.45, true);
INSERT INTO type_loan (id_type_loan, name, minimum_amount, maximum_amount, interest_rate, automatic_validation)
VALUES ('f8820448-a6ef-4d0d-beb8-130a71dc3fd0', 'Prestamo 2', 3000, 10000, 0.60, false);