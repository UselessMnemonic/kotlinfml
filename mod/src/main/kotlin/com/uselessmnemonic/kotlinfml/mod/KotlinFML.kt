package com.uselessmnemonic.kotlinfml.mod

import com.uselessmnemonic.kotlinfml.IKotlinMod
import net.minecraftforge.fml.common.Mod
import org.apache.logging.log4j.LogManager

@Mod("kotlinfml_mod")
object KotlinFML: IKotlinMod {
    private val LOGGER = LogManager.getLogger()
    init {
        LOGGER.info("initialized")
    }
}
