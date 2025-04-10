# Do not include this file into your device tree manually.

# Build broken rules
ifndef BUILD_BROKEN_DUP_RULES
    BUILD_BROKEN_DUP_RULES := true
endif
ifndef BUILD_BROKEN_ELF_PREBUILT_PRODUCT_COPY_FILES
    BUILD_BROKEN_ELF_PREBUILT_PRODUCT_COPY_FILES := true
endif
