package dev.kir.sync.networking;

import dev.kir.sync.api.networking.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class SyncPackets {
    public static void init() {
        PayloadTypeRegistry.playS2C().register(ShellUpdatePacket.ID, ShellUpdatePacket.CODEC);
        PayloadTypeRegistry.playS2C().register(ShellStateUpdatePacket.ID, ShellStateUpdatePacket.CODEC);
        PayloadTypeRegistry.playS2C().register(SynchronizationResponsePacket.ID, SynchronizationResponsePacket.CODEC);
        PayloadTypeRegistry.playS2C().register(PlayerIsAlivePacket.ID, PlayerIsAlivePacket.CODEC);
        PayloadTypeRegistry.playS2C().register(ShellDestroyedPacket.ID, ShellDestroyedPacket.CODEC);

        PayloadTypeRegistry.playC2S().register(SynchronizationRequestPacket.ID, SynchronizationRequestPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(SynchronizationRequestPacket.ID, (payload, context) -> payload.execute(context.player(), context.responseSender()));
    }

    @Environment(EnvType.CLIENT)
    public static void initClient() {
        ClientPlayNetworking.registerGlobalReceiver(ShellUpdatePacket.ID, (payload, context) -> payload.execute(context.player()));
        ClientPlayNetworking.registerGlobalReceiver(ShellStateUpdatePacket.ID, (payload, context) -> payload.execute(context.player()));
        ClientPlayNetworking.registerGlobalReceiver(SynchronizationResponsePacket.ID, (payload, context) -> payload.execute(context.player()));
        ClientPlayNetworking.registerGlobalReceiver(PlayerIsAlivePacket.ID, ((payload, context) -> payload.apply(context.player())));
        ClientPlayNetworking.registerGlobalReceiver(ShellDestroyedPacket.ID, ((payload, context) -> payload.apply(context.player())));
    }
}
