package com.amelynn.lilli.commands

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class ChatFeelingsCommands {
    companion object{
        val instance = ChatFeelingsCommands()
    }
    private val feelingsMap = HashMap<String, FeelingsCommand>()
    fun registerCommands(dispatcher: CommandDispatcher<ServerCommandSource>){
        feelingsMap["pat"] = FeelingsCommand("Du pattest den Kopf von $2 sanft.", "$1 pattet sanft deinen Kopf.")
        feelingsMap["kuscheln"] = FeelingsCommand("Du nimmst $2 liebevoll in den Arm.", "$1 nimmt dich liebevoll in den Arm.")
        feelingsMap["schlagen"] = FeelingsCommand("Du hast $2 geschlagen. Aua!", "Du wurdest von $1 geschlagen. Aua!")
        feelingsMap.forEach {
            dispatcher.register(
                CommandManager.literal(it.key).then(CommandManager.argument("target", EntityArgumentType.players()).executes{ ctx ->
                    val target = EntityArgumentType.getPlayer(ctx, "target")
                    val sender = ctx.source.player
                    sender.sendMessage(it.value.getSenderString(sender, target), false)
                    target.sendMessage(it.value.getReceiverString(sender, target), false)
                    return@executes 1
                })
            )
        }
    }
    private class FeelingsCommand (senderText:String, receiverText:String){
        val senderText:String
        val receiverText:String
        init {
            this.senderText = senderText
            this.receiverText = receiverText
        }
        fun getSenderString (sender:ServerPlayerEntity, receiver:ServerPlayerEntity): Text {
            return LiteralText(senderText.replace("$1", sender.name.asString()).replace("$2", receiver.name.asString())).styled{ s -> s.withColor(Formatting.GOLD)}
        }
        fun getReceiverString (sender:ServerPlayerEntity, receiver:ServerPlayerEntity): Text {
            return LiteralText(receiverText.replace("$1", sender.name.asString()).replace("$2", receiver.name.asString())).styled{ s -> s.withColor(Formatting.GOLD)}
        }
    }
}

