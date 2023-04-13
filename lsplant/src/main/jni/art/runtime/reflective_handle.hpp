#pragma once

#include <type_traits>

#include "reflective_reference.hpp"
#include "value_object.hpp"

namespace lsplant::art {

class ArtMethod;

template <typename T>
class ReflectiveHandle : public ValueObject {
public:
    static_assert(std::is_same_v<T, ArtMethod>, "Expected ArtField or ArtMethod");

    T *Get() { return reference_->Ptr(); }

    void Set(T *val) { reference_->Assign(val); }

protected:
    ReflectiveReference<T> *reference_;
};

}  // namespace lsplant::art
