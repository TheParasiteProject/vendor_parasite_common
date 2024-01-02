# Keylayout
PRODUCT_COPY_FILES += \
    $(VENDOR_PARASITE_COMMON_DIR)/misc/keylayout/Vendor_2dc8_Product_6006.kl:$(TARGET_COPY_OUT_PRODUCT)/usr/keylayout/Vendor_2dc8_Product_6006.kl

ifneq ($(PRODUCT_TYPE),go)
# Freeform window management
PRODUCT_COPY_FILES += \
    frameworks/native/data/etc/android.software.freeform_window_management.xml:$(TARGET_COPY_OUT_PRODUCT)/etc/permissions/android.software.freeform_window_management.xml
endif

# Cloned app exemption
PRODUCT_COPY_FILES += \
     $(VENDOR_PARASITE_COMMON_DIR)/prebuilt/common/etc/sysconfig/preinstalled-packages-platform-custom-product.xml:$(TARGET_COPY_OUT_PRODUCT)/etc/sysconfig/preinstalled-packages-platform-custom-product.xml
