# Template Fix Report

> **Date:** 2026-03-25  
> **Total Files Modified:** 6  
> **Total Fixes Applied:** 9  
> **Controllers Modified:** 0 (templates only)

---

## ✅ Applied Fixes

### Fix 1 — Prefix `/authen/` → `/auth/` (Rule 1)

#### FILE: `authentication/RegisterWithGoogle.html`

**BEFORE:**
```html
<form th:action="@{/authen/otp}" method="get" ...>
```

**AFTER:**
```html
<form th:action="@{/auth/otp}" method="get" ...>
```

---

#### FILE: `authentication/otp.html` (3 changes)

**BEFORE:**
```html
<form th:action="@{/authen/verifiedOtp}" method="post" ...>
<a th:href="@{/authen/registerEmail}" ...>
<form th:action="@{/authen/otp}" method="get" id="resendForm">
```

**AFTER:**
```html
<form th:action="@{/auth/verifiedOtp}" method="post" ...>
<a th:href="@{/auth/registerEmail}" ...>
<form th:action="@{/auth/otp}" method="get" id="resendForm">
```

---

#### FILE: `authentication/registerForm.html`

**BEFORE:**
```html
<form th:action="@{/authen/registerForm}" method="post" ...>
```

**AFTER:**
```html
<form th:action="@{/auth/registerForm}" method="post" ...>
```

---

### Fix 2 — Dead HTML Link (Rule 4)

#### FILE: `authentication/RegisterWithGoogle.html`

**Logo onclick:**
```diff
-onclick="window.location.href='customer_login.html'"
+onclick="window.location.href='/auth/login'"
```

**Footer link:**
```diff
-<a href="customer_login.html" ...>Đăng nhập</a>
+<a th:href="@{/auth/login}" ...>Đăng nhập</a>
```

---

### Fix 3 — Missing Endpoint (Rule 5)

#### FILE: `private/Staff_acc_create.html`

```diff
-<a th:href="@{/admin/accounts}" ...>
+<a th:href="@{/admin/account_list}" ...>
```
> `/admin/accounts` had no controller. Correct endpoint: `/admin/account_list` (`ViewUserListController`).

---

### Fix 4 — Controller URL Sync

#### FILE: `private/Feedback_detail.html`

```diff
-<form th:action="@{/admin/feedback/reply}" method="post">
+<form th:action="@{/staff/feedback/reply}" method="post">
```
> User changed `ReplyFeedbackController` from `@RequestMapping("/admin/feedback")` to `@RequestMapping("/staff/feedback")`. Template updated to match.

---

### Fix 5 — Context Path (Rule 2)

#### FILE: `public/authentication/login-customer.html`

```diff
-onclick="window.location.href='/book-now/home'"
+th:onclick="'window.location.href=\'' + @{/home} + '\''"
```

---

## ⏸️ Deferred (Requires Structural Change)

| File | Issue | Reason |
|------|-------|--------|
| `customer/check_in.html` | 3× hardcoded `/book-now/` in `<script>` | Missing `th:inline="javascript"` on `<script>` tag |
| `admin/staff-booking-updated-status.html` | 2× hardcoded `/book-now/` in `<script>` | Same — missing `th:inline="javascript"` |
| `error/401.html`, `403.html`, `404.html`, `500.html` | Hardcoded `/book-now/home` in `onclick` | No `xmlns:th` — not Thymeleaf-processed |
| `private/Admin_dashboard.html` | Hardcoded `/book-now/` in export JS | Missing `th:inline="javascript"` |
| `private/Room_list.html` | Hardcoded `/book-now/admin/list` in JS | Missing `th:inline="javascript"` |
| 12× dead `.html` links | `window.location.href = 'xxx.html'` | Embedded in commented-out code or static mockups |

> **To fix deferred items:** Add `th:inline="javascript"` to each `<script>` tag, then use `[[@{/path}]]` syntax. Error pages need `xmlns:th="http://www.thymeleaf.org"` on `<html>`.

---

## Summary

| Category | Applied | Deferred |
|----------|---------|----------|
| Prefix `/authen/` → `/auth/` | 5 | 0 |
| Missing endpoint | 1 | 0 |
| Dead HTML links | 2 | 12 (mostly commented-out) |
| Controller URL sync | 1 | 0 |
| Context path (HTML) | 1 | 0 |
| Context path (JS `<script>`) | 0 | 8 |
| Context path (error pages) | 0 | 4 |
| **Total** | **10** | **24** |
