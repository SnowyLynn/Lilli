package com.amelynn.lilli

import com.amelynn.lilli.commands.ChatFeelingsCommands
import com.amelynn.lilli.commands.TpaCommands
import com.amelynn.lilli.util.TickStats
import com.amelynn.lilli.data.Config
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class Lilli : ModInitializer, DedicatedServerModInitializer {
    companion object{
        val LOGGER:Logger = LogManager.getLogger("lilli")
        val tickStats = TickStats()
    }
    @Override
    override fun onInitialize() {
        CommandRegistrationCallback.EVENT.register{ dispatcher, _ ->
            run {
                TpaCommands.instance.registerCommands(dispatcher)
                ChatFeelingsCommands.instance.registerCommands(dispatcher)
            }
        }
        Config.INSTANCE.load()
        LOGGER.info(
            """
-----------------------------------------------------
  ${"\u001B[35;1m"}  .---.    .-./`)   .---.     .---.    .-./`)
    | ,_|    \\ .-.')  | ,_|     | ,_|    \\ .-.')
  ,-./  )    / `-' \\,-./  )   ,-./  )    / `-' \\
  \\  '_ '`)   `-'`"`\\  '_ '`) \\  '_ '`)   `-'`"`
   > (_)  )   .---.  > (_)  )  > (_)  )   .---. 
  (  .  .-'   |   | (  .  .-' (  .  .-'   |   | 
   `-'`-'|___ |   |  `-'`-'|___`-'`-'|___ |   | 
    |        \\|   |   |        \\|        \\|   | 
    `--------`'---'   `--------``--------`'---' 
    ${"\u001B[34;1m"}   v0.1-beta-21w43        ${"\u001B[0m"}
-----------------------------------------------------"""
        )
    }

    override fun onInitializeServer() {
        TODO("Not yet implemented")
    }


}
