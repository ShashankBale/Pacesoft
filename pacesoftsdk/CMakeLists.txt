# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

cmake_minimum_required(VERSION 3.18.1)

set (CMAKE_CXX_STANDARD 17)

#set(source_files src/main/cpp/native-lib.cpp src/main/cpp/pkcs7_padding.c)

set(source_files  src/main/cpp/km_l2/km_l2.c
        src/main/cpp/km_l2/aes_p.c
        src/main/cpp/km_l2/pkcs7_padding.c
        src/main/cpp/km_l2/json-parser/cJSON.c
        src/main/cpp/km_l2/http-response-parser/httpresponseparser.c
        src/main/cpp/km_l2/b64/b64.c
        src/main/cpp/native-lib.cpp)


# If this build has Link-time module configuration feature enabled, the corresponding cpp file must be added to the sample project
find_file(LINK_TIME_MODULE_CONF NAMES SkbLinkTimeConfiguration.cpp PATHS ${CMAKE_SOURCE_DIR}/../../../src/SkbModules NO_DEFAULT_PATH NO_CMAKE_FIND_ROOT_PATH)
if(LINK_TIME_MODULE_CONF)
    list(APPEND source_files "${LINK_TIME_MODULE_CONF}")
endif()

add_library(km_l2 SHARED ${source_files})
add_library( lib_km_l2 STATIC IMPORTED )
set_target_properties( lib_km_l2 PROPERTIES IMPORTED_LOCATION
        ${lib_build_DIR}/${ANDROID_ABI}/lib_km_l2.a )

#add_subdirectory(src/main/cpp/km_l2)
#find_library(kml km_l2)
#add_library(km_l2 SHARED IMPORTED)
#set_target_properties (km_l2 PROPERTIES IMPORTED_LOCATION ${PROJECT_SOURCE_DIR}/src/main/cpp/km_l2/${ANDROID_ABI}/lib/libcrypto.so )
set( lib_src_DIR  src/main/cpp/km_l2 )
set( lib_build_DIR src/main/cpp/km_l2/outputs )
file(MAKE_DIRECTORY ${lib_build_DIR})

#[[add_subdirectory( # Specifies the directory of the CMakeLists.txt file.
        ${lib_src_DIR}

        # Specifies the directory for the build outputs.
        ${lib_build_DIR} )]]


#add_library( lib_km_wrapper STATIC IMPORTED )
#set_target_properties( lib_km_l2 PROPERTIES IMPORTED_LOCATION
#        ${lib_build_DIR}/${ANDROID_ABI}/lib_km_l2.a )


target_include_directories(km_l2 PUBLIC ${CMAKE_SOURCE_DIR}/../Include)
#set_target_properties( lib_km_wrapper PROPERTIES IMPORTED_LOCATION
#        ${lib_build_DIR}/${ANDROID_ABI}/lib_kmlwrapper.a )
#add_subdirectory("src/main/cpp/km_l2/")
include_directories( ${lib_src_DIR}/include )
include_directories(${JNI_INCLUDE_DIRS})
#include_directories( "src/main/cpp")
#include_directories( "src/main/cpp/km_l2" )
#include_directories("src/main/cpp/km_l2/http-response-parser")
#include_directories("src/main/cpp/km_l2/json-parser")

# Links the top-level CMake build output against lib_gmath.
#target_link_libraries( native-lib ... lib_gmath )
#target_include_directories(native-lib PUBLIC ${CMAKE_SOURCE_DIR}/../../../Include)

#target_include_directories(native-lib PUBLIC ${CMAKE_SOURCE_DIR}/km_l2)

#target_include_directories(native-lib PUBLIC ${CMAKE_SOURCE_DIR}/src/main/cpp/km_l2)
find_library(LOG log)

#target_link_libraries(native-lib
#                      ${CMAKE_SOURCE_DIR}/../../../lib/google-android/${ANDROID_ABI}/libSecureKeyBox.a
#                      ${CMAKE_SOURCE_DIR}/../../../lib/google-android/${ANDROID_ABI}/libSkbPlatform.a
#                      ${LOG})
target_link_libraries(km_l2
                      ${CMAKE_SOURCE_DIR}/../zimplib/google-android/${ANDROID_ABI}/libSecureKeyBox.a
                      ${CMAKE_SOURCE_DIR}/../zimplib/google-android/${ANDROID_ABI}/libSkbPlatform.a
                        ${LOG}
                        ${lib_km_l2})


#include(${CMAKE_SOURCE_DIR}/src/main/cpp/amadis/CMakeLists.txt)

# If this build uses tamper resistant SKB library, the scp-update-binary tool has to be called on the final product
#find_file(SCP_NWDB libSecureKeyBox.nwdb "${CMAKE_SOURCE_DIR}/../../../lib/google-android/${ANDROID_ABI}" NO_DEFAULT_PATH NO_CMAKE_FIND_ROOT_PATH)
find_file(SCP_NWDB libSecureKeyBox.nwdb "${CMAKE_SOURCE_DIR}/../zimplib/google-android/${ANDROID_ABI}" NO_DEFAULT_PATH NO_CMAKE_FIND_ROOT_PATH)
if(SCP_NWDB)
    # Protection database file is present, which means tamper resistant SKB library is being used
    if(CMAKE_HOST_SYSTEM_NAME STREQUAL "Darwin")
        set(HOST_PLATFORM "macOS")
    else()
        set(HOST_PLATFORM ${CMAKE_HOST_SYSTEM_NAME})
    endif()
#    find_file(SCP_UPDATE_BINARY NAMES scp-update-binary.exe scp-update-binary PATHS "${CMAKE_SOURCE_DIR}/../../../bin/${HOST_PLATFORM}/scp-update-binary" NO_DEFAULT_PATH NO_CMAKE_FIND_ROOT_PATH)
    find_file(SCP_UPDATE_BINARY NAMES scp-update-binary.exe scp-update-binary PATHS "${CMAKE_SOURCE_DIR}/../bin/${HOST_PLATFORM}/scp-update-binary" NO_DEFAULT_PATH NO_CMAKE_FIND_ROOT_PATH)
    add_custom_command(
        TARGET km_l2
        POST_BUILD
        COMMAND "${SCP_UPDATE_BINARY}" --binary=$<TARGET_FILE:native-lib> "${SCP_NWDB}" 
        COMMENT Applying scp-update-binary tool... )
endif()
