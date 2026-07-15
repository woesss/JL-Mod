# Release checklist

Only H3NB publishes JL-Mod Plus releases. Public releases are distributed through GitHub Releases as signed universal APKs.

## Versioning

Use semantic versions without an additional fork suffix, for example `v0.87.1`, `v0.87.2`, and `v0.88.0`.

The release workflow removes the leading `v` for Android's `versionName` and calculates `versionCode` as:

```text
major * 1,000,000 + minor * 1,000 + patch
```

Never move or overwrite a published version tag. Publish a new patch version when a release needs correction.

## Prepare

1. Merge the intended changes into `dev` through reviewed pull requests.
2. Confirm the pull-request CI and the continuous development build pass.
3. Update user-facing documentation and third-party notices when needed.
4. Open an owner-approved pull request from `dev` to `master`.
5. Smoke-test the resulting build on a real `arm64-v8a` device.

## Create the draft release

After `master` contains the exact release commit:

```powershell
git switch master
git pull --ff-only origin master
git tag -a v0.87.1 -m "Release v0.87.1"
git push origin v0.87.1
```

The release workflow verifies that the tag is semantic, confirms its commit is contained in `origin/master`, builds with JDK 17, decodes GitHub signing secrets, and creates a draft GitHub Release.

## Verify and publish

Before publishing the draft:

1. Confirm the workflow succeeded.
2. Confirm the APK is universal and contains every supported ABI.
3. Verify the APK signature and package ID `io.github.h3nb.jlmodplus`.
4. Install it over the previous JL-Mod Plus release and verify user data remains accessible.
5. Test launch, MIDlet import, one 2D game, one 3D game, audio, controls, and storage access.
6. Review generated release notes and attached symbols.
7. Publish the draft manually from GitHub.

Keep the keystore and passwords backed up independently of GitHub Secrets. GitHub Secrets cannot be read back and are not a backup.
