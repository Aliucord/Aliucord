#pragma once

#include <type_traits>

namespace lsplant::art {
template <class ReflectiveType>
class ReflectiveReference {
public:
    static_assert(std::is_same_v<ReflectiveType, ArtMethod>, "Unknown type!");

    ReflectiveType *Ptr() { return val_; }

    void Assign(ReflectiveType *r) { val_ = r; }

private:
    ReflectiveType *val_;
};

}  // namespace lsplant::art
