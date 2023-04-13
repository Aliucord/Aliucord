#pragma once

#include <cstdint>

namespace lsplant::art {
template <bool kPoisonReferences, class MirrorType>
class alignas(4) [[gnu::packed]] ObjectReference {
    static MirrorType* Decompress(uint32_t ref) {
        uintptr_t as_bits = kPoisonReferences ? -ref : ref;
        return reinterpret_cast<MirrorType*>(as_bits);
    }

    uint32_t reference_;

public:
    MirrorType* AsMirrorPtr() const { return Decompress(reference_); }
};

template <class MirrorType>
class alignas(4) [[gnu::packed]] CompressedReference : public ObjectReference<false, MirrorType> {};

template <class MirrorType>
class alignas(4) [[gnu::packed]] StackReference : public CompressedReference<MirrorType> {};

template <typename To, typename From>  // use like this: down_cast<T*>(foo);
inline To down_cast(From* f) {         // so we only accept pointers
    static_assert(std::is_base_of_v<From, std::remove_pointer_t<To>>,
                  "down_cast unsafe as To is not a subtype of From");

    return static_cast<To>(f);
}
}  // namespace lsplant::art
