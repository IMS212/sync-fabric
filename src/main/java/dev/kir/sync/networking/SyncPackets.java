package dev.kir.sync.networking;

import dev.kir.sync.api.networking.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class SyncPackets {
    public static void init() {
        PayloadTypeRegistry.playC2S().register(SynchronizationRequestPacket.ID, SynchronizationRequestPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(SynchronizationRequestPacket.ID, (payload, context) -> payload.execute(context.player(), context.responseSender()));
    }

    @Environment(EnvType.CLIENT)
    public static void initClient() {
        PayloadTypeRegistry.playS2C().register(ShellUpdatePacket.ID, ShellUpdatePacket.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(ShellUpdatePacket.ID, (payload, context) -> payload.execute(context.player()));

        ClientPlayerPacket.register(ShellUpdatePacket.class);
        ClientPlayerPacket.register(ShellStateUpdatePacket.class);
        ClientPlayerPacket.register(SynchronizationResponsePacket.class);
        ClientPlayerPacket.register(PlayerIsAlivePacket.class);
        ClientPlayerPacket.register(ShellDestroyedPacket.class);
    }
}
