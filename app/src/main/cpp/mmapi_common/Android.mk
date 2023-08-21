LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_SRC_FILES = \
	src/PlayerListener.cpp \
	src/BasePlayer.cpp \
#	Player.cpp \
	mmapi_jni.cpp \

LOCAL_CFLAGS += -O2 \

LOCAL_C_INCLUDES := $(LOCAL_PATH)/include/mmapi \
	$(LOCAL_PATH)/include/mmapi/midi \

LOCAL_MODULE := mmapi_common

LOCAL_SHARED_LIBRARIES := oboe util

LOCAL_EXPORT_C_INCLUDES  :=	$(LOCAL_PATH)/include
LOCAL_EXPORT_CFLAGS := $(LOCAL_CFLAGS)

ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
    LOCAL_ARM_NEON := false
endif

# Don't strip debug builds
ifeq ($(NDK_DEBUG),1)
    cmd-strip :=
endif

include $(BUILD_SHARED_LIBRARY)

$(call import-module,prefab/oboe)