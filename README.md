# LSPlant

![](https://img.shields.io/badge/license-LGPL--3.0-orange.svg)
![](https://img.shields.io/badge/Android-5.0%20--%2013-blue.svg)
![](https://img.shields.io/badge/arch-armeabi--v7a%20%7C%20arm64--v8a%20%7C%20x86%20%7C%20x86--64%7C%20riscv64-brightgreen.svg)
![](https://github.com/LSPosed/LSPlant/actions/workflows/build.yml/badge.svg?branch=master&event=push)
![](https://img.shields.io/maven-central/v/org.lsposed.lsplant/lsplant.svg)

LSPlant is an Android ART hook library, providing Java method hook/unhook and inline deoptimization.

This project is part of LSPosed framework under GNU Lesser General Public License.

## Features

+ Support Android 5.0 - 14 (API level 21 - 34)
+ Support armeabi-v7a, arm64-v8a, x86, x86-64, riscv64
+ Support customized inline hook framework and ART symbol resolver

## Documentation

https://lsposed.org/LSPlant/namespacelsplant.html

## Quick Start

```gradle
repositories {
    mavenCentral()
}

android {
    buildFeatures {
        prefab true
    }
}

dependencies {
    implementation "org.lsposed.lsplant:lsplant:5.2"
}
```

If you don't want to include `libc++_shared.so` in your APK, you can use `lsplant-standalone` instead:

```gradle
dependencies {
    implementation "org.lsposed.lsplant:lsplant-standalone:5.2"
}
```

### 1. Init LSPlant within JNI_OnLoad

Initialize LSPlant for the proceeding hook. It mainly prefetch needed symbols and hook some functions.

+ `env` is the Java environment.

+ `info` is the information for initialized.

  Basically, the info provides the inline hooker and unhooker together with a symbol resolver of `libart.so` to hook and extract needed native functions of ART.

```c++
bool Init(JNIEnv *env,
          const InitInfo &info);
```

Returns whether initialization succeed. Behavior is undefined if calling other LSPlant interfaces before initialization or after a fail initialization.

### 2. Hook

Hook a Java method by providing the `target_method` together with the context object `hooker_object` and its callback `callback_method`.

+ `env` is the Java environment.

+ `target_method` is an `Method` object to the method you want to hook.

+ `hooker_object` is an object to store the context of the hook.

  The most likely usage is to store the backup method into it so that when `callback_method` is invoked, it can call the original method. Another scenario is that, for example, in Xposed framework, multiple modules can hook the same Java method and the `hooker_object` can be used to store all the callbacks to allow multiple modules work simultaneously without conflict.

+ `callback_method` is an `Method` object, the callback method to the `hooker_object` used to replace the `target_method`.

  Whenever the `target_method` is invoked, the callback_method will be invoked instead of the original `target_method`. The signature of the `callback_method` must be: `public Object callback_method(Object []args)`.

  That is, the return type must be `Object` and the parameter type must be `Object[]`. Behavior is undefined if the signature does not match the requirement. Extra info can be provided by defining member variables of `hooker_object`. This method must be a method to `hooker_object`.

```c++
jobject Hook(JNIEnv *env,
             jobject target_method,
             jobject hooker_object,
             jobject callback_method);
```

Returns the backup method. You can invoke it by reflection to invoke the original method. null if fails.

This function will automatically generate a stub class for hook. To help debug, you can set the generated class name, its field name, its source name and its method name by setting `generated_*` in `InitInfo`.

This function thread safe (you can call it simultaneously from multiple thread) but it's not atomic to the same `target_method`. That means `UnHook` or `IsUnhook` does not guarantee to work properly on the same `target_method` before it returns. Also, simultaneously call on this function with the same target_method does not guarantee only one will success. If you call this with different `hooker_object` on the same `target_method` simultaneously, the behavior is undefined.

### 3. Check

Check if a Java function is hooked by LSPlant or not.

```c++
bool IsHooked(JNIEnv *env,
              jobject method);
```

Returns whether the method is hooked.

### 4. Unhook

Unhook a Java function that is previously hooked.

+ `env` is the Java environment.

+ `target_method` is an `Method` object to the method you want to hook.

```c++
bool UnHook(JNIEnv *env,
            jobject target_method);
```

Returns whether the unhook succeed.

Calling backup (the return method of `Hook()`) after unhooking is undefined behavior. Please read `Hook()`'s note for more details.

### 5. Deoptimize

Deoptimize a method to avoid hooked callee not being called because of inline.

+ `env` is the Java environment.

+ `method` is an `Method` object to the method to deoptimize.

  By deoptimizing the method, the method will back all callee without inlining. For example, if you hooked a short method B that is invoked by method A, and you find that your callback to B is not invoked after hooking, then it may mean A has inlined B inside its method body. To force A to call your hooked B, you can deoptimize A and then your hook can take effect. Generally, you need to find all the callers of your hooked callee and that can be hardly achieve. Use this function if you are sure the deoptimized callers are all you need. Otherwise, it would be better to change the hook point or to deoptimize the whole app manually (by simple reinstall the app without uninstalled).

```c++
bool Deoptimize(JNIEnv *env,
                jobject method);
```

Returns whether the deoptimizing succeed or not.

It is safe to call deoptimizing on a hooked method because the deoptimization will perform on the backup method instead.


## Credits
Inspired by the following frameworks:
- [YAHFA](https://github.com/PAGalaxyLab/YAHFA)
- [SandHook](https://github.com/asLody/SandHook)
- [Pine](https://github.com/canyie/pine)
- [Epic](https://github.com/tiann/epic)
