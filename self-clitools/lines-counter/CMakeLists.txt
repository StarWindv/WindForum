cmake_minimum_required(VERSION 3.15)
project(lines_counter)

set(CMAKE_CXX_STANDARD 23)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

find_package(Python3 REQUIRED COMPONENTS Interpreter Development)

execute_process(
    COMMAND ${Python3_EXECUTABLE} -c "import pybind11; print(pybind11.get_cmake_dir())"
    OUTPUT_VARIABLE pybind11_DIR
    OUTPUT_STRIP_TRAILING_WHITESPACE
)

find_package(pybind11 REQUIRED PATHS ${pybind11_DIR} NO_DEFAULT_PATH)

pybind11_add_module(lines_counter
    pybind_lines.cpp
    lines.hpp
)

target_link_libraries(lines_counter PRIVATE Python3::Python)

install(TARGETS lines_counter DESTINATION .)
