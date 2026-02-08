# Line Counter
A high-performance for counting lines of code in files with specified extensions, with support for excluding paths and detecting newline formats (LF/CRLF/CR).

## Features
- Recursively count lines across files with user-specified extensions
- Exclude specific files or directories from counting
- Detect and report newline format (LF/CRLF/CR) for each processed file
- Minimal output mode (only total line count)
- Cross-platform filesystem path handling
- Binary-safe file reading to avoid encoding issues

## Prerequisites
- **C++23** or **later** (core language and standard library compliance)
- No additional build tools required (direct compilation via compiler CLI)

## Build Instructions
```bash
clang++ main.cpp -std=c++23 -O3 -o lines
```

## Usage
### Basic Syntax
```bash
./lines [OPTIONS] <extension1> <extension2> ...
```

### Command-Line Options
| Option                    | Description                                                                 |
|---------------------------|-----------------------------------------------------------------------------|
| `-h / --help`             | Display help message and exit                                               |
| `-e / --exclude <path>`   | Exclude a specific file or directory from line counting (absolute/relative) |
| `-ol / --only-lines`      | Output only the total line count (suppresses per-file details)              |
| `-sf / --suppress-format` | Suppress output of newline format (LF/CRLF/CR) for each file                |

### Examples
1. Count lines in all `.cpp` and `.hpp` files in the current directory:
   ```bash
   ./lines cpp hpp
   ```

2. Count lines in `.py` files, excluding the `venv` directory:
   ```bash
   ./lines py -e venv
   ```

3. Count lines in `.js` files and output only the total line count:
   ```bash
   ./lines js -ol
   ```

4. Count lines in `.ts` files, excluding `node_modules` and suppressing newline format output:
   ```bash
   ./lines ts -e node_modules -sf
   ```

## Technical Details
### C++23 Dependencies
This utility leverages core C++23 features for simplicity and performance:
- `std::expected` (standardized in C++23) for type-safe error handling
- Enhanced `std::ranges` for efficient directory traversal and data processing
- `std::filesystem` (C++23-compliant) for cross-platform path manipulation
- Constexpr improvements for compile-time validation of input arguments

### Line Counting Logic
- Counts lines by detecting newline characters (LF/CRLF/CR) in binary mode to handle all text encodings
- Adds 1 to the line count for files with content but no trailing newline (per POSIX standards)
- Handles edge cases (empty files, files with only newlines, mixed newline formats)

### Performance Optimizations
- Binary file reading to avoid text mode translation overhead
- Fixed-size buffer processing (64KB) for efficient I/O operations
- Early termination of exclude path checks (short-circuit evaluation)
- Optimized range-based iteration (C++23) for minimal memory usage

## Error Handling
- Gracefully handles filesystem errors (permission issues, invalid paths)
- Skips unreadable files with descriptive error messages
- Validates input arguments and provides clear usage guidance for invalid inputs
- Uses `std::expected` (C++23) to encapsulate success/failure states without exception overhead

## Output Format
### Default Mode
```
      42  [LF   (\n)  ]  src/main.cpp
     128  [CRLF (\r\n)]  include/lines.hpp
      87  [CR   (\r)  ]  scripts/legacy_file.py
 ---------------------------------------
     257  Total
```

### Minimal Mode (`-ol`)
```
257
```

## Limitations
- Does not handle compressed or binary files (designed for plain text files)
- Newline format detection reports the dominant format for files with mixed newlines
- Exclude path matching is case-sensitive on case-sensitive filesystems (e.g., Linux)
- Requires C++23-compliant compilers (older toolchains are not supported)

## License
[GPLv3](./LICENSE)
