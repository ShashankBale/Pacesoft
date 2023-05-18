/*****************************************************************
|
|   whiteCryption Secure Key Box
|
|   $Id: SkbInternalHelpers.h 18985 2022-12-29 13:38:40Z kristaps.straupe $
|
|   This software is provided to you pursuant to your Software
|   license agreement (SLA) with whiteCryption Corporation
|   ("whiteCryption"). This software may be used only in accordance
|   with the terms of this agreement.
|
|   Copyright (c) 2000-2023, whiteCryption Corporation. All rights reserved.
|
****************************************************************/

#ifndef SKB_Byte
    #define SKB_Byte l5b64b69185ec3de22a96ba7236cd3eb14edaf4dd6dd0b5d4
#endif
#ifndef SKB_CreateDsaPrivateFromPlain
    #define SKB_CreateDsaPrivateFromPlain l70b9b6f8bdce2678a8f05af0aeb02b7d63e6c60eaaf886af
#endif
#ifndef SKB_CreateEccPrivateFromPlain
    #define SKB_CreateEccPrivateFromPlain l98480c1e4fc1520b9d0f84c0b7ff0bed0d5912e8b01f84c5
#endif
#ifndef SKB_CreateEccPrivateFromPlainParsed
    #define SKB_CreateEccPrivateFromPlainParsed lfa3cf7a3f6ebc18831b55977f6919248dbbb8a41a56d56ee
#endif
#ifndef SKB_CreatePlainFromDsaPrivate
    #define SKB_CreatePlainFromDsaPrivate lb5c2fc54f1eaf9688a135d01f7167c344708975f5b5f59b4
#endif
#ifndef SKB_CreatePlainFromEccPrivate
    #define SKB_CreatePlainFromEccPrivate la51918319f45142300b209575a5ed8d7ec3416e763ffc9a7
#endif
#ifndef SKB_CreatePlainFromRawBytes
    #define SKB_CreatePlainFromRawBytes le75eea147686d27ef4ad22261a6ce5f2a8440bbc42dd7c4f
#endif
#ifndef SKB_CreatePlainFromRsaPrivate
    #define SKB_CreatePlainFromRsaPrivate l22c535e79c119760c196c3f298dc73659248b34e8573f9ae
#endif
#ifndef SKB_CreatePlainFromUnwrapBytes
    #define SKB_CreatePlainFromUnwrapBytes lddef150bbefc06acf9ad1186e3e6ca6e296a1a047464cede
#endif
#ifndef SKB_CreateRawBytesFromPlain
    #define SKB_CreateRawBytesFromPlain lec7052174ef8c8af4b2a0c8d89720e5b77fc467b1ec1ecd4
#endif
#ifndef SKB_CreateRsaPrivateFromPlain
    #define SKB_CreateRsaPrivateFromPlain l3b0d42d01a52cdff84cdd5d2f1399d6832be9f96d2440f51
#endif
#ifndef SKB_CreateRsaPrivateFromPlainPKCS8
    #define SKB_CreateRsaPrivateFromPlainPKCS8 l0d74354c3b39d0799e4b7b613d3ff735fa27f021885449f6
#endif
#ifndef SKB_CreateRsaPublicFromPlain
    #define SKB_CreateRsaPublicFromPlain lee06d0da831307fe80547b28f643270c203b700a8ca1784e
#endif
#ifndef SKB_CreateRsaPublicFromPlainPKCS1
    #define SKB_CreateRsaPublicFromPlainPKCS1 l757884adcebfed16e54f6b2eff52f92cd9dc390062dd537e
#endif
#ifndef SKB_CreateRsaStaticFromPlain
    #define SKB_CreateRsaStaticFromPlain l822356c05e826a1b195ba5a52ec3333fe7cb2099564844cc
#endif
#ifndef SKB_CreateRsaStaticFromPlainPKCS8
    #define SKB_CreateRsaStaticFromPlainPKCS8 lfd5d8b2a900d45f89f83731bf833ee732e1c30b55b31f1c2
#endif
#ifndef SKB_CreateUnwrapBytesFromPlain
    #define SKB_CreateUnwrapBytesFromPlain l25b9d65ba31471ad516f932e63326b5337853eaaf29fedb2
#endif
#ifndef SKB_DataType
    #define SKB_DataType l38eab32d1b9e5691febce38d995faea46411adff4fa1a05e
#endif
#ifndef SKB_Engine
    #define SKB_Engine l350d26f391b037d88e50bfcd33bb13404a551ba84b6eb30e
#endif
#ifndef SKB_Result
    #define SKB_Result l769cdd4d75a535dc4ec65180e1c1de0d5ff65f5a41786f30
#endif
#ifndef SKB_SecureData
    #define SKB_SecureData l8f962bd4e178539de3646be4fb53725b8654bb70294b014b
#endif
#ifndef SKB_Size
    #define SKB_Size lee355d2126eb616f9c74edb4ad3255a07f296366146519ce
#endif
 

#pragma once

#include "SkbSecureKeyBox.h"

#if defined(__cplusplus)
extern "C" {
#endif

SKB_Result SKB_CreateRawBytesFromPlain(const SKB_Engine* engine,
                                       const SKB_Byte*   plain,
                                       const SKB_Size    plain_size,
                                       SKB_SecureData**  data);

SKB_Result SKB_CreatePlainFromRawBytes(const SKB_SecureData* data,
                                       SKB_Byte*             plain,
                                       SKB_Size*             plain_size);

SKB_Result SKB_CreateEccPrivateFromPlain(const SKB_Engine* engine,
                                         const SKB_Byte*   plain,
                                         const SKB_Size    plain_size,
                                         SKB_SecureData**  data);

SKB_Result SKB_CreateEccPrivateFromPlainParsed(const SKB_Engine* engine,
                                               const SKB_Byte*   key_bytes,
                                               const SKB_Size    key_size_bits,
                                               SKB_SecureData**  data);

SKB_Result SKB_CreatePlainFromEccPrivate(const SKB_SecureData* data,
                                         SKB_Byte*             plain,
                                         SKB_Size*             plain_size);

SKB_Result SKB_CreateRsaPrivateFromPlainPKCS8(const SKB_Engine* engine,
                                              const SKB_Byte*   plain,
                                              const SKB_Size    plain_size,
                                              SKB_SecureData**  data);

SKB_Result SKB_CreateRsaPrivateFromPlain(const SKB_Engine* engine,
                                         const SKB_Byte*   plain_p,
                                         const SKB_Byte*   plain_q,
                                         const SKB_Byte*   plain_d,
                                         const SKB_Byte*   plain_n,
                                         const SKB_Size    key_size,
                                         SKB_SecureData**  data);

SKB_Result SKB_CreatePlainFromRsaPrivate(const SKB_SecureData* data,
                                          SKB_Byte*            p,
                                          SKB_Byte*            q,
                                          SKB_Byte*            d,
                                          SKB_Byte*            n,
                                          SKB_Size*            key_size);

SKB_Result SKB_CreateRsaPublicFromPlainPKCS1(const SKB_Engine* engine,
                                             const SKB_Byte*   plain,
                                             const SKB_Size    plain_size,
                                             SKB_SecureData**  data);

SKB_Result SKB_CreateRsaPublicFromPlain(const SKB_Engine* engine,
                                        const SKB_Byte*   plain_e,
                                        const SKB_Byte*   plain_n,
                                        const SKB_Size    key_size,
                                        SKB_SecureData**  data);

SKB_Result SKB_CreateDsaPrivateFromPlain(const SKB_Engine* engine,
                                         const SKB_Byte*   plain,
                                         const SKB_Size    plain_size,
                                         SKB_SecureData**  data);

SKB_Result SKB_CreatePlainFromDsaPrivate(const SKB_SecureData* data,
                                         SKB_Byte*             plain,
                                         SKB_Size*             plain_size);

SKB_Result SKB_CreateUnwrapBytesFromPlain(const SKB_Engine* engine,
                                          const SKB_Byte*   plain,
                                          const SKB_Size    plain_size,
                                          SKB_SecureData**  data);

SKB_Result SKB_CreatePlainFromUnwrapBytes(const SKB_SecureData* data,
                                          SKB_Byte*             plain,
                                          SKB_Size*             plain_size);

SKB_Result SKB_CreateRsaStaticFromPlain(const SKB_Engine*  engine,
                                        const SKB_Byte*    plain_p,
                                        const SKB_Byte*    plain_q,
                                        const SKB_Byte*    plain_d,
                                        const SKB_Byte*    plain_n,
                                        const SKB_Size     key_size,
                                        const SKB_DataType type,
                                        SKB_SecureData**   data);

SKB_Result SKB_CreateRsaStaticFromPlainPKCS8(const SKB_Engine*  engine,
                                             const SKB_Byte*    key,
                                             const SKB_Size     key_size,
                                             const SKB_DataType type,
                                             SKB_SecureData**   data);

#if defined(__cplusplus)
}
#endif
