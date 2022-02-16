#!/bin/sh
# Simple script to compile all the required files for pine
#     - Prompts you for Aliucord & pine dir
#     - Compiles pine core and xposed
#     - Merges core and xposed classes into one jar
#     - Compiles the jar to DEX bytecode via d8
#     - Copies classes.jar to Aliucord/.assets
#     - Copies libpine.so builds and the compiled classes.dex to the Aliucord/installer/.../assets

set -e

RED="\033[0;31m"
YELLOW="\033[0;33m"
RESET="\033[0m"

# Usage: message...
die() {
  printf "$RED%s$RESET\n" "$*"
  exit 1
} >&2

# Usage: message...
log() {
  echo
  printf "$YELLOW%s$RESET" "$*"
  echo
  sleep .3
}

# Usage: OutVariableName FolderName expectedChildren...
prompt_folder() {
  varname="$1"
  shift
  log "Please specify the $1 directory. You may use a relative path or even . "
  printf "> "
  read -r response
  case "$response" in
    ~*) response="$HOME/$(printf "%s" "$response" | cut -c 2-)" ;;
  esac
  dir="$(realpath "$response")"
  shift
  for file in "$@"; do
    [ -e "$dir/$file" ] || die "$dir/$file doesn't exist. Are you sure you specified the correct directory?"
  done
  eval "$varname=$dir"
}

ANDROID_HOME="${ANDROID_HOME:-$HOME/Android/Sdk}"
[ -d "$ANDROID_HOME" ] || die "Android SDK not found. Please set ANDROID_HOME and try again."
BUILD_TOOLS="$(find "$ANDROID_HOME/build-tools" -maxdepth 1 | sort | tail -n 1)"
[ -x "$BUILD_TOOLS/d8" ] || die "Couldn't find your buildtools. Make sure ANDROID_HOME is correct"

for dep in zip unzip; do
  command -v "$dep" >/dev/null || die "$dep not found. Please install it then try again"
done

prompt_folder PINE Pine core xposed gradlew
echo
prompt_folder ALIUCORD Aliucord installer .assets

(

cd "$PINE"
chmod +x gradlew

log "Assembling Pine Core"
./gradlew core:assemble
log "Assembling Pine Xposed"
./gradlew xposed:assemble

CORE="$(mktemp -d)"
XPOSED="$(mktemp -d)"
CLASSES="$(mktemp -d)"

log "Extracting Pine Core & Xposed builds"
unzip core/build/outputs/aar/core-release.aar -d "$CORE"
unzip xposed/build/outputs/aar/xposed-release.aar -d "$XPOSED"

log "Merging Pine Core & Xposed classes"
cd "$CLASSES"
unzip "$CORE/classes.jar"
unzip "$XPOSED/classes.jar"
zip -r classes.jar -- *

log "Compiling to DEX bytecode"
"$BUILD_TOOLS/d8" classes.jar

log "Copying files to Aliucord assets dir"
PINE_ASSETS="$ALIUCORD/installer/android/app/src/main/assets/pine"
cp classes.dex "$PINE_ASSETS"
cp classes.jar "$ALIUCORD/.assets/pine.jar"
for dir in "$CORE/jni/"*; do
  outdir="$PINE_ASSETS/$(basename "$dir")"
  mkdir -p "$outdir"
  cp "$dir/libpine.so" "$outdir"
done

log "Done! Successfully built and copied all pine artifacts to the installer assets."

)
