# Extra tools in CM
PRODUCT_PACKAGES += \
    7z \
    bzip2 \
    gdbserver \
    lib7z \
    libsepol \
    micro_bench \
    mke2fs \
    oprofiled \
    pigz \
    powertop \
    sqlite3 \
    strace \
    tune2fs \
    unrar \
    unzip \
    wget \
    zip

WITH_EXFAT ?= true
ifeq ($(WITH_EXFAT),true)
TARGET_USES_EXFAT := true
PRODUCT_PACKAGES += \
    fsck.exfat \
    mkfs.exfat
endif

# These packages are excluded from user builds
ifneq ($(TARGET_BUILD_VARIANT),user)
PRODUCT_PACKAGES += \
    procrank

PRODUCT_ARTIFACT_PATH_REQUIREMENT_ALLOWED_LIST += \
    system/bin/procrank
endif

# Extra cmdline tools
PRODUCT_PACKAGES += \
    zstd

# Cache clenaer
PRODUCT_PACKAGES += \
    init.cache_cleaner.rc \
    init.cache_cleaner.sh
