# Versioning JL-Mod Plus

JL-Mod Plus has its own application ID, signing key, goals, and release history. Its app version therefore starts at `0.1.0` instead of continuing JL-Mod's `0.87.1` number. The upstream version remains useful lineage information, but it is not the JL-Mod Plus app version.

## The two Android version values

Android stores two related values:

- `versionName` is the human-readable version shown to users, such as `0.1.0`.
- `versionCode` is an increasing integer Android uses to decide whether one APK can update another.

The only value maintained by hand is `versionName` in the repository-root `version.properties` file. Gradle calculates `versionCode` automatically:

```text
major * 1,000,000 + minor * 1,000 + patch
```

Examples:

| `versionName` | Calculated `versionCode` |
| --- | ---: |
| `0.1.0` | 1,000 |
| `0.1.1` | 1,001 |
| `0.2.0` | 2,000 |
| `1.0.0` | 1,000,000 |

Minor and patch numbers must stay between 0 and 999. Gradle rejects invalid or incomplete versions before building.

## What each number means

JL-Mod Plus uses semantic versions in the form `MAJOR.MINOR.PATCH`:

- While the app is experimental, keep `MAJOR` at `0`.
- Increase `MINOR` for a new feature, a meaningful modernization step, or an intentional compatibility or behavior change. Example: `0.1.0` to `0.2.0`.
- Increase `PATCH` for fixes and small improvements that do not intentionally change compatibility. Example: `0.1.0` to `0.1.1`.
- Use `1.0.0` only when the maintainer intentionally declares a dependable baseline. It does not mean the app is perfect; it means future compatibility is treated as a stronger promise.

A version is a compatibility label, not a percentage-complete score. Do not increment it for every commit. Change it only when preparing a public release.

## Development builds

Continuous builds from `dev` append a suffix such as `-dev.20260717.42.a1b2c3d`. They use the debug application ID `io.github.h3nb.jlmodplus.debug`, are arm64-only, and are not public semantic releases.

Public releases use an exact tag such as `v0.1.0`. The release workflow accepts only a tag that matches `version.properties` and points to a commit contained in `master`.

## Upstream lineage

The fork started from JL-Mod `0.87.1`, and the immutable `baseline` branch records a clean snapshot of upstream `dev`. When upstream changes are integrated later, describe the upstream commit or release in the pull request and release notes. Do not put the upstream version into the JL-Mod Plus `versionName`.

Once the first public JL-Mod Plus release exists, never reset the version sequence and never move an existing release tag. Publish a higher patch, minor, or major version instead.
