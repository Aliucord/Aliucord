/*
 * This file is part of LSPosed.
 *
 * LSPosed is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LSPosed is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LSPosed.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2022 LSPosed Contributors
 */

#pragma once

#include "utils/hook_helper.hpp"

namespace lsplant::art {

class Runtime {
public:
    enum class RuntimeDebugState {
        // This doesn't support any debug features / method tracing. This is the expected state
        // usually.
        kNonJavaDebuggable,
        // This supports method tracing and a restricted set of debug features (for ex: redefinition
        // isn't supported). We transition to this state when method tracing has started or when the
        // debugger was attached and transition back to NonDebuggable once the tracing has stopped /
        // the debugger agent has detached..
        kJavaDebuggable,
        // The runtime was started as a debuggable runtime. This allows us to support the extended
        // set
        // of debug features (for ex: redefinition). We never transition out of this state.
        kJavaDebuggableAtInit
    };

private:
    inline static Runtime *instance_;

    CREATE_MEM_FUNC_SYMBOL_ENTRY(void, SetJavaDebuggable, void *thiz, bool value) {
        SetJavaDebuggableSym(thiz, value);
    }

    CREATE_MEM_FUNC_SYMBOL_ENTRY(void, SetRuntimeDebugState, void *thiz, RuntimeDebugState value) {
        SetRuntimeDebugStateSym(thiz, value);
    }

    inline static size_t debug_state_offset = 0U;

public:
    inline static Runtime *Current() { return instance_; }

    void SetJavaDebuggable(RuntimeDebugState value) {
        if (SetJavaDebuggableSym) {
            SetJavaDebuggable(this, value != RuntimeDebugState::kNonJavaDebuggable);
        } else if (debug_state_offset > 0) {
            *reinterpret_cast<RuntimeDebugState *>(reinterpret_cast<uintptr_t>(instance_) +
                                                   debug_state_offset) = value;
        }
    }

    static bool Init(const HookHandler &handler) {
        int sdk_int = GetAndroidApiLevel();
        if (void **instance; !RETRIEVE_FIELD_SYMBOL(instance, "_ZN3art7Runtime9instance_E")) {
            return false;
        } else if (instance_ = reinterpret_cast<Runtime *>(*instance); !instance_) {
            return false;
        }
        LOGD("runtime instance = %p", instance_);
        if (sdk_int >= __ANDROID_API_O__) {
            if (!RETRIEVE_MEM_FUNC_SYMBOL(SetJavaDebuggable,
                                          "_ZN3art7Runtime17SetJavaDebuggableEb") &&
                !RETRIEVE_MEM_FUNC_SYMBOL(
                    SetRuntimeDebugState,
                    "_ZN3art7Runtime20SetRuntimeDebugStateENS0_17RuntimeDebugStateE")) {
                return false;
            }
        }
        if (SetRuntimeDebugStateSym) {
            static constexpr size_t kLargeEnoughSizeForRuntime = 4096;
            std::array<uint8_t, kLargeEnoughSizeForRuntime> code;
            static_assert(static_cast<int>(RuntimeDebugState::kJavaDebuggable) != 0);
            static_assert(static_cast<int>(RuntimeDebugState::kJavaDebuggableAtInit) != 0);
            code.fill(uint8_t{0});
            auto *const fake_runtime = reinterpret_cast<Runtime *>(code.data());
            SetRuntimeDebugState(fake_runtime, RuntimeDebugState::kJavaDebuggable);
            for (size_t i = 0; i < kLargeEnoughSizeForRuntime; ++i) {
                if (*reinterpret_cast<RuntimeDebugState *>(
                        reinterpret_cast<uintptr_t>(fake_runtime) + i) ==
                    RuntimeDebugState::kJavaDebuggable) {
                    LOGD("found debug_state at offset %zu", i);
                    debug_state_offset = i;
                    break;
                }
            }
            if (debug_state_offset == 0) {
                LOGE("failed to find debug_state");
                return false;
            }
        }
        return true;
    }
};
}  // namespace lsplant::art
