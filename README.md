# 🚀 Catálogo de Produtos com Config-Server

### 🎯 Sobre o Projeto

Esta API RESTful foi desenvolvida para simplificar o gerenciamento de produtos em seu sistema. <br>
Ela oferece funcionalidades para criar, ler, atualizar, excluir e gerar relatório de categorias e produtos de forma eficiente e escalável. <br>
A API também possui um recurso para enviar o catálogo de produtos para os clientes via email, aonde o mesmo receberá uma lista de produtos cadastrados. <br>
Também foi utilizado o conceito de config-server para poder realizar as configurações necessárias para o funcionamento da API, como: Banco de dados, configuração de email dentre outras. 
### 🛠️ Tecnologias Utilizadas

Java 21 <br>
Spring Boot 3 <br>
Banco de Dados: Postgres<br>
Segurança: Spring Security com Auth0 e Token JWT<br>
Documentação: SpringDoc OpenAPI (Swagger UI)<br>
Orquestração/Ambiente: Docker e Docker Compose

### ⚙️ Ambiente de Desenvolvimento

Pré-requisitos: <br>

1) Para a api do config-server, crie as seguintes variáveis de ambiente conforme abaixo: 

### Variáveis e valores do config-server:
CONFIG_SERVER_PASSWORD=(password do repositório que armazena o config-server, Ex: Github) <br>
CONFIG_SERVER_URI=(Endereço do repositório do config-server); <br>
CONFIG_SERVER_USERNAME=(Username do repositório do config-server); <br>

### Variáveis e valores do catalogo-produtos:
CONFIG_SERVER_URI=(endereço do config-server: http://localhost:8888) <br>
PROFILE_ACTIVE=(Nome do profile configurado no config-server, Ex:local ou dev) <br>

2) Executando a Aplicação com Docker Compose
   Para montar e iniciar todos os serviços (MongoDB e a API) de uma vez:

Bash:

**_docker-compose up -d --build_** <br>
A aplicação estará acessível em http://localhost:8080.

### 🧭 Documentação da API (Swagger UI)

A documentação interativa da API, gerada pelo SpringDoc OpenAPI, está disponível assim que a aplicação estiver em execução.

URL do Swagger UI:
http://localhost:8080/swagger-ui.html

### 🤝 Contribuições

Contribuições são bem-vindas! Sinta-se à vontade para abrir issues ou enviar pull requests para melhorias, correções de bugs ou adição de novos recursos.
