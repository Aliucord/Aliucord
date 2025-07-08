<div align="center">
    <img src=".github/assets/aliucord.svg" alt="Aliucord Logo" width="200" />
    <h1>Aliucord</h1>
    <p>A mod for the Android Discord app.</p>

[![Discord](https://img.shields.io/discord/811255666990907402?logo=discord&logoColor=white&style=for-the-badge&color=5865F2)](https://discord.gg/EsNDvBaHVU)
[![GitHub Stars](https://img.shields.io/github/stars/Aliucord/Aliucord?logo=github&style=for-the-badge&color=ffd663)](https://github.com/Aliucord/Aliucord/stargazers)
[![GitHub Forks](https://img.shields.io/github/forks/Aliucord/Aliucord?logo=github&style=for-the-badge&color=975fff)](https://github.com/Aliucord/Aliucord/forks)
[![Build Status](https://img.shields.io/github/actions/workflow/status/Aliucord/Aliucord/build.yml?label=Build&logo=github&style=for-the-badge&branch=main)](https://nightly.link/Aliucord/Aliucord/workflows/build/main/app.zip)
![GitHub Downloads](https://img.shields.io/github/downloads/Aliucord/Aliucord/total?style=for-the-badge&logo=github)
![Code Size](https://img.shields.io/github/languages/code-size/Aliucord/Aliucord?style=for-the-badge&color=181717)
[![GitHub License](https://img.shields.io/github/license/Aliucord/Aliucord?style=for-the-badge&color=181717)](https://github.com/Aliucord/Aliucord/blob/main/LICENSE)

</div>

## ⚠️ Important Information

Please note that this is a MODIFICATION of the Discord App and this is AGAINST THE DISCORD TOS.
While there has been no cases of anyone having their account banned, restricted, or otherwise punished, please use it at your own risk.

### Supported Android Versions

- Android 7 (SDK 24) - Android 16 QPR1 (SDK 36)
- Architectures: arm64, armeabi-v7, x86_64, x86

### Supported Discord version

- 126.21 / Stable 126021 (Will be downloaded for you)

## 🎨 Features

- No root needed
- Robust plugin system (200+ plugins)
    - Allows for adding and removing plugins without needing to rebuild or reinstall Aliucord
    - Can toggle on and off, configure, or uninstall your plugins via the plugins page
- An in-app updater to keep Aliucord and your plugins up-to-date
- Block *most* Discord tracking/analytics (blocking all isn't possible without endangering your account)
- Capture any crashes from within the app to allow developers to more easily fix issues.

## 📲 Installation

1. Download the [latest Manager APK](https://github.com/Aliucord/Manager/releases/latest) (a separate repository)
2. Open and install Aliucord Manager
    - If you have not enabled "Install from unknown sources" for your browser via system settings, do so now.
3. Open Aliucord Manager
4. Grant the permission requests
5. Click "New Install" or "Update" if you have previously installed Aliucord.
    - You can change how the Aliucord app icon will look once installed.
6. Click "Install" and wait. Do not exit the app while it is running.
    - If a failure occurs, it may be due to a variety of reasons:
        - Poor internet connection
        - Internet censorship / blocks (use a VPN or a different DNS server)
        - Insufficient free storage (at least 500MB is recommended)
    - If you are stuck, please ask for help in the `#support` channel of our [Discord](https://discord.gg/EsNDvBaHVU0).
7. Click "Launch" or directly launch the newly installed Aliucord app
8. Grant the storage permission requests (required to load & store plugins)
9. Sign in to Discord
10. Install plugins by following these [instructions](#-plugin-installation) below
11. Voila! 200+ plugins are right at your fingertips!

## 🔌 Plugin Installation

1. Join our [support server](https://discord.gg/EsNDvBaHVU) and visit the `#plugins-list` channel for a list of available plugins
2. Hold down the message (NOT the link, the entire message) with the desired plugin and click "Open PluginDownloader"
3. Find the desired plugin in the list and click install. It should immediately start working, however most plugins require you to
   restart to make them fully work

### ⚠️ IF YOU CAME HERE FROM A YOUTUBE TUTORIAL:

- PluginDownloader now comes preinstalled with Aliucord, so you don't need to manually install it
- If you were promised "free nitro", you were lied to. The only aspect of nitro that is mimickable is sending emojis,
  but as their image link instead, not as actual emojis/reactions.

## 🚬🐛 Troubleshooting

- Try closing and then reopening Aliucord
- Double check that Aliucord has permission to access files
- Reinstall Aliucord using the installer

...and if none of these work, please visit our [support server](https://discord.gg/EsNDvBaHVU) and go to the `#support` channel for help!

## Credits

- [LSPlant](https://github.com/LSPosed/LSPlant) - A hook framework for Android Runtime (ART)
- [Pine](https://github.com/canyie/pine) - Dynamic java method hook framework on ART
- [apktool](https://ibotpeaches.github.io/Apktool/) - A tool for reverse engineering Android apk files
- [jadx](https://github.com/skylot/jadx) - Dex to Java decompiler
- [dex2jar](https://github.com/pxb1988/dex2jar) - Tools to work with android .dex and java .class files
