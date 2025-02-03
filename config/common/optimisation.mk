# Don't compile SystemUITests
EXCLUDE_SYSTEMUI_TESTS := true

# Shutdown
PRODUCT_SYSTEM_EXT_PROPERTIES += \
    ro.build.shutdown_timeout?=0

# EGL - Blobcache configuration
PRODUCT_SYSTEM_EXT_PROPERTIES += \
    ro.egl.blobcache.multifile?=true \
    ro.egl.blobcache.multifile_limit?=33554432

# Disable touch video heatmap to reduce latency, motion jitter, and CPU usage
# on supported devices with Deep Press input classifier HALs and models
PRODUCT_PRODUCT_PROPERTIES += \
    ro.input.video_enabled?=false

# Disable MTE Async on some processes
PRODUCT_SYSTEM_EXT_PROPERTIES += \
    persist.arm64.memtag.system_server?=off \
    persist.arm64.memtag.process.system_server?=off \
    persist.arm64.memtag.app.com.android.se?=off \
    persist.arm64.memtag.app.com.android.nfc?=off \
    persist.arm64.memtag.app.com.android.bluetooth?=off \
    persist.arm64.memtag.app.com.google.android.bluetooth?=off

# Disable Scudo to save RAM and use 32-bit libc variant by default
PRODUCT_DISABLE_SCUDO ?= false
MALLOC_SVELTE_FOR_LIBC32 ?= true

# HWUI
PRODUCT_SYSTEM_EXT_PROPERTIES += \
    debug.hwui.enable_adpf_cpu_hint?=true \
    debug.hwui.use_hint_manager?=true \
    debug.hwui.target_cpu_time_percent?=30 \
    debug.hwui.skip_eglmanager_telemetry=true

# Suspend properties
PRODUCT_PRODUCT_PROPERTIES += \
    suspend.short_suspend_threshold_millis?=2000 \
    suspend.max_sleep_time_millis?=40000 \
    suspend.short_suspend_backoff_enabled?=true

# Watchdog timeout loop breaker
PRODUCT_PRODUCT_PROPERTIES += \
    framework_watchdog.fatal_window.second?=600 \
    framework_watchdog.fatal_count?=3

# Zygote
PRODUCT_SYSTEM_EXT_PROPERTIES += \
    zygote.critical_window.minute?=10

# Virtual ab
PRODUCT_SYSTEM_EXT_PROPERTIES += \
    ro.virtual_ab.batch_writes=true \
    ro.virtual_ab.compression.enabled=true \
    ro.virtual_ab.compression.threads=true \
    ro.virtual_ab.compression.xor.enabled=false \
    ro.virtual_ab.cow_op_merge_size=16 \
    ro.virtual_ab.io_uring.enabled=false \
    ro.virtual_ab.merge_thread_priority=19 \
    ro.virtual_ab.num_merge_threads=1 \
    ro.virtual_ab.num_verify_threads=1 \
    ro.virtual_ab.num_worker_threads=3 \
    ro.virtual_ab.o_direct.enabled=true \
    ro.virtual_ab.read_ahead_size=16 \
    ro.virtual_ab.userspace.snapshots.enabled=true \
    ro.virtual_ab.verify_block_size=1048576 \
    ro.virtual_ab.verify_threshold_size=1073741824 \
    ro.virtual_ab.worker_thread_priority=0

# Activity animation override
PERF_ANIM_OVERRIDE ?= false

PRODUCT_PRODUCT_PROPERTIES += \
    persist.sys.activity_anim_perf_override=$(PERF_ANIM_OVERRIDE)

ifeq ($(PERF_ANIM_OVERRIDE),true)
PRODUCT_PRODUCT_PROPERTIES += \
    debug.sf.predict_hwc_composition_strategy=0
endif

# Inherit art options
include $(VENDOR_PARASITE_COMMON_DIR)/config/common/art.mk

# Inherit LMKD options
include $(VENDOR_PARASITE_COMMON_DIR)/config/common/lmkd.mk
