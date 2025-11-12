# Audio service timeout
PRODUCT_SYSTEM_DEFAULT_PROPERTIES += \
    audio.service.client_wait_ms=8500

# Enable App Locale Settings to all apps
PRODUCT_SYSTEM_PROPERTIES += \
    persist.sys.fflag.override.settings_app_locale_opt_in_enabled=false

# Bluetooth auto-on
PRODUCT_PRODUCT_PROPERTIES += \
    bluetooth.server.automatic_turn_on?=true

# One Handed mode
PRODUCT_PRODUCT_PROPERTIES += \
    ro.support_one_handed_mode=true

# Media
PRODUCT_SYSTEM_PROPERTIES += \
    media.recorder.show_manufacturer_and_model=true

# Default wifi country code
PRODUCT_SYSTEM_PROPERTIES += \
    ro.boot.wificountrycode?=00

# Diable phantom process monitoring
PRODUCT_SYSTEM_PROPERTIES += \
    persist.sys.fflag.override.settings_enable_monitor_phantom_procs?=false

# Disable default frame rate limit for games
PRODUCT_PRODUCT_PROPERTIES += \
    debug.graphics.game_default_frame_rate.disabled=true

# Disable display refresh rate override 
PRODUCT_SYSTEM_PROPERTIES += \
    ro.surface_flinger.enable_frame_rate_override?=false

# Boost surfaceflinger min uclamp utilization
PRODUCT_SYSTEM_PROPERTIES += \
    ro.surface_flinger.uclamp.min?=165

# Enable blur
TARGET_ENABLE_BLUR ?= true
ifeq ($(TARGET_ENABLE_BLUR),true)
PRODUCT_SYSTEM_PROPERTIES += \
    ro.custom.blur.enable=true
else
PRODUCT_SYSTEM_PROPERTIES += \
    ro.custom.blur.enable=false
endif

PRODUCT_SYSTEM_PROPERTIES += \
    ro.surface_flinger.supports_background_blur=1

# Disable QCom Radio (IMS) logging
ifeq ($(BOARD_USES_QCOM_HARDWARE),true)
PRODUCT_SYSTEM_EXT_PROPERTIES += \
persist.vendor.ims.disableADBLogs=1 \
persist.vendor.ims.disableDebugLogs=1 \
persist.vendor.ims.disableIMSLogs=1 \
persist.vendor.ims.disableQXDMLogs=1
endif

# Appcompat overrides
ifeq ($(TARGET_BUILD_VARIANT),user)
PRODUCT_SYSTEM_PROPERTIES += \
    ro.appcompat_override.ro.boot.verifiedbootstate=green \
    ro.appcompat_override.ro.boot.realmebootstate=green \
    ro.appcompat_override.vendor.boot.verifiedbootstate=green \
    ro.appcompat_override.oplusboot.verifiedbootstate=green \
    ro.appcompat_override.ro.is_ever_orange=green
endif
