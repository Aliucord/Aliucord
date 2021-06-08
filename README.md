# Aliucord
Discord Android app mod inspired by desktop Discord client mods. Unlike other mods, you don't need to rebuild the apk on PC, because Aliucord is hooking at runtime using the [Pine](https://github.com/canyie/pine) hook framework.

⚠ **Info**: Aliucord doesn't support `x86` and `x86_64` architectures, because Pine supports `arm` and `arm64` only.

**Supported Android versions**: 7.0 or higher (sdk 24)

**Supported Discord version**: 79.1 - Alpha (79201)

[Support Server](https://discord.gg/EsNDvBaHVU)

### Download
1. Download [Installer-release.apk](https://github.com/Aliucord/Aliucord/raw/builds/Installer-release.apk) from the `builds` branch
2. Install it
3. Open it, click Install, choose the Download option (recommended)
4. Wait for it to finish its job
5. Click Install once prompted to install Aliucord

### Build
See `.github/workflows/build.yml` for all build steps.

### Additional Notes
If you want to port Aliucord to the newest version of Discord:
1. Decompile Discord apk using apktool
2. Apply `manifest.patch` on `AndroidManifest.xml`
3. Build Discord apk using apktool
4. Copy `build/apk/AndroidManifest.xml` to `.assets/AndroidManifest.xml` and `(storage)/Aliucord/AndroidManifest.xml`
5. Build Aliucord using the buildtool and copy to `(storage)/Aliucord/Aliucord.dex`
6. Clear Installer cache and enable `Use Aliucord.dex from storage` option in Installer
7. Have fun with fixing errors
