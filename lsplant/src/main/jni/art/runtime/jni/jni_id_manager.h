#pragma once

#include "art/runtime/art_method.hpp"
#include "art/runtime/reflective_handle.hpp"
#include "common.hpp"

namespace lsplant::art::jni {

class JniIdManager {
private:
    CREATE_MEM_HOOK_STUB_ENTRY(
        "_ZN3art3jni12JniIdManager15EncodeGenericIdINS_9ArtMethodEEEmNS_16ReflectiveHandleIT_EE",
        uintptr_t, EncodeGenericId, (JniIdManager * thiz, ReflectiveHandle<ArtMethod> method), {
            if (auto target = IsBackup(method.Get()); target) {
                LOGD("get generic id for %s", method.Get()->PrettyMethod().c_str());
                method.Set(target);
            }
            return backup(thiz, method);
        });

public:
    static bool Init(JNIEnv *env, const HookHandler &handler) {
        int sdk_int = GetAndroidApiLevel();
        if (sdk_int >= __ANDROID_API_R__) {
            if (IsJavaDebuggable(env) && !HookSyms(handler, EncodeGenericId)) {
                LOGW("Failed to hook EncodeGenericId, attaching debugger may crash the process");
            }
        }
        return true;
    }
};

}  // namespace lsplant::art::jni
