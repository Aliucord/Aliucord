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
Aliucord is a modification for the Android Discord app
</p>

## ⚠️ Important Information
PLEASE NOTE THAT this is a MODIFICATION of the Discord APP and this is AGAINST THE DISCORD TOS. Use it at your own risk. 

### Supported Android Versions

- Android 7 (SDK 24) - 13 (SDK 33)
- arm64, armeabi-v7, x86_64

### Supported Discord version(s)

- 126.21 / Stable 126021 (You don't need the apk, the installer will download it for you)

## 🎨 Features

- No root needed
- Robust plugin system
    - Allows swapping in and out your plugins without needing to rebuild Aliucord
    - Toggle on and off, configure or uninstall your plugins via the plugins page
- In-app updater to keep Aliucord and your plugins up-to-date
- Blocks most Discord Tracking/Analytics (doesn't completely block all tracking, that's not really possible)
- Crash logging (for the rare cases we fail to catch errors)
    - In-app crash log page to give a more native feel
    - Logs are also saved to `Aliucord/crashlogs` for easy access outside of the app

## 📲 Installation

<a href="https://github.com/Aliucord/Aliucord/actions/workflows/build-installer.yml">
  <img alt="GitHub Workflow Status" src="https://img.shields.io/github/workflow/status/Aliucord/Aliucord/Build%20Installer?label=Installer%20Build&logo=githubactions&logoColor=white&style=flat-square">
</a>
<a href="https://github.com/Aliucord/Aliucord/actions/workflows/build.yml">
  <img alt="GitHub Workflow Status" src="https://img.shields.io/github/workflow/status/Aliucord/Aliucord/Build?label=App%20Build&logo=githubactions&logoColor=white&style=flat-square">
</a>

1. Download and install [Installer-release.apk](https://github.com/Aliucord/Aliucord/releases/latest/download/Installer-release.apk) from latest
   release
2. Open the newly installed "Aliucord Installer" app from your app drawer
3. Click "Install", then choose the "Download" option
4. Wait for it to finish patching the Discord APK
5. Click "Install" once prompted by Android and wait for Aliucord to finish installing. If the installer just stops or the apk fails to install just
   try again and it should work
6. If Google Play warns you about this application being unverified, ignore it. This happens because Aliucord is built & signed locally on your device
   so Play Protect doesn't recognise the signature¹
7. Open Aliucord, grant access to files (it needs this for finding plugins), log in to your account, and voila! Aliucord is at your fingertips!

> ¹ If you'd like, you can disable this warning by turning off Play Protect in Google Play's settings, play protect is useless.
>
> Play Protect can be turned off by tapping on your user icon in the top right of Google Play, tapping on "Play Protect," tapping on the cog icon in the top right, and finally toggling "Scan apps with Play Protect" to off. This may result in Google Play "nagging" you to re-enable it sometimes when sideloading apps.\*

## 🔌 Plugin Installation

1. Join our [support server](https://discord.gg/EsNDvBaHVU) and visit the `#plugins-list` channel for a list of available plugins
2. Hold down the message (NOT the link, the entire message) with the desired plugin and click "Open PluginDownloader"
3. Find the desired plugin in the list and click install. It should immediately start working, however some plugins may require you to restart to make
   them fully work

⚠️ IF YOU CAME HERE FROM A YOUTUBE TUTORIAL:

> - PluginDownloader now comes preinstalled with Aliucord so you don't need to install it
> - If you were promised free nitro, you were clickbaited. The most that is possible is free emotes (sends emote image links instead)

## 🚬🐛 Troubleshooting

- Try closing and then reopening Aliucord
- Double check that Aliucord has permission to access files
- Reinstall Aliucord using the installer

...and if none of these work, please visit our [support server](https://discord.gg/EsNDvBaHVU) and go to `#support` for help!

## 🧱 Building from source

See `.github/workflows/build.yml` for all build steps.

## ⏭️ Porting Aliucord to the latest Discord version

1. Download the apk of the version you want to port to (#official-discord-updates in Aliucord server)
2. Decompile it using [Apktool](https://github.com/iBotPeaches/Apktool)
    - `apktool d discord.apk` (Replace discord.apk with whatever the file name is)
3. Apply `manifest.patch` to the `AndroidManifest.xml` file (Using git bash or any shell on Linux or Macos run in the apktool decompile
   folder: `patch < manifest.patch`)
4. IMPORTANT: set targetSDK to 29 in both apktool.yml and AndroidManifest.xml or Aliucord will fail to install
5. Rebuild the Discord APK using Apktool
    - `apktool b discord` (Replace discord with the folder name)
6. Copy `build/apk/AndroidManifest.xml` to `.assets/AndroidManifest.xml` and to `Aliucord/AndroidManifest.xml` on your Android device
7. Repeat the same steps and this time add `android:debuggable=true` in the main category where there's also app name and icon, name this manifest
   AndroidManifest-debuggable.xml
8. Change `discord_version` to the correct one in gradle.properties and resync gradle
9. Fix any errors you encounter and deploy Aliucord to your device with `./gradlew Aliucord:deployWithAdb`
10. Open Aliucord > Settings > Updater > Top right settings > Use Aliucord.zip from storage and restart Aliucord
11. Enjoy debugging if all hell breaks loose

## Credits

- [LSPlant](https://github.com/LSPosed/LSPlant) - A hook framework for Android Runtime (ART)
- [Pine](https://github.com/canyie/pine) - Dynamic java method hook framework on ART
- [apktool](https://ibotpeaches.github.io/Apktool/) - A tool for reverse engineering Android apk files
- [jadx](https://github.com/skylot/jadx) - Dex to Java decompiler
- [dex2jar](https://github.com/pxb1988/dex2jar) - Tools to work with android .dex and java .class files
