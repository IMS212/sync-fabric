package dev.kir.sync.api.networking;

import dev.kir.sync.Sync;
import dev.kir.sync.api.shell.ClientShell;
import dev.kir.sync.api.shell.ShellState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public class SynchronizationResponsePacket implements CustomPayload {
    public static final CustomPayload.Id<SynchronizationResponsePacket> ID = new CustomPayload.Id<>(Sync.locate("packet.shell.synchronization.response"));
    public static final PacketCodec<RegistryByteBuf, SynchronizationResponsePacket> CODEC = PacketCodec.of(SynchronizationResponsePacket::write, SynchronizationResponsePacket::new);
    private Identifier startWorld;
    private BlockPos startPos;
    private Direction startFacing;
    private Identifier targetWorld;
    private BlockPos targetPos;
    private Direction targetFacing;
    private ShellState storedState;

    public SynchronizationResponsePacket(Identifier startWorld, BlockPos startPos, Direction startFacing, Identifier targetWorld, BlockPos targetPos, Direction targetFacing, @Nullable ShellState storedState) {
        this.startWorld = startWorld;
        this.startPos = startPos;
        this.startFacing = startFacing;
        this.targetWorld = targetWorld;
        this.targetPos = targetPos;
        this.targetFacing = targetFacing;
        this.storedState = storedState;
    }

    public SynchronizationResponsePacket(RegistryByteBuf byteBuf) {
        read(byteBuf);
    }

    @Override
    public Id<SynchronizationResponsePacket> getId() {
        return ID;
    }

    public void write(RegistryByteBuf buffer) {
        buffer.writeIdentifier(this.startWorld);
        buffer.writeBlockPos(this.startPos);
        buffer.writeVarInt(this.startFacing.getId());
        buffer.writeIdentifier(this.targetWorld);
        buffer.writeBlockPos(this.targetPos);
        buffer.writeVarInt(this.targetFacing.getId());
        if (this.storedState == null) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            buffer.writeNbt(this.storedState.writeNbt(new NbtCompound(), buffer.getRegistryManager()));
        }
    }

    public void read(RegistryByteBuf buffer) {
        this.startWorld = buffer.readIdentifier();
        this.startPos = buffer.readBlockPos();
        this.startFacing = Direction.byId(buffer.readVarInt());
        this.targetWorld = buffer.readIdentifier();
        this.targetPos = buffer.readBlockPos();
        this.targetFacing = Direction.byId(buffer.readVarInt());
        this.storedState = buffer.readBoolean() ? ShellState.fromNbt((NbtCompound) buffer.readNbt(NbtSizeTracker.ofUnlimitedBytes()), buffer.getRegistryManager()) : null;
    }

    public Identifier getTargetWorldId() {
        return this.targetWorld;
    }

    @Environment(EnvType.CLIENT)
    public void execute(ClientPlayerEntity player) {
        ((ClientShell)player).endSync(this.startWorld, this.startPos, this.startFacing, this.targetWorld, this.targetPos, this.targetFacing, this.storedState);
    }
}