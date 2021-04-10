cmake_minimum_required(VERSION 3.4.1)

# 定义一个全局变量SOURCE，保存指定目录（CMAKE_SOURCE_DIR）下的所有源文件
#file(GLOB SOURCE ${CMAKE_SOURCE_DIR}/*.cpp)

# 查找指定目录(${CMAKE_SOURCE_DIR})下所有源文件，并保存到SOURCE中
aux_source_directory(${CMAKE_SOURCE_DIR} SOURCE)

message(STATUS "SOURCE = ${SOURCE}")    # 测试打印信息

# 指定头文件目录
include_directories(${CMAKE_SOURCE_DIR})

add_library(
        gifloader

        SHARED

        ${SOURCE})

find_library(
        log-lib
        log)

find_library(
        jnigraphics-lib
        jnigraphics)

target_link_libraries(
        gifloader
        ${jnigraphics-lib}
        ${log-lib})