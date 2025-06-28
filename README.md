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

- Android 7 (SDK 24) - Android 14 QPR2 (SDK 34)
- arm64, armeabi-v7, x86_64, x86

### Supported Discord version

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
<a href="https://github.com/Aliucord/Aliucord/actions/workflows/build.yml">
  <img alt="GitHub Workflow Status" src="https://img.shields.io/github/actions/workflow/status/Aliucord/Aliucord/build-aliucord.yml?label=App%20Build&logo=githubactions&logoColor=white&style=flat-square">
</a>

1. Download the Aliucord Manager](https://github.com/Aliucord/Manager/releases/latest)
2. Open and install Aliucord Manager
    - If you have not enabled "Install from unknown sources" for your browser via system settings, do so now.
3. Open Aliucord Manager
4. Grant permission requests
5. Click "New Install"
    - You may change the the Aliucord app icon will look once installed
6. Click "Install" and wait. Do not exit the app while it is running.
    - If a failure occurs, it may be due to a variety of reasons:
        - Poor internet connection
        - Internet censorship / blocks (use a VPN or a different DNS server)
        - Insufficient free storage (at least 500MB is recommended)
    - If you are stuck, please ask for help in the `#support` channel of our [Discord](https://discord.gg/EsNDvBaHVU0).
7. Click "Launch" or launch the newly installed Aliucord app directly
8. Grant permission requests
9. Sign in

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

## Credits

- [LSPlant](https://github.com/LSPosed/LSPlant) - A hook framework for Android Runtime (ART)
- [Pine](https://github.com/canyie/pine) - Dynamic java method hook framework on ART
- [apktool](https://ibotpeaches.github.io/Apktool/) - A tool for reverse engineering Android apk files
- [jadx](https://github.com/skylot/jadx) - Dex to Java decompiler
- [dex2jar](https://github.com/pxb1988/dex2jar) - Tools to work with android .dex and java .class files
