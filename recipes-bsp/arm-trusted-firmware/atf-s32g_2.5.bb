# Copyright 2019-2020 NXP

DESCRIPTION = "ARM Trusted Firmware"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://license.rst;md5=1dd070c98a281d18d9eefd938729b031"

DEPENDS += "dtc-native xxd-native bc-native"
DEPENDS += "openssl-native"

S = "${WORKDIR}/git"
B = "${WORKDIR}/build"

# ATF repository
URL ?= "git://source.codeaurora.org/external/autobsps32/arm-trusted-firmware;protocol=https"
BRANCH ?= "release/bsp32.0-2.5"
SRC_URI = "${URL};branch=${BRANCH}"
SRCREV ?= "b6b0419ec2d8cf84d1bf17cb3cd13d16558d639f"
SRC_URI[sha256sum] = "15d263b62089b46375effede12a1917cd7b267b93dd97c68fd5ddbd1dddede07"

FILESEXTRAPATHS_prepend := "${THISDIR}/files:" 
SRC_URI += " \
    file://0001-Fix-fiptool-build-error.patch \
    file://0001-s32_common.mk-Fix-DTC_VERSION.patch \
    file://0001-Makefile-Add-BUILD_PLAT-to-FORCE-s-order-only-prereq.patch \
"

PLATFORM_nxp-s32g = "s32g2xxaevb s32g274ardb2 s32g399ardb3"
BUILD_TYPE = "release"
ATF_S32G_ENABLE = "1"

EXTRA_OEMAKE += " \
                CROSS_COMPILE=${TARGET_PREFIX} \
                ARCH=${TARGET_ARCH} \
                BUILD_BASE=${B} \
                "

# FIXME: Allow linking of 'tools' binaries with native libraries
#        used for generating the boot logo and other tools used
#        during the build process.
EXTRA_OEMAKE += 'HOSTCC="${BUILD_CC} ${BUILD_CPPFLAGS} ${BUILD_LDFLAGS}" \
                 HOSTLD="${BUILD_LD}" \
                 LIBPATH="${STAGING_LIBDIR_NATIVE}" \
                 HOSTSTRIP=true'

do_compile() {
	unset LDFLAGS
	unset CFLAGS
	unset CPPFLAGS

	for plat in ${PLATFORM}; do
		ATF_BINARIES="${B}/${plat}/${BUILD_TYPE}"

		if [ "${plat}" = "s32g2xxaevb" ] && [ "${HSE_SEC_ENABLED}" = "1" ]; then
			oe_runmake -C ${S} PLAT=${plat} BL33="${DEPLOY_DIR_IMAGE}/${plat}/${UBOOT_BINARY}" MKIMAGE_CFG="${DEPLOY_DIR_IMAGE}/${plat}/tools/${UBOOT_CFGOUT}" FIP_MEMORY_OFFSET=0x3407e910 HSE_SECBOOT=1 all
		else
			oe_runmake -C ${S} PLAT=${plat} BL33="${DEPLOY_DIR_IMAGE}/${plat}/${UBOOT_BINARY}" MKIMAGE_CFG="${DEPLOY_DIR_IMAGE}/${plat}/tools/${UBOOT_CFGOUT}" all
		fi
	done
}

do_install() {
	install -d ${D}/boot
	for plat in ${PLATFORM}; do
		ATF_BINARIES="${B}/${plat}/${BUILD_TYPE}"
		cp -v ${ATF_BINARIES}/fip.s32 ${D}/boot/atf-${plat}.s32
	done
}

do_deploy() {
	install -d ${DEPLOY_DIR_IMAGE}
	for plat in ${PLATFORM}; do
		ATF_BINARIES="${B}/${plat}/${BUILD_TYPE}"

		if [ "${plat}" = "s32g2xxaevb" ] && [ "${HSE_SEC_ENABLED}" = "1" ]; then
			install -d ${ATF_BINARIES}/${HSE_SEC_KEYS}
			openssl genrsa -out ${ATF_BINARIES}/${HSE_SEC_KEYS}/${HSE_SEC_PRI_KEY}
			openssl rsa -in ${ATF_BINARIES}/${HSE_SEC_KEYS}/${HSE_SEC_PRI_KEY} -outform DER -pubout -out ${ATF_BINARIES}/${HSE_SEC_KEYS}/${HSE_SEC_PUB_KEY}
			openssl dgst -sha1 -sign ${ATF_BINARIES}/${HSE_SEC_KEYS}/${HSE_SEC_PRI_KEY} -out ${ATF_BINARIES}/${HSE_SEC_SIGN_DST} ${ATF_BINARIES}/${HSE_SEC_SIGN_SRC}
			cp -v ${ATF_BINARIES}/${HSE_SEC_KEYS}/${HSE_SEC_PUB_KEY} ${DEPLOY_DIR_IMAGE}/
			cp -v ${ATF_BINARIES}/${HSE_SEC_SIGN_DST} ${DEPLOY_DIR_IMAGE}/atf-${plat}.s32.signature
		fi

		cp -v ${ATF_BINARIES}/fip.s32 ${DEPLOY_DIR_IMAGE}/atf-${plat}.s32
	done
}

addtask deploy after do_compile

do_compile[depends] = "virtual/bootloader:do_install"

FILES_${PN} += "/boot/*"

COMPATIBLE_MACHINE = "^$"
COMPATIBLE_MACHINE_nxp-s32g = "nxp-s32g"
