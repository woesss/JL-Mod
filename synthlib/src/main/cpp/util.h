#include <jni.h>

#ifndef JL_MOD_UTIL_H
#define JL_MOD_UTIL_H

class JniStringPtr {
public:
    JniStringPtr(JNIEnv *env, jstring ref)
            : env(env), ref(ref), ptr(env->GetStringUTFChars(ref, nullptr)) {}

    ~JniStringPtr() {
        env->ReleaseStringUTFChars(ref, ptr);
    }

private:
    JNIEnv *env;
    jstring ref;
public:
    const char *ptr;
};
#endif //JL_MOD_UTIL_H
