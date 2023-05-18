/*****************************************************************
|
|   whiteCryption Secure Key Box
|
|   $Id: SkbGieseckeDevrient.h 18985 2022-12-29 13:38:40Z kristaps.straupe $
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
#ifndef SKB_CFS_GD_AlgorithmT
    #define SKB_CFS_GD_AlgorithmT l4e1fd9e6c073d66c7caafca07b1273426d66f8f64e8ef2cb
#endif
#ifndef SKB_CFS_GD_AlgorithmX
    #define SKB_CFS_GD_AlgorithmX lefc15258969ed694c7397aeebd5330ba7dd88f68ee1f70a3
#endif
#ifndef SKB_CFS_GD_BASIC_KDF
    #define SKB_CFS_GD_BASIC_KDF l9980fe3ae899f9a70980bd3e3e21897818f4ea8b53014da7
#endif
#ifndef SKB_CFS_GD_Kdf
    #define SKB_CFS_GD_Kdf lf6639ff480c8560512189410bea17b61462a788483d63382
#endif
#ifndef SKB_CFS_GD_PADDING_METHOD_1
    #define SKB_CFS_GD_PADDING_METHOD_1 l2b1d5240e5fbceb08d4155ad14905d2b4f5b52fbe6d804ce
#endif
#ifndef SKB_CFS_GD_PADDING_METHOD_2
    #define SKB_CFS_GD_PADDING_METHOD_2 lc3018a9df93442b5b9b21670e9484a0c4970c9d0c9d1f820
#endif
#ifndef SKB_CFS_GD_PBKDF2
    #define SKB_CFS_GD_PBKDF2 l59a8dceac630a88b0eb03b9a3198e7722116fcb4bacc7b39
#endif
#ifndef SKB_CFS_GD_PIN_XOR_KDF
    #define SKB_CFS_GD_PIN_XOR_KDF l55b997bd677d69ae47b185077ab2ed14b064e6ee2dff0e3f
#endif
#ifndef SKB_CFS_GD_PaddingMethod
    #define SKB_CFS_GD_PaddingMethod lb928fc4c5969adf888c162b96722e98d010a0db907745eb8
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

enum SKB_CFS_GD_Kdf
{
    SKB_CFS_GD_BASIC_KDF,       
    SKB_CFS_GD_PIN_XOR_KDF,     
    SKB_CFS_GD_PBKDF2           
};

enum SKB_CFS_GD_PaddingMethod
{
    SKB_CFS_GD_PADDING_METHOD_1,  
    SKB_CFS_GD_PADDING_METHOD_2   
};

SKB_Result
SKB_CFS_GD_AlgorithmX(const SKB_CFS_GD_Kdf           kdf,
                      const SKB_Size                 kdf_iteration_count,
                      const SKB_CFS_GD_PaddingMethod padding_method,
                      const SKB_SecureData*          session_key_unwrapping_key,
                      const SKB_Byte*                wrapped_session_key,
                      const SKB_Byte*                salt,
                      const SKB_Size                 salt_size,
                      const SKB_Byte*                pin,
                      const SKB_Size                 pin_size,
                      const SKB_Byte*                input_data,
                      const SKB_Size                 input_data_size,
                      SKB_Byte*                      mac_output);

SKB_Result
SKB_CFS_GD_AlgorithmT(const SKB_SecureData* session_key_unwrapping_key,
                      const SKB_Byte*       wrapped_session_key,
                      const SKB_Byte*       pin,
                      const SKB_Size        pin_size,
                      const SKB_Byte*       input_data,
                      const SKB_Size        input_data_size,
                      SKB_Byte*             mac_output);

#if defined(__cplusplus)
}
#endif
