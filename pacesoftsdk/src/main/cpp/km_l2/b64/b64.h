//
// Created by dell on 1/17/2023.
//

#ifndef PACESOFT_ANDROID_B64_H
#define PACESOFT_ANDROID_B64_H

#include <stddef.h>

#ifdef __cpluplus
#include "stdlib.h"
extern "C"
{
#endif
char *base64_encode(const unsigned char *data,
                    size_t input_length,
                    size_t *output_length);

unsigned char *base64_decode(const char *data,
                             size_t input_length,
                             size_t *output_length);

#ifdef __cpluplus
}
#endif
#endif //PACESOFT_ANDROID_B64_H
