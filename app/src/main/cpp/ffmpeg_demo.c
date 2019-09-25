#include <jni.h>
#include "ffmpeg.h"
#include <malloc.h>

JNIEXPORT jint JNICALL
Java_com_coder_hcj_ffmpeg_ffm_CommandExecutor_runCommand(JNIEnv *env, jclass clazz, jint length,
                                                   jobjectArray arrCmd) {
    int i = 0;
    char **argv = NULL;
    jstring *strr = NULL;

    if (arrCmd != NULL) {
        argv = (char **) malloc(sizeof(char *) * length);
        strr = (jstring *) malloc(sizeof(jstring) * length);

        for (i = 0; i < length; ++i) {
            strr[i] = (jstring) (*env)->GetObjectArrayElement(env, arrCmd, i);
            argv[i] = (char *) (*env)->GetStringUTFChars(env, strr[i], 0);
        }

    }
    ffmpeg_run_cmd(length, argv);

    for (int i = 0; i < length; i++) {
        free(argv[i]);
    }
    free(argv);
    free(strr);
    return 0;
}

