# Beginner-friendly maintenance workflow

This workflow is designed for a solo hobby maintainer using AI assistance. Its purpose is to make experiments recoverable and understandable after a long break, not to imitate a large company process.

## Normal change loop

1. **State one outcome.** Describe what should become different for the user. Avoid combining an unrelated cleanup, dependency upgrade, and feature in one task.
2. **Start from current `dev`.** Create a short-lived branch such as `feature/display-scaling` or `fix/midi-crash`. Never develop directly on `master` or `baseline`.
3. **Ask for an explanation before risky edits.** For native code, storage, save data, signing, permissions, dependencies, or CI, require the AI to explain the risk in plain language.
4. **Make the smallest coherent patch.** Preserve unrelated changes and avoid speculative refactors.
5. **Verify behavior.** Run the narrow relevant check and the arm64 debug build. A successful compile does not replace a manual test of the changed behavior.
6. **Inspect the diff.** Confirm no secrets, APKs, local paths, build output, unexplained dependency changes, or unrelated rewrites appeared.
7. **Open a pull request to `dev`.** Record the outcome, risk, verification evidence, and any remaining uncertainty.
8. **Wait for green CI.** Read failures; do not repeatedly ask AI to change unrelated code until the actual cause is identified.
9. **Squash-merge and delete the branch.** One pull request becomes one understandable commit on `dev`.

## Useful local checks

Run commands from the repository root:

```powershell
.\gradlew.bat :app:tasks --all --offline
.\gradlew.bat :app:lintEmulatorDebug
.\gradlew.bat :app:assembleEmulatorDebug
git diff --check
git status --short
```

Use only the checks relevant to the change while iterating. Before a pull request, the debug build and `git diff --check` are the normal minimum. CI repeats Android lint, the arm64 debug build, and crash-reporter checks.

The tracked `app/lint-baseline.xml` is a snapshot of inherited lint findings. When lint reports a new problem, fix or understand that problem; do not regenerate the baseline simply to turn the check green.

## Troubleshooting with the baseline

When new behavior looks suspicious, compare against `baseline` before assuming the latest patch caused it:

```powershell
git diff baseline...HEAD -- path/to/suspected/file
git log --oneline baseline..HEAD -- path/to/suspected/file
```

If practical, build `baseline` in detached-head mode and reproduce the same test. A problem present on `baseline` is inherited upstream behavior; a problem absent there was introduced somewhere in the fork's changes. See [BASELINE.md](BASELINE.md).

## When to change the app version

Do not change the version for each branch or pull request. Update `version.properties` only when deliberately preparing a public release. Use [VERSIONING.md](VERSIONING.md) to choose patch versus minor, then follow [RELEASING.md](RELEASING.md).

## Stop conditions

Pause and ask for a clearer explanation when any of these occurs:

- the proposed change deletes user data or changes its format;
- the AI cannot explain why a dependency or permission is needed;
- verification requires disabling a safety check;
- the diff is much larger than the requested outcome;
- a signing key, password, token, or private path appears in tracked files;
- CI fails for a reason that has not been identified;
- the only proposed recovery is force-pushing or rewriting shared history.

It is acceptable to stop with a documented experiment branch. It is safer than merging an unexplained change into `dev`.

Public project documentation belongs in `docs/`. Local notes may use the ignored `docs/private/`, `docs/local/`, or `*.private.md` paths, but ignored files are not encrypted. Passwords, tokens, signing credentials, and recovery codes belong in an encrypted password manager or backup, never in documentation.
