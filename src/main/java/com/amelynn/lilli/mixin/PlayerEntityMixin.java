package com.amelynn.lilli.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Shadow public abstract GameProfile getGameProfile();

    @Inject(method="getName", at=@At("HEAD"), cancellable = true)
    void applyRoleToName(CallbackInfoReturnable<LiteralText> cir){
        //TODO: Find a better Solution to apply Roles (+PlayerListMixin)
        cir.setReturnValue(new LiteralText(/*"[§4Owner§f] " + */this.getGameProfile().getName()));
    }

}