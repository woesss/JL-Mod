# AGENTS.md

## Scope

These instructions apply to the entire repository. More specific `AGENTS.md` files may override them for their own subdirectories.

## Project overview

JL-Mod Plus is an Android J2ME emulator fork based on `woesss/JL-Mod`.

- `app/` contains the Android application, Java/Kotlin code, resources, and native C/C++ code.
- `dexlib/` contains the Android library used for DEX-related functionality.
- `app/src/main/cpp/mmapi_tsf/TinySoundFont` is a Git submodule.
- `dev` is the development branch and `master` is the release branch inherited from upstream.
- `origin` must point to `H3nb/JL-Mod-Plus`; `upstream` must point to `woesss/JL-Mod`.

## Required toolchain

- JDK 17 for normal and CI builds.
- Android SDK 34.
- Android NDK `22.1.7171670`.
- The checked-in Gradle Wrapper; do not replace it with a system Gradle installation.
- Initialize dependencies with `git submodule update --init --recursive` after cloning.

On Windows, keep the checkout path free of spaces because NDK 22 `ndk-build` cannot reliably process paths such as `JL-Mod Plus`. Prefer a path such as `D:\03_Projects\JL-Mod-Plus` or use a temporary drive mapping while migrating the checkout.

## Build and verification

Run commands from the repository root.

- Windows debug APK: `.\gradlew.bat :app:assembleEmulatorDebug`
- Unix debug APK: `./gradlew :app:assembleEmulatorDebug`
- Gradle configuration check: `.\gradlew.bat :app:tasks --all --offline`
- Before committing: `git diff --check` and `git status --short`

Use the existing `GRADLE_USER_HOME` configured by the developer. Do not commit `.gradle/`, `build/`, `.cxx/`, APKs, local SDK paths, signing files, or credentials.

## Compatibility rules

- Keep `applicationId = "io.github.h3nb.jlmodplus"` so JL-Mod Plus can be installed alongside upstream JL-Mod. Source-code package names do not need to match the application ID.
- Keep the user-facing app name `JL-Mod Plus` and the Gradle-safe project name `JL-Mod-Plus`.
- Minimum Android API is 16 because NDK 22 does not support native builds for API 14–15.
- Development and testing builds must target `arm64-v8a` only to keep local and CI build times and resource usage low.
- Release builds intended for distribution must produce a universal APK containing every ABI supported by the project. Do not treat an `arm64-v8a`-only APK as a public release artifact.
- Preserve upstream attribution, licensing, translations, and existing data formats.
- Treat native code, storage paths, emulator profiles, and save-data compatibility as high-risk areas.

## Git workflow

- Fetch upstream changes with `git fetch upstream --prune`.
- Review upstream changes before integrating them into `dev`.
- Push normal work to `origin`, never to `upstream`.
- Do not force-push shared branches unless the user explicitly requests it.
- Start normal work from `dev`. Human branches should use a descriptive prefix such as `feature/`, `fix/`, or `docs/`; Codex-created branches use `codex/`.
- Use Conventional Commit subjects in English: `feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `build:`, `ci:`, or `chore:` followed by a concise imperative description.
- Keep one logical concern per commit. Do not mix generated output, unrelated cleanup, or another contributor's work into the same commit.
- Before staging, inspect `git diff` and `git status --short`. Stage explicit paths instead of using `git add .` in a dirty worktree.
- Before committing, run the smallest relevant verification plus `git diff --cached --check`. Never commit secrets, signing material, local paths, or APKs.
- Examples: `fix: avoid pointer truncation in M3G cache`, `docs: document release signing`, and `ci: verify pull requests on arm64 debug`.
- Preserve unrelated user changes in a dirty working tree.

Pull requests target `dev`. The `master` branch is release-only. Use semantic versions without a fork suffix (for example `v0.87.1`), and create release tags only from commits contained in `master`. Only H3NB publishes releases.

## GitHub Actions

- `.github/workflows/nightly.yml` builds an installable debug APK from `dev` and updates the continuous development release without production signing secrets.
- `.github/workflows/ci.yml` verifies pull requests to `dev` without write permissions or release side effects.
- `.github/workflows/android.yml` builds a signed universal release APK for semantic-version tags created from `master`; it requires `SIGNING_KEY` and `KEYSTORE_PROPERTIES` repository secrets and should fail early when they are absent.
- Keep workflow permissions minimal. Release-producing workflows require only `contents: write` unless a new feature clearly needs more.
- Follow `docs/SIGNING.md` for release-key handling. Never commit the keystore or real passwords.

## Change discipline

- Prefer small, reviewable patches.
- Use `rg` for repository searches.
- Do not change package names, versioning, signing, SDK levels, or branch policy silently.
- When a full Android build is unavailable, state exactly which lighter checks passed and what remains unverified.
