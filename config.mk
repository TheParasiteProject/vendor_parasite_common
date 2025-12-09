# Parasite Configuration
VENDOR_PARASITE_COMMON_DIR := vendor/parasite/common
$(call inherit-product, $(VENDOR_PARASITE_COMMON_DIR)/config/common.mk)

# Parasite Signatures
$(call inherit-product-if-exists, vendor/parasite/signatures/config.mk)

# Parasite Prebuilts
$(call inherit-product-if-exists, vendor/parasite/prebuilts/config.mk)

# Parasite Proprietary
$(call inherit-product-if-exists, vendor/parasite/proprietary/config.mk)

# GMS
TARGET_CUSTOM_APEX_CERTIFICATE_DIR := \
    $(abspath $(TOP)/vendor/parasite/signatures/common/data)
$(call inherit-product, vendor/google/gms/products/gms.mk)

# Sony
$(call inherit-product, vendor/sony/somc/products/somc.mk)

# Parasite Test Suite
$(call inherit-product-if-exists, vendor/parasite/testsuite/config.mk)
