package com.amelynn.lilli.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerListS2CPacket.Entry.class)
public abstract class PlayerListMixin {
    @Mutable
    @Shadow @Final private @Nullable Text displayName;

    @Inject(method = "<init>", at=@At("RETURN"))
    void playerListApplyRole(GameProfile gameProfile, int i, GameMode gameMode, Text text, CallbackInfo ci){
        this.displayName = Text.of(/*"[§4Owner§f] " + */gameProfile.getName());
    }

}
