# Architecture

## Layered Architecture

- Presentation: `controller/api`, `controller/web`
- Service: `service`, `service/impl`
- Data Access: `repository`
- Domain: `model`
- API Contract: `dto/request`, `dto/response`
- Mapping: `mapper`
- Cross-cutting: `config`, `exception`

## Main REST resources

- `GET/POST/PUT/DELETE /api/v1/users`
- `GET/POST/PUT/DELETE /api/v1/groups`
- `GET/POST/PUT/DELETE /api/v1/bills`
- `GET /api/v1/bills?userId={id}&page=0&size=10&sort=createdAt,desc`

Swagger UI: `/swagger-ui.html`
