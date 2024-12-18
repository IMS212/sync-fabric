package dev.kir.sync.api.networking;

import dev.kir.sync.Sync;
import dev.kir.sync.api.shell.Shell;
import dev.kir.sync.api.shell.ShellState;
import dev.kir.sync.api.shell.ShellStateUpdateType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.UUID;

public class ShellStateUpdatePacket implements CustomPayload {
    public static final PacketCodec<RegistryByteBuf, ShellStateUpdatePacket> CODEC = PacketCodec.of(ShellStateUpdatePacket::write, ShellStateUpdatePacket::new);
    public static final CustomPayload.Id<ShellStateUpdatePacket> ID = new CustomPayload.Id<>(Sync.locate("packet.shell.state.update"));
    private ShellStateUpdateType type;
    private ShellState shellState;
    private UUID uuid;
    private float progress;
    private DyeColor color;
    private BlockPos pos;

    public ShellStateUpdatePacket(ShellStateUpdateType type, ShellState shellState) {
        this.type = type;
        this.shellState = shellState;
    }

    public ShellStateUpdatePacket(RegistryByteBuf byteBuf) {
        read(byteBuf, byteBuf.getRegistryManager());
    }

    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void write(RegistryByteBuf buffer) {
        if (this.shellState == null && this.type != ShellStateUpdateType.NONE) {
            throw new IllegalStateException();
        }

        buffer.writeEnumConstant(type);
        switch (type) {
            case ADD:
                buffer.writeNbt(this.shellState.writeNbt(new NbtCompound(), buffer.getRegistryManager()));
                break;

            case REMOVE:
                buffer.writeUuid(this.shellState.getUuid());
                break;

            case UPDATE:
                buffer.writeUuid(this.shellState.getUuid());
                buffer.writeVarInt((int)(this.shellState.getProgress() * 100));
                buffer.writeVarInt(this.shellState.getColor() == null ? Byte.MAX_VALUE : this.shellState.getColor().getId());
                buffer.writeBlockPos(this.shellState.getPos());
                break;

            default:
                break;
        }
    }

    public void read(PacketByteBuf buffer, RegistryWrapper.WrapperLookup lookup) {
        this.type = buffer.readEnumConstant(ShellStateUpdateType.class);
        switch (this.type) {
            case ADD:
                this.shellState = ShellState.fromNbt((NbtCompound) buffer.readNbt(NbtSizeTracker.ofUnlimitedBytes()), lookup);
                break;

            case REMOVE:
                this.uuid = buffer.readUuid();
                break;

            case UPDATE:
                this.uuid = buffer.readUuid();
                this.progress = MathHelper.clamp(buffer.readVarInt() / 100F, 0F, 1F);
                int colorId = buffer.readVarInt();
                this.color = colorId < 0 || colorId > 15 ? null : DyeColor.byId(colorId);
                this.pos = buffer.readBlockPos();
                break;

            default:
                break;
        }
    }

    @Environment(EnvType.CLIENT)
    public void execute(ClientPlayerEntity player) {
        Shell shell = (Shell)player;
        if (shell == null) {
            return;
        }

        ShellState state;
        switch (this.type) {
            case ADD:
                shell.add(this.shellState);
                break;

            case REMOVE:
                state = shell.getShellStateByUuid(this.uuid);
                if (state != null) {
                    shell.remove(state);
                }
                break;

            case UPDATE:
                state = shell.getShellStateByUuid(this.uuid);
                if (state != null) {
                    state.setProgress(this.progress);
                    state.setColor(this.color);
                    state.setPos(this.pos);
                }
                break;

            default:
                break;
        }
    }
}