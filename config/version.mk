CUSTOM_BRAND := TheParasiteProject

CUSTOM_BUILD_DATE := $(shell date +%Y%m%d-%H%M)
CUSTOM_BUILD_DATE_UTC := $(shell date '+%Y-%m-%d %H:%M UTC %s')
CUSTOM_BUILD_VERSION := $(BUILD_ID)
CUSTOM_RELEASETYPE ?= release

CUSTOM_VERSION := $(CUSTOM_BRAND)_$(CUSTOM_BUILD)-$(BUILD_ID)-$(CUSTOM_BUILD_DATE)

PRODUCT_BRAND := $(CUSTOM_BRAND)
LINEAGE_BUILD_DATE := $(CUSTOM_BUILD_DATE)
LINEAGE_BUILDTYPE := $(CUSTOM_RELEASETYPE)
LINEAGE_VERSION := $(CUSTOM_BUILD_VERSION)
LINEAGE_DISPLAY_VERSION := $(CUSTOM_BUILD_VERSION)

# Build fingerprint
ifeq ($(BUILD_FINGERPRINT),)
BUILD_NUMBER_CUSTOM := $(shell date -u +%H%M)
CUSTOM_DEVICE ?= $(TARGET_DEVICE)
BUILD_SIGNATURE_KEYS := release-keys
BUILD_FINGERPRINT := $(PRODUCT_BRAND)/$(CUSTOM_DEVICE)/$(CUSTOM_DEVICE):$(PLATFORM_VERSION)/$(BUILD_ID)/$(BUILD_NUMBER_CUSTOM):$(TARGET_BUILD_VARIANT)/$(BUILD_SIGNATURE_KEYS)

PRODUCT_SYSTEM_PROPERTIES += \
    ro.build.fingerprint=$(BUILD_FINGERPRINT)
endif

# Versioning props
PRODUCT_SYSTEM_PROPERTIES += \
    ro.lineage.brand=$(CUSTOM_BRAND)
