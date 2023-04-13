#pragma once

#include "art_method.hpp"
#include "common.hpp"

namespace lsplant::art {

class Instrumentation {
    inline static ArtMethod *MaybeUseBackupMethod(ArtMethod *art_method, const void *quick_code) {
        if (auto backup = IsHooked(art_method); backup && art_method->GetEntryPoint() != quick_code)
            [[unlikely]] {
            LOGD("Propagate update method code %p for hooked method %s to its backup", quick_code,
                 art_method->PrettyMethod().c_str());
            return backup;
        }
        return art_method;
    }

    CREATE_MEM_HOOK_STUB_ENTRY(
        "_ZN3art15instrumentation15Instrumentation40UpdateMethodsCodeToInterpreterEntryPointEPNS_9ArtMethodE",
        void, UpdateMethodsCodeToInterpreterEntryPoint,
        (Instrumentation * thiz, ArtMethod *art_method), {
            if (IsDeoptimized(art_method)) {
                LOGV("skip update entrypoint on deoptimized method %s",
                     art_method->PrettyMethod(true).c_str());
                return;
            }
            backup(thiz, MaybeUseBackupMethod(art_method, nullptr));
        });

    CREATE_MEM_HOOK_STUB_ENTRY(
        "_ZN3art15instrumentation15Instrumentation21InitializeMethodsCodeEPNS_9ArtMethodEPKv", void,
        InitializeMethodsCode,
        (Instrumentation * thiz, ArtMethod *art_method, const void *quick_code), {
            if (IsDeoptimized(art_method)) {
                LOGV("skip update entrypoint on deoptimized method %s",
                     art_method->PrettyMethod(true).c_str());
                return;
            }
            backup(thiz, MaybeUseBackupMethod(art_method, quick_code), quick_code);
        });

public:
    static bool Init(JNIEnv *env, const HookHandler &handler) {
        if (!IsJavaDebuggable(env)) [[likely]] {
            return true;
        }
        int sdk_int = GetAndroidApiLevel();
        if (sdk_int >= __ANDROID_API_P__) [[likely]] {
            if (!HookSyms(handler, InitializeMethodsCode,
                          UpdateMethodsCodeToInterpreterEntryPoint)) {
                return false;
            }
        }
        return true;
    }
};

}  // namespace lsplant::art
