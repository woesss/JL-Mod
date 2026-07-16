# Release checklist

Only H3NB publishes JL-Mod Plus releases. Public releases are signed universal APKs created from the protected `master` branch. Read [VERSIONING.md](VERSIONING.md) before changing the version.

## Release model

- `dev` contains ongoing work.
- `baseline` is a locked troubleshooting snapshot of the original upstream `dev`; never release or merge from it.
- `master` contains only owner-approved release candidates.
- `version.properties` is the single source of the public app version.
- A public tag must exactly match that file, for example `versionName=0.1.0` uses tag `v0.1.0`.

The first planned JL-Mod Plus public release is `0.1.0`. Never move or overwrite a published version tag. Publish a higher patch version when a release needs correction.

## Prepare on `dev`

1. Merge the intended focused changes into `dev` through pull requests with green CI.
2. Decide whether the release is a patch, minor, or major change using `docs/VERSIONING.md`.
3. Change `versionName` in `version.properties` once, in a dedicated release-preparation change.
4. Update user-facing documentation, credits, and third-party notices when needed.
5. Confirm the continuous development build passes.
6. Build and smoke-test on a real `arm64-v8a` device.
7. Confirm the active keystore and its passwords have encrypted backups outside both the development PC and GitHub Secrets.
8. Open an owner-approved pull request from `dev` to `master` and wait for CI.

For the first release, the version file should contain:

```properties
versionName=0.1.0
```

## Rehearse without publishing

Before creating a tag, build the same release variant locally:

```powershell
.\gradlew.bat :app:assembleEmulatorRelease
```

Verify that the APK installs, launches, uses package ID `io.github.h3nb.jlmodplus`, contains every supported ABI, and retains app data when installed over the preceding JL-Mod Plus release. A rehearsal must not create or move a Git tag.

## Create the draft release

After `master` contains the exact release commit:

```powershell
git switch master
git pull --ff-only origin master
git tag -a v0.1.0 -m "Release v0.1.0"
git push origin v0.1.0
```

The release workflow then:

1. confirms the tag is `vMAJOR.MINOR.PATCH`;
2. confirms the tag matches `version.properties`;
3. confirms the commit is contained in `origin/master`;
4. builds with JDK 17 and the configured signing secrets;
5. verifies the APK signature, package ID, certificate fingerprint, and supported ABIs; and
6. creates a draft GitHub Release with the APK and symbols.

## Verify and publish

Before publishing the draft:

1. Confirm the workflow succeeded without ignored failures.
2. Download and smoke-test the exact APK attached to the draft.
3. Install it over the previous JL-Mod Plus release and verify user data remains accessible.
4. Test launch, MIDlet import, one 2D game, one 3D game, audio, controls, and storage access.
5. Review the generated release notes, version number, and attached symbols.
6. Publish the draft manually from GitHub.

GitHub Secrets cannot be read back and are not a signing-key backup. Losing the keystore or passwords prevents future APKs from updating existing JL-Mod Plus installations.
