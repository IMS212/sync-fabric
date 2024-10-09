package dev.kir.sync.compat.origins;

import dev.kir.sync.api.shell.ShellStateComponent;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.networking.packet.s2c.OpenChooseOriginScreenS2CPacket;
import io.github.apace100.origins.registry.ModComponents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;

class OriginsShellStateComponent extends ShellStateComponent {
    private final ServerPlayerEntity player;
    private boolean activated;
    private NbtCompound originComponentNbt;
    private NbtCompound powerHolderComponentNbt;

    public OriginsShellStateComponent() {
        this(null, false);
    }

    public OriginsShellStateComponent(ServerPlayerEntity player) {
        this(player, true);
    }

    private OriginsShellStateComponent(ServerPlayerEntity player, boolean activated) {
        this.player = player;
        this.activated = activated;
    }

    @Override
    public String getId() {
        return "origins";
    }

    public boolean isActivated() {
        return this.activated;
    }

    public NbtCompound getOriginComponentNbt(RegistryWrapper.WrapperLookup lookup) {
        NbtCompound nbt = this.originComponentNbt;
        if (this.player != null) {
            nbt = new NbtCompound();
            ModComponents.ORIGIN.get(this.player).writeToNbt(nbt, lookup);
        }
        return nbt == null ? new NbtCompound() : nbt;
    }

    public NbtCompound getPowerHolderComponentNbt(RegistryWrapper.WrapperLookup lookup) {
        NbtCompound nbt = this.powerHolderComponentNbt;
        if (this.player != null) {
            nbt = new NbtCompound();
            PowerHolderComponent.KEY.get(this.player).writeToNbt(nbt, lookup);
        }
        return nbt == null ? new NbtCompound() : nbt;
    }

    @Override
    public void clone(ShellStateComponent component, RegistryWrapper.WrapperLookup lookup) {
        OriginsShellStateComponent other = component.as(OriginsShellStateComponent.class);
        if (other == null) {
            return;
        }

        this.originComponentNbt = other.getOriginComponentNbt(lookup);
        this.powerHolderComponentNbt = other.getPowerHolderComponentNbt(lookup);
        this.activated = other.isActivated();
        if (this.player == null) {
            return;
        }

        OriginComponent originComponent = ModComponents.ORIGIN.get(this.player);
        if (this.activated) {
            originComponent.readFromNbt(this.originComponentNbt, lookup);
            PowerHolderComponent powerHolderComponent = PowerHolderComponent.KEY.get(this.player);
            powerHolderComponent.readFromNbt(this.powerHolderComponentNbt, lookup);
            originComponent.sync();
        } else {
            ServerPlayNetworking.send(this.player, new OpenChooseOriginScreenS2CPacket(false));
            this.activated = true;
        }
    }

    @Override
    protected void readComponentNbt(NbtCompound nbt) {
        this.originComponentNbt = nbt.contains("origins", NbtElement.COMPOUND_TYPE) ? nbt.getCompound("origins") : new NbtCompound();
        this.powerHolderComponentNbt = nbt.contains("powers", NbtElement.COMPOUND_TYPE) ? nbt.getCompound("powers") : new NbtCompound();
        this.activated = nbt.getBoolean("activated");
    }

    @Override
    protected NbtCompound writeComponentNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        nbt.put("origins", this.getOriginComponentNbt(lookup));
        nbt.put("powers", this.getPowerHolderComponentNbt(lookup));
        nbt.putBoolean("activated", this.isActivated());
        return nbt;
    }
}
