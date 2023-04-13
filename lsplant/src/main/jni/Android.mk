LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE           := lsplant
LOCAL_C_INCLUDES       := $(LOCAL_PATH)/include $(LOCAL_PATH)/external/dex_builder/include
LOCAL_SRC_FILES        := lsplant.cc
LOCAL_EXPORT_C_INCLUDES:= $(LOCAL_PATH)/include
LOCAL_SHARED_LIBRARIES := dex_builder
LOCAL_LDLIBS           := -llog
LOCAL_EXPORT_LDLIBS    := $(LOCAL_LDLIBS)
LOCAL_CFLAGS           := -flto
LOCAL_LDFLAGS          := -flto
include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)

LOCAL_MODULE           := lsplant_static
LOCAL_C_INCLUDES       := $(LOCAL_PATH)/include $(LOCAL_PATH)/external/dex_builder/include
LOCAL_SRC_FILES        := lsplant.cc
LOCAL_EXPORT_C_INCLUDES:= $(LOCAL_PATH)/include
LOCAL_STATIC_LIBRARIES := dex_builder_static
LOCAL_EXPORT_LDLIBS    := -llog
include $(BUILD_STATIC_LIBRARY)

include jni/external/dex_builder/Android.mk

