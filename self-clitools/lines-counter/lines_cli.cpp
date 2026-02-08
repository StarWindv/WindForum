#include <vector>
#include <string>
#include <filesystem>
#include <algorithm>
#include <optional>
#include <ranges>
#include <iomanip>
#include <cctype>
#include <iostream>

#include "lines.hpp"


namespace fs = std::filesystem;
namespace ranges = std::ranges;


const std::string HELP = R"(Count the lines of files with the specified extensions
[Usage] <bin_name> <extension>
           [-h /--help] | Output Message and Exit
        [-e /--exclude] | Exclude Files From the Specified Path
     [-ol/--only_lines] | Output Total Lines Only
[-sf/--suppress-format] | Prohibit outputting the line break format of the file
)";


struct CliConfig {
    std::vector<std::string> include_extensions;
    std::vector<fs::path> exclude_paths;
    fs::path root_dir = fs::current_path();

    std::string bin_name = "";

    bool only_lines = false;
    bool suppress_format = false;
    bool output_help = false;
};


std::string unify_path_separator(const std::string& path) {
    std::string unified_path = path;

    char system_sep = static_cast<char>(fs::path::preferred_separator);

    std::replace(unified_path.begin(), unified_path.end(), system_sep, '/');

    return unified_path;
}


std::optional<CliConfig> parse_cli_args(int argc, char* argv[]) {
    CliConfig config;
    bool expect_exclude = false;
    config.bin_name = argv[0];

    std::string help_msg = HELP;
    std::string ph = "<bin_name>";
    size_t pos = help_msg.find(ph);
    help_msg.replace(pos, ph.length(), config.bin_name);

    for (int i = 1; i < argc; ++i) {
        std::string arg = argv[i];

        if (arg == "-ol" || arg == "--only-lines") {
            config.only_lines = true;
            continue;
        }

        if (arg == "-h" || arg == "--help") {
            std::cout << help_msg << std::endl;
            exit(0);
        }

        if (arg == "-sf" || arg == "--suppress-format") {
            config.suppress_format = true;
            continue;
        }

        if (arg == "-e" || arg == "--exclude") {
            expect_exclude = true;
            continue;
        }

        if (expect_exclude) {
            fs::path exclude_path = arg;
            if (exclude_path.is_relative()) {
                exclude_path = fs::absolute(exclude_path);
            }
            config.exclude_paths.push_back(exclude_path);
            expect_exclude = false;
            continue;
        }

        std::string ext = arg;
        if (!ext.empty() && ext[0] == '.') {
            ext = ext.substr(1);
        }
        if (!ext.empty()) {
            config.include_extensions.push_back(ext);
        }
    }

    if (config.include_extensions.empty()) {
        std::cerr << "[Error  ] Please specify at least one file extension to count (e.g. js, ts, py)" << std::endl;
        std::cerr << "[Usage  ] " << argv[0] << " [-e/--exclude <exclude path>] [-so/--suppress-output] [-sf/--suppress-format] <extension1> <extension2> ..." << std::endl;
        std::cerr << "[Example] " << argv[0] << " js -e outerPKG -so -sf" << std::endl;
        return std::nullopt;
    }

    return config;
}


bool is_excluded(const fs::path& path, const std::vector<fs::path>& exclude_paths) {
    for (const auto& exclude_path : exclude_paths) {
        if (fs::is_directory(exclude_path)) {
            if (path.string().starts_with(exclude_path.string())) {
                return true;
            }
        }
        else if (fs::is_regular_file(exclude_path) && path == exclude_path) {
            return true;
        }
    }
    return false;
}


uint64_t process_files_and_count(const CliConfig& config) {
    uint64_t total_lines = 0;
    bool any_file_processed = false;

    try {
        for (const auto& entry : fs::recursive_directory_iterator(config.root_dir)) {
            if (!entry.is_regular_file()) {
                continue;
            }

            const fs::path& file_path = entry.path();
            const fs::path abs_path = fs::absolute(file_path);

            if (is_excluded(abs_path, config.exclude_paths)) {
                continue;
            }

            std::string ext = file_path.extension().string();
            if (!ext.empty() && ext[0] == '.') {
                ext = ext.substr(1);
            }
            if (ranges::find(config.include_extensions, ext) == config.include_extensions.end()) {
                continue;
            }

            any_file_processed = true;
            auto result = count_lines(file_path.string());
            if (!result) {
                std::cerr << "[ERROR] Cannot Count Lines for: " << unify_path_separator(file_path.string()) << std::endl;
                continue;
            }

            const auto& res = result.value();
            total_lines += res.line_count;

            if (!config.only_lines) {
                std::string unified_file_path = unify_path_separator(file_path.string());
                std::cout << std::right << std::setw(8) << res.line_count;

                if (!config.suppress_format) {
                    const char* newline_str = [&]() -> const char* {
                        switch (res.newline_type) {
                            case NewlineType::LF:   return "LF   (\\n)  ";
                            case NewlineType::CRLF: return "CRLF (\\r\\n)";
                            case NewlineType::CR:   return "CR   (\\r)  ";
                            case NewlineType::None: return "NoLineBreak";
                            default:                return "Unknown    ";
                        }
                    }();
                    std::cout << "  [" << newline_str << "]";
                }
                std::cout << "  " << std::left << unified_file_path << std::endl;
            }
        }
    } catch (const fs::filesystem_error& e) {
        std::cerr << "[ERROR] Filesystem Error: " << e.what() << std::endl;
        return total_lines;
    }

    if (!any_file_processed && !config.only_lines) {
        std::cout << "[Warn ] No matching files found." << std::endl;
    }

    return total_lines;
}
