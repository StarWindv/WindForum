#include <iostream>
#include <iomanip>

#include "lines.hpp"
#include "lines_cli.cpp"


int main(int argc, char* argv[]) {
    auto config_opt = parse_cli_args(argc, argv);
    if (!config_opt) {
        return 1;
    }
    const CliConfig& config = config_opt.value();

    uint64_t total_lines = process_files_and_count(config);

    if (!config.only_lines) {
        std::cout << " ---------------------------------------" << std::endl;
        std::cout << std::right << std::setw(8) << total_lines << "  Total" << std::endl;
    } else {
        std::cout << total_lines << std::endl;
    }

    return 0;
}
