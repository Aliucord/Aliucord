# Aliucord
Discord Android app mod inspired by desktop Discord client mods. Unlike other mods, you don't need to rebuild full apk on PC. You need only to rebuild Aliucord patches after installing plugins that requires patching new classes, which can be done on Android phone using Aliucord Installer.

**Supported Discord version**: 78.4 - Alpha (78204)

[Support Server](https://discord.gg/EsNDvBaHVU)

### Download
1. Download [Installer-release.apk](https://github.com/Aliucord/Aliucord/raw/builds/Installer-release.apk) from `builds` branch
2. Install Installer app
3. Open Installer app, click Install, leave Download option (recommended)
4. Wait for Installer to finish its job
5. Click Install when it will prompt you to install Aliucord

### Build
See `.github/workflows/build.yml` for all build steps.

### Additional Notes
If you want to port Aliucord to newest version of Discord:
1. Decompile Discord apk using apktool
2. Apply `manifest.patch` on `AndroidManifest.xml` (make sure to correct version first in 10th line of patch)
3. Build Discord apk using apktool
4. Copy `build/apk/AndroidManifest.xml` to `.assets/AndroidManifest.xml` and `(storage)/Aliucord/AndroidManifest.xml`
5. Build Aliucord using buildtool and copy to `(storage)/Aliucord/Aliucord.dex`
6. Clear Installer cache and enable `Use Aliucord.dex from storage` option in Installer
7. Have fun with fixing errors
