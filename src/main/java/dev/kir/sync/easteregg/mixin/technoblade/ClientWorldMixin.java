package dev.kir.sync.easteregg.mixin.technoblade;

import dev.kir.sync.easteregg.technoblade.Technoblade;
import dev.kir.sync.easteregg.technoblade.TechnobladeTransformable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.EntityTrackingSoundInstance;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
@Mixin(ClientWorld.class)
abstract class ClientWorldMixin extends World {
    @Shadow
    private @Final MinecraftClient client;

    protected ClientWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, profiler, isClient, debugWorld, biomeAccess, maxChainedNeighborUpdates);
    }

    @Inject(method = "playSound(DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FFZJ)V", at = @At("HEAD"), cancellable = true)
    private void playSound(double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch, boolean useDistance, long seed, CallbackInfo ci) {
        if (category != SoundCategory.NEUTRAL) {
            return;
        }

        List<MobEntity> Technoblades = this.getEntitiesByClass(MobEntity.class, new Box(x - 0.1, y - 0.1, z - 0.1, x + 0.1, y + 0.1, z + 0.1), e -> e instanceof TechnobladeTransformable && ((TechnobladeTransformable)e).isTechnoblade());
        MobEntity entity = Technoblades.size() == 0 ? null : Technoblades.get(0);
        if (entity == null) {
            return;
        }

        Technoblade Technoblade = ((TechnobladeTransformable)entity).asTechnoblade();
        sound = this.getTechnobladeSound(sound);
        if (sound == null) {
            ci.cancel();
            return;
        }

        double distance = this.client.gameRenderer.getCamera().getPos().squaredDistanceTo(x, y, z);
        PositionedSoundInstance positionedSoundInstance = new PositionedSoundInstance(sound, Technoblade.getSoundCategory(), volume, pitch, Random.create(seed), x, y, z);
        if (useDistance && distance > 100) {
            this.client.getSoundManager().play(positionedSoundInstance, (int)(Math.sqrt(distance) * 0.5));
        } else {
            this.client.getSoundManager().play(positionedSoundInstance);
        }
        ci.cancel();
    }

    @Inject(method = "playSoundFromEntity(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/sound/SoundCategory;FFJ)V", at = @At("HEAD"), cancellable = true)
    private void playSoundFromEntity(PlayerEntity source, Entity entity, RegistryEntry<SoundEvent> sound, SoundCategory category, float volume, float pitch, long seed, CallbackInfo ci) {
        if (source != this.client.player || !(entity instanceof TechnobladeTransformable) || !((TechnobladeTransformable)entity).isTechnoblade()) {
            return;
        }

        Technoblade Technoblade = ((TechnobladeTransformable)entity).asTechnoblade();
        SoundEvent sound1 = this.getTechnobladeSound(sound.value());
        if (sound1 == null) {
            ci.cancel();
            return;
        }

        this.client.getSoundManager().play(new EntityTrackingSoundInstance(sound1, Technoblade.getSoundCategory(), volume, pitch, entity, seed));
        ci.cancel();
    }

    private @Nullable SoundEvent getTechnobladeSound(SoundEvent sound) {
        Identifier originalSoundId = sound.getId();
        if (originalSoundId.getPath().endsWith(".ambient") || originalSoundId.getPath().endsWith(".death")) {
            return null;
        }

        SoundEvent fixedSound = Registries.SOUND_EVENT.get(Identifier.of(originalSoundId.getNamespace(), originalSoundId.getPath().replaceFirst("\\.[^.]+", ".player")));
        if (fixedSound == null) {
            fixedSound = sound;
        }
        return fixedSound;
    }
}
