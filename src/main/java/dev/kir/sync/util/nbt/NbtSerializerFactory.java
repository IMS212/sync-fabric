package dev.kir.sync.util.nbt;

import com.google.common.collect.ImmutableList;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import org.apache.commons.lang3.function.TriConsumer;

public class NbtSerializerFactory<T> {
    private final Iterable<TriConsumer<T, NbtCompound, RegistryWrapper.WrapperLookup>> readers;
    private final Iterable<TriConsumer<T, NbtCompound, RegistryWrapper.WrapperLookup>> writers;

    public NbtSerializerFactory(Iterable<TriConsumer<T, NbtCompound, RegistryWrapper.WrapperLookup>> readers, Iterable<TriConsumer<T, NbtCompound, RegistryWrapper.WrapperLookup>> writers) {
        this.readers = ImmutableList.copyOf(readers);
        this.writers = ImmutableList.copyOf(writers);
    }

    public NbtSerializer<T> create(T target) {
        return new NbtSerializer<>(target, this.readers, this.writers);
    }
}
