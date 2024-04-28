package com.uselessmnemonic.kotlinfml;

import net.minecraftforge.fml.Logging;
import net.minecraftforge.fml.ModLoadingException;
import net.minecraftforge.fml.ModLoadingStage;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.language.IModLanguageProvider;
import net.minecraftforge.forgespi.language.ModFileScanData;

import java.lang.reflect.InvocationTargetException;

import static com.uselessmnemonic.kotlinfml.KotlinModLanguageProvider.LOGGER;

public class KotlinModLanguageLoader implements IModLanguageProvider.IModLanguageLoader {
    private final String modClassName;

    KotlinModLanguageLoader(String modClassName) {
        this.modClassName = modClassName;
    }

    @Override
    public <T> T loadMod(IModInfo modInfo, ModFileScanData modScanData, ModuleLayer modModules) {
        final var marker = ModLoggingMarkerFactory.getDetachedMarker(Logging.LOADING, modInfo);
        LOGGER.debug(marker, "loading mod");
        try {
            final var clazz = Class.forName("com.uselessmnemonic.kotlinfml.KotlinModContainer", true, Thread.currentThread().getContextClassLoader());
            final var constructor = clazz.getDeclaredConstructor(IModInfo.class, String.class, ModFileScanData.class, ModuleLayer.class);
            @SuppressWarnings("unchecked") // required by method signature
            final var container = (T) constructor.newInstance(modInfo, modClassName, modScanData, modModules);
            return container;
        } catch (InvocationTargetException e) {
            final var cause = e.getCause();
            LOGGER.fatal(marker, "error loading mod", cause);
            if (cause instanceof ModLoadingException mle) {
                throw mle;
            }
            throw new ModLoadingException(modInfo, ModLoadingStage.CONSTRUCT, "fml.modloading.failedtoloadmodclass", cause);
        } catch (ReflectiveOperationException e) {
            LOGGER.fatal(marker, "class loading error", e);
            throw new ModLoadingException(modInfo, ModLoadingStage.CONSTRUCT, "fml.modloading.failedtoloadmodclass", e);
        }
    }
}
