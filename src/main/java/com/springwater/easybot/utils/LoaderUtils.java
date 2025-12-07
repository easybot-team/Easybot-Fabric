package com.springwater.easybot.utils;

import net.fabricmc.loader.api.FabricLoader;

public class LoaderUtils {
    /**
     * 判断当前是否是 quilt 环境
     * @return true: quilt, false: fabric
     */
    public static boolean isQuilt() {
        return FabricLoader.getInstance().isModLoaded("quilt_loader");
    }
}
