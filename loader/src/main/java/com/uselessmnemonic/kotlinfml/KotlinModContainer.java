package com.uselessmnemonic.kotlinfml;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.EventBusErrorMessage;
import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.IEventListener;
import net.minecraftforge.fml.Logging;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingException;
import net.minecraftforge.fml.ModLoadingStage;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.IModBusEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.moddiscovery.ModAnnotation;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.apache.logging.log4j.Marker;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.uselessmnemonic.kotlinfml.KotlinModLanguageProvider.LOGGER;

/**
 * A container and initializer for a mod instance. Lifecycle events begin here.
 * @param <M> The type of the mod that is initialized by this container.
 */
public class KotlinModContainer<M> extends ModContainer {

    private final Marker marker;
    private final ModFileScanData modScanData;
    private final Class<M> modClass;
    private final IEventBus eventBus;
    private M mod;

    @Override
    public M getMod() {
        return mod;
    }

    public IEventBus getEventBus() {
        return this.eventBus;
    }

    @Override
    public boolean matches(Object o) {
        if (o instanceof KotlinModContainer<?> other) {
            return matches(other);
        }
        return false;
    }

    public boolean matches(KotlinModContainer<?> other) {
        return other.mod == this.mod;
    }

    KotlinModContainer(IModInfo modInfo, String modClassName, ModFileScanData modScanData, ModuleLayer modModules) {
        super(modInfo);
        this.marker = ModLoggingMarkerFactory.getDetachedMarker(Logging.LOADING, modInfo);
        this.modScanData = modScanData;
        activityMap.put(ModLoadingStage.CONSTRUCT, this::constructMod);

        this.eventBus = BusBuilder.builder()
            .setExceptionHandler(this::onEventFailed)
            .setTrackPhases(false)
            .markerType(IModBusEvent.class)
            .useModLauncher()
            .build();

        final var contextExtension = new KotlinModLoadingContext<>(this);
        super.contextExtension = () -> contextExtension;

        final var moduleName = modInfo.getOwningFile().moduleName();
        final var parentModule = modModules.findModule(moduleName);
        if (parentModule.isEmpty()) {
            LOGGER.debug(marker, "unable to find module {}", moduleName);
            throw new ModLoadingException(modInfo, ModLoadingStage.CONSTRUCT, "fml.modloading.failedtoloadmod", null);
        }

        @SuppressWarnings("unchecked")
        final var modClass = (Class<M>) Class.forName(parentModule.get(), modClassName);
        this.modClass = modClass;
    }

    private void constructMod() {
        if (mod != null) {
            return;
        }

        // Mod may be a Kotlin object declaration
        final Optional<M> singleton;
        try {
            singleton = KotlinUtilities.getObjectInstance(modClass);
        } catch (ExceptionInInitializerError e) {
            LOGGER.debug(marker, "exception while retrieving mod singleton {}", modClass.getSimpleName());
            throw new ModLoadingException(modInfo, ModLoadingStage.CONSTRUCT, "fml.modloading.failedtoloadmod", e);
        }
        if (singleton.isPresent()) {
            LOGGER.debug(marker, "loading mod singleton {}", modClass.getSimpleName());
            mod = singleton.get();
            registerKotlinEventSubscribers();
            return;
        }

        // Mod may be the companion object of another class
        final Optional<KotlinUtilities.ParentCompanion<?, M>> parentCompanion;
        try {
            parentCompanion = KotlinUtilities.getCompanionParent(modClass);
        } catch (ExceptionInInitializerError e) {
            LOGGER.debug(marker, "exception while retrieving parent for companion class {}", modClass.getSimpleName());
            throw new ModLoadingException(modInfo, ModLoadingStage.CONSTRUCT, "fml.modloading.failedtoloadmod", e);
        }
        if (parentCompanion.isPresent()) {
            LOGGER.debug(marker, "loading mod {}::Companion", parentCompanion.get().parent().getSimpleName());
            mod = parentCompanion.get().companion();
            registerKotlinEventSubscribers();
            return;
        }

        // manually build the mod
        LOGGER.debug(marker, "constructing mod from {}", modClass.getSimpleName());

        final var constructors = modClass.getConstructors();
        if (constructors.length != 1) {
            LOGGER.fatal(marker, "too many or no public mod constructors");
            throw new ModLoadingException(modInfo, ModLoadingStage.CONSTRUCT, "fml.modloading.failedtoloadmod", null);
        }
        if (!Objects.equals(constructors[0].getDeclaringClass(), modClass)) {
            LOGGER.fatal(marker, "unexpected constructor");
            throw new ModLoadingException(modInfo, ModLoadingStage.CONSTRUCT, "fml.modloading.failedtoloadmod", null);
        }

        @SuppressWarnings("unchecked") // guaranteed by above
        final var constructor = (Constructor<M>) constructors[0];
        final var parameters = constructor.getParameterTypes();
        for (var i = 0; i < parameters.length; i++) {
            final var param = parameters[i];
            if (
                param != IEventBus.class &&
                param != ModContainer.class &&
                param != KotlinModContainer.class &&
                param != Dist.class
            ) {
                LOGGER.fatal(marker, "unsupported mod constructor parameter of type {} at index {}", param.getSimpleName(), i);
                throw new ModLoadingException(modInfo, ModLoadingStage.CONSTRUCT, "fml.modloading.failedtoloadmod", null);
            }
            for (var j = i + 1; j < parameters.length; j++) {
                final var otherType = parameters[j];
                if (param == otherType) {
                    LOGGER.fatal(marker, "duplicate mod constructor parameter of type {} at index {}", otherType.getSimpleName(), j);
                    throw new ModLoadingException(modInfo, ModLoadingStage.CONSTRUCT, "fml.modloading.failedtoloadmod", null);
                }
            }
        }

        final var arguments = new Object[parameters.length];
        for (int i = 0; i < arguments.length; i++) {
            final var parameter = parameters[i];
            if (parameter == IEventBus.class) {
                arguments[i] = this.getEventBus();
            } else if (parameter == ModContainer.class) {
                arguments[i] = this;
            } else if (parameter == KotlinModContainer.class) {
                arguments[i] = this;
            } else if (parameter == Dist.class) {
                arguments[i] = FMLEnvironment.dist;
            }
        }

        try {
            mod = constructor.newInstance(arguments);
        } catch (InvocationTargetException e) {
            final var cause = e.getCause();
            LOGGER.fatal(marker, "exception in mod constructor");
            if (cause instanceof ModLoadingException mle) {
                throw mle;
            }
            throw new ModLoadingException(modInfo, ModLoadingStage.CONSTRUCT, "fml.modloading.failedtoloadmod", cause);
        } catch (ExceptionInInitializerError e) {
            LOGGER.fatal(marker, "exception in static initializer for mod class");
            throw new ModLoadingException(modInfo, ModLoadingStage.CONSTRUCT, "fml.modloading.failedtoloadmod", e);
        } catch (ReflectiveOperationException e) {
            LOGGER.fatal(marker, "failed to invoke mod constructor");
            throw new ModLoadingException(modInfo, ModLoadingStage.CONSTRUCT, "fml.modloading.failedtoloadmod", e);
        }
    }

    private void registerKotlinEventSubscribers() {
        LOGGER.debug(marker, "registering event bus subscribers");

        for (final var annotation : modScanData.getAnnotations()) {
            if (!Objects.equals(annotation.annotationType().getDescriptor(), Mod.EventBusSubscriber.class.descriptorString())) {
                continue;
            }
            final var annotationData = annotation.annotationData();
            final var subscriberClassName = annotation.clazz().getClassName();

            final var modId = (String) annotationData.getOrDefault("modid", this.modId);
            if (!this.modId.equals(modId)) {
                continue;
            }

            // extract sided-ness for this subscriber
            final var sidesHolders = (List<?>) annotationData.getOrDefault("value", null);
            final var sides = EnumSet.noneOf(Dist.class);
            if (sidesHolders != null) {
                for (final var holder : sidesHolders) {
                    if (!(holder instanceof ModAnnotation.EnumHolder enumHolder)) {
                        LOGGER.fatal(marker, "unexpected value");
                        throw new ModLoadingException(modInfo, ModLoadingStage.CONSTRUCT, "fml.modloading.failedtoloadmod", null);
                    }
                    final var sideName = enumHolder.getValue();
                    try {
                        sides.add(Dist.valueOf(sideName));
                    } catch (IllegalArgumentException e) {
                        LOGGER.fatal(marker, "invalid side \"{}\" for event bus subscriber class {}", sideName, subscriberClassName);
                        throw new ModLoadingException(modInfo, ModLoadingStage.CONSTRUCT, "fml.modloading.failedtoloadmod", e);
                    }
                }
            } else {
                sides.add(Dist.CLIENT);
                sides.add(Dist.DEDICATED_SERVER);
            }
            if (!sides.contains(FMLEnvironment.dist)) {
                continue;
            }

            // determine which bus will handle this subscriber's events
            final var busTargetHolder = (ModAnnotation.EnumHolder) annotationData.getOrDefault("bus", null);
            final Mod.EventBusSubscriber.Bus busTarget;
            if (busTargetHolder != null) {
                final var busTargetName = busTargetHolder.getValue();
                try {
                    busTarget = Mod.EventBusSubscriber.Bus.valueOf(busTargetName);
                } catch (IllegalArgumentException e) {
                    LOGGER.fatal(marker, "invalid bus \"{}\" for event bus subscriber class {}", busTargetName, subscriberClassName);
                    throw new ModLoadingException(modInfo, ModLoadingStage.CONSTRUCT, "fml.modloading.failedtoloadmod", e);
                }
            } else {
                busTarget = Mod.EventBusSubscriber.Bus.FORGE;
            }

            final Class<?> subscriberClass;
            try {
                subscriberClass = Class.forName(subscriberClassName, true, modClass.getClassLoader());
            } catch (ClassNotFoundException e) {
                LOGGER.fatal(marker, "failed to load subscriber class {}", subscriberClassName);
                throw new ModLoadingException(modInfo, ModLoadingStage.CONSTRUCT, "fml.modloading.failedtoloadmod", e);
            }

            // subscriber may be an object declaration
            final Optional<?> singleton;
            try {
                singleton = KotlinUtilities.getObjectInstance(subscriberClass);
            } catch (ExceptionInInitializerError e) {
                LOGGER.debug(marker, "exception while retrieving subscriber singleton {}", subscriberClassName);
                throw new ModLoadingException(modInfo, ModLoadingStage.CONSTRUCT, "fml.modloading.failedtoloadmod", e);
            }
            if (singleton.isPresent()) {
                LOGGER.debug(marker, "registering subscriber singleton object {}", subscriberClassName);
                registerSubscriber(singleton.get(), busTarget);
                continue;
            }

            // subscriber may be the companion of another class
            final Optional<KotlinUtilities.ParentCompanion<?, M>> companion;
            try {
                companion = KotlinUtilities.getCompanionParent(modClass);
            } catch (ExceptionInInitializerError e) {
                LOGGER.debug(marker, "exception while retrieving subscriber companion {}", subscriberClassName);
                throw new ModLoadingException(modInfo, ModLoadingStage.CONSTRUCT, "fml.modloading.failedtoloadmod", e);
            }
            if (companion.isPresent()) {
                LOGGER.debug(marker, "registering subscriber {}.Companion", companion.get().parent().getName());
                registerSubscriber(companion.get().companion(), busTarget);
                continue;
            }

            // use the subscriber's class as the registrant
            try {
                LOGGER.debug(marker, "registering subscriber class {}", subscriberClassName);
                registerSubscriber(subscriberClass, busTarget);
            } catch (Throwable t) {
                LOGGER.fatal(marker, "failed to register subscriber class {}", subscriberClassName);
                throw new ModLoadingException(modInfo, ModLoadingStage.CONSTRUCT, "fml.modloading.failedtoloadmod", t);
            }
        }
    }

    private void registerSubscriber(Object subscriber, Mod.EventBusSubscriber.Bus bus) {
        switch (bus) {
            case FORGE:
                bus.bus().get().register(subscriber);
                break;
            case MOD:
                this.getEventBus().register(subscriber);
        }
    }

    private void onEventFailed(IEventBus iEventBus, Event event, IEventListener[] iEventListeners, int i, Throwable throwable) {
        LOGGER.error(new EventBusErrorMessage(event, i, iEventListeners, throwable));
    }
}
