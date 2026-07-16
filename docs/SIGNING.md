# Release signing

Android uses a signing key as the permanent identity of an application. Every update installed over an existing release must be signed with the same key. Losing the key or its passwords means existing users cannot install future updates over the old app.

## What this repository does

- Debug builds from `dev` use Android's debug signing and do not need repository secrets.
- Release builds from `master` use `keystore.properties` locally.
- GitHub Actions expects the encrypted secrets `SIGNING_KEY` and `KEYSTORE_PROPERTIES` for release builds.
- Keystores and real property files are ignored by Git and must never be committed.

## 1. Create and back up the key

Create a directory outside the repository, then run this from PowerShell with JDK 17 available. Use a unique file name and alias; the placeholders below are not real values:

```powershell
New-Item -ItemType Directory -Force 'X:\private\keystores'
keytool -genkeypair -v `
  -keystore 'X:\private\keystores\JL-Mod-Plus-UNIQUE-ID.jks' `
  -storetype JKS `
  -alias 'jl-mod-plus-UNIQUE-ID' `
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
storeFile=X:/private/keystores/JL-Mod-Plus-UNIQUE-ID.jks
storePassword=YOUR_STORE_PASSWORD
keyAlias=jl-mod-plus-UNIQUE-ID
keyPassword=YOUR_KEY_PASSWORD
```

Test the release configuration from a checkout path without spaces:

```powershell
.\gradlew.bat :app:assembleEmulatorRelease
```

## 3. Configure GitHub Actions secrets

Run these commands from the repository root. They pipe secret values directly to GitHub CLI instead of printing them:

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes(
  (Select-String -Path .\keystore.properties -Pattern '^storeFile=(.+)$').Matches.Groups[1].Value
)) | gh secret set SIGNING_KEY --repo H3nb/JL-Mod-Plus

$localProperties = @{}
Get-Content .\keystore.properties | ForEach-Object {
  if ($_ -match '^([^#!][^=]*)=(.*)$') {
    $localProperties[$matches[1].Trim()] = $matches[2]
  }
}
$ciProperties = @(
  'storeFile=keystore.jks'
  "storePassword=$($localProperties.storePassword)"
  "keyAlias=$($localProperties.keyAlias)"
  "keyPassword=$($localProperties.keyPassword)"
) -join "`n"
[Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($ciProperties)) |
  gh secret set KEYSTORE_PROPERTIES --repo H3nb/JL-Mod-Plus
```

`KEYSTORE_PROPERTIES` intentionally contains only signing configuration. Keep `CRASH_REPORT_TOKEN` as its own GitHub secret so rotating or debugging one credential cannot silently replace another.

Verify only the secret names and update dates; GitHub will never return their values:

```powershell
gh secret list --repo H3nb/JL-Mod-Plus
```

After both secrets exist, follow `docs/RELEASING.md`. Release builds are created from semantic-version tags that point to commits contained in `master`.
