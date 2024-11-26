# Non-Runtime Resource Overlays
PRODUCT_ENFORCE_RRO_EXCLUDED_OVERLAYS += \
    $(VENDOR_PARASITE_COMMON_DIR)/overlay

PRODUCT_PACKAGE_OVERLAYS += \
    $(VENDOR_PARASITE_COMMON_DIR)/overlay/common

# Cutout control overlay
PRODUCT_PACKAGES += \
    NoCutoutOverlay \
    DummyCutoutOverlay

# Hide navigation bar hint
PRODUCT_PACKAGES += \
    NavigationBarNoHintOverlay

# Navigation bar IME space overlay
PRODUCT_PACKAGES += \
    NavigationBarModeGesturalOverlayNarrowSpace \
    NavigationBarModeGesturalOverlayNoSpace

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

# Themes
PRODUCT_PACKAGES += \
    AndroidBlackThemeOverlay
