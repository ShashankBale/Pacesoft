

#include <jni.h>
#include <string.h>
#include <string>
#include <stdlib.h>
#include "SkbSecureKeyBox.h"
#include "SkbTamperCallbacks.h"
#include <android/log.h>
#include "kmlwrapper.h"


/* *****************
 * GLOBAL VARIABLES
 * *****************
 */
static SKB_Engine* g_engine = NULL;
static SKB_SecureData* g_key = NULL;
static SKB_Result g_result = SKB_SUCCESS;
static const char* g_errored_function = "";
static const SKB_CipherAlgorithm g_cipherAlgorithm = SKB_CIPHER_ALGORITHM_AES_256_CBC;//SKB_CIPHER_ALGORITHM_AES_128_CTR;
#define IV_LENGTH 16
#define COUNTER_SIZE 8
#define KEY_SIZE 32

static void encrypt(const char* plain_text, char* cipher_text, size_t plain_text_length, const char* iv);

static void decrypt(const char* cipher_text, char* plain_text, size_t cipher_text_length, const char* iv);

static char* exportKey(size_t* buffer_size);

static void importOrCreateKey(const char* exported_key_bytes, size_t exported_key_size);


//bool to_hex(char* dest, size_t dest_len, const uint8_t* values, size_t val_len);
//uint8_t* hex_str_to_uint8(const char* string);

JNIEnv *g_env;
char* appendedIV;

/* ******************************
 *  Functions interfacing with Java
 * ******************************
 **/

static void ThrowJniError(JNIEnv *env, SKB_Result result, const char* function)
{
    jclass exceptionCls = env->FindClass("com/pacesoft/sdk/base/SKBException");
    jmethodID exceptionConstructor = env->GetMethodID(exceptionCls,"<init>","(ILjava/lang/String;)V");
    jstring fnc = env->NewStringUTF(function);
    jobject exceptionObj = env->NewObject(exceptionCls,exceptionConstructor, (jint) result, fnc);
    env->Throw((jthrowable) exceptionObj);
}

/**
 * JNI method to encrypt data using AES-128 in CTR mode
 * receives input text and the IV from Java as byte array and returns the cipher text
 * in a byte array to Java
 */
extern "C"
jbyteArray
Java_com_pacesoft_sdk_session_Xskb_encryptNative(JNIEnv *env, jobject thiz, jbyteArray input_text,
                                                 jbyteArray iv) {
    jsize length_of_input_text = env->GetArrayLength(input_text);
    int pad_len = 16 - ( length_of_input_text % 16 ) ;
    int length_with_padding = length_of_input_text+pad_len;
    char * buffer;
    buffer = (char *) malloc (length_with_padding);
    jbyte  *lib = (env)->GetByteArrayElements( input_text, 0);
    memcpy ( buffer , lib , length_of_input_text) ;
    char* iv_bytes = (char*) env->GetByteArrayElements(iv, NULL);
    char* cipher_text = (char*) malloc((length_with_padding) * sizeof(char));
    int j = 0;
    while( j <  pad_len){
        buffer[length_of_input_text+j] = (char)pad_len;
        cipher_text[length_of_input_text+j] = (char)pad_len;
        j++;
    }
    encrypt(buffer, cipher_text, length_with_padding, iv_bytes);
    if (g_result != SKB_SUCCESS)
    {
        // Free the allocated cipher_text array in case of error
        free(cipher_text);
        ThrowJniError(env, g_result, g_errored_function);
        return nullptr;
    }

    // copy cipher text to jbyte array
    jbyteArray cipher_text_java = env->NewByteArray(length_with_padding);
    env->SetByteArrayRegion(cipher_text_java, 0, length_with_padding, (const jbyte*) cipher_text);

    // Free the allocated cipher_text array anyways
    free(cipher_text);
    env->ReleaseByteArrayElements(input_text, (jbyte*) buffer, JNI_ABORT);
    env->ReleaseByteArrayElements(iv, (jbyte*) iv_bytes, JNI_ABORT);

    return cipher_text_java;
}


int pkcs7_padding_data_length_( char* buffer, int buffer_size, int modulus ){
    /* test for valid buffer size */
    if( buffer_size % modulus != 0 ||
        buffer_size < modulus ){
        return 0;
    }
    char padding_value;
    padding_value = buffer[buffer_size-1];
    /* test for valid padding value */
    if( padding_value < 1 || padding_value > modulus ){
        return buffer_size;
    }
    /* buffer must be at least padding_value + 1 in size */
    if( buffer_size < padding_value + 1 ){
        return 0;
    }
    uint8_t count = 1;
    buffer_size --;
    for( ; count  < padding_value ; count++){
        buffer_size --;
        if( buffer[buffer_size] != padding_value ){
            return 0;
        }
    }
    return buffer_size;
}


/**
 * JNI method to decrypt data using AES-128 in CTR mode
 * receives the cipher text and IV from Java as a byte array and returns the plain text
 * in a byte array to Java
 */
//extern "C" JNIEXPORT jbyteArray  JNICALL
extern "C"
jbyteArray
Java_com_pacesoft_sdk_session_Xskb_decryptNative(JNIEnv* env,
                                                 jobject /* this */,
                                                 jbyteArray input_text,
                                                 jbyteArray iv)
{

    jsize length_of_cipher_text = env->GetArrayLength(input_text);
    char * buffer;
    buffer = (char *) malloc (length_of_cipher_text);
    jbyte  *lib = (*env).GetByteArrayElements( input_text, 0);
    memcpy ( buffer , lib , length_of_cipher_text ) ;
    char* iv_bytes = (char*) env->GetByteArrayElements(iv, NULL);
    char* plain_text = (char*) malloc(length_of_cipher_text * sizeof(char));
    decrypt(buffer, plain_text, length_of_cipher_text, iv_bytes);
    int size = pkcs7_padding_data_length_(plain_text,length_of_cipher_text,16);
    if (g_result != SKB_SUCCESS)
    {
        // Free the allocated plain_text array in case of error
        free(plain_text);
//        ThrowJniError(env, g_result, g_errored_function);
        return nullptr;
    }

    // copy plain text to jbyte array
    jbyteArray plain_text_java = env->NewByteArray(size);
    env->SetByteArrayRegion(plain_text_java, 0, size, (const jbyte*) plain_text);

    free(plain_text);
    env->ReleaseByteArrayElements(input_text, (jbyte*) buffer, JNI_ABORT);
    env->ReleaseByteArrayElements(iv, (jbyte*) iv_bytes, JNI_ABORT);

    return plain_text_java;
}




/**
 * JNI method to get the SKB key as a byte array so it can be written to persistent storage.
 * The key is in the SKB exported form, thus, protected and not in plain.
 * @params: void
 * returns: The key exported by SKB in a byte array
*/
extern "C" //JNIEXPORT jbyteArray JNICALL
jbyteArray
Java_com_pacesoft_sdk_session_Xskb_getKey(JNIEnv* env, jobject /* this */)
{

    // export the key and get its bytes
    size_t byte_array_size = 0;
    char* skb_exported_key_bytes = exportKey(&byte_array_size);
    if (g_result != SKB_SUCCESS)
    {
        ThrowJniError(env, g_result, g_errored_function);
        return nullptr;
    }

    // copy from char[] to byte[] and hand it to Java
    jbyteArray skb_exported_key_java = env->NewByteArray((jsize) byte_array_size);
    env->SetByteArrayRegion(skb_exported_key_java, 0, (jsize) byte_array_size,
                            (const jbyte*) skb_exported_key_bytes);

    free(skb_exported_key_bytes);
    return skb_exported_key_java;
}


static char* recallKey(){
    // export the key and get its bytes
    size_t byte_array_size = 0;
    char* skb_exported_key_bytes = exportKey(&byte_array_size);
    if (g_result != SKB_SUCCESS)
    {
//        ThrowJniError(env, g_result, g_errored_function);
        return nullptr;
    }

    /* // copy from char[] to byte[] and hand it to Java
     jbyteArray skb_exported_key_java = env->NewByteArray((jsize) byte_array_size);
     env->SetByteArrayRegion(skb_exported_key_java, 0, (jsize) byte_array_size,
                             (const jbyte*) skb_exported_key_bytes);*/
    importOrCreateKey(skb_exported_key_bytes, (size_t) byte_array_size);

    if (g_result != SKB_SUCCESS)
    {
//         ThrowJniError(env, g_result, g_errored_function);
    }

    free(skb_exported_key_bytes);
    return skb_exported_key_bytes;
}
/**
 * Initialize the SKB key or load the key from a previously exported SKB key.
 * The SKB exported key is stored on persistent storage, it is read and written in the java module
 * and a byte array containing the data is passed to native code for use.
 */
extern "C" //JNIEXPORT void JNICALL
void
Java_com_pacesoft_sdk_session_Xskb_setKey(JNIEnv* env, jobject /* this */,
                                          jbyteArray skb_exported_key_java)
{
    g_env = env;
    const char* skb_exported_key_bytes = NULL;
    SKB_Size length_of_key_bytes = 0;
    // load the byte array into a C char array
    if (skb_exported_key_java)
    {
        skb_exported_key_bytes = (const char*) env->GetByteArrayElements( skb_exported_key_java, NULL);
        length_of_key_bytes = env->GetArrayLength(skb_exported_key_java);
    }
    importOrCreateKey(skb_exported_key_bytes, (size_t) length_of_key_bytes);

    if (g_result != SKB_SUCCESS)
    {
        ThrowJniError(env, g_result, g_errored_function);
    }
}



/**
 * Return the length of IV that the encryptNative and decryptNative methods expect.
 */
extern "C" //JNIEXPORT jint JNICALL
jint
Java_com_pacesoft_sdk_session_Xskb_getIvSize(JNIEnv* /* env */, jobject /* this */)
{
    return IV_LENGTH;
}



/**
 * Returns a String version of a given error code.
 */
extern "C" //JNIEXPORT jstring JNICALL
jstring
Java_com_pacesoft_sdk_session_Xskb_getReturnCodeString(JNIEnv* env, jobject _this
        , jint result)
{
    const char* code = "UNKNOWN ERROR";
    switch (result)
    {
        case SKB_SUCCESS:
            code = "SKB_SUCCESS";
            break;
        case SKB_FAILURE:
            code = "SKB_FAILURE";
            break;
        case SKB_ERROR_INTERNAL:
            code = "SKB_ERROR_INTERNAL";
            break;
        case SKB_ERROR_INVALID_PARAMETERS:
            code = "SKB_ERROR_INVALID_PARAMETERS";
            break;
        case SKB_ERROR_NOT_SUPPORTED:
            code = "SKB_ERROR_NOT_SUPPORTED";
            break;
        case SKB_ERROR_OUT_OF_RESOURCES:
            code = "SKB_ERROR_OUT_OF_RESOURCES";
            break;
        case SKB_ERROR_BUFFER_TOO_SMALL:
            code = "SKB_ERROR_BUFFER_TOO_SMALL";
            break;
        case SKB_ERROR_INVALID_FORMAT:
            code = "SKB_ERROR_INVALID_FORMAT";
            break;
        case SKB_ERROR_ILLEGAL_OPERATION:
            code = "SKB_ERROR_ILLEGAL_OPERATION";
            break;
        case SKB_ERROR_INVALID_STATE:
            code = "SKB_ERROR_INVALID_STATE";
            break;
        case SKB_ERROR_OUT_OF_RANGE:
            code = "SKB_ERROR_OUT_OF_RANGE";
            break;
        case SKB_ERROR_EVALUATION_EXPIRED:
            code = "SKB_ERROR_EVALUATION_EXPIRED";
            break;
        case SKB_ERROR_KEY_CACHE_FAILED:
            code = "SKB_ERROR_KEY_CACHE_FAILED";
            break;
        case SKB_ERROR_INVALID_EXPORT_KEY_VERSION:
            code = "SKB_ERROR_INVALID_EXPORT_KEY_VERSION";
            break;
        case SKB_ERROR_INVALID_EXPORT_KEY:
            code = "SKB_ERROR_INVALID_EXPORT_KEY";
            break;
        case SKB_ERROR_AUTHENTICATION_FAILURE:
            code = "SKB_ERROR_AUTHENTICATION_FAILURE";
            break;
    }
    return env->NewStringUTF(code);
}




/*****************************************
 * Private/internal function definitions
 *****************************************
 */

/**
 * Encrypt the plain text with AES-128 in CTR mode using SKB library
 * @param plain_text
 * @param plain_text_length
 * @param iv
 * @return pointer to the newly allocated cipher text
 */
static void encrypt(const char* plain_text, char* cipher_text, size_t plain_text_length, const char* iv)
{
    SKB_Cipher* cipher = NULL;
    SKB_Size skb_buffer_length = (SKB_Size) plain_text_length;

    if (skb_buffer_length % 16) {
        skb_buffer_length += 16 - (skb_buffer_length % 16);      // make the length multiple of 16 bytes
    }


//    SKB_CtrModeCipherParameters counter_mode_params;
//    counter_mode_params.counter_size = COUNTER_SIZE;

//    SKB_AesUnwrapParameters cbcParam;
//    cbcParam.padding = SKB_CBC_PADDING_TYPE_NONE;
//    __android_log_print(ANDROID_LOG_DEBUG, "Amadis", "encryptedlen:%d,%d",skb_buffer_length ,strlen(cipher_text));
//    if(g_engine != NULL)
//        __android_log_print(ANDROID_LOG_DEBUG, "Amadis", "gengine not null");
//
//    if(g_key != NULL)
//        __android_log_print(ANDROID_LOG_DEBUG, "Amadis", "g_key not null");

    g_result = SKB_Engine_CreateCipher(g_engine, g_cipherAlgorithm, SKB_CIPHER_DIRECTION_ENCRYPT, 0,
                                       NULL, g_key, &cipher);
    if (g_result != SKB_SUCCESS)
    {
        g_errored_function = "SKB_Engine_CreateCipher";
        return;
    }

    g_result = SKB_Cipher_ProcessBuffer(cipher, (SKB_Byte*) plain_text, skb_buffer_length,
                                        (SKB_Byte*) cipher_text, &skb_buffer_length,
                                        (const SKB_Byte*) iv, IV_LENGTH);
    SKB_Cipher_Release(cipher);

    if (g_result != SKB_SUCCESS)
    {
        g_errored_function = "SKB_Cipher_ProcessBuffer";
        return;
    }
}

/**
 * Decrypt the plain text with AES-128 in CTR mode using SKB library
 * @param cipher_text
 * @param cipher_text_length
 * @param iv
 * @return pointer to the plain text
 */
static void decrypt(const char* cipher_text, char* plain_text, size_t cipher_text_length, const char* iv)
{
    SKB_Cipher* cipher = NULL;
    SKB_Size skb_buffer_length = (SKB_Size) cipher_text_length;
    SKB_Size actual_size = (SKB_Size) cipher_text_length;
    if (skb_buffer_length % 16) {
        skb_buffer_length += 16 - (skb_buffer_length % 16);      // make the length multiple of 16 bytes
    }
//    SKB_CtrModeCipherParameters counter_mode_params;
//    counter_mode_params.counter_size = COUNTER_SIZE;
//    SKB_AesUnwrapParameters cbcParam;
//    cbcParam.padding = SKB_CBC_PADDING_TYPE_NONE;
    g_result = SKB_Engine_CreateCipher(g_engine, g_cipherAlgorithm, SKB_CIPHER_DIRECTION_DECRYPT, 0,
                                       NULL, g_key, &cipher);
    if (g_result != SKB_SUCCESS)
    {
        g_errored_function = "SKB_Engine_CreateCipher";
        return;
    }

    g_result = SKB_Cipher_ProcessBuffer(cipher, (SKB_Byte*) cipher_text, skb_buffer_length,
                                        (SKB_Byte*) plain_text, &skb_buffer_length,
                                        (const SKB_Byte*) iv, IV_LENGTH);
    SKB_Cipher_Release(cipher);

    if (g_result != SKB_SUCCESS)
    {
        g_errored_function = "SKB_Cipher_ProcessBuffer";
        return;
    }
}


/**
 * Load the SKB keys so we can use it for encryption/decryption
 * @param skb_exported_key_bytes, contents of the key file stored on disk
 * @param exported_key_size , size of exported key in bytes
 */
static void importOrCreateKey(const char* skb_exported_key_bytes, size_t exported_key_size)
{
    // Instantiate the SKB_Engine singleton and SKB_SecureData key.
    // The engine and the key are never released in this example (although they should).
    SKB_Engine_GetInstance(&g_engine);
    if (skb_exported_key_bytes && exported_key_size && SKB_Engine_CreateDataFromExported(g_engine,
                                                                                         (const SKB_Byte*)skb_exported_key_bytes,
                                                                                         exported_key_size, &g_key) == SKB_SUCCESS)
    {
        return; // imported successfully
    }

    // if key could not be loaded generate a new random key
    SKB_RawBytesParameters keyParams;
    keyParams.byte_count = KEY_SIZE;
    g_result = SKB_Engine_GenerateSecureData(g_engine, SKB_DATA_TYPE_BYTES, &keyParams, &g_key);
    if (g_result != SKB_SUCCESS)
    {
        g_errored_function = "SKB_Engine_GenerateSecureData";
    }
}

/**
 * export the key using SKB API
 * @param buffer_size: pointer to return the size of byte buffer that stores the key
 * @return char* of key bytes
 */
static char* exportKey(size_t* buffer_size)
{
    // determine the space needed to save the key
    SKB_Size skb_buffer_size = 0;
    g_result = SKB_SecureData_Export(g_key, SKB_EXPORT_TARGET_PERSISTENT, NULL, NULL,
                                     &skb_buffer_size);
    if (g_result != SKB_SUCCESS)
    {
        g_errored_function = "SKB_SecureData_Export";
        return nullptr;
    }

    // now allocate buffer and save export they key
    SKB_Byte* skb_exported_key_bytes = (SKB_Byte*) malloc(sizeof(char) * skb_buffer_size);
    g_result = SKB_SecureData_Export(g_key, SKB_EXPORT_TARGET_PERSISTENT, NULL,
                                     skb_exported_key_bytes, &skb_buffer_size);

    *buffer_size = skb_buffer_size;
    return (char*) skb_exported_key_bytes;
}



char* func_zencrypt( char *input_text,
                     char *iv){
//    char* reKey = recallKey();

    int length_of_input_text = strlen(input_text);
    int pad_len = 16 - ( length_of_input_text % 16 ) ;
    int length_with_padding = length_of_input_text+pad_len;
    char * buffer;
    buffer = (char *) malloc (length_with_padding);
//    jbyte  *lib = (env)->GetByteArrayElements( input_text, 0);
    uint8_t* lib = (uint8_t *)input_text;
    memcpy ( buffer , lib , length_of_input_text) ;
    char* iv_bytes = reinterpret_cast<char *>((uint8_t *) iv);
    char * cipher_text ;
    cipher_text = (char*) malloc((length_with_padding));
    int j = 0;
    while( j <  pad_len){
        buffer[length_of_input_text+j] = (char)pad_len;
        cipher_text[length_of_input_text+j] = (char)pad_len;
        j++;
    }
    __android_log_print(ANDROID_LOG_DEBUG, "Amadis", "length with padding:%d", length_with_padding);
    encrypt(buffer, cipher_text, length_with_padding, iv_bytes);
    if (g_result != SKB_SUCCESS)
    {
        // Free the allocated cipher_text array in case of error
        free(cipher_text);
//        ThrowJniError(g_env, g_result, g_errored_function);
        return nullptr;
    }


    int dlenu = length_with_padding;//strlen(cipher_text);
    uint8_t hexarray[dlenu];
    memset( hexarray, (uint8_t)0, dlenu );
    for (int i=0;i<dlenu;i++) {
        hexarray[i] = (uint8_t)cipher_text[i];
//        __android_log_print(ANDROID_LOG_DEBUG,"MyLib","%02x",hexarray[i]);
    }

    __android_log_print(ANDROID_LOG_DEBUG, "Amadis", "zkeybox size of hexarr:%d\n%d", sizeof(hexarray) ,dlenu);
    char* buf = (char*) malloc((sizeof(hexarray)*2)+1);//char buf[sizeof(hexarray)*2+1];
    if(to_hex(buf, (sizeof(hexarray)*2)+1, hexarray, sizeof(hexarray)))
        __android_log_print(ANDROID_LOG_DEBUG, "Amadis", "zkeybox encryptedHexKey:%s", buf);
    else
        return nullptr;
    buf[sizeof(hexarray)*2]='\0';
//    __android_log_print(ANDROID_LOG_DEBUG, "Amadis", "%d\n%d\n%d", strlen(buf),sizeof(hexarray),strlen(iv));

    char* iv_buf = (char*) malloc((sizeof(hexarray)*2)+32+1);//char iv_buf[sizeof(buf)+ 33];
//    memcpy(iv_buf,iv, 32);
    strncpy(iv_buf,iv+(0), 33);
    iv_buf[(sizeof(hexarray)*2)+32] = '\0';
//    __android_log_print(ANDROID_LOG_DEBUG, "Amadis", "encryptedHexIV :%s\n%d", iv_buf, strlen(iv_buf));
    strncat(iv_buf,buf,(sizeof(hexarray)*2));

    __android_log_print(ANDROID_LOG_DEBUG, "Amadis", "encryptedHexCombineWithIV :%s\nLength:%d", iv_buf, strlen(iv_buf));

    return iv_buf;
}

char* func_zdecrypt( char * input_text,
                     char * iv){
    uint8_t* lib = hex_str_to_uint8(input_text);
    __android_log_print(ANDROID_LOG_DEBUG, "Amadis", "sizeinput :%d",strlen(input_text));

    int length_of_cipher_text = 80;//
    char * buffer;
    buffer = (char *) malloc ((length_of_cipher_text+1)* sizeof(char ));
//    uint8_t* lib = (uint8_t *)input_text;
    memcpy ( buffer , lib , length_of_cipher_text ) ;
    buffer[length_of_cipher_text]= '\0';
    char* iv_bytes = reinterpret_cast<char *>((uint8_t *) iv);
    char* plain_text = (char*) malloc((length_of_cipher_text+1) * sizeof(char));
    plain_text[length_of_cipher_text]='\0';
    decrypt(buffer, plain_text, length_of_cipher_text, iv_bytes);
    int size = pkcs7_padding_data_length_(plain_text,length_of_cipher_text,16);
    if (g_result != SKB_SUCCESS)
    {
        // Free the allocated plain_text array in case of error
        free(plain_text);
//        ThrowJniError(g_env, g_result, g_errored_function);
        return nullptr;
    }


    // copy plain text to jbyte array
//    jbyteArray plain_text_java = env->NewByteArray(size);
//    env->SetByteArrayRegion(plain_text_java, 0, size, (const jbyte*) plain_text);
    char* actualbuffer = (char*)malloc((size+1)* sizeof(char));
    memcpy(actualbuffer,plain_text,size);
    actualbuffer[size]='\0';
//    free(plain_text);

    return actualbuffer;
}


uint8_t* hex_str_to_uint8(const char* string) {

    if (string == NULL)
        return NULL;

    size_t slength = strlen(string);
    if ((slength % 2) != 0) // must be even
        return NULL;

    size_t dlength = slength / 2;

//    uint8_t* data = (uint8_t*)malloc(dlength);
//
//    memset(data, 0, dlength);
    uint8_t *data = (uint8_t*)malloc((dlength+1)* sizeof(uint8_t));

    memset(data, 0, dlength);
    data[dlength]='\0';

    size_t index = 0;
    while (index < slength) {
        char c = string[index];
        int value = 0;
        if (c >= '0' && c <= '9')
            value = (c - '0');
        else if (c >= 'A' && c <= 'F')
            value = (10 + (c - 'A'));
        else if (c >= 'a' && c <= 'f')
            value = (10 + (c - 'a'));
        else
            return NULL;

        data[(index / 2)] += value << (((index + 1) % 2) * 4);

        index++;
    }

    return data;
}

bool to_hex(char* dest, size_t dest_len, const uint8_t* values, size_t val_len) {
    static const char hex_table[] = "0123456789ABCDEF";
    if(dest_len < (val_len*2+1)) /* check that dest is large enough */
        return false;
    while(val_len--) {
        /* shift down the top nibble and pick a char from the hex_table */
        *dest++ = hex_table[*values >> 4];
        /* extract the bottom nibble and pick a char from the hex_table */
        *dest++ = hex_table[*values++ & 0xF];
    }
    *dest = 0;
    return true;
}




extern "C"
JNIEXPORT jstring JNICALL
Java_com_pacesoft_sdk_session_KeyPref_getDefaultIv(JNIEnv *env, jobject thiz) {
    jstring iv = env->NewStringUTF("LRweqjpbkm1ruiqyjaAtOg==");
    return iv;
}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_pacesoft_sdk_session_KeyPref_getDefaultDk(JNIEnv *env, jobject thiz) {
    jstring dK = env->NewStringUTF("Slj6g5QrC9nb2KFyc9qCngVzo1sWnBQBnlvg1HybOEE=");
    return dK;
}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_pacesoft_sdk_session_KeyPref_getDefaultTk(JNIEnv *env, jobject thiz) {
    jstring tK = env->NewStringUTF("Slj6g5QrC9nb2KFyc9qCngVzo1sWnBQBnlvg1HybOEE=");
    return tK;
}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_pacesoft_sdk_session_KeyPref_getDefaultApiKey(JNIEnv *env, jobject thiz) {
    jstring apiKey = env->NewStringUTF("3f685cd0-c92d-4ce0-80e8-664f0cf7d235");
    return apiKey;
}



void setIV(char * paramIV){
    appendedIV = paramIV;
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_pacesoft_sdk_agnos_Sred_getIV(JNIEnv *env, jobject thiz) {

    if(appendedIV[0]!='\0') {
        jstring stringValue = (*env).NewStringUTF( appendedIV);
        jclass stringClass = (*env).FindClass( "java/lang/String");
        jmethodID getBytesMId = (*env).GetMethodID( stringClass, "getBytes", "()[B");
        jbyteArray keyBytes = (jbyteArray) (*env).CallObjectMethod( stringValue, getBytesMId);
        // determine the needed length and allocate a buffer for it
//        jsize num_bytes = (*env).GetArrayLength( keyBytes);
        return keyBytes;
    }else
        return NULL;
}
