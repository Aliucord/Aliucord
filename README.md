<h1 align="center">Aliucord</h1>
<p align="center">
  <a href="https://discord.gg/EsNDvBaHVU">
    <img alt="Discord" src="https://img.shields.io/discord/811255666990907402?color=%2300C853&label=Support%20Server&logo=discord&logoColor=%2300C853&style=for-the-badge">
  </a>
</p>
<p align="center">
  <img alt="GitHub Repo stars" src="https://img.shields.io/github/stars/Aliucord/Aliucord?color=181717&logo=github&style=for-the-badge">
  <img alt="GitHub forks" src="https://img.shields.io/github/forks/Aliucord/Aliucord?color=181717&logo=github&style=for-the-badge">
  <a href="https://github.com/Aliucord/Aliucord/blob/main/LICENSE">
    <img alt="License" src="https://img.shields.io/badge/LICENSE-OSL--3.0-0099E5?style=for-the-badge">
  </a>
</p>

<p align="center">
Aliucord is a modification for the Android Discord app inspired by desktop client modifications.
</p>
<p align="center">
Unlike other Android Discord app modifications, you don't need to rebuild the APK when adding or removing plugins, because Aliucord hooks at runtime using the <a href="https://github.com/canyie/pine">Pine</a> java method hook framework.
</p>

## 📃 Table of contents

- [Important Information](#%EF%B8%8F-important-information)
  - [Supported Architectures](#supported-architectures)
  - [Supported Android version(s)](#supported-android-versions)
  - [Supported Discord version(s)](#supported-discord-versions)
- [Features](#-features)
- [Installation](#-installation)
- [Plugin Installation](#-plugin-installation)
  - [Troubleshooting](#troubleshooting)
- [Building from source](#-building-from-source)
  - [Porting Aliucord to the latest Discord version](#-porting-aliucord-to-the-latest-discord-version)

## ⚠️ Important Information

### Supported Architectures

- `arm`
- `arm64`

Pine does not support `x86` or `x86_64` architectures, and thus Aliucord does not either.

### Supported Android version(s)

- 7.0+ (SDK 24+)

### Supported Discord version(s)

- 88.2 / Alpha 88202 (You don't need the apk, the installer will download it for you)

## 🎨 Features

- Rootless! Aliucord itself does not require a rooted device in order to use it
- Robust plugin system using Pine!
    - Allows swapping in and out your plugins without needing to rebuild Aliucord
    - Toggle on and off, configure or uninstall your plugins via the plugins page
    - In-app updater to keep your plugins up-to-date
    - Minimum Discord versions for plugins so no breaking changes are sent out to outdated Discord versions
- Crash logging!
    - In-app crash log page to give a more native feel
    - Logs are also saved to `Aliucord/crashlogs` for easy access outside of the app
- Installer app with multiple features!
    - Helps you keep track of which updates to this repository require you to update
    - Automatically downloads the latest files for Aliucord, so you can get busy updating
    - Allows you to provide your own `Aliucord.dex` so you can develop your own additions, fixes etc. for Aliucord and test them yourself

## 📲 Installation

<a href="https://github.com/Aliucord/Aliucord/actions/workflows/build-installer.yml">
  <img alt="GitHub Workflow Status" src="https://img.shields.io/github/workflow/status/Aliucord/Aliucord/Build%20Installer?label=Installer%20Build&logo=githubactions&logoColor=white&style=flat-square">
</a>
<a href="https://github.com/Aliucord/Aliucord/actions/workflows/build.yml">
  <img alt="GitHub Workflow Status" src="https://img.shields.io/github/workflow/status/Aliucord/Aliucord/Build?label=App%20Build&logo=githubactions&logoColor=white&style=flat-square">
</a>

1. Download and install [Installer-release.apk](https://github.com/Aliucord/Aliucord/raw/builds/Installer-release.apk) from the `builds` branch
2. Open the newly installed "Aliucord Installer" app from your app drawer
3. Click "Install", then choose the "Download" option
    - ⚠️ DO NOT USE "From installed app" OR "From storage" UNLESS YOU KNOW WHAT YOU'RE DOING, OTHERWISE ALIUCORD WILL LIKELY NOT WORK CORRECTLY
    - If you get a 404 error [change your dns to 1.1.1.1](https://is.gd/aliucorddns)
4. Wait for it to finish patching the Discord APK
5. Click "Install" once prompted by Android and wait for Aliucord to finish installing
6. If Google Play warns you about this application being unverified, ignore it as it triggers thanks to an unverified signature¹
7. Open Aliucord, grant access to files (it needs this for finding plugins), log in to your account, and voila! Aliucord is at your fingertips!

> ¹ If you'd like, you can disable this warning by turning off Play Protect in Google Play's settings, it's mostly useless.
> 
> Play Protect can be turned off by tapping on your user icon in the top right of Google Play, tapping on "Play Protect," tapping on the cog icon in the top right, and finally toggling "Scan apps with Play Protect" to off. This may result in Google Play "nagging" you to re-enable it sometimes when sideloading apps.*


## 🔌 Plugin Installation

1. Open your preferred file manager
2. Navigate to your internal storage (likely `/storage/emulated/0/` or `/sdcard/`)
3. Look for the folder named "Aliucord" and open it
4. Open the "plugins" folder, or if it doesn't exist, create it yourself. Remember: LOWERCASE "p"
5. Either search GitHub or join our [support server](https://discord.gg/EsNDvBaHVU) and visit the `#plugin-links` channel for plugins to download
6. Visit the `builds` branch of any GitHub repositories you get linked to and download the ZIP files of the plugins you wish to load with Aliucord
7. Once you've downloaded the plugins, move them into the `Aliucord/plugins` folder (Do not extract the zip)
8. Open Aliucord, check the plugins tab and hopefully see your plugin(s) listed!

> Hint: There is a [PluginDownloader plugin](https://github.com/Vendicated/AliucordPlugins/blob/builds/PluginDownloader.zip?raw=true) that makes installing plugins a lot easier by adding download buttons to messages in either of the plugin channels


### Troubleshooting

- Try closing and then reopening Aliucord
- Double check that Aliucord has permission to access files
- Reinstall Aliucord, making sure to use the "Download" option

...and if none of these work, please visit our [support server](https://discord.gg/EsNDvBaHVU) and go to `#support` for help!


## 🧱 Building from source
See `.github/workflows/build.yml` for all build steps.

## ⏭️ Porting Aliucord to the latest Discord version

1. Acquire the version of the Discord APK you'd like to port Aliucord to
2. Decompile it using [Apktool](https://github.com/iBotPeaches/Apktool)
    - e.g `apktool d discord-n.apk` (replace n with build number)
3. Apply `manifest.patch` to the `AndroidManifest.xml` file
4. Rebuild the Discord APK using Apktool
    - e.g `apktool b discord-n.apk` (replace n with build number)
5. Copy `build/apk/AndroidManifest.xml` to `.assets/AndroidManifest.xml` and to `Aliucord/AndroidManifest.xml` on your Android device
6. Build Aliucord using [buildtool](https://github.com/Aliucord/buildtool) and copy to `Aliucord/Aliucord.dex` on your Android device
7. Open Aliucord Installer, open the app settings, clear cache, and enable "Use Aliucord.dex from storage"
8. Install Aliucord
9. Ensure you've got a `logcat` catcher ready to go
10. Open Aliucord and fix any errors that show in `logcat`
