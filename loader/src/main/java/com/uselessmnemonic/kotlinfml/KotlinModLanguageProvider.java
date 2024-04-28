package com.uselessmnemonic.kotlinfml;

import net.minecraftforge.fml.Logging;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forgespi.language.ILifecycleEvent;
import net.minecraftforge.forgespi.language.IModLanguageProvider;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * The root of the Kotlin loading system.
 * Searches for mod declarations and prepares them to be loaded.
 */
public class KotlinModLanguageProvider implements IModLanguageProvider {

    static final Logger LOGGER = LogManager.getLogger("kotlinfml");

    @Override
    public String name() {
        return "kotlinfml";
    }

    private final Consumer<ModFileScanData> fileVisitor = (scanData) -> {
        final var loaders = new HashMap<String, IModLanguageLoader>();
        for (final var annotation : scanData.getAnnotations()) {
            if (!Objects.equals(annotation.annotationType().getDescriptor(), Mod.class.descriptorString())) {
                continue;
            }
            final var modId = (String) annotation.annotationData().get("value");
            final var modClassName = annotation.clazz().getClassName();
            final var marker = ModLoggingMarkerFactory.getDetachedMarker(Logging.SCAN, modId);
            LOGGER.debug(marker, "found mod class {}", modClassName);
            final var modLoader = new KotlinModLanguageLoader(modClassName);
            loaders.put(modId, modLoader);
        }
        scanData.addLanguageLoader(loaders);
    };

    @Override
    public Consumer<ModFileScanData> getFileVisitor() {
        return fileVisitor;
    }

    @Override
    public <R extends ILifecycleEvent<R>> void consumeLifecycleEvent(Supplier<R> consumeEvent) {}
}
