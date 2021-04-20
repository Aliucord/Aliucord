# Aliucord
Discord Android app mod inspired by desktop Discord client mods. Unlike other mods, you don't need to rebuild full apk on PC. You need only to rebuild Aliucord patches after installing plugins that requires patching new classes, which can be done on Android phone using Aliucord Installer.

**Supported Discord version**: 71.3 (1520)

### Download
Download `Installer-release.apk` from `builds` branch.

### Build
See `.github/workflows/build.yml` for all build steps.

### Additional Notes
If you want to port Aliucord to newest version of Discord:
1. Decompile Discord apk using apktool
2. Apply `manifest.patch` on `AndroidManifest.xml` (make sure to correct version first in 10th line of patch)
3. Build Discord apk using apktool
4. Copy `build/apk/AndroidManifest.xml` to `Installer/src/main/assets/AndroidManifest.xml`
5. Have fun with fixing errors
