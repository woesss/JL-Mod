# Contributing to JL-Mod Plus

Thank you for helping improve JL-Mod Plus. The project accepts issues and pull requests while keeping releases owner-controlled.

## Before you start

1. Search existing issues and pull requests.
2. Open an issue before a large or compatibility-sensitive change.
3. Never include proprietary games, ROMs, firmware, signing material, credentials, or code copied from an incompatible source.

By submitting a contribution, you agree that it may be distributed under the repository's Apache License 2.0. Third-party code retains its original license and must be identified clearly.

## Set up the repository

Requirements:

- JDK 17
- Android SDK 34
- Android NDK `22.1.7171670`
- Git with submodule support

Clone and initialize dependencies:

```shell
git clone --recurse-submodules https://github.com/H3nb/JL-Mod-Plus.git
cd JL-Mod-Plus
git remote add upstream https://github.com/woesss/JL-Mod.git
```

On Windows, use a checkout path without spaces because the NDK version used by this project does not reliably handle them.

## Branch and pull-request workflow

Use one issue or goal, one branch, and one pull request:

1. Update `dev` and create a branch from it.
2. Use `feature/`, `fix/`, `docs/`, or another descriptive branch prefix.
3. State the user-visible outcome before editing and keep the change focused.
4. Build and test locally, then inspect the diff for unrelated changes and secrets.
5. Open a pull request targeting `dev`, not `master`.
6. Wait for green CI, resolve review conversations, then squash-merge and delete the branch.

The `master` branch is reserved for owner-approved releases. The locked `baseline` branch is read-only upstream reference code; never branch normal feature work from it and never merge into it.

Maintainers using AI assistance should also follow the [beginner-friendly maintenance workflow](docs/BEGINNER_WORKFLOW.md).

## Commit messages

Use a short Conventional Commit subject in English:

```text
feat: add per-game display scaling
fix: avoid pointer truncation in M3G cache
docs: explain release signing
ci: verify pull requests on arm64 debug
```

Recommended types are `feat`, `fix`, `docs`, `refactor`, `test`, `build`, `ci`, and `chore`. Keep one logical concern per commit. Explain the reason and compatibility impact in the commit body when the subject is not enough.

## Verification

Development and test builds intentionally target `arm64-v8a` only:

```powershell
.\gradlew.bat :app:assembleEmulatorDebug
git diff --check
git status --short
```

Do not commit APKs, build directories, IDE state, local SDK paths, keystores, passwords, or generated native intermediates.

Do not change `version.properties` for ordinary development commits. Change it only while intentionally preparing a public release, following `docs/VERSIONING.md` and `docs/RELEASING.md`.

## AI-assisted work

AI-assisted contributions are welcome. AI should explain important terms and provide verification evidence in plain language. Before merging, the contributor should still confirm:

- the requested behavior and risks are understandable;
- the affected behavior was tested rather than trusted from generated code alone;
- disclosing substantial AI assistance in the pull-request description;
- confirming that generated or copied material does not violate third-party copyright or licenses.
