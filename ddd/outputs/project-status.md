# Library DDD Lab — Project Status

_Last updated: 2026-08-07_

## Goal

Learn Domain-Driven Design hands-on by building a small Library application in Java/Spring Boot. The project is split into bounded contexts (`catalog`, `lending`, `member`) under `com.anazcom.labingddd`, each following the same internal shape: a framework-free `domain` package (aggregates, value objects, repository ports, domain exceptions), an `application` package for use cases, and an `infrastructure` package for JPA/REST adapters. An ArchUnit test enforces that `domain` packages never import framework code.

## Done

**Catalog (Book) — closed end to end**
- `Book` aggregate root, `BookId`/`Isbn` value objects, `BookConstrains` (shared length limits used by both domain validation and JPA `@Column(length=...)`, keeping the two in sync).
- Persistence: `BookJpaEntity`, `SpringDataBookRepository`, `BookRepositoryAdapter` — named unique constraint (`uk_book_isbn`), `DataIntegrityViolationException` translated to a domain `BookRepositoryException` with cause chain preserved.
- REST: `GET /books/{id}` in `BookController`, backed directly by `BookRepository` (no use case needed — pure read).
- Full `MockMvc` test suite using a fake repository registered via `@TestConfiguration` (no mocks).

**Lending (Loan) — domain modeled, one use case implemented, not yet persisted**
- `Loan` aggregate, `LoanId`, `LoanPeriod` (value object, validates `startDate <= dueDate`), `LoanStatus` enum (`ACTIVE`, `RETURNED`, `OVERDUE`). `isOverdue()` is intentionally no-arg (uses `LocalDate.now()` internally) — deliberate choice to avoid callers controlling "today."
- `BorrowBookUseCase`: orchestrates `Book`/`Member`/`Loan` repositories, full fake-based test suite (`InMemory*Repository`).

**Member — domain modeled only, not yet persisted**
- `Member` aggregate, `MemberId`, `Email` (value object with a corrected validation regex), `MembershipStatus`, `suspend()`/`reactivate()`/`canBorrow()`.

**Cross-cutting**
- `shared.domain.Ids` — shared non-blank validation for all ID value objects.
- `shared.domain.DomainValidationException` / `DomainStateException` — replaced raw `IllegalArgumentException`/`IllegalStateException` across the whole domain, so the domain layer stays framework-free while still expressing *why* something failed.
- `shared.infrastructure.GlobalRestExceptionHandler` (`@RestControllerAdvice`) — translates domain exceptions to `ProblemDetail` responses: `BookNotFoundException` → 404, `DomainValidationException` → 400.
- `ArchitectureTests` (ArchUnit) — fails the build if anything in `..domain..` depends on Spring/Jakarta persistence packages.
- 12 tests passing across `BookControllerTest`, `ArchitectureTests`, `EmailTest`, `BorrowBookUseCaseTest`.

## Pending / In Progress

- **`LoanJpaEntity` is a bare skeleton** (`@Entity` + `id` only). Needs:
  - `LoanStatus` → `@Enumerated(EnumType.STRING)` directly on the domain enum type (no extra class needed).
  - `LoanPeriod` → decision not yet made: flatten to two plain columns (consistent with how `Isbn` was mapped) vs. a proper `@Embeddable`/`@Embedded`.
  - `bookId`/`memberId` → plain string columns, deliberately **not** `@ManyToOne`, to keep `Loan` from navigating into other aggregates.
- **Foreign key enforcement — open design decision.** Established that JPA has no `@Table(foreignKeys=...)`; a named FK constraint requires either:
  1. A "shadow" read-only `@ManyToOne`/`@JoinColumn(foreignKey=...)` alongside the plain ID column (DB constraint, no real ORM navigation used in code), or
  2. Dropping Hibernate auto-DDL for a hand-written `schema.sql` (`ddl-auto=none`), or
  3. Not enforcing it at the DB level at all, relying instead on an application-level guard (e.g. a future "remove book" use case checking `hasActiveLoanForBook` before allowing deletion) — the more idiomatic DDD answer, since cross-aggregate invariants are an application responsibility, not something the aggregate or schema guarantees for you.
  - Leaning toward doing **both**: an application-level guard as the real rule, plus a DB constraint as a fail-fast backstop, since this project is a single shared H2 database. Not yet implemented.
- `LoanRepositoryAdapter` / `SpringDataLoanRepository` — not created.
- `MemberJpaEntity` / `MemberRepositoryAdapter` — not created; `Member` has no persistence path yet.
- No `POST /loans` endpoint wired to `BorrowBookUseCase` yet.
- `GlobalRestExceptionHandler` doesn't yet handle `DomainStateException` or `MemberNotFoundException` — both are thrown by `BorrowBookUseCase` and will surface as unhandled 500s the moment an endpoint calls it.
- No "remove/delete book" use case yet (the use case that would actually need the cross-aggregate guard discussed above).
- Cosmetic: `BookConstrains` should be `BookConstraints` (missing "t") — known, not fixed.
