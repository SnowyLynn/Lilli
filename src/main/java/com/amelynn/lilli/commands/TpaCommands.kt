package com.amelynn.lilli.commands

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.LiteralText
import net.minecraft.util.Formatting
import java.util.*

class TpaCommands {
    companion object {
        val instance = TpaCommands()
    }

    private val tpasOpen = HashMap<String, HashMap<String, Timer>>()
    fun registerCommands(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register(
            CommandManager.literal("tpa")
                .then(CommandManager.argument("target", EntityArgumentType.players()).executes { ctx ->
                    val targetPlayer = EntityArgumentType.getPlayer(ctx, "target")
                    val sourcePlayer = ctx.source.player
                    if (sourcePlayer.uuidAsString == targetPlayer.uuidAsString) {
                        sourcePlayer.sendMessage(LiteralText("Du bist schon bei dir selbst.").styled { s ->
                            s.withColor(
                                Formatting.RED
                            )
                        }, false); return@executes 0
                    }
                    if (tpasOpen[targetPlayer.uuidAsString] == null) tpasOpen[targetPlayer.uuidAsString] = HashMap()
                    if (tpasOpen[targetPlayer.uuidAsString]!!.keys.contains(sourcePlayer.uuidAsString)) {
                        sourcePlayer.sendMessage(LiteralText("Du hast bereits eine ausstehende Teleportanfrage bei ${targetPlayer.name}").styled { s ->
                            s.withColor(
                                Formatting.RED
                            )
                        }, false); return@executes 0
                    }
                    val sendMessageText =
                        LiteralText("${sourcePlayer.name.asString()} möchte sich zu dir Teleportieren.\n").styled { s ->
                            s.withColor(Formatting.GOLD)
                        }
                            .append(LiteralText("[Akzeptieren]").styled { s ->
                                s.withClickEvent(
                                    ClickEvent(
                                        ClickEvent.Action.RUN_COMMAND,
                                        "/tpaccept ${sourcePlayer.entityName}"
                                    )
                                )
                                    .withColor(Formatting.GREEN)
                                    .withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, LiteralText("Akzeptieren")))
                            })
                            .append(LiteralText("         "))
                            .append(LiteralText("[Ablehnen]").styled { s ->
                                s.withClickEvent(
                                    ClickEvent(
                                        ClickEvent.Action.RUN_COMMAND,
                                        "/tpdeny ${sourcePlayer.entityName}"
                                    )
                                )
                                    .withColor(Formatting.RED)
                                    .withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, LiteralText("Ablehnen")))
                            })
                    targetPlayer.sendMessage(sendMessageText, false)
                    sourcePlayer.sendMessage(LiteralText("Deine Teleportanfrage an ${targetPlayer.name.asString()} wurde verschickt!").styled { s ->
                        s.withColor(
                            Formatting.GREEN
                        )
                    }, false)
                    val cancelTimer = Timer()
                    cancelTimer.schedule(TpaCancelTask(sourcePlayer, targetPlayer), 30000)
                    tpasOpen[targetPlayer.uuidAsString]!![sourcePlayer.uuidAsString] = cancelTimer
                    return@executes 1
                })
        )
        dispatcher.register(
            CommandManager.literal("tpaccept")
                .then(CommandManager.argument("target", EntityArgumentType.players()).executes { ctx ->
                    val requestPlayer = EntityArgumentType.getPlayer(ctx, "target")
                    val targetPlayer = ctx.source.player
                    tpasOpen[targetPlayer.uuidAsString].let {
                        if (it != null) {
                            if (it.keys.contains(requestPlayer.uuidAsString)) {
                                requestPlayer.teleport(
                                    targetPlayer.getWorld(),
                                    targetPlayer.x,
                                    targetPlayer.y,
                                    targetPlayer.z,
                                    targetPlayer.yaw,
                                    targetPlayer.pitch
                                )
                                requestPlayer.sendMessage(LiteralText("Du wurdest zu ${targetPlayer.name.asString()} teleportiert!").styled { s ->
                                    s.withColor(
                                        Formatting.GREEN
                                    )
                                }, false)
                                targetPlayer.sendMessage(LiteralText("${requestPlayer.name.asString()} wurde zu dir teleportiert!").styled { s ->
                                    s.withColor(
                                        Formatting.GREEN
                                    )
                                }, false)
                                it[requestPlayer.uuidAsString]!!.cancel()
                                it.remove(requestPlayer.uuidAsString)
                                return@executes 1
                            }
                        }
                    }
                    targetPlayer.sendMessage(LiteralText("Diese Teleportanfrage ist abgelaufen!").styled { s ->
                        s.withColor(
                            Formatting.RED
                        )
                    }, false)
                    return@executes 1
                })
        )
        dispatcher.register(
            CommandManager.literal("tpdeny")
                .then(CommandManager.argument("target", EntityArgumentType.players()).executes { ctx ->
                    val requestPlayer = EntityArgumentType.getPlayer(ctx, "target")
                    val targetPlayer = ctx.source.player
                    tpasOpen[targetPlayer.uuidAsString].let {
                        if (it != null) {
                            if (it.keys.contains(requestPlayer.uuidAsString)) {
                                targetPlayer.sendMessage(LiteralText("Die Anfrage von ${requestPlayer.name.asString()} wurde abgelehnt!").styled { s ->
                                    s.withColor(
                                        Formatting.RED
                                    )
                                }, false)
                                requestPlayer.sendMessage(LiteralText("${targetPlayer.name.asString()} hat deine Teleportanfrage abgelehnt!").styled { s ->
                                    s.withColor(
                                        Formatting.RED
                                    )
                                }, false)
                                it[requestPlayer.uuidAsString]!!.cancel()
                                it.remove(requestPlayer.uuidAsString)
                                return@executes 1
                            }
                        }
                    }
                    targetPlayer.sendMessage(LiteralText("Diese Teleportanfrage ist abgelaufen!").styled { s ->
                        s.withColor(
                            Formatting.RED
                        )
                    }, false)
                    return@executes 1
                })
        )
    }

    class TpaCancelTask(from: ServerPlayerEntity, to: ServerPlayerEntity) : TimerTask() {
        private val from: ServerPlayerEntity
        private val to: ServerPlayerEntity

        init {
            this.from = from
            this.to = to
        }

        override fun run() {
            from.sendMessage(LiteralText("Deine Teleportanfrage an ${to.name.asString()} ist abgelaufen!").styled { s ->
                s.withColor(
                    Formatting.RED
                )
            }, false)
            to.sendMessage(LiteralText("Deine Teleportanfrage von ${from.name.asString()} ist abgelaufen!").styled { s ->
                s.withColor(
                    Formatting.RED
                )
            }, false)
            instance.tpasOpen[to.uuidAsString]!!.remove(from.uuidAsString)
        }
    }
}