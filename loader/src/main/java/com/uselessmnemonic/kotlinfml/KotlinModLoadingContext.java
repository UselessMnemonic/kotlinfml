package com.uselessmnemonic.kotlinfml;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;

import java.util.Optional;

/**
 * A Kotlin-mod-specific context class.
 * @param <M> The mod type for which this context was created.
 * <br>
 * Kotlin mods are Java mods that follow Kotlin conventions. This class should be used for new Kotlin code,
 * but {@link net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext} will continue to work for old code.
 */
public class KotlinModLoadingContext<M> {

    private final KotlinModContainer<M> container;

    KotlinModLoadingContext(KotlinModContainer<M> container) {
        super();
        this.container = container;
    }

    public M getMod() {
        return container.getMod();
    }

    public IEventBus getModEventBus()
    {
        return container.getEventBus();
    }

    public static Optional<KotlinModLoadingContext<?>> getContext() {
        final var contextExtension = ModLoadingContext.get().extension();
        if (!(contextExtension instanceof KotlinModLoadingContext<?> kContextExtension)) {
            return Optional.empty();
        }
        return Optional.of(kContextExtension);
    }
}
