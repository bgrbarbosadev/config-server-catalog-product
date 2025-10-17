# API CATALOGO DE PRODUTOS
<br>
Esta API RESTful foi desenvolvida para simplificar o gerenciamento de produtos em seu sistema. Ela oferece funcionalidades para criar, ler, atualizar, excluir e gerar relatório de categorias e produtos  de forma eficiente e escalável. A API também possui um recurso para enviar o catálogo de produtos para os clientes via email, aonde o mesmo receberá uma lista de produtos cadastrados.

---
## 🚀 Tecnologias Utilizadas

Linguagem: Java - versão 21
Framework: Spring Boot
Banco de Dados: Postgres
Containerização: Docker
---
## 🛠️ Montagem do Ambiente com Docker

 Para facilitar a configuração e garantir um ambiente de desenvolvimento consistente, utilizamos o Docker. Siga estes passos para subir a aplicação:

### Clone o repositório com o comando

1° - git clone https://github.com/bgrbarbosadev/catalogo-produtos.git

2° - entre no diretório do projeto catalogo-produtos

3° - Construa e inicie os contêineres através do git bash com o comando abaixo: **docker-compose up -d**

### Criar uma conexão com o banco e rodar o sql abaixo para o insert do usuário padrão para o inicio das atividades:

  ### Dados da conexão: 
  
  DATASOURCE_URL=jdbc:postgresql://product-catalog-db:5432/postgres <br>
  DATASOURCE_USERNAME=postgres <br>
  DATASOURCE_PASSWORD=example

  ### SQL:

  INSERT INTO tb_role("uuid", authority)VALUES('55f46b07-1c87-4bb4-b67f-ccbeaa36b631'::uuid, 'ROLE_ADMIN');
  INSERT INTO tb_role("uuid", authority)VALUES('9cfae039-7c5f-4369-97b2-883b68e2c031'::uuid, 'ROLE_USER');
  
  INSERT INTO tb_user("uuid", email, first_name, last_name, "password")
  VALUES('c2347cd4-53f0-4555-84f5-3c78763da59b'::uuid, 'admin@gmail.com', 'admin', 'admin', '$2a$10$fAdC5xIHW5/uCJfHpHEA/.N4P3jQ7ivnssz.Y0pLvCzyF8VT0NRwS');
  
  INSERT INTO tb_user("uuid", email, first_name, last_name, "password")
  VALUES('ea58c7c6-f3c7-41e3-bddd-f001512220db'::uuid, 'user@gmail.com', 'user', 'user', '$2a$10$JJBfgNfKE2SxqyEbko/gO.SmSGCS0Z7uW3HkAXEYtFu4zf0Kid1Ri');
  
  INSERT INTO tb_user_role(user_id, role_id)VALUES(c2347cd4-53f0-4555-84f5-3c78763da59b, 55f46b07-1c87-4bb4-b67f-ccbeaa36b631);
  INSERT INTO tb_user_role(user_id, role_id)VALUES(ea58c7c6-f3c7-41e3-bddd-f001512220db, 9cfae039-7c5f-4369-97b2-883b68e2c031);

### Acesse a API
Após os contêineres estarem rodando, a API estará disponível. 
<br>Consulte a seção de "Recursos" para ver as coleções de endpoints.

---
## 📚 Recursos utilizados no desenvolvimento

1) Logs com log4j       
2) Validação e mensagens padrão com spring validation
3) Pesquisa avançada com specification e filtros criteria com retorno paginado
4) Envio de catálogo via email com JavaMailSender
5) Geração de relatórios com jasper-report
6) Autenticação e autorização com spring security com oauth0
8) Documentação de API com swagger
9) Testes unitários e de integração com MockMVC e Mockito
10) Docker
---
## 💡 Ajuda.

#### As collections utilizadas na api vocês encontram dentro do diretório raiz do projeto - POSTMAN_COLLECTION.json
#### Documentação da API (Swagger/OpenAPI): http://localhost:8080/swagger-ui/index.html#/


### **&copy; 2025 Bruno Gaspar Romeiro Barbosa - Email: bgrbarbosa@hotmail.com**
