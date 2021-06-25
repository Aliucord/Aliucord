<h1 align="center">Aliucord</h1>
<p align= "center">
  <a href="https://discord.gg/EsNDvBaHVU">
    <img alt="Discord" src="https://img.shields.io/discord/811255666990907402?color=%2300C853&label=Support%20Server&logo=discord&logoColor=%2300C853&style=for-the-badge">
  </a>
</p>

## What is Aliucord?

A modification for the Android Discord app inspired by desktop client modifications. 
Unlike other Android Discord app modifications, you don't need to rebuild the APK when adding or removing plugins, because Aliucord hooks at runtime using the [Pine](https://github.com/canyie/pine) framework.

## Important Information

⚠ **Please Read**

Aliucord does not support the `x86` or `x86_64` architectures, because Pine only supports `arm` and `arm64`.

**Supported Android version(s)** 

- 7.0+ (SDK 24+)

**Supported Discord version(s)**

- 80.2 - Alpha (80202)

## Build/Installation Instructions

### Installation
1. Download [Installer-release.apk](https://github.com/Aliucord/Aliucord/raw/builds/Installer-release.apk) from the `builds` branch
2. Find it via your preferred files viewing app, then install it
3. Open the newly installed "Aliucord Installer" app from your app drawer
4. Click "Install", then choose the "Download" option 

- *psst... only choose the other options if you know what you're doing!*

5. Wait for it to finish patching the Discord APK
6. Click "Install" once prompted by Android and wait for Aliucord to finish installing
7. If Google Play warns you about this application being unverified, ignore it¹
8. Open Aliucord, grant access to files (it needs this for finding plugins), log in to your account, and voila! Aliucord is at your fingertips!

### Plugin Installation

1. Open your preferred files viewing app of choice
2. Navigate to `/storage/emulated/0/`, your device/app may call it something else like "Internal Storage" or
`/sdcard/`
3. Look for the folder named "Aliucord" and if you can't find it, you're probably looking in the wrong spot
4. Once you've found the folder, enter it, look for a "plugins" folder and if you don't have one, create it yourself, and remember, LOWERCASE "p"
5. Enter the plugins folder, this is where plugins you download need to be placed
6. Either search GitHub or join our [support server](https://discord.gg/EsNDvBaHVU) and visit the `#plugin-links` and `#plugin-links-updates` text channels for plugins to download²
7. Visit the `builds` branch of any GitHub repositories you get linked to and download the ZIP files of the plugins you wish to load with Aliucord
8. Once you've downloaded the plugins, move them into the `Aliucord/plugins` folder
9. Open Aliucord, check the plugins tab and hopefully see your plugin(s) listed!

If you had troubles with these processes, such as plugins not loading...

- Try closing and then reopening Aliucord
- Double check that Aliucord has permission to access files
- Reinstall Aliucord, making sure to use the "Download" option

...and if none of these work, please visit our [support server](https://discord.gg/EsNDvBaHVU) and go to `#support` for help!

¹If you'd like, you can disable this warning by turning off Play Protect in Google Play's settings, it's mostly useless but if you'd rather be safe, ignore this.

²There is a more automated installation process available that is linked in these Discord channels

### Building
See `.github/workflows/build.yml` for all build steps.

### Additional Notes
If you wish to port Aliucord to a newer version of Discord, follow these steps...
1. Acquire the version of the Discord APK you'd like to port Aliucord to
2. Decompile it using [Apktool](https://github.com/iBotPeaches/Apktool)

- e.g `apktool d discord-n.apk` (replace n with build number)

3. Apply `manifest.patch` to the `AndroidManifest.xml` file
4. Rebuild the Discord APK using Apktool

- e.g `apktool b discord-n.apk` (replace n with build number)

5. Copy `build/apk/AndroidManifest.xml` to `.assets/AndroidManifest.xml` and to `Aliucord/AndroidManifest.xml` on your Android device
6. Build Aliucord using [buildtool](https://github.com/Aliucord/buildtool) and copy to `Aliucord/Aliucord.dex` on your Android device
6
7. Open Aliucord Installer, open the app settings, clear cache, and enable "Use Aliucord.dex from storage"
8. Install Aliucord
9. Ensure you've got a `logcat` catcher ready to go
10. Open Aliucord and fix any errors that show in `logcat`
