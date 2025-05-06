package io.github.sn00b1.oneblockswimming.mixins;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Pose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
    @Shadow
    public ServerPlayer player;

    @Inject(method = "handlePlayerInput", at= @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V", shift = At.Shift.AFTER))
    public void setSwimming(ServerboundPlayerInputPacket packet, CallbackInfo ci) {
        if (this.player.isInWater() && (packet.input().sprint() || this.player.isSprinting()) && this.canSprint()) {
            this.player.setSwimming(true);
            this.player.setPose(Pose.SWIMMING);
            this.player.setSprinting(true);
        }
    }

    @Inject(method = "handleMovePlayer", at= @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V", shift = At.Shift.AFTER))
    public void setSwimming(ServerboundMovePlayerPacket packet, CallbackInfo ci) {
        if (this.player.isInWater() && this.player.isSprinting() && this.canSprint()) {
            this.player.setSwimming(true);
            this.player.setPose(Pose.SWIMMING);
        }
    }

    @Unique
    private boolean canSprint() {
        return (this.player.getFoodData().getFoodLevel() > 6.0F || this.player.getAbilities().mayfly);
    }
}
