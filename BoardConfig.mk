# Parasite Configuration
VENDOR_PARASITE_COMMON_DIR := vendor/parasite/common
include $(VENDOR_PARASITE_COMMON_DIR)/config/BoardConfig.mk

# Sepolicies
-include device/parasite/sepolicy/common/sepolicy.mk
