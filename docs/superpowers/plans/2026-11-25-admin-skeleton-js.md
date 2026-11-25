# Admin Skeleton Pages & Base JS Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement the 6 admin skeleton JSP pages (`dashboard`, `kamar`, `tipe-kamar`, `penghuni`, `pembayaran`, `keluhan`) using the shared layout shell, plus vanilla JavaScript in `app.js` for modal open/close, delete confirmation, and flash toast dismissal (Issue #3).

**Architecture:** All 6 admin JSPs reuse the existing `WEB-INF/views/layout/` shell (`head.jspf`, `sidebar.jspf`, `topbar.jspf`, `footer.jspf`) and include two shared admin fragments (`toast.jspf` and `confirm-delete-modal.jspf`) under `WEB-INF/views/admin/fragments/`. Declarative `data-*` attributes connect buttons/forms to `src/main/webapp/assets/js/app.js` with zero inline scripts and zero business logic in JSPs. Because `AuthServlet` and `sidebar.jspf` already target `/admin/dashboard` while `DashboardServlet` does not yet exist on `main`, a minimal `DashboardServlet` wired to `DashboardDao` is included so all 6 admin routes render end-to-end.

**Tech Stack:** Java 17, Tomcat 10.1 (Jakarta EE 10, `jakarta.*`), JSP 3.0 + JSTL (`jakarta.tags.core`, `jakarta.tags.functions`), Vanilla JS (ES6+), CSS tokens (`tokens.css` + `components.css`), Maven (`./bin/mvn`), Node `node --test` (unit test for `app.js`), Docker Compose + Playwright/Chrome DevTools MCP (browser verification).

**Spec:** GitHub Issue #3 (`[S0][FE] Skeleton 6 halaman admin + JS modal/konfirmasi/toast`), `docs/SPRINT.md` (T1 & DoD), `docs/ADMIN-SPEC.md`, `docs/UI-KIT.md`, `docs/CONTRACT.md`, `PRD-quiz2.md`.

## Global Constraints

- **Jakarta EE 10 namespace:** Strictly use `jakarta.*` (`jakarta.servlet.*`, `jakarta.tags.core`, `jakarta.tags.functions`); never use `javax.*`.
- **No business logic in JSP:** No `<% ... %>` scriptlets and no database queries in JSPs; JSPs only display request/session attributes.
- **XSS & CSRF safety:** Escape all dynamic HTML outputs with `<c:out value="${...}"/>` or `fn:escapeXml(...)`; every `<form method="post">` must include `<input type="hidden" name="_csrf" value="${fn:escapeXml(csrfToken)}">`; JS must use `.textContent` (never `.innerHTML`) for dynamic strings.
- **CSS & JS rules:** Use only existing CSS classes from `components.css`/`pages.css` and tokens from `tokens.css` (no Bootstrap/Tailwind, no gradients, no inline styles). Vanilla JS only in `src/main/webapp/assets/js/app.js`.
- **Folder ownership coordination note:** Issue #3 primary folders are `src/main/webapp/WEB-INF/views/admin/` and `src/main/webapp/assets/js/app.js` (Atma). `src/main/java/id/dekost/servlet/DashboardServlet.java` (Task 3) touches Anang's `servlet/` folder only by adding the missing `/admin/dashboard` servlet (zero edits to existing Java files) so `GET /admin/dashboard` renders instead of 404.
- **Git & Remote:** Develop on branch `feat/atma/admin-skeleton-js`; never commit `db/seed.sql` local changes; **do not push anything**.

## Review Focus

1. **Missing modal target or missing DOM container:** Clicking `[data-modal-open="nonexistent"]`, pressing `Escape` when no modal is open, or loading a page without `.toast-stack` or `#confirm-delete-modal` must be silent no-ops with zero console errors.
2. **Pre-opened server validation modal:** A `.modal.is-open` rendered directly by the server (e.g., validation failure) must close via `.modal__close`, `[data-modal-close]`, backdrop click, or `Escape` and restore `document.body.style.overflow`.
3. **XSS in delete confirmation message:** Passing `<script>alert(1)</script>` or quotes inside `data-delete-message` / `data-delete-title` must render literally via `.textContent` without executing script or breaking DOM attributes.
4. **Auth & route availability across all 6 pages:** Unauthenticated `GET /admin/*` redirects to `/login`; authenticated `ADMIN` `GET` on `/admin/dashboard`, `/admin/kamar`, `/admin/tipe-kamar`, `/admin/penghuni`, `/admin/pembayaran`, and `/admin/keluhan` all return HTTP `200` with zero browser console/page errors.
5. **Toast auto-dismiss & manual dismiss race:** Manually clicking/dismissing a `.toast` before its auto-dismiss timer fires must cancel/ignore the pending timer cleanly without errors, and `data-toast-timeout="0"` must disable auto-dismiss.

---

### Task 1: Implement `app.js` (Modal, Delete Confirmation, Toast) with Unit Tests

**Files:**
- Modify: `src/main/webapp/assets/js/app.js`
- Create: `src/test/js/app.test.mjs`

**Interfaces:**
- Produces (DOM declarative API in `src/main/webapp/assets/js/app.js`):
  - `[data-modal-open="<modalId>"]` — opens `.modal#<modalId>` by adding `.is-open`
  - `[data-modal-close]` or `.modal__close` — closes nearest `.modal`
  - `[data-confirm-open="<modalId>"]` with optional `data-delete-url`, `data-delete-id`, `data-delete-title`, `data-delete-message` — populates `#<modalId>` (`form[action]`, `input[name="id"]`, `[data-confirm-title]`, `[data-confirm-message]`) via `.textContent` and opens it
  - `form[data-confirm]` — intercepts submit, opens `#confirm-delete-modal` if present, submits original form on confirmation
  - `.toast-stack[data-toast-timeout]` / `.toast` / `[data-toast-dismiss]` — auto-dismisses after `4500` ms (or `data-toast-timeout`, `0` = persistent) and dismisses on click
- Produces (JS API on `window.DeKostUI`):
  - `openModal(modalOrId: HTMLElement | string, trigger?: HTMLElement | null): boolean`
  - `closeModal(modalOrId?: HTMLElement | string | null): boolean`
  - `showToast(message: string, type?: "success" | "error" | "warning", timeoutMs?: number): HTMLElement | null`
  - `init(root?: Document): void` (auto-runs once on `DOMContentLoaded` or immediately if `document.readyState !== "loading"`)

- [ ] **Step 1: Write failing unit tests in `src/test/js/app.test.mjs`**

Create `src/test/js/app.test.mjs` using `node:test` and `node:assert/strict` with a minimal DOM stub covering `document`, `Element`, `classList`, `closest`, `querySelector`/`querySelectorAll`, event bubbling, timers, and `window.DeKostUI`:

```js
import test from "node:test";
import assert from "node:assert/strict";
import fs from "node:fs";
import vm from "node:vm";
import path from "node:path";

// Test cases to include:
// 1. "openModal and closeModal toggle .is-open via data-modal-open, .modal__close, [data-modal-close], backdrop, and Escape"
//    - asserts modal.classList.contains("is-open") true on trigger click, body.style.overflow === "hidden"
//    - asserts close via .modal__close, [data-modal-close], backdrop click (event.target === modal), and Escape restores body.style.overflow === "" and trigger focus
// 2. "pre-opened .modal.is-open closes cleanly via Escape and backdrop without trigger"
// 3. "missing modal target on [data-modal-open] is a safe no-op without throwing"
// 4. "delete confirmation trigger populates form action, id, title, and XSS-safe textContent message"
//    - sets data-delete-url="/admin/kamar/delete", data-delete-id="42",
//      data-delete-message='Hapus kamar <script>alert(1)</script>?'
//    - asserts form.getAttribute("action") === "/admin/kamar/delete", idInput.value === "42",
//      messageEl.textContent === 'Hapus kamar <script>alert(1)</script>?', innerHTML untouched, modal opened
// 5. "toast auto-dismisses after timeout, dismisses on click, and respects data-toast-timeout=0"
//    - asserts DeKostUI.showToast("Tersimpan", "success", 50) creates .toast in .toast-stack and removes it after timer
//    - asserts clicking .toast removes it immediately and does not throw when timer fires
//    - asserts toast with data-toast-timeout="0" stays in DOM
```

- [ ] **Step 2: Run unit tests to verify they fail**

Run: `node --test src/test/js/app.test.mjs`
Expected: FAIL because `window.DeKostUI` is undefined and modal/toast handlers are not implemented in `src/main/webapp/assets/js/app.js`.

- [ ] **Step 3: Implement `src/main/webapp/assets/js/app.js`**

Implement an IIFE in `src/main/webapp/assets/js/app.js` with clean helper functions (`openModal`, `closeModal`, `openConfirmDelete`, `dismissToast`, `scheduleToast`, `showToast`, `init`):
- Use delegated `click` and `keydown` (`Escape`) listeners on `root` (guarded against double-initialization per `root` using a `WeakSet` or flag).
- In `openModal`: support `HTMLElement` or string `id`, add `.is-open`, set `body.style.overflow = "hidden"`, save `trigger` in a `WeakMap`, focus first focusable control (`button, [href], input:not([type="hidden"]), select, textarea, [tabindex]:not([tabindex="-1"])`) or `.modal__dialog` if focusable.
- In `closeModal`: when `modalOrId` omitted, close the last `.modal.is-open`; remove `.is-open`; restore `body.style.overflow = ""` only when `root.querySelectorAll(".modal.is-open").length === 0`; restore focus to saved trigger if connected.
- In `openConfirmDelete`: read `data-confirm-open` (default `"confirm-delete-modal"`), find modal, set `form.action` if `data-delete-url` present, set `form.querySelector('input[name="id"]').value` if `data-delete-id` present, set `[data-confirm-title].textContent` if `data-delete-title` present (fallback `"Konfirmasi hapus"`), set `[data-confirm-message].textContent` if `data-delete-message` present, clear pending `form[data-confirm]` callback if any, and call `openModal(modal, trigger)`.
- Also handle `submit` delegation for `form[data-confirm]`: if `form.dataset.confirmed === "true"`, allow submit; otherwise prevent default, and if `#confirm-delete-modal` exists, populate its message from `form.dataset.confirm`, attach a one-time confirm click handler on `#confirm-delete-modal [data-confirm-submit]` that sets `form.dataset.confirmed = "true"` and calls `form.requestSubmit ? form.requestSubmit() : form.submit()`, then open `#confirm-delete-modal`.
- In toast manager: track timers in a `WeakMap` so manual click dismissal (`toast.remove()`) clears `clearTimeout(timerId)`. `showToast(message, type = "success", timeoutMs = 4500)` creates `.toast` (adding `.toast--error` for `"error"`, `.toast--warning` for `"warning"`), sets `.textContent = message`, appends to `.toast-stack` (creating `<div class="toast-stack" aria-live="polite"></div>` in `document.body` if none exists), and schedules auto-dismiss when `timeoutMs > 0`.

- [ ] **Step 4: Run unit tests to verify they pass**

Run: `node --test src/test/js/app.test.mjs`
Expected: PASS (all 5 test cases green, 0 failures).

- [ ] **Step 5: Commit Task 1**

```bash
git add src/main/webapp/assets/js/app.js src/test/js/app.test.mjs
git commit -m "feat(fe): implement modal, delete confirmation, and toast handlers in app.js"
```

---

### Task 2: Create Shared Admin JSP Fragments (`toast.jspf` and `confirm-delete-modal.jspf`)

**Files:**
- Create: `src/main/webapp/WEB-INF/views/admin/fragments/toast.jspf`
- Create: `src/main/webapp/WEB-INF/views/admin/fragments/confirm-delete-modal.jspf`

**Interfaces:**
- Consumes:
  - `requestScope.flash` (`id.dekost.util.Flash.Message` with `type` in `{"success", "error", "warning"}` and `text`)
  - `requestScope.csrfToken` (`String` set by `CsrfFilter`)
  - CSS classes `.toast-stack`, `.toast`, `.toast--error`, `.toast--warning`, `.modal`, `.modal__dialog`, `.modal__header`, `.modal__title`, `.modal__close`, `.modal__body`, `.modal__footer`, `.button`, `.button--secondary`, `.button--primary` from `components.css`
  - JS hooks `data-modal-close`, `data-confirm-title`, `data-confirm-message`, `data-confirm-submit` from Task 1 `app.js`
- Produces:
  - Reusable JSP fragments included by all admin JSPs via `<%@ include file="/WEB-INF/views/admin/fragments/..." %>`

- [ ] **Step 1: Create `src/main/webapp/WEB-INF/views/admin/fragments/toast.jspf`**

Render `.toast-stack` with `aria-live="polite"` and `data-toast-timeout="4500"`. Inside `<c:if test="${not empty flash}">`, map `flash.type` using `<c:choose>` (`error` -> `toast toast--error`, `warning` -> `toast toast--warning`, `<c:otherwise>` -> `toast`), and render `<p class="${toastClass}" role="status"><c:out value="${flash.text}"/></p>`.

- [ ] **Step 2: Create `src/main/webapp/WEB-INF/views/admin/fragments/confirm-delete-modal.jspf`**

Render `<div class="modal" id="confirm-delete-modal" role="dialog" aria-modal="true" aria-labelledby="confirm-delete-title">` containing:
- `.modal__dialog`
- `.modal__header` with `<h2 class="modal__title" id="confirm-delete-title" data-confirm-title>Konfirmasi hapus</h2>` and `<button class="modal__close" type="button" aria-label="Tutup" data-modal-close>&times;</button>`
- `<form method="post" action="${pageContext.request.contextPath}/admin/dashboard">` (action replaced dynamically by `app.js` from `data-delete-url`):
  - `<input type="hidden" name="_csrf" value="${fn:escapeXml(csrfToken)}">`
  - `<input type="hidden" name="id" value="">`
  - `.modal__body` with `<p data-confirm-message>Apakah Anda yakin ingin menghapus data ini?</p>`
  - `.modal__footer` with `<button class="button button--secondary" type="button" data-modal-close>Batal</button>` and `<button class="button button--primary" type="submit" data-confirm-submit>Hapus</button>`

- [ ] **Step 3: Verify fragment syntax and security rules**

Run: `rg -n "<%|javax\.|innerHTML" src/main/webapp/WEB-INF/views/admin/fragments/`
Expected: Only `<%@` JSTL directives if any, zero `<% ... %>` scriptlets, zero `javax.`, zero unescaped EL expressions.

- [ ] **Step 4: Commit Task 2**

```bash
git add src/main/webapp/WEB-INF/views/admin/fragments/
git commit -m "feat(fe): add shared admin toast and confirm-delete modal fragments"
```

---

### Task 3: Create `DashboardServlet` and Admin `dashboard.jsp` Skeleton

**Files:**
- Create: `src/main/java/id/dekost/servlet/DashboardServlet.java` (Note: servlet folder owned by Anang per `docs/SPRINT.md`; added only so `/admin/dashboard` renders instead of 404)
- Create: `src/main/webapp/WEB-INF/views/admin/dashboard.jsp`

**Interfaces:**
- Consumes:
  - `id.dekost.dao.DashboardDao` (`getRoomOccupancy()`, `getOccupancyByFloor()`, `getMonthlyRevenue()`, `getOverdueBillingSummary()`, `getOpenComplaintSummary()`, `findUpcomingPayments()`, `findRecentComplaints()`)
  - `id.dekost.util.Flash.consume(HttpServletRequest)`
  - `/WEB-INF/views/layout/{head,sidebar,topbar,footer}.jspf` and `/WEB-INF/views/admin/fragments/toast.jspf`
- Produces:
  - `GET /admin/dashboard` endpoint forwarding to `/WEB-INF/views/admin/dashboard.jsp` with `pageTitle="Dashboard"`, `activeNav="dashboard"`, `flash`, and `docs/CONTRACT.md` dashboard attributes (`roomOccupancy`, `floorOccupancy`, `monthlyRevenue`, `overdueBilling`, `openComplaints`, `duePayments`, `recentComplaints`).

- [ ] **Step 1: Create `src/main/java/id/dekost/servlet/DashboardServlet.java`**

Implement `@WebServlet(name = "DashboardServlet", urlPatterns = "/admin/dashboard")` extending `HttpServlet`:
- `VIEW = "/WEB-INF/views/admin/dashboard.jsp"`
- `DashboardDao dashboardDao = new DashboardDao()`
- In `doGet`: set the 7 `DashboardDao` attributes documented in `docs/CONTRACT.md`, set `pageTitle` (`"Dashboard"`), `activeNav` (`"dashboard"`), call `Flash.consume(request)`, and forward to `VIEW`. Wrap `SQLException` in `ServletException("Gagal memuat data dashboard.", e)`.

- [ ] **Step 2: Create `src/main/webapp/WEB-INF/views/admin/dashboard.jsp`**

Follow the exact `docs/ADMIN-SPEC.md` page structure:
- Directives: `contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"`, `taglib` `c` (`jakarta.tags.core`) and `fn` (`jakarta.tags.functions`)
- Include `head.jspf`, `<body class="admin-body">`, `.admin-layout`, `sidebar.jspf`, `.admin-main`, `topbar.jspf`, `toast.jspf`
- Inside `<main class="admin-content">`: render a clean `.card.card--outline` containing `.empty-state` (`<h2 class="empty-state__title">Dashboard siap diisi</h2>` and helper text noting ringkasan kamar, pendapatan, tagihan jatuh tempo, dan keluhan terbaru will be wired in the next sprint)
- Include `footer.jspf` and `<script src="${pageContext.request.contextPath}/assets/js/app.js" defer></script>` before `</body>`.

- [ ] **Step 3: Build WAR with Maven to verify compilation**

Run: `./bin/mvn clean package`
Expected: `BUILD SUCCESS`, `target/ROOT.war` generated with exit code 0.

- [ ] **Step 4: Commit Task 3**

```bash
git add src/main/java/id/dekost/servlet/DashboardServlet.java src/main/webapp/WEB-INF/views/admin/dashboard.jsp
git commit -m "feat(admin): add DashboardServlet and skeleton dashboard.jsp"
```

---

### Task 4: Create Remaining 5 Admin List Skeleton JSPs & Verify End-to-End in Live App

**Files:**
- Create: `src/main/webapp/WEB-INF/views/admin/kamar/list.jsp`
- Create: `src/main/webapp/WEB-INF/views/admin/tipe-kamar/list.jsp`
- Create: `src/main/webapp/WEB-INF/views/admin/penghuni/list.jsp`
- Create: `src/main/webapp/WEB-INF/views/admin/pembayaran/list.jsp`
- Create: `src/main/webapp/WEB-INF/views/admin/keluhan/list.jsp`

**Interfaces:**
- Consumes:
  - Existing servlets (`KamarServlet`, `TipeKamarServlet`, `PenghuniServlet`, `PembayaranServlet`, `KeluhanServlet`) forwarding to those exact 5 JSP paths with `pageTitle`, `activeNav`, `csrfToken`, `flash`
  - Shared fragments (`layout/{head,sidebar,topbar,footer}.jspf`, `admin/fragments/{toast,confirm-delete-modal}.jspf`)
  - JS hooks (`data-modal-open`, `data-modal-close`, `.modal__close`, `data-confirm-open`) from `assets/js/app.js`

- [ ] **Step 1: Create the 5 skeleton list JSPs**

For each of the 5 pages (`kamar`, `tipe-kamar`, `penghuni`, `pembayaran`, `keluhan`), use the `docs/ADMIN-SPEC.md` shell (`head.jspf`, `sidebar.jspf`, `topbar.jspf`, `toast.jspf`, `<main class="admin-content">`, `footer.jspf`, `app.js` script) and include:
1. `.toolbar` with a skeleton primary button `<button class="button button--primary" type="button" data-modal-open="<slug>-modal"><Label></button>`:
   - `kamar`: `id="kamar-modal"`, label `Tambah kamar`
   - `tipe-kamar`: `id="tipe-kamar-modal"`, label `Tambah tipe kamar`
   - `penghuni`: `id="penghuni-modal"`, label `Tambah penghuni`
   - `pembayaran`: `id="pembayaran-modal"`, label `Tambah tagihan`
   - `keluhan`: `id="keluhan-modal"`, label `Catat keluhan`
2. `.table-card` with `.table-card__header` (using `<c:out value="${pageTitle}"/>`) and `.empty-state` indicating the list table is ready for Sprint 2/3 data binding.
3. Skeleton `.modal` (`id="<slug>-modal"`, `role="dialog"`, `aria-modal="true"`, `aria-labelledby="<slug>-modal-title"`) containing `.modal__header` (title + `.modal__close` with `data-modal-close`), `.modal__body` placeholder `<p class="field__hint">...</p>`, and `.modal__footer` (`Batal` with `data-modal-close`).
4. `<%@ include file="/WEB-INF/views/admin/fragments/confirm-delete-modal.jspf" %>`.

- [ ] **Step 2: Run static compliance checks & build WAR**

Run:
```bash
rg -n "javax\.|<%=|gradient" src/main/webapp/WEB-INF/views/admin/ src/main/webapp/assets/js/app.js ; ./bin/mvn clean package
```
Expected: `rg` returns no matches; `./bin/mvn clean package` exits 0 (`BUILD SUCCESS`).

- [ ] **Step 3: Rebuild/start Docker app container and verify health + auth redirect**

Run:
```bash
docker compose up --build -d app && sleep 3 && curl -fsS http://localhost:8080/health && curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/admin/dashboard
```
Expected: `/health` returns `OK` (HTTP 200) and unauthenticated `/admin/dashboard` returns `302` redirecting to `/login`.

- [ ] **Step 4: Run Playwright/Chrome DevTools browser verification on all 6 pages**

Using Playwright MCP (`browser_navigate`, `browser_fill_form`, `browser_click`, `browser_console_messages`, `browser_evaluate`):
1. Navigate to `http://localhost:8080/login`, sign in as `admin@kos.test`, verify redirect to `/admin/dashboard` (`200 OK`).
2. Visit all 6 routes (`/admin/dashboard`, `/admin/kamar`, `/admin/tipe-kamar`, `/admin/penghuni`, `/admin/pembayaran`, `/admin/keluhan`) and verify each renders its heading, active sidebar item, and `0` console/page errors.
3. On `/admin/kamar`, click `Tambah kamar` (`[data-modal-open="kamar-modal"]`), verify `#kamar-modal.is-open` appears, close via `Escape`, re-open and close via `.modal__close`, and test `#confirm-delete-modal` + `DeKostUI.showToast(...)` dismissal with `0` console errors.
4. Check desktop layout width `1024x768` (`document.documentElement.scrollWidth <= window.innerWidth`).

- [ ] **Step 5: Run Node unit tests again and commit Task 4**

Run: `node --test src/test/js/app.test.mjs && git status -sb`
Expected: All JS unit tests pass; only intended files staged (keep `db/seed.sql` unstaged).

```bash
git add src/main/webapp/WEB-INF/views/admin/kamar/list.jsp \
        src/main/webapp/WEB-INF/views/admin/tipe-kamar/list.jsp \
        src/main/webapp/WEB-INF/views/admin/penghuni/list.jsp \
        src/main/webapp/WEB-INF/views/admin/pembayaran/list.jsp \
        src/main/webapp/WEB-INF/views/admin/keluhan/list.jsp
git commit -m "feat(admin): add 5 skeleton list JSP pages wired to app.js"
```
