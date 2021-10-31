package com.amelynn.lilli.mixin;

import com.amelynn.lilli.Lilli;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.PlayerListHeaderS2CPacket;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(PlayerManager.class)
abstract class PlayerManagerMixin {
    @Shadow public abstract void sendToAll(Packet<?> packet);

    @Inject(at= @At("HEAD"), method = "updatePlayerLatency")
    public void updatePlayerLatency(CallbackInfo ci) {
        PlayerListHeaderS2CPacket packet = new PlayerListHeaderS2CPacket(Text.of(String.format("⌛ §5%.2f§f", Lilli.Companion.getTickStats().tps5Sec())), Text.of(""));
        sendToAll(packet);
    }

    @Inject(at=@At("TAIL"), method = "onPlayerConnect")
    public void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        player.sendMessage(new LiteralText(String.format("§6Hallo §5%s\n§6Willkommen auf §8Dunkle Welt§f!", player.getName().asString())), false);
    }
}