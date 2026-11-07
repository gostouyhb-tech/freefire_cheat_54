cmake_minimum_required(VERSION 3.22.1)
project("ffcheat")

add_library(
    ffcheat
    SHARED
    memory.cpp
    aimbot.cpp
    jni_bridge.cpp
)

find_library(log-lib log)

target_link_libraries(
    ffcheat
    ${log-lib}
    android
)

# Strip symbols no release — reduz assinatura detectável
target_link_options(ffcheat PRIVATE
    $<$<CONFIG:Release>:-Wl,--strip-all>
)
