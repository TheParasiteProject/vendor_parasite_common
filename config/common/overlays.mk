# Non-Runtime Resource Overlays
PRODUCT_ENFORCE_RRO_EXCLUDED_OVERLAYS += \
    $(VENDOR_PARASITE_COMMON_DIR)/overlay

PRODUCT_PACKAGE_OVERLAYS += \
    $(VENDOR_PARASITE_COMMON_DIR)/overlay/common

# Custom config overlay
PRODUCT_PACKAGES += \
    CustomConfigOverlay

# Cutout control overlay
PRODUCT_PACKAGES += \
    DummyCutoutOverlay \
    NoCutoutOverlay \
    AvoidAppsInCutoutOverlay

PRODUCT_PRODUCT_PROPERTIES += \
    ro.support_hide_display_cutout=true

# Navigation bar IME space overlay
PRODUCT_PACKAGES += \
    LineageNavigationBarNarrowSpace \
    LineageNavigationBarNoSpace

# SystemUI Customisation
PRODUCT_PACKAGES += \
    SystemUICustomOverlay \
    SystemUIFlagFlipperOverlay

# Settings Customisation
PRODUCT_PACKAGES += \
    SettingsCustomOverlay

# SettingsProvider Customisation
PRODUCT_PACKAGES += \
    SettingsProviderOverlay
