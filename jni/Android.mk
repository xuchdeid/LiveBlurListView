LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := blurjni
LOCAL_CFLAGS    := -Werror 
LOCAL_SRC_FILES := Blur.cpp

LOCAL_LDLIBS    := -llog -lGLESv2 -lm -ljnigraphics


include $(BUILD_SHARED_LIBRARY)
