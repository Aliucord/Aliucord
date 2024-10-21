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
Az Aliucord egy módosítás az Androidos Discord alkalmazáshoz
</p>

## ⚠️ Fontos információk
KÉRJÜK, VEDD FIGYELEMBE, HOGY ez a Discord alkalmazás MÓDOSÍTÁSA, ami ELLENTÉTES A DISCORD FELHASZNÁLÁSI FELTÉTELEIVEL. Használata saját felelősségre történik. 

### Támogatott Android verziók

- Android 7 (SDK 24) - Android 14 QPR2 (SDK 34)
- arm64, armeabi-v7, x86_64, x86

### Támogatott Discord verzió

- 126.21 / Stable 126021 (Nincs szükség a .apk fájlra, az installer letölti)

## 🎨 Funkciók

- Nincs szükség root hozzáférésre
- Robusztus plugin rendszer
    - Lehetővé teszi a pluginok cseréjét újrafordítás nélkül
    - Pluginok be- és kikapcsolása, konfigurálása vagy eltávolítása a plugin oldalon keresztül
- Alkalmazáson belüli frissítő, hogy naprakészen tartsd az Aliucordot és a pluginokat
- A legtöbb Discord követés/analitika blokkolása (nem lehetséges az összes követés teljes blokkolása)
- Hibajelentés (a ritka esetekre, amikor nem sikerül elkapnunk egy hibát)
    - Alkalmazáson belüli hibajelentés oldal, natívabb érzést nyújtva
    - A jelentések az `Aliucord/crashlogs` mappában is elérhetők az appon kívüli könnyű hozzáférés érdekében

## 📲 Telepítés

<a href="https://github.com/Aliucord/Aliucord/actions/workflows/build-installer.yml">
  <img alt="GitHub Workflow Status" src="https://img.shields.io/github/actions/workflow/status/Aliucord/Aliucord/build-installer.yml?label=Installer%20Build&logo=githubactions&logoColor=white&style=flat-square">
</a>
<a href="https://github.com/Aliucord/Aliucord/actions/workflows/build.yml">
  <img alt="GitHub Workflow Status" src="https://img.shields.io/github/actions/workflow/status/Aliucord/Aliucord/build-aliucord.yml?label=App%20Build&logo=githubactions&logoColor=white&style=flat-square">
</a>

1. Töltsd le és telepítsd az [Installer-release.apk](https://github.com/Aliucord/Aliucord/releases/latest/download/Installer-release.apk) fájlt a legfrissebb kiadásból
2. Nyisd meg a telepített "Aliucord Installer" alkalmazást az alkalmazáslistából
3. Kattints az "Install" gombra, majd válaszd a "Download" lehetőséget
4. Várd meg, amíg a Discord APK-t befejezi a módosítást
5. Kattints az "Install" gombra, amikor az Android erre kér, majd várd meg, amíg az Aliucord telepítése befejeződik. Ha a telepítő leáll vagy a .apk nem sikerül telepíteni, próbáld újra, és működni fog
6. Ha a Google Play figyelmeztet az alkalmazás ellenőrizetlensége miatt, hagyd figyelmen kívül. Ez azért van, mert az Aliucord helyileg épül és aláírásra kerül az eszközödön, így a Play Protect nem ismeri fel az aláírást¹
7. Nyisd meg az Aliucordot, adj hozzáférést a fájlokhoz (erre szükség van a pluginok megtalálásához), jelentkezz be a fiókodba, és kész is! Az Aliucord már használatra kész!

> ¹ Ha szeretnéd, kikapcsolhatod ezt a figyelmeztetést a Play Protect letiltásával a Google Play beállításaiban, a Play Protect nem hasznos.
>
> A Play Protect kikapcsolásához érintsd meg a felhasználói ikonodat a Google Play jobb felső sarkában, majd a "Play Protect" opciót, a fogaskerék ikont a jobb felső sarokban, végül kapcsold ki a "Scan apps with Play Protect" opciót. Ez néha azt eredményezheti, hogy a Google Play "zaklat" a visszakapcsolásra alkalmazások sideloadolásakor.\*

## 🔌 Plugin telepítés

1. Csatlakozz a [támogatói szerverünkhöz](https://discord.gg/EsNDvBaHVU) és keresd fel a `#plugins-list` csatornát az elérhető pluginok listájáért
2. Hosszan nyomd meg az üzenetet (NE a linket, hanem az egész üzenetet) a kívánt pluginnal, és kattints az "Open PluginDownloader" gombra
3. Keresd meg a listában a kívánt plugint, és kattints a telepítésre. Azonnal működésbe kell lépnie, de egyes pluginok újraindítást igényelhetnek a teljes működéshez

⚠️ HA EGY YOUTUBE TUTORIALBÓL JÖTTÉL:

> - A PluginDownloader már előre telepítve van az Aliucorddal, így nem kell külön telepíteni
> - Ha ingyenes nítrot ígértek, becsaptak. A legtöbb, ami lehetséges, az ingyenes emojik használata (az emojik képlinkjeit küldi)

## 🚬🐛 Hibajavítás

- Próbáld meg bezárni, majd újra megnyitni az Aliucordot
- Győződj meg róla, hogy az Aliucordnak van fájlhozzáférési engedélye
- Telepítsd újra az Aliucordot az installer segítségével

...ha ezek egyike sem segít, kérj segítséget a [támogatói szerverünkön](https://discord.gg/EsNDvBaHVU) a `#support` csatornán!

## 🧱 Fordítás forráskódból

Nézd meg a `.github/workflows/build.yml` fájlt az összes fordítási lépéshez.

## Kreditek

- [LSPlant](https://github.com/LSPosed/LSPlant) - Egy hook keretrendszer az Android Runtime (ART) számára
- [Pine](https://github.com/canyie/pine) - Dinamikus Java metódus hook keretrendszer az ART-hoz
- [apktool](https://ibotpeaches.github.io/Apktool/) - Egy eszköz Androidos .apk fájlok visszafejtéséhez
- [jadx](https://github.com/skylot/jadx) - Dex to Java decompiler
- [dex2jar](https://github.com/pxb1988/dex2jar) - Eszközök az Android .dex és Java .class fájlok kezeléséhez
