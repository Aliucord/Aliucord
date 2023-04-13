#pragma once

#include <type_traits>

#include "object_reference.hpp"
#include "value_object.hpp"

namespace lsplant::art {

namespace mirror {
class Class;
};

template <typename T>
class Handle : public ValueObject {
public:
    Handle(const Handle<T>& handle) : reference_(handle.reference_) {}

    Handle<T>& operator=(const Handle<T>& handle) {
        reference_ = handle.reference_;
        return *this;
    }
    static_assert(std::is_same_v<T, mirror::Class>, "Expected mirror::Class");

    auto operator->() { return Get(); }

    T* Get() { return down_cast<T*>(reference_->AsMirrorPtr()); }

protected:
    StackReference<T>* reference_;
};

static_assert(!std::is_trivially_copyable_v<Handle<mirror::Class>>);

// https://cs.android.com/android/_/android/platform/art/+/38cea84b362a10859580e788e984324f36272817
template <typename T>
class TrivialHandle : public ValueObject {
public:
    static_assert(std::is_same_v<T, mirror::Class>, "Expected mirror::Class");

    auto operator->() { return Get(); }

    T* Get() { return down_cast<T*>(reference_->AsMirrorPtr()); }

protected:
    StackReference<T>* reference_;
};
static_assert(std::is_trivially_copyable_v<TrivialHandle<mirror::Class>>);

}  // namespace lsplant::art
