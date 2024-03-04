# Libs
PRODUCT_PACKAGES += \
	org.lineageos.lib.phone

# Parasite Prebuilts
$(call inherit-product-if-exists, vendor/parasite-prebuilts/config.mk)
