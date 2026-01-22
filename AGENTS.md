# AGENTS.md (Backend)

## Source of truth
- Follow `BE-STANDARDS.md` strictly. If there is a conflict, this file + BE-STANDARDS.md wins.

## Scope rules
- Only change files necessary for the requested task.
- Do NOT introduce "temporary" hacks that block the project later:
    - no hardcoded secrets/URLs
    - no disabling security checks
    - no TODO left as a workaround
    - no silent catch / swallow exceptions

## Change size / approach
- Prefer small, reviewable diffs.
- If the solution touches many modules, split into steps/PRs.

## Commands to run (must be green)
- `./mvnw test`
- (if exists) `./mvnw -q spotless:check` or `./mvnw checkstyle:check`

## Definition of done
- Tests pass
- No new warnings introduced
- Code matches BE-STANDARDS.md
- Clear commit message suggestion included in the response
