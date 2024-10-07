package dev.kir.sync.api.networking;

import dev.kir.sync.Sync;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;

public class NewShellDestroyedPacket implements CustomPayload {
    public static final CustomPayload.Id<NewShellDestroyedPacket> ID = new CustomPayload.Id<>(Sync.locate("packet.shell.destroyed"));
    public static final PacketCodec<RegistryByteBuf, NewShellDestroyedPacket> CODEC = BlockPos.PACKET_CODEC.xmap(NewShellDestroyedPacket::new, NewShellDestroyedPacket::getBlockPos).cast();
    private BlockPos blockPos;

    public NewShellDestroyedPacket(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    @Environment(EnvType.CLIENT)
    public void apply(ClientPlayerEntity player) {
        for (int i = 0; i < 3; ++i) {
            player.clientWorld.addBlockBreakParticles(blockPos, Blocks.DEEPSLATE.getDefaultState());
            player.clientWorld.addBlockBreakParticles(blockPos.up(), Blocks.DEEPSLATE.getDefaultState());
        }
        player.clientWorld.playSound(player, blockPos, SoundEvents.BLOCK_DEEPSLATE_BREAK, SoundCategory.BLOCKS, 1F, player.getSoundPitch());
    }
}
