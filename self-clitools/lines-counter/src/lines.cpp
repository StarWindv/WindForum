#include <fstream>
#include <span>
#include <expected>
#include <string>
#include <cstddef>
#include <system_error>
#include <algorithm>
#include <type_traits>
#include <cstdint>


enum class NewlineType : uint8_t {
    LF,
    CRLF,
    CR,
    None,
    Unknown
};


struct LineCountResult {
    std::size_t line_count{0};
    NewlineType newline_type{NewlineType::Unknown};
};


using LineCountExpected = std::expected<LineCountResult, std::error_code>;


namespace detail {
    constexpr std::size_t BUFFER_SIZE = 64 * 1024;

    constexpr std::size_t to_size_t(std::streamsize s) {
        if (s < 0) return 0;
        return static_cast<std::size_t>(s);
    }

    void process_buffer(std::span<const char> buffer,
                        bool& prev_was_cr,
                        std::size_t& lf_count,
                        std::size_t& crlf_count,
                        std::size_t& cr_count) {
        for (char c : buffer) {
            if (prev_was_cr) {
                prev_was_cr = false;
                if (c == '\n') {
                    crlf_count++;
                    continue;
                } else {
                    cr_count++;
                }
            }

            if (c == '\n') {
                lf_count++;
            } else if (c == '\r') {
                prev_was_cr = true;
            }
        }
    }

    NewlineType determine_newline_type(std::size_t lf, std::size_t crlf, std::size_t cr) {
        const auto total = lf + crlf + cr;
        if (total == 0) return NewlineType::Unknown;

        if (crlf >= lf && crlf >= cr) return NewlineType::CRLF;
        if (lf >= cr) return NewlineType::LF;
        return NewlineType::CR;
    }
}


std::optional<LineCountResult> count_lines(const std::string& filename) {
    std::ifstream file(filename, std::ios::binary);
    if (!file.is_open()) {
        return std::nullopt;
    }

    uint64_t newline_count = 0;
    NewlineType detected_type = NewlineType::None;
    bool has_content = false;
    char prev_ch = 0;
    char ch;

    while (file.get(ch)) {
        has_content = true;

        if (ch == '\r') {
            newline_count++;
            if (detected_type == NewlineType::None) {
                detected_type = NewlineType::CR;
            }
        }
        else if (ch == '\n') {
            if (prev_ch != '\r') {
                newline_count++;
                if (detected_type == NewlineType::None) {
                    detected_type = NewlineType::LF;
                }
            }
            else {
                if (detected_type == NewlineType::CR) {
                    detected_type = NewlineType::CRLF;
                }
            }
        }
        prev_ch = ch;
    }

    uint64_t line_count = newline_count;
    if (has_content) {
        line_count += 1;
    }

    return LineCountResult{
        .line_count = line_count,
        .newline_type = detected_type
    };
}
