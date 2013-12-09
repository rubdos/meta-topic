DESCRIPTION = "Xilinx Zynq kernel with ADI extensions"
SECTION = "kernel"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=d7810fab7487fb0aad327b76f1be7cd7"
S = "${WORKDIR}/git"

KBRANCH = "xcomm_zynq"

inherit kernel
require recipes-kernel/linux/linux-dtb.inc

# Using LZO compression in the kernel requires "lzop"
DEPENDS += "lzop-native"

FILESEXTRAPATHS_prepend := "${THISDIR}/linux-milo:"

# If you have a local repository, you can set this variable to point to
# another kernel repo. Or to another kernel entirely.
KERNEL_GIT_REPO ?= "git://github.com/milosoftware/linux.git;protocol=git"

SRC_URI = "\
	${KERNEL_GIT_REPO};branch=${KBRANCH} \
	file://defconfig \
	"

KERNEL_IMAGETYPE = "uImage"
KERNEL_DEVICETREE = "${WORKDIR}/devicetree.dts"
KERNEL_DEVICETREE_zynq-zc702 = "arch/arm/boot/dts/${MACHINE}-adv7511.dts"
KERNEL_DEVICETREE_zedboard = "arch/arm/boot/dts/zynq-zed-adv7511.dts"
# See http://permalink.gmane.org/gmane.linux.kernel.commits.head/371588
KERNEL_EXTRA_ARGS += "LOADADDR=0x00008000"
KERNEL_IMAGEDEST = "tmp/boot"

FILES_kernel-image = "${KERNEL_IMAGEDEST}/${KERNEL_IMAGETYPE}*"

LINUX_VERSION ?= "3.10"
LINUX_VERSION_EXTENSION ?= "-milo"

SRCREV = "dbebd8b890aaf18cd53433eb84ef917b0d70b275"

# Zedboard has a defconfig for 3.12, the zc702 not (yet) so use the 3.12
# kernel only for the zedboard for now.
LINUX_VERSION_zedboard = "3.12"
SRCREV_zedboard = "4424de9244e2fa2f4873a96c8cf633d53d56ce74"

PR = "r0"
PV = "${LINUX_VERSION}+git${SRCPV}"

COMPATIBLE_MACHINE = "(zynq-zc702|zedboard)"

pkg_postinst_kernel-image () {
	if [ "x$D" == "x" ]; then
		if [ -f /${KERNEL_IMAGEDEST}/${KERNEL_IMAGETYPE}-${KERNEL_VERSION} ] ; then
			if grep -q "ubi0:qspi-rootfs" /proc/mounts
			then
				flashcp -v /${KERNEL_IMAGEDEST}/${KERNEL_IMAGETYPE}-${KERNEL_VERSION} /dev/mtd3
			else
				if [ -f /media/mmcblk0p1/${KERNEL_IMAGETYPE} ]; then
					cp /${KERNEL_IMAGEDEST}/${KERNEL_IMAGETYPE}-${KERNEL_VERSION} /media/mmcblk0p1/${KERNEL_IMAGETYPE}
				fi
			fi
			rm -f /${KERNEL_IMAGEDEST}/${KERNEL_IMAGETYPE}-${KERNEL_VERSION}
		fi
	fi
	true
}
