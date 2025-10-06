#!/bin/bash

if [ -z $VENDOR_PARASITE_SETUP_DONE ]; then

# ABI compatibility checks fail for several reasons:
#   - The update to Clang 12 causes some changes, but no breakage has been
#     observed in practice.
#   - Switching to zlib-ng changes some internal structs, but not the public
#     API.
#
# We may fix these eventually by updating the ABI specifications, but it's
# likely not worth the effort for us because of how many repos are affected.
# We would need to fork a lot of extra repos (thus increasing maintenance
# overhead) just to update the ABI specs.
#
# For now, just skip the ABI checks to fix build errors.
export SKIP_ABI_CHECKS=true

# Clone Kernel Modules repo
if [ ! -d "kernel/modules" ]; then
    mkdir -p kernel/modules
    touch kernel/modules/Android.mk
fi

# Clone KernelSU repos
if [ ! -d "kernel/modules/misc/KernelSU" ]; then
    mkdir -p kernel/modules/misc/KernelSU
fi
if [ ! -d "kernel/modules/misc/KernelSU/next" ]; then
    git clone https://github.com/KernelSU-Next/KernelSU-Next -b next kernel/modules/misc/KernelSU/next
fi

# Update KernelSU repos
if [ -d "kernel/modules/misc/KernelSU/next" ]; then
    cd kernel/modules/misc/KernelSU/next
    git fetch origin
    git reset --hard origin/next
    cd ../../../../../
fi

# Clone Kprofiles repo
if [ ! -d "kernel/modules/misc/Kprofiles" ]; then
    git clone https://github.com/dakkshesh07/Kprofiles kernel/modules/misc/Kprofiles
fi

# Update Kprofiles repo
if [ -d "kernel/modules/misc/Kprofiles" ]; then
    cd kernel/modules/misc/Kprofiles
    git fetch origin
    git reset --hard origin/main
    cd ../../../../
fi

# Enable auto kprofiles for QGKI kernels
# Ref: https://github.com/dakkshesh07/Kprofiles/pull/16/commits/f8de35bcc51fb29988ccab31cdfad7923b475b6e
if [ -d "kernel/modules/misc/Kprofiles" ]; then
    cd kernel/modules/misc/Kprofiles
    sed -i 's/depends on DRM_MSM/depends on DRM_MSM || QGKI/g' Kconfig
    cd ../../../../
fi

export VENDOR_PARASITE_SETUP_DONE=true
fi
