SOONG_CONFIG_lineageGlobalVars += \
    camera_needs_camera_needs_depth_sensor_override \
    camera_needs_client_info \
    camera_needs_client_info_lib \
    camera_needs_client_info_lib_oplus \
    camera_needs_miui_camera_mode_support \
    camera_uses_newer_hidl_override_format \
    disable_bluetooth_le_read_buffer_size_v2 \
    disable_bluetooth_le_set_host_feature \
    uses_miui_camera \
    needs_camera_boottime \
    needs_oplus_tag \
    target_alternative_futex_waiters \
    target_camera_package_name \
    target_inputdispatcher_skip_event_key \
    target_uses_prebuilt_dynamic_partitions \
    uses_legacy_fd_fbdev \
    target_libcameraservice_ext_lib

# Set default values
TARGET_INPUTDISPATCHER_SKIP_EVENT_KEY ?= 0
TARGET_CAMERA_SERVICE_EXT_LIB ?= libcameraservice_ext_lib

# Soong value variables
SOONG_CONFIG_lineageGlobalVars_camera_needs_camera_needs_depth_sensor_override := $(TARGET_USES_DEPTHSENSOR_OVERRIDE)
SOONG_CONFIG_lineageGlobalVars_camera_needs_client_info := $(TARGET_CAMERA_NEEDS_CLIENT_INFO)
SOONG_CONFIG_lineageGlobalVars_camera_needs_client_info_lib := $(TARGET_CAMERA_NEEDS_CLIENT_INFO_LIB)
SOONG_CONFIG_lineageGlobalVars_camera_needs_client_info_lib_oplus := $(TARGET_CAMERA_NEEDS_CLIENT_INFO_LIB_OPLUS)
SOONG_CONFIG_lineageGlobalVars_camera_needs_miui_camera_mode_support := $(TARGET_USES_MIUI_CAMERA)
SOONG_CONFIG_lineageGlobalVars_camera_uses_newer_hidl_override_format := $(TARGET_CAMERA_USES_NEWER_HIDL_OVERRIDE_FORMAT)
SOONG_CONFIG_lineageGlobalVars_disable_bluetooth_le_read_buffer_size_v2 := $(TARGET_DISABLE_BLUETOOTH_LE_READ_BUFFER_SIZE_V2)
SOONG_CONFIG_lineageGlobalVars_disable_bluetooth_le_set_host_feature := $(TARGET_DISABLE_BLUETOOTH_LE_SET_HOST_FEATURE)
SOONG_CONFIG_lineageGlobalVars_uses_miui_camera := $(TARGET_USES_MIUI_CAMERA)
SOONG_CONFIG_lineageGlobalVars_needs_camera_boottime := $(TARGET_CAMERA_BOOTTIME_TIMESTAMP)
SOONG_CONFIG_lineageGlobalVars_needs_oplus_tag := $(TARGET_NEEDS_OPLUS_VENDOR_TAG)
SOONG_CONFIG_lineageGlobalVars_target_alternative_futex_waiters := $(TARGET_ALTERNATIVE_FUTEX_WAITERS)
SOONG_CONFIG_lineageGlobalVars_target_camera_package_name := $(TARGET_CAMERA_PACKAGE_NAME)
SOONG_CONFIG_lineageGlobalVars_target_inputdispatcher_skip_event_key := $(TARGET_INPUTDISPATCHER_SKIP_EVENT_KEY)
SOONG_CONFIG_lineageGlobalVars_target_uses_prebuilt_dynamic_partitions := $(TARGET_USES_PREBUILT_DYNAMIC_PARTITIONS)
SOONG_CONFIG_lineageGlobalVars_uses_legacy_fd_fbdev := $(TARGET_USES_LEGACY_FD_FBDEV)
SOONG_CONFIG_lineageGlobalVars_target_libcameraservice_ext_lib := $(TARGET_CAMERA_SERVICE_EXT_LIB)
