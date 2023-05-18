//
// Created by dell on 1/16/2023.
//

#ifndef PACESOFT_ANDROID_KMLWRAPPER_H
#define PACESOFT_ANDROID_KMLWRAPPER_H

#include <stdbool.h>
#include "jni.h"
#if defined(__cplusplus)
extern "C" {
#endif

char* func_zencrypt( char * input_text,
                     char * iv);
char* func_zdecrypt( char * input_text,
                     char * iv);

//jbyteArray getIV();
void setIV(char * paramIV);
uint8_t* hex_str_to_uint8(const char* string);
void clearAmadis(JNIEnv * jniEnv,jobject context);
bool to_hex(char* dest, size_t dest_len, const uint8_t* values, size_t val_len);

//size_t Utils2_func(wchar_t* data, size_t size);

#if defined(__cplusplus)
}
#endif
#endif //PACESOFT_ANDROID_KMLWRAPPER_H
