#pragma once

#include "common.hpp"

namespace lsplant::art::thread_list {

class ScopedSuspendAll {
    CREATE_MEM_FUNC_SYMBOL_ENTRY(void, constructor, ScopedSuspendAll *thiz, const char *cause,
                                 bool long_suspend) {
        if (thiz && constructorSym) [[likely]] {
            return constructorSym(thiz, cause, long_suspend);
        } else {
            SuspendVM();
        }
    }

    CREATE_MEM_FUNC_SYMBOL_ENTRY(void, destructor, ScopedSuspendAll *thiz) {
        if (thiz && destructorSym) [[likely]] {
            return destructorSym(thiz);
        } else {
            ResumeVM();
        }
    }

    CREATE_FUNC_SYMBOL_ENTRY(void, SuspendVM) {
        if (SuspendVMSym) [[likely]] {
            SuspendVMSym();
        }
    }

    CREATE_FUNC_SYMBOL_ENTRY(void, ResumeVM) {
        if (ResumeVMSym) [[likely]] {
            ResumeVMSym();
        }
    }

public:
    ScopedSuspendAll(const char *cause, bool long_suspend) {
        constructor(this, cause, long_suspend);
    }

    ~ScopedSuspendAll() { destructor(this); }

    static bool Init(const HookHandler &handler) {
        if (!RETRIEVE_MEM_FUNC_SYMBOL(constructor, "_ZN3art16ScopedSuspendAllC2EPKcb") &&
            !RETRIEVE_FUNC_SYMBOL(SuspendVM, "_ZN3art3Dbg9SuspendVMEv")) [[unlikely]] {
            return false;
        }
        if (!RETRIEVE_MEM_FUNC_SYMBOL(destructor, "_ZN3art16ScopedSuspendAllD2Ev") &&
            !RETRIEVE_FUNC_SYMBOL(ResumeVM, "_ZN3art3Dbg8ResumeVMEv")) [[unlikely]] {
            return false;
        }
        return true;
    }
};

}  // namespace lsplant::art::thread_list
