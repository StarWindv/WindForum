#include <pybind11/pybind11.h>
#include <pybind11/stl.h>

#include "lines.hpp"
#include "lines_cli.cpp"
#include "main.cpp"

namespace py = pybind11;

int run(const std::vector<std::string>& args) {
    std::vector<char*> argv;
    argv.push_back(const_cast<char*>("lines_counter"));
    for (const auto& arg : args) {
        argv.push_back(const_cast<char*>(arg.c_str()));
    }
    int argc = argv.size();

    return main(argc, argv.data());
}

PYBIND11_MODULE(lines_counter, m) {
    m.doc() = " ";

    m.def("main", &run,
          py::arg("args") = std::vector<std::string>(),
          " ");
}