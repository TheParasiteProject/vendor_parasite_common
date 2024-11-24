SOONG_CONFIG_NAMESPACES += lineageGlobalVars
SOONG_CONFIG_lineageGlobalVars += \
    camera_needs_miui_camera_mode_support \
    camera_needs_camera_needs_depth_sensor_override \
    camera_needs_client_info \
    camera_needs_client_info_lib \
    camera_needs_client_info_lib_oplus \
    target_camera_package_name \
    disable_bluetooth_le_read_buffer_size_v2 \
    disable_bluetooth_le_set_host_feature \
    needs_camera_boottime \
    target_alternative_futex_waiters \
    camera_uses_newer_hidl_override_format \
    target_inputdispatcher_skip_event_key \
    target_uses_prebuilt_dynamic_partitions \
    uses_legacy_fd_fbdev \
    needs_oplus_tag \
    include_miui_camera

# Set default values
TARGET_INPUTDISPATCHER_SKIP_EVENT_KEY ?= 0

# Soong value variables
SOONG_CONFIG_lineageGlobalVars_target_inputdispatcher_skip_event_key := $(TARGET_INPUTDISPATCHER_SKIP_EVENT_KEY)
