package dev.kir.sync.api.networking;

import dev.kir.sync.Sync;
import dev.kir.sync.api.shell.Shell;
import dev.kir.sync.api.shell.ShellState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.List;

public class ShellUpdatePacket implements CustomPayload {
    private Identifier worldId;
    private boolean isArtificial;
    private Collection<ShellState> states;
    public static final CustomPayload.Id<ShellUpdatePacket> ID = new CustomPayload.Id<>(Sync.locate("packet.shell.update"));
    public static final PacketCodec<RegistryByteBuf, ShellUpdatePacket> CODEC = PacketCodec.of(ShellUpdatePacket::write, ShellUpdatePacket::new);

    public ShellUpdatePacket(Identifier worldId, boolean isArtificial, Collection<ShellState> states) {
        this.worldId = worldId;
        this.isArtificial = isArtificial;
        this.states = states == null ? List.of() : states;
    }

    public ShellUpdatePacket(PacketByteBuf byteBuf) {
        read(byteBuf);
    }

    @Override
    public Id<ShellUpdatePacket> getId() {
        return ID;
    }

    public void write(PacketByteBuf buffer) {
        buffer.writeIdentifier(this.worldId);
        buffer.writeBoolean(this.isArtificial);
        buffer.writeVarInt(this.states.size());
        this.states.forEach(x -> buffer.writeNbt(x.writeNbt(new NbtCompound())));
    }

    public void read(PacketByteBuf buffer) {
        this.worldId = buffer.readIdentifier();
        this.isArtificial = buffer.readBoolean();
        this.states = buffer.readList(subBuffer -> ShellState.fromNbt((NbtCompound) subBuffer.readNbt(NbtSizeTracker.ofUnlimitedBytes())));
    }

    public Identifier getTargetWorldId() {
        return this.worldId;
    }

    @Environment(EnvType.CLIENT)
    public void execute(ClientPlayerEntity player) {
        Shell shell = (Shell)player;
        shell.changeArtificialStatus(this.isArtificial);
        shell.setAvailableShellStates(this.states.stream());
    }
}