# Release signing

Android uses a signing key as the permanent identity of an application. Every update installed over an existing release must be signed with the same key. Losing the key or its passwords means existing users cannot install future updates over the old app.

## What this repository does

- Debug builds from `dev` use Android's debug signing and do not need repository secrets.
- Release builds from `master` use `keystore.properties` locally.
- GitHub Actions expects the encrypted secrets `SIGNING_KEY` and `KEYSTORE_PROPERTIES` for release builds.
- Keystores and real property files are ignored by Git and must never be committed.

## 1. Create and back up the key

Create a directory outside the repository, then run this from PowerShell with JDK 17 available:

```powershell
New-Item -ItemType Directory -Force 'F:\Android\keystores'
keytool -genkeypair -v `
  -keystore 'F:\Android\keystores\JL-Mod-Plus.jks' `
  -storetype JKS `
  -alias 'jl-mod-plus' `
  -keyalg RSA `
  -keysize 4096 `
  -validity 10000
```

Choose strong, unique passwords. Keep at least two encrypted backups in separate locations. Do not store the only copy on a development PC or only in GitHub Actions.

## 2. Configure local release builds

Copy `keystore.properties.example` to the ignored file `keystore.properties`, then replace the placeholders:

```powershell
Copy-Item .\keystore.properties.example .\keystore.properties
```

The file must contain:

```properties
storeFile=F:/Android/keystores/JL-Mod-Plus.jks
storePassword=YOUR_STORE_PASSWORD
keyAlias=jl-mod-plus
keyPassword=YOUR_KEY_PASSWORD
```

Test the release configuration from a checkout path without spaces:

```powershell
.\gradlew.bat :app:assembleEmulatorRelease
```

## 3. Configure GitHub Actions secrets

Run these commands from the repository root. They pipe secret values directly to GitHub CLI instead of printing them:

```powershell
[Convert]::ToBase64String(
  [IO.File]::ReadAllBytes('F:\Android\keystores\JL-Mod-Plus.jks')
) | gh secret set SIGNING_KEY --repo H3nb/JL-Mod-Plus

$ciProperties = (Get-Content -Raw .\keystore.properties) -replace `
  '(?m)^storeFile=.*$', 'storeFile=keystore.jks'
[Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($ciProperties)) |
  gh secret set KEYSTORE_PROPERTIES --repo H3nb/JL-Mod-Plus
```

Verify only the secret names and update dates; GitHub will never return their values:

```powershell
gh secret list --repo H3nb/JL-Mod-Plus
```

After both secrets exist, follow `docs/RELEASING.md`. Release builds are created from semantic-version tags that point to commits contained in `master`.
