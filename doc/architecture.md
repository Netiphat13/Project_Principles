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


## Web UI Architecture

The web UI is intentionally split into two modes:

- Server-rendered authentication pages: `src/main/resources/templates/auth/login.html` and `register.html` via Thymeleaf.
- Static application pages: `src/main/resources/static/*.html` for dashboard, bill, group, statistics, and profile screens. These pages call REST APIs with `fetch()` and session cookies.

There must be no duplicate application-page HTML under `templates/`. The web controller keeps friendly routes such as `/home` and `/bills` as authenticated aliases that redirect to their corresponding static pages.
