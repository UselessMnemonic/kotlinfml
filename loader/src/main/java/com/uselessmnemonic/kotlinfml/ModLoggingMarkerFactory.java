package com.uselessmnemonic.kotlinfml;

import net.minecraftforge.forgespi.language.IModInfo;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

class ModLoggingMarkerFactory {
    private ModLoggingMarkerFactory() {}

    public static Marker getDetachedMarker(Marker parent, String name) {
        final var marker = new MarkerManager.Log4jMarker(name);
        marker.addParents(parent);
        return marker;
    }

    public static Marker getDetachedMarker(Marker parent, IModInfo modInfo) {
        final var marker = new MarkerManager.Log4jMarker(modInfo.getModId());
        marker.addParents(parent);
        return marker;
    }
}
