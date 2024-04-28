package com.uselessmnemonic.kotlinfml;

/**
 * A marker interface for Kotlin-based mods.
 * It's not required to implement this interface, but certain utilities will become unavailable without it.
 * <br>
 * Kotlin mods are still Java mods, and must be annotated with {@link net.minecraftforge.fml.common.Mod}
 * even when this interface is implemented.
 */
public interface IKotlinMod {}
