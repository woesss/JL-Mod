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

- Keep `applicationId = "ru.woesss.j2meloader"` unless a migration is explicitly requested and planned.
- Keep the user-facing app name `JL-Mod Plus` and the Gradle-safe project name `JL-Mod-Plus`.
- Minimum Android API is 16 because NDK 22 does not support native builds for API 14–15.
- APK builds intentionally target `arm64-v8a` only. Do not restore other ABIs or a universal APK unless explicitly requested.
- Preserve upstream attribution, licensing, translations, and existing data formats.
- Treat native code, storage paths, emulator profiles, and save-data compatibility as high-risk areas.

## Git workflow

- Fetch upstream changes with `git fetch upstream --prune`.
- Review upstream changes before integrating them into `dev`.
- Push normal work to `origin`, never to `upstream`.
- Do not force-push shared branches unless the user explicitly requests it.
- Keep commits focused and do not mix generated build output with source changes.
- Preserve unrelated user changes in a dirty working tree.

## GitHub Actions

- `.github/workflows/nightly.yml` builds an installable debug APK from `dev` and updates the continuous development release without production signing secrets.
- `.github/workflows/android.yml` builds signed release APKs from `master`; it requires `SIGNING_KEY` and `KEYSTORE_PROPERTIES` repository secrets and should fail early when they are absent.
- Keep workflow permissions minimal. Release-producing workflows require only `contents: write` unless a new feature clearly needs more.
- Follow `docs/SIGNING.md` for release-key handling. Never commit the keystore or real passwords.

## Change discipline

- Prefer small, reviewable patches.
- Use `rg` for repository searches.
- Do not change package names, versioning, signing, SDK levels, or branch policy silently.
- When a full Android build is unavailable, state exactly which lighter checks passed and what remains unverified.
