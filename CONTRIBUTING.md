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

1. Update `dev` and create a branch from it.
2. Use `feature/`, `fix/`, `docs/`, or another descriptive branch prefix.
3. Keep the change focused and preserve unrelated work.
4. Build and test locally.
5. Open a pull request targeting `dev`, not `master`.

The `master` branch is reserved for owner-approved releases.

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

## AI-assisted work

AI-assisted contributions are welcome. The contributor is still responsible for:

- understanding and testing the submitted change;
- reviewing generated code for security and correctness;
- disclosing substantial AI assistance in the pull-request description;
- confirming that generated or copied material does not violate third-party copyright or licenses.
