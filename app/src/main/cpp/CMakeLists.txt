cmake_minimum_required(VERSION 3.6)

set(OpenCV_STATIC on)
set(OpenCV_DIR $ENV{OPENCV_ANDROID}/sdk/native/jni)
find_package(OpenCV REQUIRED)

add_library(photoEditor SHARED photoEditor.c)

find_library(jnigraphics-lib jnigraphics)


target_link_libraries(photoEditor android ${OpenCV_LIBS} ${jnigraphics-lib})
