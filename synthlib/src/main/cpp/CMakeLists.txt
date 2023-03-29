# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.22.1)

# Declares and names the project.

project("synthlib")


# Create a variable fluidsynth_DIR to specify where the fluidsynth library is located.
set(fluidsynth_DIR ${CMAKE_CURRENT_SOURCE_DIR}/fluidsynth)

add_library(libFLAC SHARED IMPORTED)
set_target_properties(libFLAC PROPERTIES IMPORTED_LOCATION ${fluidsynth_DIR}/lib/${ANDROID_ABI}/libFLAC.so)

add_library(libfluidsynth SHARED IMPORTED)
set_target_properties(libfluidsynth PROPERTIES IMPORTED_LOCATION ${fluidsynth_DIR}/lib/${ANDROID_ABI}/libfluidsynth.so)

add_library(libfluidsynth-assetloader SHARED IMPORTED)
set_target_properties(libfluidsynth-assetloader PROPERTIES IMPORTED_LOCATION ${fluidsynth_DIR}/lib/${ANDROID_ABI}/libfluidsynth-assetloader.so)

add_library(libgio-2.0 SHARED IMPORTED)
set_target_properties(libgio-2.0 PROPERTIES IMPORTED_LOCATION ${fluidsynth_DIR}/lib/${ANDROID_ABI}/libgio-2.0.so)

add_library(libglib-2.0 SHARED IMPORTED)
set_target_properties(libglib-2.0 PROPERTIES IMPORTED_LOCATION ${fluidsynth_DIR}/lib/${ANDROID_ABI}/libglib-2.0.so)

add_library(libgmodule-2.0 SHARED IMPORTED)
set_target_properties(libgmodule-2.0 PROPERTIES IMPORTED_LOCATION ${fluidsynth_DIR}/lib/${ANDROID_ABI}/libgmodule-2.0.so)

add_library(libgobject-2.0 SHARED IMPORTED)
set_target_properties(libgobject-2.0 PROPERTIES IMPORTED_LOCATION ${fluidsynth_DIR}/lib/${ANDROID_ABI}/libgobject-2.0.so)

add_library(libgthread-2.0 SHARED IMPORTED)
set_target_properties(libgthread-2.0 PROPERTIES IMPORTED_LOCATION ${fluidsynth_DIR}/lib/${ANDROID_ABI}/libgthread-2.0.so)

add_library(libinstpatch-1.0 SHARED IMPORTED)
set_target_properties(libinstpatch-1.0 PROPERTIES IMPORTED_LOCATION ${fluidsynth_DIR}/lib/${ANDROID_ABI}/libinstpatch-1.0.so)

add_library(liboboe SHARED IMPORTED)
set_target_properties(liboboe PROPERTIES IMPORTED_LOCATION ${fluidsynth_DIR}/lib/${ANDROID_ABI}/liboboe.so)

add_library(libogg SHARED IMPORTED)
set_target_properties(libogg PROPERTIES IMPORTED_LOCATION ${fluidsynth_DIR}/lib/${ANDROID_ABI}/libogg.so)

add_library(libopus SHARED IMPORTED)
set_target_properties(libopus PROPERTIES IMPORTED_LOCATION ${fluidsynth_DIR}/lib/${ANDROID_ABI}/libopus.so)

add_library(libsndfile SHARED IMPORTED)
set_target_properties(libsndfile PROPERTIES IMPORTED_LOCATION ${fluidsynth_DIR}/lib/${ANDROID_ABI}/libsndfile.so)

add_library(libvorbis SHARED IMPORTED)
set_target_properties(libvorbis PROPERTIES IMPORTED_LOCATION ${fluidsynth_DIR}/lib/${ANDROID_ABI}/libvorbis.so)

add_library(libvorbisenc SHARED IMPORTED)
set_target_properties(libvorbisenc PROPERTIES IMPORTED_LOCATION ${fluidsynth_DIR}/lib/${ANDROID_ABI}/libvorbisenc.so)

add_library(libvorbisfile SHARED IMPORTED)
set_target_properties(libvorbisfile PROPERTIES IMPORTED_LOCATION ${fluidsynth_DIR}/lib/${ANDROID_ABI}/libvorbisfile.so)

add_library(libz SHARED IMPORTED)
set_target_properties(libz PROPERTIES IMPORTED_LOCATION ${fluidsynth_DIR}/lib/${ANDROID_ABI}/libz.so)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
        synthlib

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        synthlib.cpp
        player.cpp
)

target_include_directories(synthlib PRIVATE ${fluidsynth_DIR}/include)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        synthlib

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib}

        libFLAC
        libfluidsynth
        libfluidsynth-assetloader
        libgio-2.0
        libglib-2.0
        libgmodule-2.0
        libgobject-2.0
        libgthread-2.0
        libinstpatch-1.0
        liboboe
        libogg
        libopus
        libsndfile
        libvorbis
        libvorbisenc
        libvorbisfile
)