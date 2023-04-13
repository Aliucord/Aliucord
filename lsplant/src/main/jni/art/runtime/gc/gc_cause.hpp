#pragma once
namespace lsplant::art::gc {
// What caused the GC?
enum GcCause {
    // Invalid GC cause used as a placeholder.
    kGcCauseNone,
    // GC triggered by a failed allocation. Thread doing allocation is blocked waiting for GC before
    // retrying allocation.
    kGcCauseForAlloc,
    // A background GC trying to ensure there is free memory ahead of allocations.
    kGcCauseBackground,
    // An explicit System.gc() call.
    kGcCauseExplicit,
    // GC triggered for a native allocation when NativeAllocationGcWatermark is exceeded.
    // (This may be a blocking GC depending on whether we run a non-concurrent collector).
    kGcCauseForNativeAlloc,
    // GC triggered for a collector transition.
    kGcCauseCollectorTransition,
    // Not a real GC cause, used when we disable moving GC (currently for
    // GetPrimitiveArrayCritical).
    kGcCauseDisableMovingGc,
    // Not a real GC cause, used when we trim the heap.
    kGcCauseTrim,
    // Not a real GC cause, used to implement exclusion between GC and instrumentation.
    kGcCauseInstrumentation,
    // Not a real GC cause, used to add or remove app image spaces.
    kGcCauseAddRemoveAppImageSpace,
    // Not a real GC cause, used to implement exclusion between GC and debugger.
    kGcCauseDebugger,
    // GC triggered for background transition when both foreground and background collector are CMS.
    kGcCauseHomogeneousSpaceCompact,
    // Class linker cause, used to guard filling art methods with special values.
    kGcCauseClassLinker,
    // Not a real GC cause, used to implement exclusion between code cache metadata and GC.
    kGcCauseJitCodeCache,
    // Not a real GC cause, used to add or remove system-weak holders.
    kGcCauseAddRemoveSystemWeakHolder,
    // Not a real GC cause, used to prevent hprof running in the middle of GC.
    kGcCauseHprof,
    // Not a real GC cause, used to prevent GetObjectsAllocated running in the middle of GC.
    kGcCauseGetObjectsAllocated,
    // GC cause for the profile saver.
    kGcCauseProfileSaver,
};
}  // namespace lsplant::art::gc
