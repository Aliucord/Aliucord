#pragma once

#include "art/runtime/thread.hpp"
#include "collector_type.hpp"
#include "common.hpp"
#include "gc_cause.hpp"

namespace lsplant::art::gc {

class GCCriticalSection {
private:
    [[maybe_unused]] void *self_;
    [[maybe_unused]] const char *section_name_;
};

class ScopedGCCriticalSection {
    CREATE_MEM_FUNC_SYMBOL_ENTRY(void, constructor, ScopedGCCriticalSection *thiz, Thread *self,
                                 GcCause cause, CollectorType collector_type) {
        if (thiz && constructorSym) [[likely]]
            return constructorSym(thiz, self, cause, collector_type);
    }

    CREATE_MEM_FUNC_SYMBOL_ENTRY(void, destructor, ScopedGCCriticalSection *thiz) {
        if (thiz && destructorSym) [[likely]]
            destructorSym(thiz);
    }

public:
    ScopedGCCriticalSection(Thread *self, GcCause cause, CollectorType collector_type) {
        constructor(this, self, cause, collector_type);
    }

    ~ScopedGCCriticalSection() { destructor(this); }

    static bool Init(const HookHandler &handler) {
        // for Android M, it's safe to not found since we have suspendVM & resumeVM
        auto sdk_int = GetAndroidApiLevel();
        if (sdk_int >= __ANDROID_API_N__) [[likely]] {
            if (!RETRIEVE_MEM_FUNC_SYMBOL(constructor,
                                          "_ZN3art2gc23ScopedGCCriticalSectionC2EPNS_6ThreadENS0_"
                                          "7GcCauseENS0_13CollectorTypeE")) [[unlikely]] {
                return false;
            }
            if (!RETRIEVE_MEM_FUNC_SYMBOL(destructor, "_ZN3art2gc23ScopedGCCriticalSectionD2Ev"))
                [[unlikely]] {
                return false;
            }
        }
        return true;
    }

private:
    [[maybe_unused]] GCCriticalSection critical_section_;
    [[maybe_unused]] const char *old_no_suspend_reason_;
};
}  // namespace lsplant::art::gc
