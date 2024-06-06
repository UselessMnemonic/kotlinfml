# kotlinfml
The Kotlin (Neo)Forge Mod Loader is a bring-your-own Kotlin mod language provider designed for users who want as little
headache as possible when authoring and loading mods written in idiomatic Kotlin.

This project is organized into 3 components:
- *`kotlinfml-loader`*: The Kotlin Forge Mod Loader
- *`kotlinfml-mod`*: The mod component of the loader, which represents the loader within the mod list
- *`kotlinfml-extensions`*: A library loaded with utilities to promote idiomatic Kotlin in mod development

### BYO Kotlin
Bring-your-own Kotlin simply means that client users and server operators can update their own Kotlin distributions
without having to wait for mods to update. `kotlinfml-loader` is written in pure Java, meaning it doesn't depend on any
particular version of Kotlin.

Mod authors have the benefit of freely deciding a minimum version Kotlin target for their projects without forcing
users to update the mod loader. Kotlin's strong backwards-compatibility ensures that authors and users alike can upgrade
to more recent versions of Kotlin without breaking older mods.

Mod authors are discouraged from shading Kotlin or its dependencies. Updating to newer versions of Kotlin should be the
priority.

### Caveats (or, The Ugly)
Although `kotlinfml-loader` doesn't depend on any version of Kotlin, `kotlinfml-mod` and `kotlinfml-extensions` are
built with [Kotlin 1.9.20](https://kotlinlang.org/docs/releases.html#release-details) to enforce a version floor. This
is not ideal in a BYO setting, but it encourages mod authors and consumers alike to make upgrading a priority.

`kotlinfml` is meant to be used with NeoForge. Please see the releases section for more information about NeoForge,
Minecraft, and Java requirements.

## Usage

#### IKotlinMod interface
Use `IKotlinMod` in your mod's interface list to mark it as a Kotlin mod:
```kotlin
@Mod("my_mod")
object MyMod: IKotlinMod
```
Implementing `IKotlinMod` is necessary for using type safe-extensions, DSLs, and utilities. However, it can be safely
ignored.
### 
Kotlin mods can be written with the same conventions as Java mods, with additional support for Kotlin object
declarations and class companions. A typical Java mod may take the following form:

```java
@Mod("my_mod")
class MyMod {
    private static final Logger LOGGER = LogManager.getLogger();
    public MyMod() {
        LOGGER.info("initialized");
    }
}
```

Mods are generally singletons, so naturally the same mod is safer and better typed when written in Kotlin:
```kotlin
@Mod("my_mod")
object MyMod {
    private val LOGGER = LogManager.getLogger()
    init {
        LOGGER.info("initialized")
    }
}
```

Companion objects can also take the role of mod if so desired:
```kotlin
class Container {
    @Mod("my_mod")
    companion object {
        private val LOGGER = LogManager.getLogger()
        init {
            LOGGER.info("initialized")
        }
    }
}
```

### File-scoped Subscribers
Entire files can be annotated with `@Mod.EventBusSubscriber` for a functional approach:

```kotlin
@file:Mod.EventBusSubscriber
@file:JvmName("Subscribers")

@SubscribeEvent
fun someEvent(event: SomeEvent) { /* ... */ }

@SubscribeEvent
fun anotherEvent(event: AnotherEvent) { /* ... */ }
```

### Kotlin DSL
Useful extensions are shipped in `kotlinfml-extensions`, offered separately from the loader. Authors should shade this
library into their projects and not expect for it to be present in the classpath.
