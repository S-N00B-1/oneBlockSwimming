package io.github.sn00b1.oneblockswimming.mixins;

import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
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

import java.time.Instant;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
    @Shadow
    public ServerPlayer player;

    @Unique
    long lastForwardPacketMillis = 0L;

    @Inject(method = "handlePlayerInput", at= @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V", shift = At.Shift.AFTER))
    public void setSprinting(ServerboundPlayerInputPacket packet, CallbackInfo ci) {
        // In water & Can Sprint
        if (this.player.isInWater() && (this.player.getFoodData().getFoodLevel() > 6.0F || this.player.getAbilities().mayfly)) {

            // This is so, so bad.
            boolean doubleTapped = false;
            long forwardPacketMillis = Instant.now().toEpochMilli();

            if (forwardPacketMillis - this.lastForwardPacketMillis > 500) { // Clear var to prepare for new forward input.
                this.lastForwardPacketMillis = 0L;
            }

            if (packet.input().forward() && this.lastForwardPacketMillis == 0L) { // Received new forward input.
                this.lastForwardPacketMillis = forwardPacketMillis;
            } else if (packet.input().forward() && forwardPacketMillis - this.lastForwardPacketMillis <= 500) { // Received another within 0.5s.
                doubleTapped = true;
            }


            if ((packet.input().sprint() || doubleTapped)) {
                this.player.setSwimming(true);
                this.player.setPose(Pose.SWIMMING);
                this.player.setSprinting(true);
            }

        }
    }

    @Inject(method = "handleMovePlayer", at= @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V", shift = At.Shift.AFTER))
    public void setSwimming(ServerboundMovePlayerPacket packet, CallbackInfo ci) {
        if (this.player.isInWater() && this.player.isSprinting()) {
            this.player.setSwimming(true);
            this.player.setPose(Pose.SWIMMING);
        }
    }
}
