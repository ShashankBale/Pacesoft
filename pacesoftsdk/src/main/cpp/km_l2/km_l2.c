#include "km_l2.h"
#include <android/log.h>
#include <string.h>
#include <stdlib.h>
#include <time.h>
#include <unistd.h>
#include <sys/time.h>
#include <unistd.h> /* read, write, close */
#include <sys/socket.h> /* socket, connect */
#include <netinet/in.h> /* struct sockaddr_in, struct sockaddr */
#include <netdb.h> /* struct hostent, gethostbyname */
#include "../kmlwrapper.h"
#ifdef __cpluplus
extern "C"
{
#endif
/******************Added by rekhit**********************/
#include <stdio.h>
#include <android/log.h>
#include <malloc.h>
#include "aes_p.h"
#include "pkcs7_padding.h"
#include "http-response-parser/httpresponseparser.h"
#include "json-parser/cJSON.h"

#include "b64/b64.h"

// Enable ECB, CTR and CBC mode. Note this can be done before including aes.h or at compile-time.
// E.g. with GCC by using the -D flag: gcc -c aes.c -DCBC=0 -DCTR=1 -DECB=1
#define CBC 1
#define CTR 1
#define ECB 1
typedef struct CryptoKey
{
    char* iv;
    char* dk;
    char* tk;
    char* apiKey;
} Crypto;
typedef struct ErrorMsg
{
    char* errorTitle;
    char* errorMsg;
    char* lnum;
}Err;
Err* callHSMAPI2(Crypto * crypto);
char* getAmadisEncKey(jobject cntx);
void setAmadisEncKey(jobject cntx,char *value,char *value2);
long currentTimeInMilliseconds();
char * getCurrentUTCTime();
char* getUTCTimeNow();
char* convertUTCTimestamp(time_t t);
#define RESPONSE_LENGTH 4096
Crypto * lastCrypto;
JNIEnv * v_env;
jobject _context;
char *appendIV;
char *encryptedHSMKey;
char *baseURL;
int IVLEN = 32;
//void displayError(const char * message,const char *lineNum);
void displayError(const char * title,const char * message,const char *lineNum);
uint8_t *encrypt_cbc(const uint8_t *data,const uint8_t *key,const uint8_t *iv, const size_t sz);
const uint8_t *decrypt_cbc(const uint8_t *data,const uint8_t *key,const uint8_t *iv, const size_t dataLen);
char* encryptHSMKey(char* ivKey);
char *decryptKey(char *key);
Crypto* getLatestCrypto(JNIEnv* xpJniEnv);
void testResponse();
bool parseJsonResponse(uint8_t *data1);
void setNewCryptoKeys(Crypto *crypto);
char *decodeAndDecryptMessage(char *valuestring,char *iv,char *dk);
Err *parseErrorResponse(char *body);
bool checkAndFetchHSMKey();
char *getBaseUrl(JNIEnv *xpJniEnv);
char *getIVLabel(char *iv);
char *getAPIKeyLabel(char *key);
char *getHostLabel(char *url);
int getRequestLength(char *argv[10]);
char *constructMessage(char *argv[10],char *message);
int setSocket(int sockfd, struct addrinfo *listp);

void error(const char *title,const char *msg,const char *lineNumber) {
    __android_log_print(ANDROID_LOG_ERROR, "ErrorMessage:", "%s\n#ln:%s", msg,lineNumber);
    displayError(title,msg,lineNumber);
}

SDKError_t KML2Create(JNIEnv* xpJniEnv,
                      jobject xContext,
                      KML2Session_t* const xpSession) {

    __android_log_print(ANDROID_LOG_DEBUG,"KML_exp", "KML2Create");
    v_env = (xpJniEnv);
    _context = (*v_env)->NewGlobalRef(v_env,xContext);
    baseURL = getBaseUrl(xpJniEnv);
    bool keyCheck = checkAndFetchHSMKey();
    __android_log_print(ANDROID_LOG_DEBUG,"KML_exp", "KML2Create done");
    return 0;
}



SDKError_t KML2Release(const KML2Session_t xSession) {
    __android_log_print(ANDROID_LOG_DEBUG,"KML_exp", "KML2Release");
    return 0;
}


SDKError_t KML2ExportCardData(const KML2Session_t xSession,
                              const uint8_t* xpPlainData, const size_t xPlainDataSz,
                              uint8_t** const xppEncData, size_t* const xpEncDataSz){
    __android_log_print(ANDROID_LOG_DEBUG,"MyLib_exp", "xPlainDataSz size : %d", xPlainDataSz);

    uint8_t * hexPlainData = (uint8_t*) malloc(((2*xPlainDataSz)+1)* sizeof(uint8_t));
    hexPlainData[0]='\0';
    bool res = to_hex(hexPlainData,((2*xPlainDataSz)+1),xpPlainData,xPlainDataSz);
    __android_log_print(ANDROID_LOG_DEBUG,"MyLib_exp", "xPlainDataHex : %s\n%d", hexPlainData,res);
    if(hexPlainData[0] == '0' && hexPlainData[1] == '0')
        return 2;
    char * hexKey = decryptKey(encryptedHSMKey);
    if( hexKey[0] != '\0') {
        uint8_t *key = hex_str_to_uint8(hexKey);
        uint8_t *iv = hex_str_to_uint8(appendIV);
        if (key[0] != '\0') {
            __android_log_print(ANDROID_LOG_DEBUG,"MyLib_exp", "size : %d, %02x", xPlainDataSz,
                                xpPlainData[xPlainDataSz - 1]);
            int encryptedDataLen = xPlainDataSz;//strlen(plainData);//
//            uint8_t *input = xpPlainData;//(uint8_t*)plainData;//
            __android_log_print(ANDROID_LOG_DEBUG, "MyLib_exp", "xpPlaindata:%s", (xpPlainData));
            uint8_t *out = encrypt_cbc(xpPlainData, key, iv,
                                       encryptedDataLen);//removed const

            if (encryptedDataLen % 16) {
                encryptedDataLen += 16 - (encryptedDataLen % 16);      // make the length multiple of 16 bytes
            } else {
                encryptedDataLen += 16;
            }

            __android_log_print(ANDROID_LOG_DEBUG, "MyLib_exp", "size : %d,outlen:%d",
                                xPlainDataSz, strlen(out));
            *xpEncDataSz = encryptedDataLen;//xPlainDataSz;//+ strlen(e_iv);//xPlainDataSz;//strlen(outWithIV);//strlen(iv_char);//
            *xppEncData = out;//xpPlainData;//out;

            return 0;//tests();
        }
    }else {
        __android_log_print(ANDROID_LOG_DEBUG, "MyLib_exp ", "ke[0] : %c", hexKey[0]);
        return 1;
    }

}

SDKError_t KML2ClearBuffer(const KML2Session_t xSession,
                           const uint8_t* xpBuffer, const size_t xBufferSz){
    return 0;
}

//Get Base URL from application instance
char *getBaseUrl(JNIEnv *xpJniEnv) {

    jclass devicePrefclazz1 = (*xpJniEnv)->FindClass(xpJniEnv,
                                                     "com/pacesoft/sdk/session/DevicePref");
    //getMainBaseUrl
    jmethodID getKeyMethodId2 = (*xpJniEnv)->GetMethodID(xpJniEnv, devicePrefclazz1,
                                                         "getMainBaseUrl", "()Ljava/lang/String;");
    jclass clazzXInsta = (*xpJniEnv)->FindClass(xpJniEnv, "com/pacesoft/sdk/session/XpssInsta");
    jfieldID dpstaticFieldId = (*xpJniEnv)->GetStaticFieldID(xpJniEnv, clazzXInsta,
                                                             "devicePref$delegate",
                                                             "Lkotlin/Lazy;");
    jobject jobject1 = (*xpJniEnv)->GetStaticObjectField(xpJniEnv, clazzXInsta, dpstaticFieldId);
    jclass lazyClassObject = (*xpJniEnv)->GetObjectClass(xpJniEnv, jobject1);
    jmethodID value = (*xpJniEnv)->GetMethodID(xpJniEnv, lazyClassObject, "getValue",
                                               "()Ljava/lang/Object;");
    jobject lazyObj = (*xpJniEnv)->CallObjectMethod(xpJniEnv, jobject1, value);
    jobject result = (*xpJniEnv)->CallObjectMethod(xpJniEnv, lazyObj, getKeyMethodId2);
    char* mainURL = (*xpJniEnv)->GetStringUTFChars(xpJniEnv, result, NULL);
    __android_log_print(ANDROID_LOG_DEBUG, "kml_log_android", "ServerURL:%s\n%d",mainURL, strlen(mainURL));
    int mainUrlLen = strlen(mainURL);
    char* trimmedUrl =malloc((mainUrlLen-9)+1);
    trimmedUrl[0] = '\0';
    memcpy(trimmedUrl, &mainURL[8], mainUrlLen - 9 );
    __android_log_print(ANDROID_LOG_DEBUG, "kml_log_android", "ServerURL trimmedUrl:%s\n",trimmedUrl);
    return trimmedUrl;
}

//get the amadis key if the key is not available or expired this function will return "NA"
bool checkAndFetchHSMKey(){
    encryptedHSMKey = getAmadisEncKey(_context);
    //encrypted key is NA means either key is not available or key is expired.
    if((strcmp(encryptedHSMKey, "NA") == 0)) {
        //get the latest crypto keys from application
        lastCrypto = getLatestCrypto(v_env);
        if(lastCrypto == NULL){
            error("Amadis App Error","Unable to fetch crypto keys.","146");
            return false;
        }
        bool success = false;
        Err * retErr = callHSMAPI2(lastCrypto);//success = callHSMAPI(lastCrypto);//calling HSM API
        if(strcmp(retErr->lnum,"200") == 0){
            success = parseJsonResponse(retErr->errorMsg);
        }else {
            if(retErr->errorTitle!='\0')
                error("Amadis API Error", retErr->errorMsg, retErr->lnum);
            else
                error(retErr->errorTitle, retErr->errorMsg, retErr->lnum);
        }
        if (encryptedHSMKey[0] != '\0')
            __android_log_print(ANDROID_LOG_DEBUG,"KML_exp", "KML2Create encryptedKey:%s",
                                encryptedHSMKey);
        else
            __android_log_print(ANDROID_LOG_DEBUG,"KML_exp", "KML2Create encrytedKey is null");
        return success;
        //concat IV + parsed response key
//        encryptedKey = encryptHSMKey("2d1c1eaa3a5b926d6bba2ab28da02d3a4a58fa83942b0bd9dbd8a17273da829e0573a35b169c14019e5be0d47c9b3841");
    }else{
        return true;
    }

}


// Get Latest Crypto information
Crypto* getLatestCrypto(JNIEnv* xpJniEnv) {

    jclass clazz1 = (*xpJniEnv)->FindClass(xpJniEnv, "com/pacesoft/sdk/session/KeyPref");
    jclass clazzXInsta = (*xpJniEnv)->FindClass(xpJniEnv, "com/pacesoft/sdk/session/XpssInsta");
    jfieldID kpstaticFieldId = (*xpJniEnv)->GetStaticFieldID(xpJniEnv,clazzXInsta,"keysPref$delegate", "Lkotlin/Lazy;");
    jobject jobject1 = (*xpJniEnv)->GetStaticObjectField(xpJniEnv, clazzXInsta, kpstaticFieldId);
    jclass  lazyClassObject = (*xpJniEnv)->GetObjectClass(xpJniEnv,jobject1);
    jmethodID value = (*xpJniEnv)->GetMethodID(xpJniEnv,lazyClassObject , "getValue",
                                               "()Ljava/lang/Object;");
    jobject lazyobj = (*xpJniEnv)->CallObjectMethod(xpJniEnv,jobject1,value);
    jmethodID getKeyMethodId = (*xpJniEnv)->GetMethodID(xpJniEnv,clazz1,"getLatestPsck", "()Lcom/pacesoft/sdk/module/Cryptos;");
    jobject result = (*xpJniEnv)->CallObjectMethod(xpJniEnv,lazyobj,getKeyMethodId);
    jclass cryptoCls = (*xpJniEnv)->GetObjectClass(xpJniEnv, result);
    jfieldID ivString = (*xpJniEnv)->GetFieldID(xpJniEnv, cryptoCls, "iv", "Ljava/lang/String;");
    jobject ivVal = (*xpJniEnv)->GetObjectField(xpJniEnv, result, ivString);
    char* iv_str = (*xpJniEnv)->GetStringUTFChars(xpJniEnv, ivVal, NULL);
    __android_log_print(ANDROID_LOG_DEBUG,"MyLib_exp", "Latest iv:%s\n", iv_str);
    jfieldID dkString = (*xpJniEnv)->GetFieldID(xpJniEnv, cryptoCls, "dk", "Ljava/lang/String;");
    jobject dkVal = (*xpJniEnv)->GetObjectField(xpJniEnv, result, dkString);
    char* dk_str = (*xpJniEnv)->GetStringUTFChars(xpJniEnv, dkVal, NULL);
    __android_log_print(ANDROID_LOG_DEBUG,"MyLib_exp", "Latest dk:%s\n", dk_str);
    jfieldID tkString = (*xpJniEnv)->GetFieldID(xpJniEnv, cryptoCls, "tk", "Ljava/lang/String;");
    jobject tkVal = (*xpJniEnv)->GetObjectField(xpJniEnv, result, tkString);
    char* tk_str = (*xpJniEnv)->GetStringUTFChars(xpJniEnv, tkVal, NULL);
    __android_log_print(ANDROID_LOG_DEBUG,"MyLib_exp", "Latest tk:%s\n", tk_str);

    jfieldID apiKeyString = (*xpJniEnv)->GetStaticFieldID(xpJniEnv,clazzXInsta,"apiKey", "Ljava/lang/String;");
    jobject apikeyObj = (*xpJniEnv)->GetStaticObjectField(xpJniEnv, clazzXInsta, apiKeyString);
    char* apikey_str = (*xpJniEnv)->GetStringUTFChars(xpJniEnv, apikeyObj, NULL);
    __android_log_print(ANDROID_LOG_DEBUG,"MyLib_exp", "Latest apikey:%s\n", apikey_str);
    Crypto* crypto = (Crypto*) malloc(sizeof(Crypto));
    memset(crypto,0, sizeof(crypto));
    crypto->iv = iv_str;
    crypto->dk = dk_str;
    crypto->tk = tk_str;
    crypto->apiKey = apikey_str;

//    free(dk_str);
//    free(iv_str);
//    free(apikey_str);
//    free(tk_str);
    (*xpJniEnv)->DeleteLocalRef(xpJniEnv,apikeyObj);
    (*xpJniEnv)->DeleteLocalRef(xpJniEnv,ivVal);
    (*xpJniEnv)->DeleteLocalRef(xpJniEnv,tkVal);
    (*xpJniEnv)->DeleteLocalRef(xpJniEnv,dkVal);
    (*xpJniEnv)->DeleteLocalRef(xpJniEnv,clazz1);
    (*xpJniEnv)->DeleteLocalRef(xpJniEnv,clazzXInsta);
    (*xpJniEnv)->DeleteLocalRef(xpJniEnv,jobject1);
    (*xpJniEnv)->DeleteLocalRef(xpJniEnv,lazyClassObject);
    (*xpJniEnv)->DeleteLocalRef(xpJniEnv,lazyobj);
    (*xpJniEnv)->DeleteLocalRef(xpJniEnv,result);
    (*xpJniEnv)->DeleteLocalRef(xpJniEnv,cryptoCls);
    __android_log_print(ANDROID_LOG_DEBUG, "MyLib_exp", "Latest crypto apikey:%s\n dk :%s\n tk :%s\n iv :%s\n",  crypto->apiKey, crypto->dk,crypto->tk,crypto->iv);
    return crypto;
}

//Display Error Dialog
void displayError(const char * title,const char * message,const char *lineNum) {
    jstring errorMessage =  (*v_env)->NewStringUTF(v_env, message);
    jstring errorTitle =  (*v_env)->NewStringUTF(v_env, title);
    jint lineNumber =   atoi(lineNum);
    jclass jclass_base =(*v_env)->FindClass(v_env,"com/pacesoft/xdemo/base/BaseActivity");
    jmethodID javaMethodId = (*v_env)->GetMethodID(v_env,jclass_base,"native2KotlinSww",
                                                   "(ILjava/lang/String;Ljava/lang/String;)V");

    (*v_env)->CallVoidMethod(v_env,_context,javaMethodId,lineNumber,errorTitle,errorMessage);
//    (*v_env)->CallVoidMethod(v_env,(_context),javaMethodId,errorMessage);
//    (*v_env)->DeleteLocalRef(v_env,errorMessage);
//    (*v_env)->DeleteLocalRef(v_env,lineNumber);
//    (*v_env)->DeleteLocalRef(v_env,jclass_base);
}

//Decrypt Saved Encrypted Amadis key
char *decryptKey(char *encryptedKey) {

    int buffersize = strlen(encryptedKey);
    if(buffersize>IVLEN) {
        __android_log_print(ANDROID_LOG_DEBUG, "KML_exp", "decryptKey buffersize: %d", buffersize);
        appendIV = (char *) malloc((IVLEN + 1) * sizeof(char));
        strncpy(appendIV, encryptedKey, IVLEN);
        appendIV[IVLEN] = '\0';
        setIV(appendIV);
        __android_log_print(ANDROID_LOG_DEBUG,"Amadis", "decryptKey iv_1: %s", appendIV);
        char *keyBuf_1 = (char *) malloc(((buffersize - IVLEN + 1) * sizeof(char)));
        strncpy(keyBuf_1, encryptedKey + (33 - 1), (buffersize - IVLEN));
        keyBuf_1[buffersize - IVLEN] = '\0';
        __android_log_print(ANDROID_LOG_DEBUG,"Amadis", "encrypted key from zkeybox: %s",
                            keyBuf_1);
        char *decryptedKey = func_zdecrypt(keyBuf_1, appendIV);//func_zdecrypt(encryptedKey,iv);//
        __android_log_print(ANDROID_LOG_DEBUG,"Amadis", "decryptedKey by zkeybox:%s\n",
                            decryptedKey);
        return decryptedKey;
    }else
        return "";
}



//Encrypt AES CBC 256
uint8_t * encrypt_cbc(const uint8_t *data,const uint8_t *key,const uint8_t *iv, const size_t dataLen) {
    uint8_t i;
    int padded = 16 - (dataLen % 16);
    int dlenu = dataLen+padded;
    uint8_t * hexArray = (uint8_t*) malloc((dlenu+1)* sizeof(uint8_t));
    hexArray[0]='\0';
    int reportPad = pkcs7_padding_pad_buffer( hexArray, dataLen, sizeof(hexArray), 16);
    memcpy(hexArray,data,dataLen);
    struct AES_ctx ctx;
    AES_init_ctx_iv(&ctx, key, iv);
    AES_CBC_encrypt_buffer(&ctx, hexArray, dlenu);
    return hexArray;
}


//Decrypt AES CBC 256
const uint8_t *decrypt_cbc(const uint8_t *data,const uint8_t *key,const uint8_t *iv, const size_t dataLen) {
    uint8_t i;
    uint8_t hexArray[dataLen];
    memset( hexArray, (uint8_t)(0), dataLen);
    memcpy(hexArray,data,dataLen);
    struct AES_ctx ctx;
    __android_log_print(ANDROID_LOG_DEBUG,"MyLib","AES CTX");
    AES_init_ctx_iv(&ctx, key, iv);
    AES_CBC_decrypt_buffer(&ctx, hexArray, dataLen);
    size_t actualDataLength = pkcs7_padding_data_length( hexArray, dataLen, 16);
    __android_log_print(ANDROID_LOG_DEBUG,"MyLib","The actual data length (without the padding) = %ld\n", actualDataLength);
    char* decryptedData = (char*) malloc(sizeof(char)*(actualDataLength+1));
    memcpy(decryptedData,hexArray,actualDataLength);
    decryptedData[actualDataLength]='\0';
    return (uint8_t*)decryptedData;
}

//Decode and Decrypt the valuestring using iv and dk
char *decodeAndDecryptMessage(char *valuestring,char *iv,char *dk) {
    long  decodedsize  = strlen(valuestring);
    unsigned char * decoded = base64_decode(valuestring,decodedsize,&decodedsize);//decode(data,decodedsize);//char * decoded = decode(data,2*strlen(data));
    decoded[decodedsize] = '\0';
    __android_log_print(ANDROID_LOG_DEBUG,"CALLHSM",
                        "%s\ndecoded length: %d",iv,decodedsize);
    char * decodedHex = (char*)malloc((2*decodedsize+1)* sizeof(char));//(char*)malloc((2* strlen(data))* sizeof(char));
    bool res = to_hex(decodedHex,(2* decodedsize)+1,(uint8_t*)decoded,
                      decodedsize);//to_hex(decodedHex,2* strlen(data),(uint8_t*)decoded, strlen(decoded));
    __android_log_print(ANDROID_LOG_DEBUG,"CALLHSM","Decoded Hex response: %s\n%d",decodedHex,
                        res);
    decodedHex[2*decodedsize] = '\0';

    uint8_t *enc_data = hex_str_to_uint8(decodedHex);
    __android_log_print(ANDROID_LOG_DEBUG,"CALLHSM",
                        "lengthencrypteddata: %d", strlen(enc_data));
    long keysize = strlen(dk);
    uint8_t *dkKey = base64_decode(dk, keysize,&keysize);//hex_str_to_uint8(dk);
    dkKey[keysize] = '\0';
    keysize = strlen(iv);
    uint8_t *ivKey = base64_decode(iv, keysize,&keysize);//hex_str_to_uint8(iv);
    ivKey[keysize] = '\0';
    uint8_t *data_1 = decrypt_cbc(enc_data, dkKey, ivKey,decodedsize);
    __android_log_print(ANDROID_LOG_DEBUG,"CALLHSM", "data_1 : %s",
                        data_1);
    return data_1;
}


/*******************************************************************************************/


Err * callHSMAPI2(Crypto * crypto){

    Err * err =(Err*) malloc(sizeof(Err));
    int i;
    __android_log_print(ANDROID_LOG_DEBUG,"callhsm","crypto->iv:%s,%d",crypto->iv,
                        strlen(crypto->iv));
    if(strlen(crypto->iv) < 10 || strlen(crypto->apiKey) < 1){
        err->lnum = "456";
        err->errorMsg = "Unable to fetch crypto keys";
        err->errorTitle = "Amadis App Error";
        return err;
//        __android_log_print(ANDROID_LOG_DEBUG,"callhsm","strlen(crypto->iv) > 10 goto exit");
//        goto jumpExit;
    }
//a.out api.somesite.com 80 GET "/apikey=ARG1&command=ARG2"
//a.out api.somesite.com 80 POST / "name=ARG1&value=ARG2" "Content-Type: application/x-www-form-urlencoded"
    char * argv[10];
    char * iv = crypto->iv;
    char * apiKey = crypto->apiKey;
    char * dk = crypto->dk;
    argv[0] = baseURL;
    argv[1] = "80";
    argv[2] = "GET";//"POST";//
    argv[3] = "/cli/Amadis/GetKeys";//"/pro/login";//
    argv[4] = "{\"message\":\"6+MWbJ2wIXnCZdTXQIIcWjavCFkQMrCxHwFIZaRAH1pOds+PREvYdKNKCC7bpwgWV+PzgT41WgRv/7tO5GLKbsZjB+mn5utlKIoqy1rIhZI=\"}";
    argv[5] = getIVLabel(iv);//iv_tag;//IV
    argv[6] = getAPIKeyLabel(apiKey);//apikey_tag;//3f685cd0-c92d-4ce0-80e8-664f0cf7d235";//API KEY
    argv[7] = "Content-Type: application/json";
    argv[8] = getHostLabel(baseURL);//host_tag;//"Host: api-prod.pacegateway.com";//IV
    argv[9] = "Connection: close";

/* first where are we going to send it? */
    int portno = atoi(argv[1])>0?atoi(argv[1]):80;
    char *host =  argv[0] ;
    int argc = 10;
    int sockfd=0, bytes=0, sent=0, received=0, total=0, message_size=0;
    char *message, response[4096];

/* How big is the message? */
    message_size=getRequestLength(argv);
    __android_log_print(ANDROID_LOG_DEBUG,"Requestsize:","\n%d",message_size);
/* allocate space for the message */
    message=(char *)malloc(message_size);
/* fill in the parameters */
    message = constructMessage(argv,message);

/* What are we going to send? */
    __android_log_print(ANDROID_LOG_DEBUG,"Request:","\n%s\n",message);
    struct addrinfo hints, * listp, * p;
    memset( & hints, 0, sizeof(struct addrinfo));
    hints.ai_socktype = SOCK_STREAM; // Use TCP
    hints.ai_flags = AI_NUMERICSERV; // Use numeric port arg
    // Generate a list of addrinfo in listp
    int retValue =getaddrinfo(host, "80", & hints, & listp);
    if(retValue != 0){
        err->lnum = "699";
        err->errorMsg = "Unable to connect to the Amadis API endpoint.";
        err->errorTitle = "Amadis API Error";
        return err;
//        goto jumpExit;
    }
//    sockfd = setSocket(sockfd,listp);
    for (p = listp; p; p = p -> ai_next)
    {
        // Create a socket based on addrinfo struct
        if ((sockfd = socket(p -> ai_family, p -> ai_socktype, p -> ai_protocol)) < 0)
            continue;
        // Setting timeout for reading response (i.e. to read() function)
        struct timeval tv;
        tv.tv_sec = 5;
        tv.tv_usec = 0;
        if(setsockopt(sockfd, SOL_SOCKET, SO_RCVTIMEO, (const char*)&tv, sizeof tv)==-1)
        {
            err->errorTitle = "Amadis App Error";
            err->errorMsg = "Error while setting timeout";
            err->lnum="709";
            return err;
//            goto jumpExit;
        }
        if (connect(sockfd, p -> ai_addr, p -> ai_addrlen) != -1)
            break;
        close(sockfd); // Bind fail, loop to try again
    }
    freeaddrinfo(listp); // Not needed anymore
    if (!p) // Entire loop failed
    {
        err->errorTitle = "Amadis App Error";
        err->errorMsg = "Error connecting to Amadis API endpoint.";//"Failed in Amadis API binding";
        err->lnum="722";
        return err;
//        goto jumpExit;
    }


//    const char * message2 = "POST /pro/login HTTP/1.1\r\nIV: LRweqjpbkm1ruiqyjaAtOg==\r\nAPIKey: 3f685cd0-c92d-4ce0-80e8-664f0cf7d235\r\nContent-Type: application/json\r\nAccept-Encoding: gzip, deflate, br\r\nHost: api-dev.pacegateway.com\r\nContent-Length: 122\r\n\r\n{\"message\":\"6+MWbJ2wIXnCZdTXQIIcWjavCFkQMrCxHwFIZaRAH1pOds+PREvYdKNKCC7bpwgWV+PzgT41WgRv/7tO5GLKbsZjB+mn5utlKIoqy1rIhZI=\"}";
/* send the request */
    total = strlen(message);
    sent = 0;
    do {
        bytes = write(sockfd,message+sent,total-sent);
        if (bytes < 0) {
//            error("ERROR writing message to socket","733");
            err->errorTitle = "Amadis API Error";
            err->lnum = "733";
            err->errorMsg = "Error writing message to Amadis API endpoint.";
            close(sockfd);
            return err;
//            goto jumpExit;
        }if (bytes == 0)
            break;

        sent+=bytes;
    } while (sent < total);
//    bytes = write(sockfd, ((const void *) ('\0') )+ sent, total - sent);


/* receive the response */
    int retry = 0;
    memset(response,0,sizeof(response));
    total = sizeof(response);
    received = 0;
    do {
        bytes = read(sockfd,response+received,total-received);
        if (bytes < 0) {
            if(retry == 0){
                err->errorTitle = "Amadis API Error";
                err->lnum = "749";
                err->errorMsg = "Error reading response from Amadis API endpoint.";
                close(sockfd);
                __android_log_print(ANDROID_LOG_ERROR, "CallHSMAPI2:", "%d", retry);
                return err;
//                goto jumpExit;
            }else{
                __android_log_print(ANDROID_LOG_ERROR, "CallHSMAPI2:", "%d", retry);
                retry += 1;
            }
//            error("ERROR reading response from socket","749");
        }if (bytes == 0)
            break;
        received+=bytes;
    } while (received < total);

/*
 * if the number of received bytes is the total size of the
 * array then we have run out of space to store the response
 * and it hasn't all arrived yet - so that's a bad thing
 */
    if (received == total){
        err->errorTitle="Amadis API Error";
        err->lnum = "762";
        err->errorMsg = "Error in data processing from Amadis API endpoint.";
        return err;
//        goto jumpExit;
    }

    response[received]='\0';
/* close the socket */
    close(sockfd);

/* process response */
    bool responseParsed = false;
    __android_log_print(ANDROID_LOG_DEBUG,"Response:","\n%s\n",response);
    bool result=false;
    if(response[0]!='\0') {
        Response *parsedResponse = ParseResponse(response, &result);
        if (parsedResponse && result) {
            if (parsedResponse->statusCode == 200) {
                __android_log_print(ANDROID_LOG_DEBUG, "CALLHSM", "API SUCCESS\n");
                if (parsedResponse->hasBody && parsedResponse->body) {
                    cJSON *monitorJson = cJSON_Parse(parsedResponse->body);
                    if (monitorJson) {
                        cJSON *message = cJSON_GetObjectItemCaseSensitive(monitorJson, "message");
                        if (message) {
                            // message key found
                            if (cJSON_IsString(message) && message->valuestring) {
                                __android_log_print(ANDROID_LOG_DEBUG, "CALLHSM", "%s\n%d",
                                                    message->valuestring,
                                                    strlen(message->valuestring));
                               char* data_1 = decodeAndDecryptMessage(message->valuestring,iv,dk);
//                            char* key = "4a58fa83942b0bd9dbd8a17273da829e0573a35b169c14019e5be0d47c9b3841";
//                            char* iv = "2d1c1eaa3a5b926d6bba2ab28da02d3a";
                                //"xGbdEofZQnPCxcJlmlO0GRbh6FG8Xuqj00xLt9QBPyVMMSI1DJmKk5ES9lgzaL3ahDgZ6nL6ql1ExSikHdYCrfr4sssjYLt6c4CIhDN/gscBOsltWJVlMKZgXdHnv9yGhPOCNKlrlsWytFs4RWoM9eWAnR03E1fHfkNeOZoDoqX/x15mLRFQl+LgMi6EGgRVRY65IoQCxIC3ovw6vCfhEtK9b3rNPjELTsnE/0YuJuMKlWZoFwiOm88VXglpsB7nBG5rz+AQ2R5vHTuPLUnJryNC9dAwYH6ej/DqzfVQuNs1bE7Ehf6Qaq2xbVtGH7GA5B18cPmx/o/13UwBwtPG057eW7P2NUiH+z6//uto5Fqmd0p6wt1OKjjQTBUukIGRE7ESfOgv775SebwNS122AL+Gske1tQQ9CKoysk1RN0hd4/Jm4j8DqYD7xzEFMUFyvEVrwX6+f29VNg5CF1GCDA==";//message->valuestring;
                                err->errorTitle = "Amadis";
                                err->lnum="200";
                                err->errorMsg = data_1;
                            }else{
                                err->errorTitle = "Amadis API Error";
                                err->errorMsg = "Invalid data into Amadis API endpoint.";
                                err->lnum = "667";
                            }
                        } else {
                            // message key not present in response body
                            __android_log_print(ANDROID_LOG_DEBUG, "CALLHSM",
                                                "Message key not present in JSON");
//                            error("Message key not present in JSON.","673");
                            err->errorTitle = "Amadis API Error";
                            err->errorMsg = "Invalid data into Amadis API endpoint.";
//                            err->errorMsg = "Message key not present in JSON";
                            err->lnum = "673";
                        }
                    } else {
                        // response body is not in JSON format.
                        __android_log_print(ANDROID_LOG_DEBUG, "CALLHSM",
                                            "Response body is not in Json format.");
//                        error("Key Downloading Failed!!\n  Field missing","680");
                        err->errorTitle = "Amadis API Error";
                        err->errorMsg = "Invalid data into Amadis API endpoint.";
                        err->lnum = "680";
                    }
                    cJSON_Delete(monitorJson);
                }else {
//                    error("Key Downloading Failed!!\n  Response body is not in Json format.","684");
                    err->errorTitle = "Amadis API Error";
                    err->errorMsg = "Invalid data into Amadis API endpoint.";
                    err->lnum = "684";
                }
            } else {
                __android_log_print(ANDROID_LOG_DEBUG, "CALLHSM", "Status Code: %d\n",
                                    parsedResponse->statusCode);
                if (parsedResponse->hasBody && parsedResponse->body){
                    __android_log_print(ANDROID_LOG_DEBUG, "CALLHSM", "Message: %s\n",
                                        parsedResponse->body);
                    cJSON *errorJson = cJSON_Parse(parsedResponse->body);
                    if (errorJson) {
                        cJSON *message = cJSON_GetObjectItemCaseSensitive(errorJson,"Message");
                        if (message) {
//                                    {"Message":"oNZIenJe+KEjXruzxrna7NHXhNtr13g2TPN627p388DRI1+cfedC08kcyD+2Il2376CbMK2FR3iJ/zaaJW6gNe/cezwzU6koH43PnHruoY8KMGITLAqGELTDgFKg5F4cS+ewgonJ6MVOo9oH+ad9Jq0t7HGuF4hEMRGpyvwD+xJUGhQXujDYWLFgJGtTx54cycYB83lLowiBYhhXWzDwrLDxkWl0Dh1XhY8+8Nnyv+5U+/uMD9wynDkZwfhOJ8ACFjqIfw1QVUzooWt/499T5g=="}
                            Header * header = parsedResponse->headers;
                            bool isDefault = false;
                            while(header->next != NULL){
                                __android_log_print(ANDROID_LOG_DEBUG, "CALLHSM", "%s: %s",
                                                    header->name,header->value);
                                if(strcmp(header->name,"IsDefault"))
                                {
                                    isDefault = true;
                                    break;
                                }
                                header = header->next;
                            }

                            char *dataKey = (char*) malloc(sizeof(char)* (strlen(dk)+1));
                            char *ivKey = (char *) malloc(sizeof(char)* (strlen(iv)+1));
                            dataKey[0]='\0';
                            ivKey[0]='\0';
                            if(isDefault){
                                memset(dataKey,0, strlen(dk));
                                memset(ivKey,0, strlen(iv));
                                dataKey = "LRweqjpbkm1ruiqyjaAtOg==";
                                ivKey = "Slj6g5QrC9nb2KFyc9qCngVzo1sWnBQBnlvg1HybOEE=";
                            }else{
                                dataKey = dk;
                                ivKey = iv;
                            }

                            // deode and decrypt message
                            char* decryptedJson = decodeAndDecryptMessage(message->valuestring,dataKey,ivKey);
                            Err * errorMsg = parseErrorResponse(decryptedJson);
                            err = errorMsg;
                        } else {
                            err->errorTitle = "Amadis API Error";
                            err->errorMsg = "Invalid data into Amadis API endpoint.";
                            err->lnum = "740";
                        }
                    }else {
                        err->errorTitle = "Amadis API Error";
                        err->errorMsg = "Invalid data into Amadis API endpoint.";
                        err->lnum = "746";
                    }
                    cJSON_Delete(errorJson);
                }else{
//                    char* msg = strcat("Key Downloading Failed!!\n Status Code: %d ",
//                                           (const char *) parsedResponse->statusCode);
//                    error(message,"752");
                    err->errorTitle = "Amadis API Error";
                    err->errorMsg = "Invalid data into Amadis API endpoint.";
                    err->lnum = "752";
                }
            }
            FreeResponse(parsedResponse);
        } else {

            if(parsedResponse->body[0]=='{'){
                Err * error_msg = parseErrorResponse(parsedResponse->body);
                err->errorTitle = "Amadis API Error";
                err->errorMsg = error_msg->errorMsg;
                err->lnum = error_msg->lnum;
                return err;
//                goto jumpExit;
            }else{
                err->errorTitle =  "Amadis API Error";
                err->errorMsg = "Invalid data into Amadis API endpoint.";
                err->lnum = "825";
                return err;
//                goto jumpExit;
            }
        }

    }else{
        err->errorTitle = "Amadis API Error";
        err->lnum="1041";
        err->errorMsg = "Invalid data into Amadis API endpoint.";//"Response body is empty";
        return err;
//        goto jumpExit;
    }


    return err;
}

// set socket configuration
int setSocket(int sockfd, struct addrinfo *listp) {
    struct addrinfo * p;
    for (p = listp; p; p = p -> ai_next)
    {
        // Create a socket based on addrinfo struct
        if ((sockfd = socket(p -> ai_family, p -> ai_socktype, p -> ai_protocol)) < 0)
            continue;
        // Setting timeout for reading response (i.e. to read() function)
        struct timeval tv;
        tv.tv_sec = 10;
        tv.tv_usec = 0;
        if(setsockopt(sockfd, SOL_SOCKET, SO_RCVTIMEO, (const char*)&tv, sizeof tv)==-1)
        {
            return 709;
        }
        if (connect(sockfd, p -> ai_addr, p -> ai_addrlen) != -1)
            break;
        close(sockfd); // Bind fail, loop to try again
    }
    freeaddrinfo(listp); // Not needed anymore
    if (!p) // Entire loop failed
    {
          return 722;
    }
    return sockfd;
}

//Construct request message for socket
char *constructMessage(char * argv[10],char *message) {
    int argc = 10;
    int i=0;
    if(!strcmp(argv[2],"GET"))
    {
        sprintf(message,"%s %s HTTP/1.1\r\n",
                strlen(argv[2])>0?argv[2]:"GET",               /* method         */
                strlen(argv[3])>0?argv[3]:"/");                /* path           */
        for(i=5;i<argc;i++)                                    /* headers        */
        {
            strcat(message,argv[i]);strcat(message,"\r\n");
        }
        strcat(message,"\r\n");                                /* blank line     */
    }else
    {
        sprintf(message,"%s %s HTTP/1.1\r\n",
                strlen(argv[2])>0?argv[2]:"POST",                  /* method         */
                strlen(argv[3])>0?argv[3]:"/");                    /* path           */
        for(i=5;i<argc;i++)                                    /* headers        */
        {strcat(message,argv[i]);strcat(message,"\r\n");}
        if(argc>4)
            sprintf(message+strlen(message),"Content-Length: %d\r\n",strlen(argv[4]));
        strcat(message,"\r\n");                                /* blank line     */
        if(argc>4)
            strcat(message,argv[4]);                           /* body           */
    }

    return message;
}

//Calculate request length
int getRequestLength(char *argv[10]) {
    int message_size = 0;
    int i=0;
    int argc = 10;
    if(!strcmp(argv[2],"GET"))
    {
        message_size+=strlen("%s %s%s%s HTTP/1.1\r\n");
        message_size+=strlen(argv[2]);                        /* method         */
        message_size+=strlen(argv[3]);                         /* path           */
        for(i=5;i<argc;i++)                                    /* headers        */
            message_size+=strlen(argv[i])+strlen("\r\n");
        message_size+=strlen("\r\n");                          /* blank line     */
    }
    else
    {
        message_size+=strlen("%s %s HTTP/1.1\r\n");
        message_size+=strlen(argv[2]);                         /* method         */
        message_size+=strlen(argv[3]);                         /* path           */
        for(i=5;i<argc;i++)                                    /* headers        */
            message_size+=strlen(argv[i])+strlen("\r\n");
        if(argc>4)
            message_size+=strlen("Content-Length: %d\r\n")+10; /* content length */
        message_size+=strlen("\r\n");                          /* blank line     */
        if(argc>4)
            message_size+=strlen(argv[4]);                     /* body           */
    }

    return message_size;
}


//Get HOST Header label
char *getHostLabel(char *url) {
    char * host_tag = (char*) malloc((strlen(baseURL)+7)*sizeof(char));
    strcpy(host_tag, "HOST: ");
    strcat(host_tag,baseURL);
    host_tag[(strlen(baseURL)+6)] = '\0';
    return host_tag;
}

//Get API KEY Header label
char *getAPIKeyLabel(char *apiKey) {
    char * apikey_tag = (char*) malloc((strlen(apiKey)+ strlen("APIKey: ")+1)*sizeof(char));
    __android_log_print(ANDROID_LOG_DEBUG,"callhsm","apikey:%s,%d,%d\n",apiKey,
                        strlen(apiKey),strlen("APIKey: "));
    strcpy(apikey_tag, "APIKey: ");
    strcat(apikey_tag,apiKey);
    apikey_tag[strlen(apiKey)+strlen("APIKey: ")] = '\0';
    __android_log_print(ANDROID_LOG_DEBUG,"callhsm","apikey:%s,%d\n",apikey_tag,
                        strlen(apikey_tag));
    return apikey_tag;
}

//Get IV Header label
char *getIVLabel(char *iv) {
    char * iv_tag = (char*) malloc((strlen(iv)+5)*sizeof(char));
    strcpy(iv_tag, "IV: ");
    strcat(iv_tag,iv);
    iv_tag[(strlen(iv)+4)] = '\0';
    return iv_tag;
}

/***********************************************************************************************/
//parse Error response using default key or dynamic key
Err *parseErrorResponse(char *body) {

    cJSON* jsonObj =cJSON_Parse(body);
    Err * error = (Err*)malloc(sizeof(Err));
    memset(error,0, sizeof(error));
    if(jsonObj) {
        cJSON *object = cJSON_GetObjectItemCaseSensitive(jsonObj,
                                                         "ErrorLists");
        if (cJSON_IsArray(object)) {
            int n = cJSON_GetArraySize(object);
            if(n>0){
                cJSON *jelement = cJSON_GetArrayItem(object,0);
                cJSON *errObj = cJSON_GetObjectItemCaseSensitive(jelement,"Error");
                cJSON *feildObj = cJSON_GetObjectItemCaseSensitive(errObj,"Message");
                char *errMsg = feildObj->valuestring;
                feildObj = cJSON_GetObjectItemCaseSensitive(errObj,"Code");
                char *errCode = feildObj->valuestring;
                char *errorMsg = (char*) malloc((strlen(errMsg)+
                                                 strlen(errCode)+4)*
                                                sizeof(char));
                memset(errorMsg,0, (strlen(errMsg)+
                                    strlen(errCode)+4));
                errorMsg[(strlen(errMsg)+
                          strlen(errCode)+3)]='\0';
                strcat(errorMsg,errMsg);
                strcat(errorMsg," (");
                strcat(errorMsg,errCode);
                strcat(errorMsg,")");

//                error(errorMsg,"721");
                error->errorTitle =  "Amadis API Error";
                error->errorMsg = errorMsg;
                error->lnum = "842";
            }else{
//                error(, "723");
                error->errorTitle = "Amadis API Error";
                error->errorMsg = "Error in JSON response parsing from Amadis API endpoint.";
                error->lnum = "845";
            }
        }else {
//            error("Something Went Wrong.", "726");
            error->errorTitle = "Amadis API Error";
            error->errorMsg = "Error in JSON response parsing from Amadis API endpoint.";
            error->lnum = "851";
        }
    }else{
//        error("Something Went Wrong.", "729");
        error->errorTitle = "Amadis API Error";
        error->errorMsg = "Error in JSON response parsing from Amadis API endpoint.";
        error->lnum = "856";
    }
    return error;
}

//API Json response parsing
bool parseJsonResponse(uint8_t *data1) {
    char * _key = "Key";
    char * _IV = "IV";
    char * _ExpireAt = "ExpireAt";
    char * _cryptos = "Crypto";
    char * _DK = "DK";
    char * _ApiKey = "ApiKey";
    char * _TK = "TK";
    char * amadisKEY;
    char * amadisIV;
    char * expiredAt ;
    bool failed = false;

    Crypto* received_crypto;
    cJSON* jsonObj =cJSON_Parse(data1);
    if(jsonObj) {
        cJSON *object = cJSON_GetObjectItemCaseSensitive(jsonObj, _key);

        if(cJSON_IsString(object) && object->valuestring)
            amadisKEY = object->valuestring;
        else {
            error("Amadis API Error","Invalid data into Amadis API endpoint.","929");
            return false;
        }
        __android_log_print(ANDROID_LOG_DEBUG,"CALLHSM","Parsed response:amadiskey %s",
                            amadisKEY);

        object = cJSON_GetObjectItemCaseSensitive(jsonObj, _IV);
        if(cJSON_IsString(object) && object->valuestring)
            amadisIV = object->valuestring;
        else {
            error("Amadis API Error","Invalid data into Amadis API endpoint.","939");
            return false;
        }
        __android_log_print(ANDROID_LOG_DEBUG,"CALLHSM","Parsed response:amadisiv %s",
                            amadisIV);
        object = cJSON_GetObjectItemCaseSensitive(jsonObj, _ExpireAt);

        if(cJSON_IsString(object) && object->valuestring)
            expiredAt = object->valuestring;
        else {
            error("Amadis API Error","Invalid data into Amadis API endpoint.","949");
            return false;
        }

        __android_log_print(ANDROID_LOG_DEBUG,"CALLHSM","Parsed response:expiredAt %s",
                            expiredAt);
        cJSON *cryptoJson = cJSON_GetObjectItemCaseSensitive(jsonObj, _cryptos);
//        __android_log_print(ANDROID_LOG_DEBUG,"CALLHSM","Parsed response:cryptoJson %s",
//                            cryptoJson->valuestring);
//        cJSON* jsonCryptoObj =cryptoJson;//cJSON_Parse(cryptoJson);
        received_crypto = (Crypto*) malloc(sizeof(Crypto));
        memset(received_crypto,0, sizeof(received_crypto));
        object = cJSON_GetObjectItemCaseSensitive(cryptoJson, _DK);
        if(cJSON_IsString(object) && object->valuestring)
            received_crypto->dk = object->valuestring;
        else {
            error("Amadis API Error","Invalid data into Amadis API endpoint.","965");
            return false;
        }
        __android_log_print(ANDROID_LOG_DEBUG,"CALLHSM","Parsed response:newDk %s",
                            received_crypto->dk);
        object = cJSON_GetObjectItemCaseSensitive(cryptoJson, _IV);
        if(cJSON_IsString(object) && object->valuestring)
            received_crypto->iv = object->valuestring;
        else {
            error("Amadis API Error","Invalid data into Amadis API endpoint.","974");
            return false;
        }
        __android_log_print(ANDROID_LOG_DEBUG,"CALLHSM","Parsed response:newIV %s",
                            received_crypto->iv);
        object = cJSON_GetObjectItemCaseSensitive(cryptoJson, _TK);
        if(cJSON_IsString(object) && object->valuestring)
            received_crypto->tk = object->valuestring;
        else {
            error("Amadis API Error","Invalid data into Amadis API endpoint.","983");
            return false;
        }
        __android_log_print(ANDROID_LOG_DEBUG,"CALLHSM","Parsed response:newTK %s",
                            received_crypto->tk);
        object = cJSON_GetObjectItemCaseSensitive(cryptoJson, _ApiKey);
        if(cJSON_IsString(object) && object->valuestring)
            received_crypto->apiKey = object->valuestring;
        else {
            error("Amadis API Error","Invalid data into Amadis API endpoint.","992");
            return false;
        }
        __android_log_print(ANDROID_LOG_DEBUG,"CALLHSM","Parsed response:newApiKey %s",
                            received_crypto->apiKey);


//        cJSON_Delete(object);
//        cJSON_Delete(cryptoJson);
//        cJSON_Delete(jsonObj);
//        free(_key);
//        free(_IV);
//        free(_ExpireAt);
//        free(_cryptos);
//        free(_DK);
//        free(_TK);
//        free(_ApiKey);

//        uint8_t * currentformatedtime = getCurrentUTCTime();
//        __android_log_print(ANDROID_LOG_DEBUG,"CALLHSM","expiredAt %s,\n%d",
//                            currentformatedtime, strcmp(currentformatedtime,expiredAt));
        struct tm tm;
        time_t time1 = 0;

        if (strptime(expiredAt, "%Y-%m-%dT%H:%M:%S", &tm) != NULL ) {
            tm.tm_isdst = -1;
           time1= timegm(&tm);
        }else {
            error("Amadis API Error","Invalid data into Amadis API endpoint.","1006");
            return false;
        }

        __android_log_print(ANDROID_LOG_DEBUG,"strptime","getUTCTimeNow:%s",
                            getUTCTimeNow());

        __android_log_print(ANDROID_LOG_DEBUG,"strptime","convertUTCTimestampepoch%s to  %ld",expiredAt,time1);
        long amadisIVLen = strlen(amadisIV);
        char* decodedIV = base64_decode(amadisIV,amadisIVLen,&amadisIVLen);
        decodedIV[amadisIVLen] = '\0';
        char* hexIV = (char*) malloc(((2*amadisIVLen)+1)*sizeof (char));
        bool res = to_hex(hexIV,(2*amadisIVLen)+1,(uint8_t*)decodedIV, amadisIVLen);//strlen(decodedIV));
        hexIV[2*amadisIVLen] ='\0';
        if(res == 0){
            error("Amadis API Error","Invalid data into Amadis API endpoint.","997");
            return false;
        }
        __android_log_print(ANDROID_LOG_DEBUG,"strptime","IVHex %s,\n%d",hexIV,res);
        __android_log_print(ANDROID_LOG_DEBUG,"parseJsonResponse","length:%d",(strlen(amadisKEY)+ strlen(hexIV)+1));
        long vIn = time1;
        char timeOut [11];
        sprintf(timeOut, "%lld", vIn);
        timeOut[10]='\0';
        __android_log_print(ANDROID_LOG_DEBUG,"parseJsonResponse","time_str %s",timeOut);
        setNewCryptoKeys(received_crypto);
        encryptedHSMKey = func_zencrypt(amadisKEY,hexIV);
        __android_log_print(ANDROID_LOG_DEBUG, "Amadis ", "IV+(encryptedKey by zkeybox) :%s\n", encryptedHSMKey);
        if(encryptedHSMKey == NULL || strlen(encryptedHSMKey) < 64 || (strlen(encryptedHSMKey)%2 != 0)) {
            error("Amadis API Error","Invalid data into Amadis API endpoint.","1035");
            return false;
        }
        setAmadisEncKey(_context,encryptedHSMKey,timeOut);

        return true;

    }else {
        error("Amadis API Error","Invalid data into Amadis API endpoint.", "1824");
        return false;
    }

}

//Set New Crypto keys to application
void setNewCryptoKeys(Crypto* crypto) {
    __android_log_print(ANDROID_LOG_DEBUG, "Amadis ", "setNewCryptoKeys called");
    jlong ts = getUTCTimeNow();
    jclass clazz1 = (*v_env)->FindClass(v_env, "com/pacesoft/sdk/session/KeyPref");
    jclass clazzXInsta = (*v_env)->FindClass(v_env, "com/pacesoft/sdk/session/XpssInsta");
    jclass clazzCryptoPayload = (*v_env)->FindClass(v_env, "com/pacesoft/sdk/module/CryptoPayload");
    jfieldID kpstaticFieldId = (*v_env)->GetStaticFieldID(v_env,clazzXInsta,"keysPref$delegate", "Lkotlin/Lazy;");
    jobject jobject1 = (*v_env)->GetStaticObjectField(v_env, clazzXInsta, kpstaticFieldId);
    jclass  lazyClassObject = (*v_env)->GetObjectClass(v_env,jobject1);
    jmethodID value = (*v_env)->GetMethodID(v_env,lazyClassObject , "getValue",
                                            "()Ljava/lang/Object;");

    jobject lazyobj = (*v_env)->CallObjectMethod(v_env,jobject1,value);
    jmethodID midConstructor = (*v_env)->GetMethodID(v_env, clazzCryptoPayload, "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;J)V");
    jobject  newCryptoObj = (*v_env)->NewObject(v_env,clazzCryptoPayload,midConstructor,(*v_env)->NewStringUTF(v_env,crypto->iv),(*v_env)->NewStringUTF(v_env,crypto->dk)
            ,(*v_env)->NewStringUTF(v_env,crypto->tk),(*v_env)->NewStringUTF(v_env,crypto->apiKey)
            ,ts);
    __android_log_print(ANDROID_LOG_DEBUG,"Amadis","setNewCryptoKeys");
    jmethodID addKeyMethodId = (*v_env)->GetMethodID(v_env,clazz1,"addPsck", "(Lcom/pacesoft/sdk/module/CryptoPayload;)V");
    (*v_env)->CallVoidMethod(v_env,lazyobj,addKeyMethodId,newCryptoObj);
}



char* getUTCTimeNow() {
   time_t now = time(&now);
    if (now == -1) {
        puts("The time() function failed");
    }

    struct tm *ptm = gmtime(&now);
    if (ptm == NULL) {
        puts("The gmtime() function failed");
    }
      int BUF_LEN = 256;
     char buf[256] = {0};
     size_t  max = 16;
     const char *format = "%Y-%m-%dT%H:%M:%S%z";
     size_t tf =  strftime(buf, BUF_LEN,format ,ptm);

    time_t seconds;
    seconds = time(NULL);
    __android_log_print(ANDROID_LOG_DEBUG,"strptime","Seconds since January 1, 1970 = %ld\n", seconds);

    long vIn = seconds;
    char timeOut [11];
    sprintf(timeOut, "%lld", vIn);
    timeOut[10]='\0';
//    __android_log_print(ANDROID_LOG_DEBUG,"strptime","Seconds bugg = %s\n", buf);
//    char* expiredAt = buf;//"2023-03-03T14:10:15";//1677852615-1677832815
//    __android_log_print(ANDROID_LOG_DEBUG,"strptime","Seconds bugg = %s\n", expiredAt);

//    struct tm tm;
//    time_t retval = 0;
//    if ( strptime(expiredAt, "%Y-%m-%dT%H:%M:%S", &tm) != NULL )
//        retval = mktime(&tm);
//
//    __android_log_print(ANDROID_LOG_DEBUG,"strptime","convertUTCTimestampepoch %ld",retval);
    return timeOut;
}



char* getAmadisEncKey(jobject cntx) {
    __android_log_print(ANDROID_LOG_DEBUG, "KML_exp", "getAmadisEncKey");
    jclass jcSharedPreferences = (*v_env)->FindClass(v_env, "android/content/SharedPreferences");
    jclass contextcls = (*v_env)->FindClass(v_env, "android/content/Context");
    jmethodID  jmGetSharedPreferences = (*v_env)->GetMethodID(v_env, contextcls, "getSharedPreferences",
                                                       "(Ljava/lang/String;I)Landroid/content/SharedPreferences;");
    jmethodID jmGetString = (*v_env)->GetMethodID(v_env, jcSharedPreferences, "getString",
                                            "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
    jobject  joSharedPreferences=(*v_env)->CallObjectMethod(v_env,(cntx),jmGetSharedPreferences,(*v_env)->NewStringUTF(v_env,"pref_amadis"),0);
    jobject keyval =(*v_env)->CallObjectMethod(v_env,joSharedPreferences, jmGetString ,(*v_env)->NewStringUTF(v_env,"KEY"), (*v_env)->NewStringUTF(v_env,"NA"));
    char* keyStr = (*v_env)->GetStringUTFChars(v_env,(jstring) keyval, NULL); // should be released but what a heck, it's a tutorial :)
    __android_log_print(ANDROID_LOG_DEBUG, "Amadis", " Get Saved encrypted key:%s\n%d", keyStr,
                        strlen(keyStr));

    jobject expiryVal =(*v_env)->CallObjectMethod(v_env,joSharedPreferences, jmGetString ,(*v_env)->NewStringUTF(v_env,"key_expiretime"), (*v_env)->NewStringUTF(v_env,"0"));
    const char* expStr = (*v_env)->GetStringUTFChars(v_env,(jstring) expiryVal, NULL); // should be released but what a heck, it's a tutorial :)
    __android_log_print(ANDROID_LOG_DEBUG, "Amadis ", "Get Saved Key expired timestamp:%s\n", expStr);

    char* utctime =  getUTCTimeNow();
    long currentUTC = atol(utctime);
    long expiredUTC = atol(expStr);
//    (*v_env)->DeleteLocalRef(v_env,jcSharedPreferences);
//    (*v_env)->DeleteLocalRef(v_env,contextcls);
//    (*v_env)->DeleteLocalRef(v_env,joSharedPreferences);
//    (*v_env)->DeleteLocalRef(v_env,keyval);
//    (*v_env)->DeleteLocalRef(v_env,expiryVal);
    if((strcmp(keyStr,"NA")==0) || (currentUTC >= expiredUTC)) {
        __android_log_print(ANDROID_LOG_DEBUG, "KML_exp", "Need to Fetch HSM KEY expired time is : %s", expStr);
        return "NA";
    }else {
        __android_log_print(ANDROID_LOG_DEBUG, "KML_exp", "No need to call HSM KEY, expired time is: %s",expStr);
        return (keyStr);
    }

}



void setAmadisEncKey(jobject cntx,char *value,char *value2) {
    __android_log_print(ANDROID_LOG_DEBUG, "KML_exp", "setAmadisKey %s\n,%s ", value,value2);
    jstring jstrBuf = (*v_env)->NewStringUTF(v_env, value);
    jstring jstrBuf2 = (*v_env)->NewStringUTF(v_env, value2);
    jclass jcSharedPreferences =(*v_env)->FindClass(v_env,"android/content/SharedPreferences");
    jclass jcSpEditorcls = (*v_env)->FindClass(v_env,"android/content/SharedPreferences$Editor");
    jclass contextcls = (*v_env)->FindClass(v_env,"android/content/Context");
    jmethodID jmGetSharedPreferences = (*v_env)->GetMethodID(v_env,contextcls,"getSharedPreferences",
                                                             "(Ljava/lang/String;I)Landroid/content/SharedPreferences;");
    jmethodID midedit = (*v_env)->GetMethodID(v_env,jcSharedPreferences, "edit",
                                              "()Landroid/content/SharedPreferences$Editor;");

    jmethodID jmPutString=(*v_env)->GetMethodID(v_env,jcSpEditorcls,"putString","(Ljava/lang/String;Ljava/lang/String;)Landroid/content/SharedPreferences$Editor;");
    jmethodID midapply = (*v_env)->GetMethodID(v_env,jcSpEditorcls, "apply",
                                               "()V");

    jobject  joSharedPreferences=(*v_env)->CallObjectMethod(v_env,cntx,jmGetSharedPreferences,(*v_env)->NewStringUTF(v_env,"pref_amadis"),0);
    jobject joObjectSharedEdit = (*v_env)->CallObjectMethod(v_env,joSharedPreferences, midedit);
    jobject joObjectPutEdit = (*v_env)->CallObjectMethod(v_env,joObjectSharedEdit, jmPutString, (*v_env)->NewStringUTF(v_env,"KEY"), jstrBuf);
    (*v_env)->CallVoidMethod(v_env,joObjectPutEdit,
                             midapply);

    jobject joObjectSharedEdit2 = (*v_env)->CallObjectMethod(v_env,joSharedPreferences, midedit);
    jobject joObjectPutEdit2 = (*v_env)->CallObjectMethod(v_env,joObjectSharedEdit2, jmPutString, (*v_env)->NewStringUTF(v_env,"key_expiretime"), jstrBuf2);
    (*v_env)->CallVoidMethod(v_env,joObjectPutEdit2,
                             midapply);

}


JNIEXPORT void JNICALL
Java_com_pacesoft_sdk_session_KeyPref_clearAmadisKey(JNIEnv *env, jobject thiz,jobject context) {

    __android_log_print(ANDROID_LOG_DEBUG, "KML_exp", "Clear Amadis or Logout");
    jstring jstrBuf = (*env)->NewStringUTF(env, "NA");
    jstring jstrBuf2 = (*env)->NewStringUTF(env, "0");
    jclass jcSharedPreferences =(*env)->FindClass(env,"android/content/SharedPreferences");
    jclass jcSpEditorcls = (*env)->FindClass(env,"android/content/SharedPreferences$Editor");
    jclass contextcls = (*env)->FindClass(env,"android/content/Context");
    jmethodID jmGetSharedPreferences = (*env)->GetMethodID(env,contextcls,"getSharedPreferences",
                                                             "(Ljava/lang/String;I)Landroid/content/SharedPreferences;");
    jmethodID midedit = (*env)->GetMethodID(env,jcSharedPreferences, "edit",
                                              "()Landroid/content/SharedPreferences$Editor;");

    jmethodID jmPutString=(*env)->GetMethodID(env,jcSpEditorcls,"putString","(Ljava/lang/String;Ljava/lang/String;)Landroid/content/SharedPreferences$Editor;");
    jmethodID midapply = (*env)->GetMethodID(env,jcSpEditorcls, "apply",
                                               "()V");

    jobject  joSharedPreferences=(*env)->CallObjectMethod(env,context,jmGetSharedPreferences,(*env)->NewStringUTF(env,"pref_amadis"),0);
    jobject joObjectSharedEdit = (*env)->CallObjectMethod(env,joSharedPreferences, midedit);
    jobject joObjectPutEdit = (*env)->CallObjectMethod(env,joObjectSharedEdit, jmPutString, (*env)->NewStringUTF(env,"KEY"), jstrBuf);
    (*env)->CallVoidMethod(env,joObjectPutEdit,
                             midapply);

    jobject joObjectSharedEdit2 = (*env)->CallObjectMethod(env,joSharedPreferences, midedit);
    jobject joObjectPutEdit2 = (*env)->CallObjectMethod(env,joObjectSharedEdit2, jmPutString, (*env)->NewStringUTF(env,"key_expiretime"), jstrBuf2);
    (*env)->CallVoidMethod(env,joObjectPutEdit2,
                             midapply);

}


JNIEXPORT jboolean JNICALL
Java_com_pacesoft_sdk_session_KeyPref_validAmadisKey(JNIEnv *env, jobject thiz, jobject context) {
    jclass jcSharedPreferences = (*env)->FindClass(env, "android/content/SharedPreferences");
    jclass contextcls = (*env)->FindClass(env, "android/content/Context");
    jclass jcSpEditorcls = (*env)->FindClass(env,"android/content/SharedPreferences$Editor");//edit

    jmethodID  jmGetSharedPreferences = (*env)->GetMethodID(env, contextcls, "getSharedPreferences",
                                                              "(Ljava/lang/String;I)Landroid/content/SharedPreferences;");
    jmethodID jmGetString = (*env)->GetMethodID(env, jcSharedPreferences, "getString",
                                                  "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");

    /*************************************************************/
//    jmethodID midedit = (*v_env)->GetMethodID(v_env,jcSharedPreferences, "edit",
//                                              "()Landroid/content/SharedPreferences$Editor;");
//    jmethodID jmPutString=(*v_env)->GetMethodID(v_env,jcSpEditorcls,"putString","(Ljava/lang/String;Ljava/lang/String;)Landroid/content/SharedPreferences$Editor;");
//    jmethodID midapply = (*v_env)->GetMethodID(v_env,jcSpEditorcls, "apply",
//                                               "()V");
   /*************************************************************/
    jobject  joSharedPreferences=(*env)->CallObjectMethod(env,(context),jmGetSharedPreferences,(*env)->NewStringUTF(env,"pref_amadis"),0);
    jobject keyval =(*env)->CallObjectMethod(env,joSharedPreferences, jmGetString ,(*env)->NewStringUTF(env,"KEY"), (*env)->NewStringUTF(env,"NA"));
    char* keyStr = (*env)->GetStringUTFChars(env,(jstring) keyval, NULL); // should be released but what a heck, it's a tutorial :)
    __android_log_print(ANDROID_LOG_DEBUG, "Amadis", " Get Saved encrypted key:%s\n", keyStr);

    jobject expiryVal =(*env)->CallObjectMethod(env,joSharedPreferences, jmGetString ,(*env)->NewStringUTF(env,"key_expiretime"), (*env)->NewStringUTF(env,"0"));
    const char* expStr = (*env)->GetStringUTFChars(env,(jstring) expiryVal, NULL); // should be released but what a heck, it's a tutorial :)
    __android_log_print(ANDROID_LOG_DEBUG, "Amadis ", "Get Saved Key expired timestamp:%s\n", expStr);

    char* utctime =  getUTCTimeNow();
    long currentUTC = atol(utctime);
    long expiredUTC = atol(expStr);

    if((strcmp(keyStr,"NA")==0) || (currentUTC >= (expiredUTC-300)) || (strlen(keyStr)%2 != 0)) {
        /********************************************************************/
//        jstring jstrBuf = (*v_env)->NewStringUTF(v_env, "NA");
//        jstring jstrBuf2 = (*v_env)->NewStringUTF(v_env, "0");
//        jobject joObjectSharedEdit = (*v_env)->CallObjectMethod(v_env,joSharedPreferences, midedit);
//        jobject joObjectPutEdit = (*v_env)->CallObjectMethod(v_env,joObjectSharedEdit, jmPutString, (*v_env)->NewStringUTF(v_env,"KEY"), jstrBuf);
//        (*v_env)->CallVoidMethod(v_env,joObjectPutEdit,
//                                 midapply);
//
//        jobject joObjectSharedEdit2 = (*v_env)->CallObjectMethod(v_env,joSharedPreferences, midedit);
//        jobject joObjectPutEdit2 = (*v_env)->CallObjectMethod(v_env,joObjectSharedEdit2, jmPutString, (*v_env)->NewStringUTF(v_env,"key_expiretime"), jstrBuf2);
//        (*v_env)->CallVoidMethod(v_env,joObjectPutEdit2,
//                                 midapply);
        /********************************************************************/
        __android_log_print(ANDROID_LOG_DEBUG, "KML_exp", "Need to Fetch HSM KEY expired time is : %s,\ncurrent time:%s", expStr,utctime);

        return false;
    }else {
        __android_log_print(ANDROID_LOG_DEBUG, "KML_exp", "No need to call HSM KEY, expired time is: %s\ncurrent time:%s",expStr,utctime);
        return true;
    }
}


char * getCurrentUTCTime() {
    struct timespec ts;
    int errorCode = clock_gettime(CLOCK_REALTIME,&ts);
    if(errorCode != 0){
        __android_log_print(ANDROID_LOG_DEBUG, "getCurrentUTCTime", "ERROR");
        return 1;
    }
    struct tm tm = *gmtime(&ts.tv_sec);
    int length = 64;
    char timeStrWithoutMilliseconds[length];
    errorCode = strftime(timeStrWithoutMilliseconds,length,"%Y-%m-%dT%H:%M:%S",&tm);
    if(errorCode ==0){
        __android_log_print(ANDROID_LOG_DEBUG, "getCurrentUTCTime", "ERROR");
        return 1;
    }
    char timeStrWithMilliseconds[length];
    errorCode =snprintf(timeStrWithMilliseconds,length,"%s.%07ldZ",timeStrWithoutMilliseconds,(ts.tv_nsec/100));
    if(errorCode <= 0)
    {
        __android_log_print(ANDROID_LOG_DEBUG, "getCurrentUTCTime", "ERROR");
        return 1;
    }
//    __android_log_print(ANDROID_LOG_DEBUG, "getCurrentUTCTime", "%s",timeStrWithoutMilliseconds);

    return timeStrWithMilliseconds;
}

long currentTimeInMilliseconds()
{
    struct timeval tv;
    gettimeofday(&tv, NULL);
    return ((tv.tv_sec * 1000) + (tv.tv_usec / 1000));
}

void testResponse() {
    char* key = "Slj6g5QrC9nb2KFyc9qCngVzo1sWnBQBnlvg1HybOEE=";//"4a58fa83942b0bd9dbd8a17273da829e0573a35b169c14019e5be0d47c9b3841";
    char* iv = "LRweqjpbkm1ruiqyjaAtOg==";//"2d1c1eaa3a5b926d6bba2ab28da02d3a";
    char * data = "xGbdEofZQnPCxcJlmlO0GRbh6FG8Xuqj00xLt9QBPyVMMSI1DJmKk5ES9lgzaL3ahDgZ6nL6ql1ExSikHdYCrfr4sssjYLt6c4CIhDN/gscBOsltWJVlMKZgXdHnv9yGhPOCNKlrlsWytFs4RWoM9eWAnR03E1fHfkNeOZoDoqX/x15mLRFQl+LgMi6EGgRVRY65IoQCxIC3ovw6vCfhEtK9b3rNPjELTsnE/0YuJuMKlWZoFwiOm88VXglpsB7nBG5rz+AQ2R5vHTuPLUnJryNC9dAwYH6ej/DqzfVQuNs1bE7Ehf6Qaq2xbVtGH7GA5B18cPmx/o/13UwBwtPG057eW7P2NUiH+z6//uto5Fqmd0p6wt1OKjjQTBUukIGRE7ESfOgv775SebwNS122AL+Gske1tQQ9CKoysk1RN0hd4/Jm4j8DqYD7xzEFMUFyvEVrwX6+f29VNg5CF1GCDA==";

//                                    "32ck5KmciRiIcL9NWq7pO+GxpIZsJIWIvHLLDWKYmuaclH6bNNOiGUzPNAxrJ+9ZsvFAiwPX8h/6n4Y04ND21CcNoijrZJ09K6PPmdEBa7TdJUeC3QkP1fWnqAuCmiToNM2j0OsdzmuQaIjQvoxQGiH6Jw0WuHGDKaDDGbiSGJAhOmOP/pYqC0SoPYey3asmwVHowOL9PPzvZozF0yomTUQ0JYPf7fCI+vnxGyoXhHUNDjZTAwRznZQhKdv+neHZPEnSi2R9S5UBq3lkGSpDrb56Li1qebPnUsPOOwjCPppx5+NOjeeuyB363jMgAkzymC0imeYYeqLXyYMBj7og3mha4aLLl8qZ301aXF3e37V5Ape3MKjy/wOcLXkVYqqorNvzuiP7B16RRu1UhWDeiBTiNo/rfZR76NnKJRYnJ0trKh4fihMC2ba+TkssawF4nTyiCRZheH2cXYOOTatLqA==";
//                                    "AVDa4HPTKQzdxuWWBwdI8OT6CxlW89HlWwb/mliYfTTR8YmyhF0SBMfW/EUF2S9ySyGlKRIhqouwJJRjPuLsZcuqMKXQhGFbmEmFAXBRz6g2QOxn/AXapNStoWZYkKNuv9J81eUo6qN3PmiBfUPXpeY7oWVI9tXvpMx7q0L52MlHnYZdOd6nPlccRQgEKVJd88jZYUH1UwXIxZMRxx1gBa+3hVPiVODV3Wkva1O8Wly8oAOsK4luqrX7oE+Fy0EOA78mCw2Oc0fCrkxBvMhV0pNS1AXnYOZNrmnPTvJXSd1br0XtUjFhDoc1XQ0/b63CtpYLS/UH3en7jBvHPapDJaZCS03fgrQxz9SWz2oLCd73Tg3PDs04dYAeeqogz3Zf+KDQEnaFt40cvDgn4oYwcI5ylAnVyb8MBW5Mc6tNgXrTyDgTKUl8C/ycgKy1U8Z6caTTjwFz6LPCGlLFNiKGiQ==";//message->valuestring;

    long decode_size = strlen(data);
    char * decoded = base64_decode(data,decode_size,&decode_size);//char * decoded = decode(data,2*strlen(data));
    decoded[decode_size]='\0';
    char * decodedHex = (char*)malloc((2*decode_size+1)* sizeof(char));//(char*)malloc((2* strlen(data))* sizeof(char));
    bool res = to_hex(decodedHex,2*decode_size+1,(uint8_t*)decoded, decode_size);//to_hex(decodedHex,2* strlen(data),(uint8_t*)decoded, strlen(decoded));
    decodedHex[2*decode_size] ='\0';
    __android_log_print(ANDROID_LOG_DEBUG,"CALLHSM","Decoded Hex response: %s\n%d",decodedHex,
                        res);


//    __android_log_print(ANDROID_LOG_DEBUG,"CALLHSM","Decoded dkKey: %s",dkKey);
//    char * decodedKeyHex = (char*) malloc((2*strlen(key)+1)* sizeof(char));
//    to_hex(decodedKeyHex,2* strlen(key),(uint8_t*)dkKey, strlen(dkKey));
//    __android_log_print(ANDROID_LOG_DEBUG,"CALLHSM","decodedKeyHex dkKey: %s",decodedKeyHex);
//    uint8_t * uint_key = hex_str_to_uint8(decodedKeyHex);
//    __android_log_print(ANDROID_LOG_DEBUG,"CALLHSM","uint_key dkKey: %s",uint_key);
    uint8_t * enc_data = hex_str_to_uint8(decodedHex);
    long keylen = strlen(key);
    char * dkKey = base64_decode(key,keylen,&keylen);//hex_str_to_uint8(dk);
    dkKey[keylen]='\0';
    keylen = strlen(iv);
    uint8_t * ivKey = (uint8_t*)base64_decode(iv,keylen,&keylen);//hex_str_to_uint8(iv);
    ivKey[keylen]='\0';
    uint8_t * data_1 = decrypt_cbc(enc_data,dkKey,ivKey, decode_size);
    __android_log_print(ANDROID_LOG_DEBUG,"CALLHSM","data_1 : %s",data_1);
    parseJsonResponse(data_1);

}

char* encryptHSMKey(char* ivKey) {
    int totalCharactersInIv = 32;
    char* iv = (char*) malloc((totalCharactersInIv+1)*sizeof(char ));//"2d1c1eaa3a5b926d6bba2ab28da02d3a";
    strncpy(iv,ivKey,totalCharactersInIv);
    iv[32] = '\0';
    int totalCharactersInKey = strlen(ivKey) - totalCharactersInIv;
    char* key = (char*) malloc((totalCharactersInKey+1)* sizeof(char));//getAmadisEncKey(xContext);// IV+Key
    strncpy(key,ivKey+(totalCharactersInIv),totalCharactersInKey);
    key[totalCharactersInKey] = '\0';
    __android_log_print(ANDROID_LOG_DEBUG, "Amadis", "IV :%s\n", iv);
    __android_log_print(ANDROID_LOG_DEBUG, "Amadis", "key:%s\n", key);
    char* encryptedKey = func_zencrypt(key,iv);
    __android_log_print(ANDROID_LOG_DEBUG, "Amadis ", "IV+(encryptedKey by zkeybox) :%s\n", encryptedKey);
    return encryptedKey;
}
char* convertUTCTimestamp(time_t t) {
    time_t now = time(&t);
    if (now == -1) {
        puts("The time() function failed");
    }
    struct tm *ptm = gmtime(&now);
    if (ptm == NULL) {
        puts("The gmtime() function failed");
    }
    int BUF_LEN = 256;
    char buf[256] = {0};
    size_t  max = 16;
    const char *format = "%s";
    size_t tf =  strftime(buf, BUF_LEN,format ,ptm);

    return buf;//asctime(ptm);
}
char *setDummyTime() {
    char* utctime =  getUTCTimeNow();//get the timestamp
    long  currentUTC = atol(utctime);//change the double type of current timestamp
    long expiredUTC = currentUTC+2592000;//adding 30days milliseconds
    char buffer [28];
    int ret = snprintf(buffer, sizeof(buffer), "%ld", expiredUTC);
    buffer[27]='\0';
    return buffer;
}




#ifdef __cpluplus
}
#endif

