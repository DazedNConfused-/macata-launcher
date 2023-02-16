#!/bin/sh

export PATH=/usr/bin:/bin:/usr/sbin:/sbin

cd "${1}/Contents/Resources/"

V_KERNEL_RELEASE=$(uname -r | cut -d. -f1)
if [[ "${V_KERNEL_RELEASE}" -ge 11 ]]; then
    K_LIBRARY_PATH=DYLD_LIBRARY_PATH
    K_FRAMEWORK_PATH=DYLD_FRAMEWORK_PATH
else
    K_LIBRARY_PATH=DYLD_FALLBACK_LIBRARY_PATH
    K_FRAMEWORK_PATH=DYLD_FALLBACK_FRAMEWORK_PATH
fi

if [[ -f cataclysm ]]; then
    V_SHELL_SCRIPT="export PATH=${PATH} ${K_LIBRARY_PATH}=. ${K_FRAMEWORK_PATH}=.; cd '${PWD}' && ./cataclysm; exit"
    osascript -e "tell application \"Terminal\" to activate do script \"${V_SHELL_SCRIPT}\""
else
    export ${K_LIBRARY_PATH}=. ${K_FRAMEWORK_PATH}=.
fi

# after setting up environment (just like in Cataclysm.app), run application with flags
# (keep arguments here without quotes - also, "$@" doesn't work)
# ${2} is --savedir
# ${3} is the custom save directory, in quotes in case there are spaces in the path
# ${4} is --userdir
# ${5} is the custom user directory, in quotes in case there are spaces in the path
# ${5} is --world
# ${6} is the latest world's name, in quotes because it may have spaces (which is fairly common in randomized world names)
./cataclysm-tiles ${2} "${3}" ${4} "${5}" ${6} "${7}"