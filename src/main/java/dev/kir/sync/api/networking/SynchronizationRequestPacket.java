package dev.kir.sync.api.networking;

import dev.kir.sync.Sync;
import dev.kir.sync.api.shell.ServerShell;
import dev.kir.sync.api.shell.ShellState;
import dev.kir.sync.util.BlockPosUtil;
import dev.kir.sync.util.WorldUtil;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Objects;
import java.util.UUID;

public class SynchronizationRequestPacket implements CustomPayload {
    public static final CustomPayload.Id<SynchronizationRequestPacket> ID = new Id<>(Sync.locate("packet.shell.synchronization.request"));
    public static final PacketCodec<PacketByteBuf, SynchronizationRequestPacket> CODEC = Uuids.PACKET_CODEC.xmap(SynchronizationRequestPacket::new, SynchronizationRequestPacket::getShellUuid).cast();
    private UUID shellUuid;

    public SynchronizationRequestPacket(ShellState shell) {
        this.shellUuid = shell == null ? null : shell.getUuid();
    }

    public SynchronizationRequestPacket(UUID shellUuid) {
        this.shellUuid = shellUuid;
    }

    public void execute(ServerPlayerEntity player, PacketSender responseSender) {
        ServerShell shell = (ServerShell) player;
        ShellState state = shell.getShellStateByUuid(this.shellUuid);

        BlockPos currentPos = player.getBlockPos();
        World currentWorld = player.getWorld();
        Identifier currentWorldId = WorldUtil.getId(currentWorld);
        Direction currentFacing = BlockPosUtil.getHorizontalFacing(currentPos, currentWorld).orElse(player.getHorizontalFacing().getOpposite());

        shell.sync(state, currentWorld.getRegistryManager()).ifLeft(storedState -> {
            Objects.requireNonNull(state);
            Identifier targetWorldId = state.getWorld();
            BlockPos targetPos = state.getPos();
            Direction targetFacing = player.getHorizontalFacing().getOpposite();
            new SynchronizationResponsePacket(currentWorldId, currentPos, currentFacing, targetWorldId, targetPos, targetFacing, storedState).send(responseSender);
        }).ifRight(failureReason -> {
            player.sendMessage(failureReason.toText(), false);
            new SynchronizationResponsePacket(currentWorldId, currentPos, currentFacing, currentWorldId, currentPos, currentFacing, null).send(responseSender);
        });
    }

    @Override
    public Id<SynchronizationRequestPacket> getId() {
        return ID;
    }

    private UUID getShellUuid() {
        return shellUuid;
    }

    public void read(PacketByteBuf buffer) {
        this.shellUuid = buffer.readBoolean() ? buffer.readUuid() : null;
    }

    public void write(PacketByteBuf buffer) {
        if (this.shellUuid == null) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            buffer.writeUuid(this.shellUuid);
        }
    }
}