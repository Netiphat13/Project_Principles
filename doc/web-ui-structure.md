# SplitMate Web UI Structure

SplitMate intentionally uses two different rendering modes and must not duplicate application pages.

## 1. Server-rendered authentication

Only these pages belong under `src/main/resources/templates/auth/`:

- `login.html`
- `register.html`

They are rendered by Thymeleaf so Spring MVC can display flash messages after login/registration failures or success.

## 2. Static application pages

All authenticated application screens belong under `src/main/resources/static/`:

- `index.html`
- `create-bill.html`
- `bills.html`
- `bill-detail.html`
- `groups.html`
- `group-detail.html`
- `stats.html`
- `profile.html`
- `edit-profile.html`

These pages are delivered as static resources and call the REST API using `fetch()` with the existing session cookie.

## 3. Friendly routes

The MVC controller keeps user-friendly routes such as `/home`, `/bills`, `/stats`, and `/profile`. The session interceptor protects these routes, then the controller redirects to the corresponding static HTML page.

Examples:

- `/home` → `/index.html`
- `/bills` → `/bills.html`
- `/stats` → `/stats.html`
- `/profile` → `/profile.html`

## 4. Maintenance rule

Never create a second copy of an application page under `templates/`. When changing a dashboard/bill/group/profile page, edit the matching file under `static/` only.
