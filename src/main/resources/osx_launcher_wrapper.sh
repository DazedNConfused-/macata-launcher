#!/bin/sh

export PATH=/usr/bin:/bin:/usr/sbin:/sbin

APP_PATH="${1}"
RESOURCES_DIR="${APP_PATH}/Contents/Resources"
MACOS_DIR="${APP_PATH}/Contents/MacOS"

V_KERNEL_RELEASE=$(uname -r | cut -d. -f1)
if [[ "${V_KERNEL_RELEASE}" -ge 11 ]]; then
    K_LIBRARY_PATH=DYLD_LIBRARY_PATH
    K_FRAMEWORK_PATH=DYLD_FRAMEWORK_PATH
else
    K_LIBRARY_PATH=DYLD_FALLBACK_LIBRARY_PATH
    K_FRAMEWORK_PATH=DYLD_FALLBACK_FRAMEWORK_PATH
fi

# function to find cataclysm executable in a directory
find_executable() {
    local dir="$1"
    if [[ ! -d "$dir" ]]; then
        return 1
    fi

    cd "$dir"

    # check for cataclysm (curses version)
    if [[ -f cataclysm && -x cataclysm ]]; then
        echo "./cataclysm"
        return 0
    fi

    # check for cataclysm-* pattern (tiles versions)
    for candidate in ./cataclysm-*; do
        if [[ -x "$candidate" && ! -d "$candidate" ]]; then
            echo "$candidate"
            return 0
        fi
    done

    return 1
}

# try Resources directory first (traditional location)
EXECUTABLE=$(find_executable "$RESOURCES_DIR")
EXEC_DIR="$RESOURCES_DIR"

# if not found in Resources, try MacOS directory
if [[ -z "$EXECUTABLE" ]]; then
    EXECUTABLE=$(find_executable "$MACOS_DIR")
    EXEC_DIR="$MACOS_DIR"
fi

if [[ -z "$EXECUTABLE" ]]; then
    echo "No valid executable found in Resources or MacOS!"
    exit 1
fi

# set up environment and change to executable directory
cd "$EXEC_DIR"
export ${K_LIBRARY_PATH}=. ${K_FRAMEWORK_PATH}=.

# after setting up environment (just like in Cataclysm.app), run the appropriate application with flags
# (keep arguments here without quotes - also, "$@" doesn't work)
# ${2} is --savedir
# ${3} is the custom save directory, in quotes in case there are spaces in the path
# ${4} is --userdir
# ${5} is the custom user directory, in quotes in case there are spaces in the path
# ${5} is --world
# ${6} is the latest world's name, in quotes because it may have spaces (which is fairly common in randomized world names)
${EXECUTABLE} ${2} "${3}" ${4} "${5}" ${6} "${7}"