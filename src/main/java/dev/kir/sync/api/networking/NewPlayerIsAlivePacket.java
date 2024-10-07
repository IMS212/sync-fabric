package dev.kir.sync.api.networking;

import dev.kir.sync.Sync;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Uuids;

import java.util.UUID;

public class NewPlayerIsAlivePacket implements CustomPayload {
    public static final PacketCodec<RegistryByteBuf, NewPlayerIsAlivePacket> CODEC = Uuids.PACKET_CODEC.xmap(NewPlayerIsAlivePacket::new, NewPlayerIsAlivePacket::getUuid).cast();
    public static final CustomPayload.Id<NewPlayerIsAlivePacket> ID = new CustomPayload.Id<>(Sync.locate("packet.shell.alive"));
    private UUID uuid;

    public NewPlayerIsAlivePacket(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public UUID getUuid() {
        return uuid;
    }

    @Environment(EnvType.CLIENT)
    public void apply(ClientPlayerEntity player) {
        PlayerEntity updatedPlayer = player.clientWorld.getPlayerByUuid(getUuid());
        if (updatedPlayer != null) {
            if (updatedPlayer.getHealth() <= 0) {
                updatedPlayer.setHealth(0.01F);
            }
            updatedPlayer.deathTime = 0;
        }
    }
}
