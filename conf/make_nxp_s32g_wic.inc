make_nxp_s32g_wic() {
    set -e
    cd ${IMGDEPLOYDIR}
    for plat in ${UBOOT_CONFIG}; do
        atf_s32="${DEPLOY_DIR_IMAGE}/atf-$plat.s32"
        ofname="${IMAGE_BASENAME}-${MACHINE}-$plat${IMAGE_VERSION_SUFFIX}.rootfs.wic"
        cp ${IMAGE_NAME}.rootfs.wic $ofname
        if [ "$plat" = "s32g2xxaevb" ] && [ "${HSE_SEC_ENABLED}" = "1" ]; then
            dd if=$atf_s32 of=$ofname bs=512 seek=1 skip=1 conv=notrunc,fsync
            dd if=$atf_s32.signature of=$ofname  bs=512 seek=9 conv=notrunc,fsync
        else
            dd if=$atf_s32 of=$ofname conv=notrunc bs=256 count=1 seek=0
            dd if=$atf_s32 of=$ofname conv=notrunc bs=512 seek=1 skip=1
        fi
        if [ $plat = "aptiv_cvc_sousa" ]; then
            mv -f $ofname ${IMAGE_NAME}.rootfs.wic
            break
        fi
        if [ $plat = "s32g3xxaevb" ]; then
            plat="evb3"
        elif [ $plat = "s32g2xxaevb" ]; then
            plat="evb"
        else
            plat="$(echo $plat | grep -o '....$')"
        fi
        linkname="${IMAGE_LINK_NAME}-$plat.wic"
        ln -sf $ofname $linkname
    done

    if [ $plat != "aptiv_cvc_sousa" ]; then
        rm -f ${IMAGE_LINK_NAME}.wic
        rm -f ${IMAGE_NAME}.rootfs.wic
    fi
}

do_image_wic[postfuncs] += "make_nxp_s32g_wic"

BOOTFS_EXTRA_CMD ?= " \
    set -e; \
    for plat in ${UBOOT_CONFIG}; do \
        atf_s32="${DEPLOY_DIR_IMAGE}/atf-$plat.s32"; \
        if [ $plat = "s32g3xxaevb" ]; then \
            plat="evb3"; \
        elif [ $plat = "s32g2xxaevb" ]; then \
            plat="evb"; \
        elif [ $plat = "aptiv_cvc_sousa" ]; then \
            plat="aptiv"; \
        else \
            plat="$(echo $plat | grep -o '....$')"; \
        fi; \
        ofname="$plat.ustart.img"; \
        cp ustart.img $ofname; \
        if [ "$plat" = "s32g2xxaevb" ] && [ "${HSE_SEC_ENABLED}" = "1" ]; then \
            dd if=$atf_s32 of=$ofname bs=512 seek=1 skip=1 conv=notrunc,fsync; \
            dd if=$atf_s32.signature of=$ofname  bs=512 seek=9 conv=notrunc,fsync; \
        else \
            dd if=$atf_s32 of=$ofname conv=notrunc bs=256 count=1 seek=0; \
            dd if=$atf_s32 of=$ofname conv=notrunc bs=512 seek=1 skip=1; \
        fi \
    done; \
    rm -f ustart.img; \
    if [ $plat = "aptiv" ]; then \
        mv -f $ofname ustart.img; \
    fi; \
"
