# Branding
$(call inherit-product, $(VENDOR_PARASITE_COMMON_DIR)/config/version.mk)

PRODUCT_SYSTEM_PROPERTIES += \
    ro.com.android.dataroaming=false \
    ro.com.android.dateformat=MM-dd-yyyy \
    persist.sys.disable_rescue=true

ifeq ($(TARGET_BUILD_VARIANT),eng)
# Disable ADB authentication
PRODUCT_SYSTEM_PROPERTIES += ro.adb.secure.recovery=0
endif

# Allow downgrade on Updater if not disabled
ifneq ($(TARGET_ALLOW_OTA_DOWNGRADE),false)
PRODUCT_SYSTEM_PROPERTIES += \
    lineage.updater.allow_downgrading
endif

# Sensitive Phone Nmber framework
PRODUCT_PACKAGES += \
    org.lineageos.lib.phone

# To prevent rebooting due to crashing services
PRODUCT_SYSTEM_PROPERTIES += \
    init.svc_debug.no_fatal.zygote=true \
    persist.device_config.configuration.disable_rescue_party=true

# Sensors
PRODUCT_PACKAGES += \
    android.frameworks.sensorservice@1.0.vendor

# TFLite service
PRODUCT_PACKAGES += \
    libtflite \
    libtensorflowlite_jni

PRODUCT_ARTIFACT_PATH_REQUIREMENT_ALLOWED_LIST += \
    system/lib/libtensorflowlite_jni.so \
    system/lib64/libtensorflowlite_jni.so

# Custom init script
PRODUCT_PACKAGES += \
    init.custom.rc

# Apps
$(call inherit-product, $(VENDOR_PARASITE_COMMON_DIR)/config/common/apps.mk)

# Configs
$(call inherit-product, $(VENDOR_PARASITE_COMMON_DIR)/config/common/configs.mk)

# Fonts
$(call inherit-product, $(VENDOR_PARASITE_COMMON_DIR)/config/common/fonts.mk)

# Languages
$(call inherit-product, $(VENDOR_PARASITE_COMMON_DIR)/config/common/languages.mk)

# Optimisation
$(call inherit-product, $(VENDOR_PARASITE_COMMON_DIR)/config/common/optimisation.mk)

# Properties
$(call inherit-product, $(VENDOR_PARASITE_COMMON_DIR)/config/common/properties.mk)

# Overlays
$(call inherit-product, $(VENDOR_PARASITE_COMMON_DIR)/config/common/overlays.mk)

# Tools
$(call inherit-product, $(VENDOR_PARASITE_COMMON_DIR)/config/common/tools.mk)
