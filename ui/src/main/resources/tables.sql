CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Таблица жилых комплексов
CREATE TABLE residentials (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
	name VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL,
	parking_type VARCHAR(20)  NOT NULL,
	slots_num INTEGER NOT NULL
);

-- Таблица пользователей
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    fio VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password TEXT NOT NULL,
    residential_id UUID NOT NULL,
    role VARCHAR(20) CHECK (role IN ('RESIDENT', 'GUEST', 'ADMIN')) NOT NULL,
    FOREIGN KEY (residential_id) REFERENCES residentials(id) ON DELETE CASCADE
);

-- Таблица транспорта
CREATE TABLE transport (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    number VARCHAR(20) UNIQUE NOT NULL,
    model VARCHAR(100) NOT NULL,
    color VARCHAR(50) NOT NULL,
    insurance BOOLEAN NOT NULL
);

-- Таблица связи пользователей и транспорта
CREATE TABLE user_transport (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    transport_id UUID NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (transport_id) REFERENCES transport(id) ON DELETE CASCADE
);

-- Таблица парковочных слотов
CREATE TABLE slots (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    residential_id UUID NOT NULL,
    number INT NOT NULL,
    status VARCHAR(10) CHECK (status IN ('FREE', 'BUSY')) NOT NULL,
    UNIQUE (residential_id, number),
    FOREIGN KEY (residential_id) REFERENCES residentials(id) ON DELETE CASCADE
);

-- Таблица бронирования парковочных мест
CREATE TABLE reservations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    slot_id UUID NOT NULL,
    transport_id UUID NOT NULL,
    status VARCHAR(10) CHECK (status IN ('ACTIVE', 'FROZEN')) NOT NULL,
    FOREIGN KEY (slot_id) REFERENCES slots(id) ON DELETE CASCADE,
    FOREIGN KEY (transport_id) REFERENCES transport(id) ON DELETE CASCADE
);

-- Таблица КПП (контрольно-пропускных пунктов)
CREATE TABLE checkpoints (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    guard_fio VARCHAR(255) NOT NULL,
    guard_phone VARCHAR(20) NOT NULL,
    residential_id UUID NOT NULL,
    number VARCHAR(50) NOT NULL,
    status VARCHAR(20) CHECK (status IN ('OK', 'OUT_OF_ORDER')) NOT NULL,
    UNIQUE (residential_id, number),
    FOREIGN KEY (residential_id) REFERENCES residentials(id) ON DELETE CASCADE
);


CREATE TABLE reservation_logs (
    log_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reservation_id UUID NOT NULL,
    action_type TEXT NOT NULL,
    old_status VARCHAR(20),
    new_status VARCHAR(20),
    changed_at TIMESTAMPTZ DEFAULT now(),
    comment TEXT
);

ALTER TABLE reservation_logs ADD CONSTRAINT reservation_logs_action_check CHECK (action_type IN ('INSERT', 'UPDATE', 'DELETE'));

INSERT INTO residentials(name, address, parking_type, slots_num)
VALUES ('Пчелкино', 'Ленина, д.1', 'UNDERGROUND', 50);
INSERT INTO residentials(name, address, parking_type, slots_num)
VALUES ('Мечта', 'Проспект Мира, д.1', 'GROUND', 50);

INSERT INTO users(fio, email, password, residential_id, role)
	VALUES ('Нисуев Нису Флексович', 'nisu@example.com', '123123123',
	(select id from residentials where name = 'Пчелкино'), 'RESIDENT');

INSERT INTO checkpoints(guard_fio, guard_phone, residential_id, "number", status)
	VALUES ('Илюшкинс Владян Леонидович', '+79999999999',
	(select id from residentials where name = 'Пчелкино'),
	'1A', 'OK');

INSERT INTO checkpoints(guard_fio, guard_phone, residential_id, "number", status)
	VALUES ('Майклов Майкл Майклович', '+79998887766',
	(select id from residentials where name = 'Пчелкино'),
	'1Б', 'OK');

INSERT INTO slots(residential_id, "number", status)
VALUES ((SELECT id from residentials WHERE name = 'Пчелкино'),
1, 'FREE'
);

INSERT INTO slots(residential_id, "number", status)
VALUES ((SELECT id from residentials WHERE name = 'Пчелкино'),
2, 'FREE'
);