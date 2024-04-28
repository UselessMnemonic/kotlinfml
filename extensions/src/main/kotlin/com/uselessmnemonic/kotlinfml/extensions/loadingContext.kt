package com.uselessmnemonic.kotlinfml.extensions

import com.uselessmnemonic.kotlinfml.IKotlinMod
import com.uselessmnemonic.kotlinfml.KotlinModLoadingContext

/**
 * The loading context of the receiver.
 */
val <M: IKotlinMod> M.loadingContext get(): KotlinModLoadingContext<M> {
    val contextExtension = KotlinModLoadingContext.getContext().orElseThrow {
        throw IllegalCallerException("caller is not in a Kotlin mod")
    }
    if (contextExtension.mod !== this) {
        throw IllegalCallerException("caller in mod ${contextExtension::javaClass.name} cannot access context for ${this::javaClass.name}")
    }
    @Suppress("UNCHECKED_CAST") // guaranteed by above
    return contextExtension as KotlinModLoadingContext<M>
}
