# Library DDD Lab — Project Status

_Last updated: 2026-08-13_

## Goal

Learn Domain-Driven Design hands-on by building a small Library application in Java/Spring Boot. The project is split into bounded contexts (`catalog`, `lending`, `member`) under `com.anazcom.labingddd`, each following the same internal shape: a framework-free `domain` package (aggregates, value objects, repository ports, domain exceptions), an `application` package for use cases, and an `infrastructure` package for JPA/REST adapters. An ArchUnit test enforces that `domain` packages never import framework code.

## Done

**Catalog (Book) — closed end to end**
- `Book` aggregate root, `BookId`/`Isbn` value objects, `BookConstrains` (shared length limits used by both domain validation and JPA `@Column(length=...)`, keeping the two in sync).
- Persistence: `BookJpaEntity`, `SpringDataBookRepository`, `BookRepositoryAdapter` — named unique constraint (`uk_book_isbn`), `DataIntegrityViolationException` translated to a domain `BookRepositoryException` with cause chain preserved.
- REST: `GET /books/{id}` in `BookController`, backed directly by `BookRepository` (no use case needed — pure read).
- Full `MockMvc` test suite using a fake repository registered via `@TestConfiguration` (no mocks).

**Lending (Loan) — borrowing flow closed end to end; return flow not started**
- `Loan` aggregate, `LoanId`, `LoanPeriod` (value object, validates `startDate <= dueDate`), `LoanStatus` enum (`ACTIVE`, `RETURNED`, `OVERDUE`). `isOverdue()` is intentionally no-arg (uses `LocalDate.now()` internally) — deliberate choice to avoid callers controlling "today." `markReturned()` exists on the aggregate but nothing calls it yet.
- **Cross-context access via lending-owned ports, not other contexts' repositories.** `BorrowBookUseCase` depends on `BookCatalog` and `MemberDirectory` (interfaces defined in `lending.domain`), not on `catalog.domain.BookRepository`/`member.domain.MemberRepository` directly. `BookCatalogAdapter` and `MemberDirectoryAdapter` (in `lending.infrastructure`) translate from the other contexts' repositories — `MemberDirectoryAdapter` maps a `Member` down to a `MemberBorrowingProfile(memberId, canBorrow)` record, an anti-corruption layer so lending only sees the slice of Member it needs.
- New domain exceptions: `BookAlreadyOnLoanException`, `MemberCannotBorrowException`.
- `BorrowBookUseCase`: checks the book exists, the member can borrow, and the book has no active loan (`hasActiveLoanForBook`) before creating a `Loan`. Full fake-based test suite (`InMemory*` fakes for `BookCatalog`, `MemberDirectory`, `LoanRepository`), 5 tests.
- Persistence: `LoanJpaEntity` is fully mapped — `LoanStatus` via `@Enumerated(EnumType.STRING)` on the domain enum directly, `LoanPeriod` flattened into plain `start_date`/`due_date` columns (the flatten-vs-`@Embeddable` decision was resolved in favor of flattening, consistent with how `Isbn` is mapped), `bookId`/`memberId` as plain string columns (deliberately not `@ManyToOne`). `LoanRepositoryAdapter` + `SpringDataLoanRepository` (with `existsByBookIdAndStatus`) implement `LoanRepository`; `DataIntegrityViolationException` translated to `DomainRepositoryException`.
- REST: `POST /loans` wired to `BorrowBookUseCase` via `LoanController`. `GET /loans/{id}` reads directly from `LoanRepository` (same pure-read shape as `BookController`, no use case needed), 404 via a new `LoanNotFoundException`. Full `MockMvc` test suite (`LoanControllerTest`, 7 tests: 200/400/400/409/409 on the borrow flow, 404 on an unknown loan, 200 on a found loan) — same no-mocks style as `BookControllerTest`, but since `@WebMvcTest` only boots the web slice (it won't auto-create the `@Service` `BorrowBookUseCase` or find bean definitions for the `BookCatalog`/`MemberDirectory`/`LoanRepository` ports), the test's `@TestConfiguration` explicitly declares `InMemoryBookCatalog`/`InMemoryMemberDirectory`/`InMemoryLoanRepository` as beans and wires a real `BorrowBookUseCase` on top of them. The three `InMemory*` fakes (originally written for `BorrowBookUseCaseTest`) gained a `clear()` method so they can be reset per test and reused across both test classes.

**Member — domain modeled only, not yet persisted**
- `Member` aggregate, `MemberId`, `Email` (value object with a corrected validation regex), `MembershipStatus`, `suspend()`/`reactivate()`/`canBorrow()`.
- `MemberRepository` port defined in `member.domain`, but no `MemberJpaEntity`/adapter — the only implementation is the in-memory test fake, consumed indirectly through `MemberDirectoryAdapter`.

**Cross-cutting**
- `shared.domain.Ids` — shared non-blank validation for all ID value objects.
- `shared.domain.DomainValidationException` / `DomainStateException` — replaced raw `IllegalArgumentException`/`IllegalStateException` across the whole domain, so the domain layer stays framework-free while still expressing *why* something failed.
- `shared.infrastructure.GlobalRestExceptionHandler` (`@RestControllerAdvice`) — translates domain exceptions to `ProblemDetail` responses: `BookNotFoundException`/`MemberNotFoundException`/`LoanNotFoundException` → 404, `BookAlreadyOnLoanException`/`MemberCannotBorrowException` → 409, `DomainValidationException`/`DomainStateException` → 400, `DomainRepositoryException` and any other uncaught `Exception` → 500.
- `ArchitectureTests` (ArchUnit) — fails the build if anything in `..domain..` depends on Spring/Jakarta persistence packages.
- 19 tests passing: `ArchitectureTests` (2), `BookControllerTest` (2), `EmailTest` (3), `BorrowBookUseCaseTest` (5), `LoanControllerTest` (7).

## Pending / In Progress

- **No "return book" use case.** `Loan.markReturned()` exists on the aggregate but is currently unreachable from any use case or endpoint.
- **Foreign key enforcement — decision resolved: no DB-level FK, application-level guard only.** `Loan → Book`/`Loan → Member` cross both aggregate and bounded-context boundaries, and cross-aggregate invariants are a DDD application-layer responsibility, not something the schema should enforce — so no `@ManyToOne`/`@JoinColumn(foreignKey=...)` shadow mapping and no hand-written `schema.sql` are planned. Not implemented yet because there's nothing to guard: no "remove book" use case exists at all (see below), so no path can currently orphan a `Loan`.
  - **Known tradeoff of skipping the DB backstop:** once a "remove book" use case exists, a naive check-then-act guard (`hasActiveLoanForBook` before delete) has a TOCTOU race — a `Loan` could be created between the check and the delete, with no FK to catch it. Not closing this race for now (acceptable for a single-user learning app); revisit if it ever matters. Options considered, for when it does:
    1. Push the invariant into the `Book` aggregate itself (e.g. an `onLoan`/`available` flag flipped transactionally by `BorrowBookUseCase`, possibly via a `LoanCreatedEvent` handled synchronously in the same transaction) — closes the race, but adds a second source of truth that must stay in sync with `Loan` state.
    2. Wrap the cross-aggregate check + delete in one DB transaction with explicit locking (`SERIALIZABLE` isolation or `SELECT ... FOR UPDATE` on the relevant `Loan` rows) — a per-operation concurrency control, not a permanent declarative constraint, so it doesn't conflict with the no-FK stance.
  - **Decision for the "remove book" use case when it's built:** query `hasActiveLoanForBook` directly, mirroring the existing `BookCatalog`/`MemberDirectory` ACL pattern in the other direction — a `LoanLookup`-style port owned by `catalog.domain`, implemented by an adapter delegating to `lending.LoanRepository`. No denormalized flag, no events, no lock tuning — simplest correct option, revisit only if the race above turns out to matter in practice.
- `MemberJpaEntity` / `MemberRepositoryAdapter` — not created; `Member` has no persistence path yet.
- No "remove/delete book" use case yet — the use case that would actually introduce the cross-aggregate guard discussed above. Nothing in `catalog` today can delete a `Book`.
- Cosmetic: `BookConstrains` should be `BookConstraints` (missing "t") — known, not fixed.
