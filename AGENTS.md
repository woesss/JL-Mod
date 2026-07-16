# AGENTS.md

## Scope

These instructions apply to the entire repository. More specific `AGENTS.md` files may override them for their own subdirectories.

## Project overview

JL-Mod Plus is an Android J2ME emulator fork based on `woesss/JL-Mod`.

- `app/` contains the Android application, Java/Kotlin code, resources, and native C/C++ code.
- `dexlib/` contains the Android library used for DEX-related functionality.
- `app/src/main/cpp/mmapi_tsf/TinySoundFont` is a Git submodule.
- `dev` is the development branch and `master` is the release branch.
- `baseline` is a locked, immutable snapshot of upstream `dev` at commit `f723a190c0bdb44b31c3bc0ead6f8665c7ea517d`; use it only for troubleshooting comparisons and never merge into or release from it.
- `origin` must point to `H3nb/JL-Mod-Plus`; `upstream` must point to `woesss/JL-Mod`.

## Maintainer context and project intent

This is a personal hobby project maintained primarily through AI-assisted "vibe coding." The maintainer has no formal software-development background and is not yet comfortable writing or reviewing code independently. Familiarity with a technical term must not be treated as proof that the maintainer understands its meaning, consequences, or implementation details.

- Explain important terms, architecture, risks, and tradeoffs in plain language when they become relevant. Briefly connect each explanation to this project instead of giving an abstract textbook definition.
- Lead with the practical outcome: what will change for the app or its users, why it is needed, and how the result will be checked.
- Make a clear recommendation when several approaches are possible. Mention alternatives only when they represent a meaningful difference in behavior, maintenance cost, compatibility, or risk.
- Do not rely on the maintainer to catch a bug by reading a patch. Inspect the relevant code, run appropriate checks, and report evidence that a change works.
- Distinguish confirmed facts, reasonable assumptions, and unverified guesses. If verification is unavailable, say exactly what remains uncertain and give a simple manual test when useful.
- Ask for a decision before an irreversible, destructive, security-sensitive, or product-defining action. For low-risk implementation details, use a reasonable assumption, state it briefly, and continue.

JL-Mod Plus is an experimental fork focused on modernization, exploration, and new features. It is not intended to replace the stable J2ME Loader or upstream JL-Mod applications for users who prioritize reliability. Public releases may be less stable, and development may pause or stop when the maintainer no longer has the time or interest.

Design changes accordingly:

- Prefer solutions that remain understandable, easy to revert, and inexpensive to maintain after a long break.
- Avoid unnecessary dependencies, services, abstractions, large rewrites, and infrastructure that requires continuous attention.
- Keep stable checkpoints so experimentation does not leave the repository in an unexplained or unbuildable state.
- Treat the project's experimental nature as permission to explore, not permission to silently break save data, storage behavior, security, licensing, or upstream compatibility.
- Document important non-obvious decisions close to the code or in the appropriate project documentation so a future maintainer or AI can recover the reasoning.

## AI-assisted development workflow

For change requests, use a beginner-friendly, evidence-based workflow:

1. Inspect the current implementation and repository state before editing. Preserve unrelated work.
2. Restate the intended user-visible result and flag material risks or assumptions in concise language.
3. Implement the smallest coherent change that achieves the goal. Avoid unrelated cleanup and speculative improvements.
4. Verify with the narrowest relevant automated checks first, then expand verification when the affected area is high-risk.
5. Review the resulting diff for accidental changes, generated artifacts, secrets, and compatibility impact.
6. Hand off with a plain-language summary of what changed, what was verified, what remains unverified, and the safest next step.

When something fails, explain whether the failure was introduced by the current change, already existed, or is caused by the local environment. Do not present warnings as successful verification, and do not claim that a build or feature works unless the corresponding check actually passed.

## Independent multi-agent review

The primary agent owns the implementation, first-party verification, review synthesis, and final technical recommendation. External AI reviewers are advisory: their agreement is not proof, their output may contain false positives, and they must not decide or modify the result. The maintainer and primary agent retain final authority, with the maintainer deciding any irreversible, destructive, security-sensitive, release, compatibility, or product-defining action.

Use independent review in proportion to risk:

- For non-trivial changes that affect runtime behavior, multiple files, dependencies, build or release logic, native code, storage, emulator profiles, save data, permissions, security, or compatibility, run both OpenCode and Antigravity CLI reviews after the implementation and initial focused checks are complete.
- For typo-only, formatting-only, or obviously mechanical documentation changes, the primary agent may skip external review. State that it was skipped and why in the handoff.
- If either CLI is unavailable, unauthenticated, rate-limited, or repeatedly times out, do not block safe progress indefinitely. Perform the primary-agent review, report the missing perspective, and never represent the unavailable review as completed.

Review a stable target:

1. Stop implementation edits before starting external reviews. All reviewers must inspect the same working-tree diff or commit.
2. Record `git status --short` and inspect the diff before invoking reviewers. Preserve unrelated user changes.
3. Give each reviewer the task goal, acceptance criteria, relevant constraints, and review scope, but do not give it another reviewer's conclusions. Independent prompts reduce correlated agreement.
4. Run reviewers in read-only or planning mode. They must not edit tracked files, stage, commit, push, reset, switch branches, update submodules, alter `baseline`, or run destructive commands.
5. After each review, check `git status --short` again and treat any unexpected tracked-file change as a review failure that must be investigated.

When the currently installed model names remain available, use these defaults from the repository root:

```powershell
opencode run --model "opencode/deepseek-v4-flash-free" --agent plan --format json --dir "<repo-root>" "<review-prompt>"
agy --model "Gemini 3.5 Flash (High)" --mode plan --add-dir "<repo-root>" --print-timeout 5m --print "<review-prompt>"
```

- Verify available names with `opencode models` and `agy models` instead of assuming model catalogs are permanent. If a default is unavailable, select the closest capable review model and disclose the substitution.
- Do not pass OpenCode `--auto` or Antigravity `--dangerously-skip-permissions` for review sessions.
- Prefer an absolute repository path for `--dir` and `--add-dir` on Windows.
- OpenCode `--format json` emits JSON event lines. Extract the text events or preserve the raw output for the primary agent to evaluate.

Every review prompt must instruct the reviewer to inspect only, avoid tracked-file changes, and return actionable findings with:

- severity (`P0` critical, `P1` high, `P2` normal, or `P3` low);
- confidence (`confirmed`, `plausible`, or `uncertain`);
- exact file and line or the narrowest relevant location;
- the failure mechanism and user or compatibility impact;
- concrete evidence and, when feasible, reproduction or verification steps;
- the smallest reasonable remediation;
- explicit confirmation when no actionable finding is found.

Reviewers should prioritize correctness, security, data and save compatibility, regressions, lifecycle and concurrency behavior, resource handling, Android API compatibility, native-boundary risks, and missing tests. Do not report subjective style preferences unless they create a concrete maintenance or correctness risk.

The primary agent must adjudicate the results rather than count votes:

1. Independently inspect each finding against the code and project rules.
2. Merge duplicates and note disagreements between reviewers.
3. Reproduce or otherwise validate material findings whenever practical. Classify unsupported claims as unverified or false positives rather than silently accepting them.
4. Implement only validated fixes that remain in scope. Ask the maintainer before expanding the task materially.
5. Re-run the narrowest relevant checks after fixes, then expand verification according to risk. A reviewer consensus never replaces build, test, lint, or manual runtime evidence.
6. Re-run targeted external review only when the fixes materially changed the risk surface; avoid endless review loops.
7. In the final handoff, summarize which reviewers ran, their model substitutions if any, validated findings, rejected or unresolved material claims, checks performed, and what still requires maintainer judgment or device testing.

## Required toolchain

- JDK 17 for normal and CI builds.
- Android SDK 34.
- Android NDK `22.1.7171670`.
- The checked-in Gradle Wrapper; do not replace it with a system Gradle installation.
- Initialize dependencies with `git submodule update --init --recursive` after cloning.

On Windows, keep the checkout path free of spaces because NDK 22 `ndk-build` cannot reliably process paths such as `JL-Mod Plus`. Prefer a generic path such as `D:\Projects\JL-Mod-Plus` or use a temporary drive mapping while migrating the checkout.

## Build and verification

Run commands from the repository root.

- Windows debug APK: `.\gradlew.bat :app:assembleEmulatorDebug`
- Unix debug APK: `./gradlew :app:assembleEmulatorDebug`
- Gradle configuration check: `.\gradlew.bat :app:tasks --all --offline`
- Before committing: `git diff --check` and `git status --short`

Use the existing `GRADLE_USER_HOME` configured by the developer. Do not commit `.gradle/`, `build/`, `.cxx/`, APKs, local SDK paths, signing files, or credentials.

`app/lint-baseline.xml` records inherited lint debt so CI can reject new findings. Do not regenerate it merely to make CI green; fix new findings, or document and review any intentional baseline update.

## Compatibility rules

- Keep `applicationId = "io.github.h3nb.jlmodplus"` so JL-Mod Plus can be installed alongside upstream JL-Mod. Source-code package names do not need to match the application ID.
- Keep the user-facing app name `JL-Mod Plus` and the Gradle-safe project name `JL-Mod-Plus`.
- Minimum Android API is 16 because NDK 22 does not support native builds for API 14–15.
- Development and testing builds must target `arm64-v8a` only to keep local and CI build times and resource usage low.
- Release builds intended for distribution must produce a universal APK containing every ABI supported by the project. Do not treat an `arm64-v8a`-only APK as a public release artifact.
- `version.properties` is the single source of the JL-Mod Plus public version. Start at `0.1.0`, follow `docs/VERSIONING.md`, and do not reuse the upstream JL-Mod version as the fork's version.
- Preserve upstream attribution, licensing, translations, and existing data formats.
- Treat native code, storage paths, emulator profiles, and save-data compatibility as high-risk areas.

## Git workflow

- Fetch upstream changes with `git fetch upstream --prune`.
- Review upstream changes before integrating them into `dev`.
- Never move, delete, merge into, or otherwise update `baseline`. Create a separately named documented snapshot if another upstream baseline is needed later.
- Push normal work to `origin`, never to `upstream`.
- Do not force-push shared branches unless the user explicitly requests it.
- Start normal work from `dev`. Human branches should use a descriptive prefix such as `feature/`, `fix/`, or `docs/`; Codex-created branches use `codex/`.
- Use Conventional Commit subjects in English: `feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `build:`, `ci:`, or `chore:` followed by a concise imperative description.
- Keep one logical concern per commit. Do not mix generated output, unrelated cleanup, or another contributor's work into the same commit.
- Before staging, inspect `git diff` and `git status --short`. Stage explicit paths instead of using `git add .` in a dirty worktree.
- Before committing, run the smallest relevant verification plus `git diff --cached --check`. Never commit secrets, signing material, local paths, or APKs.
- Examples: `fix: avoid pointer truncation in M3G cache`, `docs: document release signing`, and `ci: verify pull requests on arm64 debug`.
- Preserve unrelated user changes in a dirty working tree.

Pull requests target `dev`. The `master` branch is release-only. Use semantic versions that exactly match `version.properties` (for example `v0.1.0`), and create release tags only from commits contained in `master`. Only H3NB publishes releases.

## GitHub Actions

- `.github/workflows/nightly.yml` builds an installable debug APK from `dev` and updates the continuous development release without production signing secrets.
- `.github/workflows/ci.yml` verifies pull requests to `dev` and `master` without write permissions or release side effects.
- `.github/workflows/android.yml` builds a signed universal release APK for semantic-version tags created from `master`; it requires `SIGNING_KEY` and `KEYSTORE_PROPERTIES` repository secrets and should fail early when they are absent.
- Keep workflow permissions minimal. Release-producing workflows require only `contents: write` unless a new feature clearly needs more.
- Follow `docs/SIGNING.md` for release-key handling. Never commit the keystore or real passwords.

## Change discipline

- Prefer small, reviewable patches.
- Use `rg` for repository searches.
- Do not change package names, versioning, signing, SDK levels, or branch policy silently.
- When a full Android build is unavailable, state exactly which lighter checks passed and what remains unverified.
