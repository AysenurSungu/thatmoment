# AGENTS.md (Mobile)

## Source of truth
- Follow `MOBILE-STANDARDS.md` strictly. If there is a conflict, this file + MOBILE-STANDARDS.md wins.

## Scope rules
- Do not introduce fragile workarounds:
  - no platform-specific hacks without guarding
  - no hardcoded endpoints
  - no skipping runtime permission flows
  - no disabling lint/type checks

## Change size / approach
- Small diffs, step-by-step.
- If a change impacts navigation/state, propose a plan first.

## Commands to run (must be green)
- `pnpm lint` (if exists)
- `pnpm test` (if exists)
- `pnpm type-check` (if exists)
- `npx expo start` (or project’s standard start command)

## Definition of done
- App starts
- Critical flows not broken
- MOBILE-STANDARDS.md respected
