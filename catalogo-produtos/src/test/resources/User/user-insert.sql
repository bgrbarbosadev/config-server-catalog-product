-- uuid user:  4b62ccd3-0003-442f-9494-9a71c88215fa role: ROLE_ADMIN
-- uuid admin: f54e1e7e-b16e-496e-9865-4eff2c0a493f role: ROLE_USER

insert into tb_user (uuid, email, first_name, last_name, password)
values ("2426cd48-463d-4dc4-803f-ca188615e7e1", 'administrador@gmail.com', 'administrador', '$2a$12$lAoZQs/af8NOQQvWNExnQuTecQl1Gux/s7Y4pIx3ooaJWaPE8BC5y');

insert into tb_user (uuid, email, first_name, last_name, password)
values ("a704b8e0-3f99-4975-bc5a-f678eda74715", 'usuario@gmail.com', 'usuario', '$2a$12$lAoZQs/af8NOQQvWNExnQuTecQl1Gux/s7Y4pIx3ooaJWaPE8BC5y');

-- Usuario admin role
insert into tb_user_role(user_id, role_id) values ('2426cd48-463d-4dc4-803f-ca188615e7e1', '4b62ccd3-0003-442f-9494-9a71c88215fa')
insert into tb_user_role(user_id, role_id) values ('2426cd48-463d-4dc4-803f-ca188615e7e1', 'f54e1e7e-b16e-496e-9865-4eff2c0a493f')

-- Usuário user role
insert into tb_user_role(user_id, role_id) values ('a704b8e0-3f99-4975-bc5a-f678eda74715', 'f54e1e7e-b16e-496e-9865-4eff2c0a493f')
