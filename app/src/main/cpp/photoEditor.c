#include "jni.h"
#include "math.h"
#include <stdlib.h>

JNIEXPORT void JNICALL
Java_com_example_photoeditingapp_MainActivity_blackAndWhite(JNIEnv *env, jclass clazz,
                                                            jintArray pixels_, jint width,
                                                            jint height) {
    jint *pixels = (*env)->GetIntArrayElements(env, pixels_, NULL);

    char *colors = (char*) pixels;
    int pixelCount = width * height * 4;
    for (int i = 0; i < pixelCount; i+=4) {
        unsigned char average = (colors[i] + colors[i+1] + colors[i+2])/3;
        colors[i] = average;
        colors[i+1] = average;
        colors[i+2] = average;
    }
    (*env)->ReleaseIntArrayElements(env,pixels_,pixels,0);
}

JNIEXPORT void JNICALL
Java_com_example_photoeditingapp_MainActivity_pastelFilter(JNIEnv *env, jclass clazz,
                                                         jintArray pixels_, jint width,
                                                         jint height) {
    // TODO: implement dawnFilter()
    jint *pixels = (*env)->GetIntArrayElements(env, pixels_, NULL);
    char *colors = (char*) pixels;
    int pixelCount = width * height * 4,r,g,b;
    for (int i = 0; i < pixelCount; i+=4) {

        r = colors[i];
        g = colors[i+1];
        b = colors[i+2];

        int tempR = r, tempG = g, tempB = b;
        int pastelR = (tempR/2) + 127;
        int pastelG = (tempG/2) + 127;
        int pastelB = (tempB/2) + 127;

        colors[i] = pastelR;
        colors[i+1] = pastelG;
        colors[i+2] = pastelB;

    }
    (*env)->ReleaseIntArrayElements(env,pixels_,pixels,0);
}

JNIEXPORT void JNICALL
Java_com_example_photoeditingapp_MainActivity_dilationFilter(JNIEnv *env, jclass clazz,
                                                             jintArray pixels_, jint width,
                                                             jint height) {
    // TODO: implement dilationFilter()
    jint *pixels = (*env)->GetIntArrayElements(env, pixels_, NULL);
    char *colors = (char*) pixels;
    int pixelCount = width*height*4;
    for (int i = 0; i < pixelCount; i+=4) {
        colors[i] = (pixels[i] + pixels[i+1] + pixels[i+2])/3;
    }
    for (int i = 0; i < pixelCount; i+=4) {
        if (colors[i] > 100)
            colors[i] = 255;
        else
            colors[i] = 0;
    }
    for (int i = 0; i < pixelCount; i+=4) {
        float value = 0.0f;
        for (int ky = -1; ky <= 1; ky++) {
            for (int kx = -1; kx <= 1; kx++) {
                value += colors[(i+ky)+(i+kx)]/255;
            }
        }
        if (value >= 1)
            colors[i] = 255;
        else
            colors[i] = 0;
    }

    (*env)->ReleaseIntArrayElements(env,pixels_,pixels,0);
}

JNIEXPORT void JNICALL
Java_com_example_photoeditingapp_MainActivity_contrastFilter(JNIEnv *env, jclass clazz,
                                                             jintArray pixels_, jint width,
                                                             jint height) {
    // TODO: implement contrastFilter()
    jint *pixels = (*env)->GetIntArrayElements(env, pixels_, NULL);
    char *colors = (char*) pixels;
    int pixelCount = width * height * 4, r,g,b, hue, saturation, brightness;
    for (int i = 0; i < pixelCount; i+=4) {
        r = colors[i];
        g = colors[i+1];
        b = colors[i+2];
        if (r >= 100 && r <= 255-20)
            r += 20;
        else if (r > 255-20)
            r = 255;
        else if (r <= 40 && r >= 20)
            r-=20;
        else if (r < 20)
            r = 0;
        else if (r > 70 && r < 100)
            r += 10;
        else if (r < 60 && r > 40)
            r -= 10;

        if (g >= 100 && g <= 255-20)
            g += 20;
        else if (g > 255-20)
            g = 255;
        else if (g <= 40 && g >= 20)
            g-=20;
        else if (g < 20)
            g = 0;
        else if (g > 70 && g < 100)
            g += 10;
        else if (g < 60 && g > 40)
            g -= 10;

        if (b >= 100 && b <= 255-20)
            b += 20;
        else if (b > 255-20)
            b = 255;
        else if (b <= 40 && b >= 20)
            b-=20;
        else if (b < 20)
            b = 0;
        else if (b > 70 && b < 100)
            b += 10;
        else if (b < 60 && b > 40)
            b -= 10;

        colors[i] = r;
        colors[i+1] = g;
        colors[i+2] = b;
    }
    (*env)->ReleaseIntArrayElements(env,pixels_,pixels,0);
}