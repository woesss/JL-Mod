# Upstream troubleshooting baseline

The `baseline` branch is an immutable snapshot of the original `woesss/JL-Mod` `dev` branch before JL-Mod Plus development diverged further.

| Field | Value |
| --- | --- |
| Source | `woesss/JL-Mod` branch `dev` |
| Commit | `f723a190c0bdb44b31c3bc0ead6f8665c7ea517d` |
| Commit subject | `LCDUI: refactoring` |
| Snapshot recorded | 2026-07-17 |

Use it to answer questions such as:

- Did this behavior already exist upstream?
- Which JL-Mod Plus change introduced a regression?
- Is a suspicious file inherited code or fork-specific code?

Useful read-only comparisons:

```powershell
git fetch origin baseline
git diff baseline...dev
git log --left-right --oneline baseline...dev
```

To inspect it without creating commits on the branch:

```powershell
git switch --detach baseline
# Inspect or build, but do not commit or push.
git switch dev
```

The GitHub branch is locked, applies protection to administrators, and disallows force-pushes and deletion. Never merge JL-Mod Plus changes into `baseline`, never publish it as JL-Mod Plus, and never move it to a newer upstream commit. If a later upstream snapshot is useful, create a separately named dated baseline and document it instead of rewriting this one.
