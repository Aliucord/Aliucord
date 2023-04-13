#pragma once

namespace lsplant::art::gc {
// Which types of collections are able to be performed.
enum CollectorType {
    // No collector selected.
    kCollectorTypeNone,
    // Non concurrent mark-sweep.
    kCollectorTypeMS,
    // Concurrent mark-sweep.
    kCollectorTypeCMS,
    // Semi-space / mark-sweep hybrid, enables compaction.
    kCollectorTypeSS,
    // Heap trimming collector, doesn't do any actual collecting.
    kCollectorTypeHeapTrim,
    // A (mostly) concurrent copying collector.
    kCollectorTypeCC,
    // The background compaction of the concurrent copying collector.
    kCollectorTypeCCBackground,
    // Instrumentation critical section fake collector.
    kCollectorTypeInstrumentation,
    // Fake collector for adding or removing application image spaces.
    kCollectorTypeAddRemoveAppImageSpace,
    // Fake collector used to implement exclusion between GC and debugger.
    kCollectorTypeDebugger,
    // A homogeneous space compaction collector used in background transition
    // when both foreground and background collector are CMS.
    kCollectorTypeHomogeneousSpaceCompact,
    // Class linker fake collector.
    kCollectorTypeClassLinker,
    // JIT Code cache fake collector.
    kCollectorTypeJitCodeCache,
    // Hprof fake collector.
    kCollectorTypeHprof,
    // Fake collector for installing/removing a system-weak holder.
    kCollectorTypeAddRemoveSystemWeakHolder,
    // Fake collector type for GetObjectsAllocated
    kCollectorTypeGetObjectsAllocated,
    // Fake collector type for ScopedGCCriticalSection
    kCollectorTypeCriticalSection,
};
}  // namespace lsplant::art::gc
