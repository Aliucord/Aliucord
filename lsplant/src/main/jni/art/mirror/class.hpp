#pragma once

#include "art/runtime/art_method.hpp"
#include "art/runtime/handle.hpp"
#include "common.hpp"

namespace lsplant::art {
class Thread;
namespace dex {
class ClassDef {};
}  // namespace dex

namespace mirror {

class Class {
private:
    CREATE_MEM_FUNC_SYMBOL_ENTRY(const char *, GetDescriptor, Class *thiz, std::string *storage) {
        if (GetDescriptorSym) [[likely]]
            return GetDescriptorSym(thiz, storage);
        else
            return "";
    }

    CREATE_MEM_FUNC_SYMBOL_ENTRY(const dex::ClassDef *, GetClassDef, Class *thiz) {
        if (GetClassDefSym) [[likely]]
            return GetClassDefSym(thiz);
        return nullptr;
    }

    using BackupMethods = std::list<std::tuple<art::ArtMethod *, void *>>;
    inline static absl::flat_hash_map<const art::Thread *,
                                      absl::flat_hash_map<const dex::ClassDef *, BackupMethods>>
        backup_methods_;
    inline static std::mutex backup_methods_lock_;

    static void BackupClassMethods(const dex::ClassDef *class_def, art::Thread *self) {
        std::list<std::tuple<art::ArtMethod *, void *>> out;
        if (!class_def) return;
        {
            std::shared_lock lk(hooked_classes_lock_);
            if (auto found = hooked_classes_.find(class_def); found != hooked_classes_.end())
                [[unlikely]] {
                for (auto method : found->second) {
                    if (method->IsStatic()) {
                        LOGV("Backup hooked method %p because of initialization", method);
                        out.emplace_back(method, method->GetEntryPoint());
                    }
                }
            }
        }
        {
            std::shared_lock lk(deoptimized_methods_lock_);
            if (auto found = deoptimized_classes_.find(class_def);
                found != deoptimized_classes_.end()) [[unlikely]] {
                for (auto method : found->second) {
                    if (method->IsStatic()) {
                        LOGV("Backup deoptimized method %p because of initialization", method);
                        out.emplace_back(method, method->GetEntryPoint());
                    }
                }
            }
        }
        if (!out.empty()) [[unlikely]] {
            std::unique_lock lk(backup_methods_lock_);
            backup_methods_[self].emplace(class_def, std::move(out));
        }
    }

    CREATE_HOOK_STUB_ENTRY(
        "_ZN3art6mirror5Class9SetStatusENS_6HandleIS1_EENS_11ClassStatusEPNS_6ThreadE", void,
        SetClassStatus, (TrivialHandle<Class> h, uint8_t new_status, Thread *self), {
            if (new_status == initialized_status) {
                BackupClassMethods(h->GetClassDef(), self);
            }
            return backup(h, new_status, self);
        });

    CREATE_HOOK_STUB_ENTRY(
        "_ZN3art6mirror5Class9SetStatusENS_6HandleIS1_EENS1_6StatusEPNS_6ThreadE", void, SetStatus,
        (Handle<Class> h, int new_status, Thread *self), {
            if (new_status == static_cast<int>(initialized_status)) {
                BackupClassMethods(h->GetClassDef(), self);
            }
            return backup(h, new_status, self);
        });

    CREATE_HOOK_STUB_ENTRY(
        "_ZN3art6mirror5Class9SetStatusENS_6HandleIS1_EENS1_6StatusEPNS_6ThreadE", void,
        TrivialSetStatus, (TrivialHandle<Class> h, uint32_t new_status, Thread *self), {
            if (new_status == initialized_status) {
                BackupClassMethods(h->GetClassDef(), self);
            }
            return backup(h, new_status, self);
        });

    CREATE_MEM_HOOK_STUB_ENTRY("_ZN3art6mirror5Class9SetStatusENS1_6StatusEPNS_6ThreadE", void,
                               ClassSetStatus, (Class * thiz, int new_status, Thread *self), {
                                   if (new_status == static_cast<int>(initialized_status)) {
                                       BackupClassMethods(thiz->GetClassDef(), self);
                                   }
                                   return backup(thiz, new_status, self);
                               });

    inline static uint8_t initialized_status = 0;

public:
    static bool Init(const HookHandler &handler) {
        if (!RETRIEVE_MEM_FUNC_SYMBOL(GetDescriptor,
                                      "_ZN3art6mirror5Class13GetDescriptorEPNSt3__112"
                                      "basic_stringIcNS2_11char_traitsIcEENS2_9allocatorIcEEEE")) {
            return false;
        }
        if (!RETRIEVE_MEM_FUNC_SYMBOL(GetClassDef, "_ZN3art6mirror5Class11GetClassDefEv")) {
            return false;
        }

        int sdk_int = GetAndroidApiLevel();

        if (sdk_int < __ANDROID_API_O__) {
            if (!HookSyms(handler, SetStatus, ClassSetStatus)) {
                return false;
            }
        } else {
            if (!HookSyms(handler, SetClassStatus, TrivialSetStatus)) {
                return false;
            }
        }

        if (sdk_int >= __ANDROID_API_R__) {
            initialized_status = 15;
        } else if (sdk_int >= __ANDROID_API_P__) {
            initialized_status = 14;
        } else if (sdk_int == __ANDROID_API_O_MR1__) {
            initialized_status = 11;
        } else {
            initialized_status = 10;
        }

        return true;
    }

    const char *GetDescriptor(std::string *storage) {
        if (GetDescriptorSym) {
            return GetDescriptor(this, storage);
        }
        return "";
    }

    std::string GetDescriptor() {
        std::string storage;
        return GetDescriptor(&storage);
    }

    const dex::ClassDef *GetClassDef() {
        if (GetClassDefSym) return GetClassDef(this);
        return nullptr;
    }

    static auto PopBackup(const dex::ClassDef *class_def, art::Thread *self) {
        BackupMethods methods;
        if (!backup_methods_.size()) [[likely]] {
            return methods;
        }
        if (class_def) {
            std::unique_lock lk(backup_methods_lock_);
            for (auto it = backup_methods_.begin(); it != backup_methods_.end();) {
                if (auto found = it->second.find(class_def); found != it->second.end()) {
                    methods.merge(std::move(found->second));
                    it->second.erase(found);
                }
                if (it->second.empty()) {
                    backup_methods_.erase(it++);
                } else {
                    it++;
                }
            }
        } else if (self) {
            std::unique_lock lk(backup_methods_lock_);
            if (auto found = backup_methods_.find(self); found != backup_methods_.end()) {
                for (auto it = found->second.begin(); it != found->second.end();) {
                    methods.merge(std::move(it->second));
                    found->second.erase(it++);
                }
                backup_methods_.erase(found);
            }
        }
        return methods;
    }
};

}  // namespace mirror
}  // namespace lsplant::art
