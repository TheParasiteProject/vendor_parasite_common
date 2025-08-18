# Don't build debug for host or device
ifeq ($(TARGET_BUILD_VARIANT), user)
# PRODUCT_ART_TARGET_INCLUDE_DEBUG_BUILD := false
# PRODUCT_MINIMIZE_JAVA_DEBUG_INFO := true
PRODUCT_OTHER_JAVA_DEBUG_INFO := false
PRODUCT_SYSTEM_SERVER_DEBUG_INFO := false
WITH_DEXPREOPT_DEBUG_INFO := false
ART_BUILD_TARGET_NDEBUG := false
ART_BUILD_TARGET_DEBUG := false
ART_BUILD_HOST_NDEBUG := false
ART_BUILD_HOST_DEBUG := false
USE_DEX2OAT_DEBUG := false
endif

# Dex preopt
ifneq ($(TARGET_INCLUDE_PIXEL_FRAMEWORKS),true)
PRODUCT_DEXPREOPT_SPEED_APPS += \
    Settings \
    SystemUI
endif

# Enable whole-program R8 Java optimizations for SystemUI and system_server,
# but also allow explicit overriding for testing and development.
SYSTEM_OPTIMIZE_JAVA ?= true
SYSTEMUI_OPTIMIZE_JAVA ?= true
# For full optimization rather than just shrinking
FULL_SYSTEM_OPTIMIZE_JAVA := true

# Disable debug infos
ifeq ($(TARGET_BUILD_VARIANT), user)
PRODUCT_SYSTEM_EXT_PROPERTIES += \
    dalvik.vm.minidebuginfo=false \
    dalvik.vm.dex2oat-minidebuginfo=false \
    dalvik.vm.dex2oat-flags?=--no-watch-dog \
    dalvik.vm.check-dex-sum=false \
    dalvik.vm.checkjni=false \
    dalvik.vm.debug.alloc=0
endif

# Dex pre-opt
WITH_DEXPREOPT := true
WITH_DEXPREOPT_BOOT_IMG_AND_SYSTEM_SERVER_ONLY := false
PRODUCT_DEX_PREOPT_DEFAULT_COMPILER_FILTER := speed-profile
PRODUCT_ALWAYS_PREOPT_EXTRACTED_APK := true

# Boot image profile
PRODUCT_USE_PROFILE_FOR_BOOT_IMAGE := true
PRODUCT_COPY_FILES += \
    $(VENDOR_PARASITE_COMMON_DIR)/config/common/lowram_boot_profiles/preloaded-classes:$(TARGET_COPY_OUT_SYSTEM)/etc/preloaded-classes
PRODUCT_DEX_PREOPT_BOOT_IMAGE_PROFILE_LOCATION := \
    $(VENDOR_PARASITE_COMMON_DIR)/config/common/lowram_boot_profiles/boot-image-profile.txt

PRODUCT_ARTIFACT_PATH_REQUIREMENT_ALLOWED_LIST += \
    system/etc/preloaded-classes

# System server compiler
PRODUCT_SYSTEM_SERVER_COMPILER_FILTER := speed-profile

# Dexopt boot types
PRODUCT_SYSTEM_EXT_PROPERTIES += \
    pm.dexopt.post-boot=extract \
    pm.dexopt.boot-after-mainline-update=verify \
    pm.dexopt.first-boot=verify \
    pm.dexopt.boot-after-ota=verify \
    pm.dexopt.boot-after-mainline-update=verify

# Dexopt install
PRODUCT_SYSTEM_EXT_PROPERTIES += \
    pm.dexopt.install=speed-profile \
    pm.dexopt.install-fast=skip \
    pm.dexopt.install-bulk=speed-profile \
    pm.dexopt.install-bulk-secondary=verify \
    pm.dexopt.install-bulk-downgraded=verify \
    pm.dexopt.install-bulk-secondary-downgraded=extract

# Dexopt filters
PRODUCT_SYSTEM_EXT_PROPERTIES += \
    dalvik.vm.dex2oat-filter=speed \
    dalvik.vm.image-dex2oat-filter=speed \
    dalvik.vm.systemuicompilerfilter=speed \
    dalvik.vm.madvise-random=true \
    dalvik.vm.enable_pr_dexopt=false \
    dalvik.vm.madvise.vdexfile.size=31457280 \
    dalvik.vm.madvise.odexfile.size=31457280 \
    dalvik.vm.madvise.artfile.size=0 \
    dalvik.vm.dex2oat-swap?=true \
    pm.dexopt.bg-dexopt=speed-profile \
    pm.dexopt.ab-ota=speed-profile \
    pm.dexopt.cmdline=verify \
    pm.dexopt.inactive=verify \
    pm.dexopt.shared=speed \
    pm.dexopt.downgrade_after_inactive_days=10

# dex2oat threads and CPU sets (default)
PRODUCT_SYSTEM_EXT_PROPERTIES += \
    dalvik.vm.boot-dex2oat-cpu-set=0,1,6,7 \
    dalvik.vm.boot-dex2oat-threads=4 \
    dalvik.vm.dex2oat-cpu-set=0,1,6,7 \
    dalvik.vm.dex2oat-threads=4 \
    dalvik.vm.image-dex2oat-cpu-set=0,1,6,7 \
    dalvik.vm.image-dex2oat-threads=4 \
    dalvik.vm.restore-dex2oat-cpu-set=0,1,6,7 \
    dalvik.vm.restore-dex2oat-threads=4

# Enable 64Bit dex2oat
PRODUCT_SYSTEM_EXT_PROPERTIES += \
    dalvik.vm.dex2oat64.enabled?=$(TARGET_SUPPORTS_64_BIT_APPS)
