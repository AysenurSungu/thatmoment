# AGENTS.md (Web Frontend)

## Source of truth
- Follow `FE-STANDARDS.md` strictly. If there is a conflict, this file + FE-STANDARDS.md wins.

## Scope rules
- Only modify what the task requires.
- Avoid short-term fixes that harm maintainability:
  - no `any`, no `@ts-ignore` unless explicitly approved
  - no hardcoded API base URLs (use env/config)
  - no bypassing auth/guards
  - no disabling lint/typecheck

## Change size / approach
- Keep diffs small and incremental.
- Prefer refactors in separate PRs from feature changes.

## Commands to run (must be green)
- `pnpm lint`
- `pnpm type-check`
- `pnpm test` (if exists)
- `pnpm build`

## Definition of done
- Lint/type-check/build pass
- FE-STANDARDS.md respected
- Minimal, consistent UI patterns
