package com.uselessmnemonic.kotlinfml;

import java.util.Optional;

class KotlinUtilities {
    private KotlinUtilities() {}

    /**
     * Retrieves the object value from its class type.
     * A Kotlin object value is a type of singleton. A Kotlin object O
     * is available from Java as the static field INSTANCE in class O.
     *
     * @return Optionally, the object value;
     * empty if no INSTANCE field is publicly visible in O.
     */
    public static <O> Optional<O> getObjectInstance(Class<O> objectClass) throws ExceptionInInitializerError {
        try {
            final var instanceField = objectClass.getDeclaredField("INSTANCE");
            final var instance = objectClass.cast(instanceField.get(null));
            return Optional.of(instance);
        } catch (IllegalAccessException | NoSuchFieldException | ClassCastException e) {
            return Optional.empty();
        }
    }

    /**
     * Searches for the companion object of class C.
     * Companion classes are uniquely instantiated in their parent classes.
     * The parent class for C must be located as it contains the companion object.
     *
     * @return Optionally, a pair including the companion object and its parent's class;
     * empty if the class does not describe a publicly accessible companion object.
     */
    public static <C> Optional<ParentCompanion<?, C>> getCompanionParent(Class<C> companionClass) throws ExceptionInInitializerError {
        if (companionClass.isPrimitive()) {
            return Optional.empty();
        }

        final var companionName = companionClass.getName();
        final var indexOfSeparator = companionName.lastIndexOf('$');
        if (indexOfSeparator < 1) {
            return Optional.empty();
        }

        final var parentName = companionName.substring(0, indexOfSeparator);
        final Class<?> parentClass;
        try {
            parentClass = companionClass.getClassLoader().loadClass(parentName);
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }

        try {
            final var companionField = parentClass.getField("Companion");
            final var companion = companionClass.cast(companionField.get(null));
            return Optional.of(new ParentCompanion<>(parentClass, companion));
        } catch (IllegalAccessException | NoSuchFieldException | ClassCastException e) {
            return Optional.empty();
        }
    }

    record ParentCompanion<P, C>(Class<P> parent, C companion) {}
}
