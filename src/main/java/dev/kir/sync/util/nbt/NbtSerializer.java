package dev.kir.sync.util.nbt;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import org.apache.commons.lang3.function.TriConsumer;

public class NbtSerializer<T> {
    private final T target;
    private final Iterable<TriConsumer<T, NbtCompound, RegistryWrapper.WrapperLookup>> readers;
    private final Iterable<TriConsumer<T, NbtCompound, RegistryWrapper.WrapperLookup>> writers;

    public NbtSerializer(T target, Iterable<TriConsumer<T, NbtCompound, RegistryWrapper.WrapperLookup>> readers,
                         Iterable<TriConsumer<T, NbtCompound, RegistryWrapper.WrapperLookup>> writers) {
        this.target = target;
        this.readers = readers;
        this.writers = writers;
    }

    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup wrapperLookup) {
        for (TriConsumer<T, NbtCompound, RegistryWrapper.WrapperLookup> x : readers) {
            x.accept(this.target, nbt, wrapperLookup);
        }
    }

    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup wrapperLookup) {
        for (TriConsumer<T, NbtCompound, RegistryWrapper.WrapperLookup> x : writers) {
            x.accept(this.target, nbt, wrapperLookup);
        }
        return nbt;
    }
}
