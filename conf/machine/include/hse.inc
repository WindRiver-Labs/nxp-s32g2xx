HSE_FW_DEFAULT_NAME ?= "s32gen1_hse_fw.bin"
HSE_LOCAL_FIRMWARE_DIR ?= ""
HSE_FW_NAME_S32G2 ?= ""
HSE_FW_NAME_S32G3 ?= ""

HSE_SEC_KEYS ?= "sec_keys"
HSE_SEC_PRI_KEY ?= "rsa2048_private.pem"
HSE_SEC_PUB_KEY ?= "rsa2048_public.der"
HSE_SEC_PRI_KEY_BIT_LEN ?= "2048"

HSE_SEC_SIGN_SRC ?= "fip.bin"
HSE_SEC_SIGN_DST ?= "fip.bin.signature"

HSE_SEC_ENABLED ?= "${@bb.utils.contains('HSE_LOCAL_FIRMWARE_DIR', '', '1', '0', d)}"
