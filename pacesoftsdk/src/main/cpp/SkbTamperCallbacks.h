/*****************************************************************
|
|   whiteCryption Secure Key Box
|
|   $Id: SkbTamperCallbacks.h 17255 2022-01-14 09:29:13Z kristaps.straupe $
|
|   This software is provided to you pursuant to your Software
|   license agreement (SLA) with whiteCryption Corporation
|   ("whiteCryption"). This software may be used only in accordance
|   with the terms of this agreement.
|
|   Copyright (c) 2000-2022, whiteCryption Corporation. All rights reserved.
|
****************************************************************/

#if defined(__cplusplus)
extern "C" {
#endif
void SKB_Callback_AntiDebug()
{
    // Do something
}

void SKB_Callback_Root()
{
    // Do something
}

void SKB_Callback_Jailbreak()
{
    // Do something
}

#if defined(__cplusplus)
}
#endif
