# Parasite Configuration
VENDOR_PARASITE_COMMON_DIR := vendor/parasite/common
$(call inherit-product, $(VENDOR_PARASITE_COMMON_DIR)/config/common.mk)

# Parasite Certification
$(call inherit-product-if-exists, vendor/parasite/certification/config.mk)

# Parasite Signatures
$(call inherit-product-if-exists, vendor/parasite/signatures/config.mk)

# Parasite Prebuilts
$(call inherit-product-if-exists, vendor/parasite/prebuilts/config.mk)

# GMS
$(call inherit-product, vendor/google/gms/products/gms.mk)

# Microsoft
TARGET_PHONE_LINK_SUPPORTED := false
$(call inherit-product-if-exists, vendor/microsoft/mms/products/mms.mk)

# Parasite Test Suite
$(call inherit-product-if-exists, vendor/parasite/testsuite/config.mk)
