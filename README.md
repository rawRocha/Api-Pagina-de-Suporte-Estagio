# API - Página de Suporte

Esta API é responsável pelo gerenciamento de usuários, autenticação e mensagens trocadas na página de suporte técnico.

## Endpoints

### Auth

- `POST /auth/login` – Realiza o login na API.

### Users

- `GET /users` – Lista todos os usuários ativos.
- `GET /users/pending` – Lista os cadastros pendentes e recusados.
- `POST /users` – Cria um novo cadastro.
- `POST /users/approve-user` – Aprova um novo cadastro.
- `POST /users/refuse-user` – Recusa um novo cadastro.

## 🚧 Status

API em construção. Novos recursos serão adicionados em breve.
