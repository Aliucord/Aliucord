#pragma once

#include <absl/container/flat_hash_map.h>
#include <absl/container/flat_hash_set.h>
#include <sys/system_properties.h>

#include <list>
#include <shared_mutex>
#include <string_view>

#include "logging.hpp"
#include "lsplant.hpp"
#include "utils/hook_helper.hpp"

namespace lsplant {

enum class Arch {
    kArm,
    kArm64,
    kX86,
    kX86_64,
    kRiscv64,
};

consteval inline Arch GetArch() {
#if defined(__i386__)
    return Arch::kX86;
#elif defined(__x86_64__)
    return Arch::kX86_64;
#elif defined(__arm__)
    return Arch::kArm;
#elif defined(__aarch64__)
    return Arch::kArm64;
#elif defined(__riscv)
    return Arch::kRiscv64;
#else
#error "unsupported architecture"
#endif
}

inline static constexpr auto kArch = GetArch();

template <typename T>
constexpr inline auto RoundUpTo(T v, size_t size) {
    return v + size - 1 - ((v + size - 1) & (size - 1));
}

inline auto GetAndroidApiLevel() {
    static auto kApiLevel = []() {
        std::array<char, PROP_VALUE_MAX> prop_value;
        __system_property_get("ro.build.version.sdk", prop_value.data());
        int base = atoi(prop_value.data());
        __system_property_get("ro.build.version.preview_sdk", prop_value.data());
        return base + atoi(prop_value.data());
    }();
    return kApiLevel;
}

inline auto IsJavaDebuggable(JNIEnv *env) {
    static auto kDebuggable = [&env]() {
        auto sdk_int = GetAndroidApiLevel();
        if (sdk_int < __ANDROID_API_P__) {
            return false;
        }
        auto runtime_class = JNI_FindClass(env, "dalvik/system/VMRuntime");
        if (!runtime_class) {
            LOGE("Failed to find VMRuntime");
            return false;
        }
        auto get_runtime_method =
            JNI_GetStaticMethodID(env, runtime_class, "getRuntime", "()Ldalvik/system/VMRuntime;");
        if (!get_runtime_method) {
            LOGE("Failed to find VMRuntime.getRuntime()");
            return false;
        }
        auto is_debuggable_method = JNI_GetMethodID(env, runtime_class, "isJavaDebuggable", "()Z");
        if (!is_debuggable_method) {
            LOGE("Failed to find VMRuntime.isJavaDebuggable()");
            return false;
        }
        auto runtime = JNI_CallStaticObjectMethod(env, runtime_class, get_runtime_method);
        if (!runtime) {
            LOGE("Failed to get VMRuntime");
            return false;
        }
        bool is_debuggable = JNI_CallBooleanMethod(env, runtime, is_debuggable_method);
        LOGD("java runtime debuggable %s", is_debuggable ? "true" : "false");
        return is_debuggable;
    }();
    return kDebuggable;
}

inline static constexpr auto kPointerSize = sizeof(void *);

namespace art {
class ArtMethod;
namespace dex {
class ClassDef;
}
namespace mirror {
class Class;
}
}  // namespace art

namespace {
// target, backup
inline absl::flat_hash_map<art::ArtMethod *, std::pair<jobject, art::ArtMethod *>> hooked_methods_;
inline std::shared_mutex hooked_methods_lock_;

inline absl::flat_hash_map<const art::dex::ClassDef *, absl::flat_hash_set<art::ArtMethod *>>
    hooked_classes_;
inline std::shared_mutex hooked_classes_lock_;

inline absl::flat_hash_set<art::ArtMethod *> deoptimized_methods_set_;
inline std::shared_mutex deoptimized_methods_lock_;

inline absl::flat_hash_map<const art::dex::ClassDef *, absl::flat_hash_set<art::ArtMethod *>>
    deoptimized_classes_;
inline std::shared_mutex deoptimized_classes_lock_;

inline std::list<std::pair<art::ArtMethod *, art::ArtMethod *>> jit_movements_;
inline std::shared_mutex jit_movements_lock_;
}  // namespace

inline art::ArtMethod *IsHooked(art::ArtMethod *art_method, bool including_backup = false) {
    std::shared_lock lk(hooked_methods_lock_);
    if (auto it = hooked_methods_.find(art_method);
        it != hooked_methods_.end() && (!including_backup || it->second.first)) {
        return it->second.second;
    }
    return nullptr;
}

inline art::ArtMethod *IsBackup(art::ArtMethod *art_method) {
    std::shared_lock lk(hooked_methods_lock_);
    if (auto it = hooked_methods_.find(art_method);
        it != hooked_methods_.end() && !it->second.first) {
        return it->second.second;
    }
    return nullptr;
}

inline bool IsDeoptimized(art::ArtMethod *art_method) {
    std::shared_lock lk(deoptimized_methods_lock_);
    return deoptimized_methods_set_.contains(art_method);
}

inline std::list<std::pair<art::ArtMethod *, art::ArtMethod *>> GetJitMovements() {
    std::unique_lock lk(jit_movements_lock_);
    return std::move(jit_movements_);
}

inline void RecordHooked(art::ArtMethod *target, const art::dex::ClassDef *class_def,
                         jobject reflected_backup, art::ArtMethod *backup) {
    {
        std::unique_lock lk(hooked_classes_lock_);
        hooked_classes_[class_def].emplace(target);
    }
    {
        std::unique_lock lk(hooked_methods_lock_);
        hooked_methods_[target] = {reflected_backup, backup};
        hooked_methods_[backup] = {nullptr, target};
    }
}

inline void RecordDeoptimized(const art::dex::ClassDef *class_def, art::ArtMethod *art_method) {
    {
        std::unique_lock lk(deoptimized_classes_lock_);
        deoptimized_classes_[class_def].emplace(art_method);
    }
    {
        std::unique_lock lk(deoptimized_methods_lock_);
        deoptimized_methods_set_.insert(art_method);
    }
}

inline void RecordJitMovement(art::ArtMethod *target, art::ArtMethod *backup) {
    std::unique_lock lk(jit_movements_lock_);
    jit_movements_.emplace_back(target, backup);
}

}  // namespace lsplant
