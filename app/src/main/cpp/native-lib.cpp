#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_myapplication_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}



JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {


    JNIEnv *env = NULL;
    jint result = -1;
    if ((vm)->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        return -1;
    }




    result = JNI_VERSION_1_4;


    return result;
}
