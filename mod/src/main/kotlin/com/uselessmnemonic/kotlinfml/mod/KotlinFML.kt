package com.uselessmnemonic.kotlinfml.mod

import com.uselessmnemonic.kotlinfml.IKotlinMod
import net.minecraftforge.fml.common.Mod
import org.apache.logging.log4j.LogManager

@Mod("kotlinfmlmod")
object KotlinFML: IKotlinMod {
    private val LOGGER = LogManager.getLogger()
    init {
        LOGGER.info("initialized")
    }
}
