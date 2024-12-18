package dev.kir.sync.api.shell;

import dev.kir.sync.entity.ShellEntity;
import dev.kir.sync.item.SimpleInventory;
import dev.kir.sync.util.WorldUtil;
import dev.kir.sync.util.math.Radians;
import dev.kir.sync.util.nbt.NbtSerializer;
import dev.kir.sync.util.nbt.NbtSerializerFactory;
import dev.kir.sync.util.nbt.NbtSerializerFactoryBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * A state that can be applied to a shell.
 */
public class ShellState {
    public static final float PROGRESS_START = 0F;
    public static final float PROGRESS_DONE = 1F;
    public static final float PROGRESS_PRINTING = 0.75F;
    public static final float PROGRESS_PAINTING = PROGRESS_DONE - PROGRESS_PRINTING;

    private static final NbtSerializerFactory<ShellState> NBT_SERIALIZER_FACTORY;

    private UUID uuid;
    private float progress;
    private DyeColor color;
    private boolean isArtificial;

    private UUID ownerUuid;
    private String ownerName;
    private float health;
    private int gameMode;
    private SimpleInventory inventory;
    private ShellStateComponent component;

    private int foodLevel;
    private float saturationLevel;
    private float exhaustion;

    private int experienceLevel;
    private float experienceProgress;
    private int totalExperience;

    private Identifier world;
    private BlockPos pos;

    private final NbtSerializer<ShellState> serializer;

    // <========================== Java Is Shit ==========================> //
    public UUID getUuid() {
        return this.uuid;
    }

    public DyeColor getColor() {
        return this.color;
    }

    public void setColor(DyeColor color) {
        this.color = color;
    }

    public float getProgress() {
        return this.progress;
    }

    public void setProgress(float progress) {
        this.progress = MathHelper.clamp(progress, 0F, 1F);
    }

    public boolean isArtificial() {
        return this.isArtificial;
    }

    public UUID getOwnerUuid() {
        return this.ownerUuid;
    }

    public String getOwnerName() {
        return this.ownerName;
    }

    public float getHealth() {
        return this.health;
    }

    public int getGameMode() {
        return this.gameMode;
    }

    public SimpleInventory getInventory() {
        return this.inventory;
    }

    public ShellStateComponent getComponent() {
        return this.component;
    }

    public int getFoodLevel() {
        return this.foodLevel;
    }

    public float getSaturationLevel() {
        return this.saturationLevel;
    }

    public float getExhaustion() {
        return this.exhaustion;
    }

    public int getExperienceLevel() {
        return this.experienceLevel;
    }

    public float getExperienceProgress() {
        return this.experienceProgress;
    }

    public int getTotalExperience() {
        return this.totalExperience;
    }

    public Identifier getWorld() {
        return this.world;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public void setPos(BlockPos pos) {
        this.pos = pos;
    }
    // <========================== Java Is Shit ==========================> //

    private ShellState() {
        this.serializer = NBT_SERIALIZER_FACTORY.create(this);
    }

    /**
     * Creates empty shell of the specified player.
     *
     * @param player The player.
     * @param pos Position of the shell.
     * @return Empty shell of the specified player.
     */
    public static ShellState empty(ServerPlayerEntity player, BlockPos pos, RegistryWrapper.WrapperLookup lookup) {
        return empty(player, pos, null, lookup);
    }

    /**
     * Creates empty shell of the specified player.
     *
     * @param player The player.
     * @param pos Position of the shell.
     * @param color Color of the shell.
     * @return Empty shell of the specified player.
     */
    public static ShellState empty(ServerPlayerEntity player, BlockPos pos, DyeColor color, RegistryWrapper.WrapperLookup lookup) {
        return create(player, pos, color, 0, true, false, lookup);
    }

    /**
     * Creates shell that is a full copy of the specified player.
     *
     * @param player The player.
     * @param pos Position of the shell.
     * @return Shell that is a full copy of the specified player.
     */
    public static ShellState of(ServerPlayerEntity player, BlockPos pos, RegistryWrapper.WrapperLookup lookup) {
        return of(player, pos, null, lookup);
    }

    /**
     * Creates shell that is a full copy of the specified player.
     *
     * @param player The player.
     * @param pos Position of the shell.
     * @param color Color of the shell.
     * @return Shell that is a full copy of the specified player.
     */
    public static ShellState of(ServerPlayerEntity player, BlockPos pos, DyeColor color, RegistryWrapper.WrapperLookup lookup) {
        return create(player, pos, color, 1, ((Shell)player).isArtificial(), true, lookup);
    }

    /**
     * Creates shell from the nbt data.
     * @param nbt The nbt data.
     * @return Shell created from the nbt data.
     */
    public static ShellState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        ShellState state = new ShellState();
        state.readNbt(nbt, lookup);
        return state;
    }

    private static ShellState create(ServerPlayerEntity player, BlockPos pos, DyeColor color, float progress, boolean isArtificial, boolean copyPlayerState, RegistryWrapper.WrapperLookup lookup) {
        ShellState shell = new ShellState();

        shell.uuid = UUID.randomUUID();
        shell.progress = progress;
        shell.color = color;
        shell.isArtificial = isArtificial;

        shell.ownerUuid = player.getUuid();
        shell.ownerName = player.getName().getString();
        shell.gameMode = player.interactionManager.getGameMode().getId();
        shell.inventory = new SimpleInventory();
        shell.component = ShellStateComponent.empty();

        if (copyPlayerState) {
            shell.health = player.getHealth();
            shell.inventory.clone(player.getInventory());
            shell.component.clone(ShellStateComponent.of(player), lookup);

            shell.foodLevel = player.getHungerManager().getFoodLevel();
            shell.saturationLevel = player.getHungerManager().getSaturationLevel();
            shell.exhaustion = player.getHungerManager().getExhaustion();

            shell.experienceLevel = player.experienceLevel;
            shell.experienceProgress = player.experienceProgress;
            shell.totalExperience = player.totalExperience;
        } else {
            shell.health = player.getMaxHealth();
            shell.foodLevel = 20;
            shell.saturationLevel = 5;
        }

        shell.world = WorldUtil.getId(player.getWorld());
        shell.pos = pos;

        return shell;
    }


    public void dropInventory(ServerWorld world) {
        this.dropInventory(world, this.pos);
    }

    public void dropInventory(ServerWorld world, BlockPos pos) {
        Stream
            .of(this.inventory.main, this.inventory.armor, this.inventory.offHand, this.component.getItems())
            .flatMap(Collection::stream)
            .forEach(x -> this.dropItemStack(world, pos, x));
    }

    public void dropXp(ServerWorld world) {
        this.dropXp(world, this.pos);
    }

    public void dropXp(ServerWorld world, BlockPos pos) {
        int xp = Math.min(this.experienceLevel * 7, 100) + this.component.getXp();
        Vec3d vecPos = new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        ExperienceOrbEntity.spawn(world, vecPos, xp);
    }

    public void drop(ServerWorld world) {
        this.drop(world, this.pos);
    }

    public void drop(ServerWorld world, BlockPos pos) {
        this.dropInventory(world, pos);
        this.dropXp(world, pos);
    }

    private void dropItemStack(World world, BlockPos pos, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        ItemEntity item = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack);
        item.setPickupDelay(40);
       if (world instanceof ServerWorld serverWorld) {
           Entity thrower = serverWorld.getEntity(this.getOwnerUuid());
           if (thrower != null) {
               item.setThrower(thrower);
           }
       }

        float h = world.random.nextFloat() * 0.5F;
        float v = world.random.nextFloat() * 2 * Radians.R_PI;
        item.setVelocity(-MathHelper.sin(v) * h, 0.2, MathHelper.cos(v) * h);
        world.spawnEntity(item);
    }


    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        return this.serializer.writeNbt(nbt, lookup);
    }

    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        this.serializer.readNbt(nbt, lookup);
    }


    @Override
    public boolean equals(Object o) {
        return o == this || o instanceof ShellState state && Objects.equals(this.uuid, state.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.uuid);
    }


    @Environment(EnvType.CLIENT)
    private ShellEntity entityInstance;

    @Environment(EnvType.CLIENT)
    public ShellEntity asEntity() {
        if (this.entityInstance == null) {
            this.entityInstance = new ShellEntity(this);
        }
        return this.entityInstance;
    }


    static {
        NBT_SERIALIZER_FACTORY = new NbtSerializerFactoryBuilder<ShellState>()
            .add(UUID.class, "uuid", (x, wrapperLookup) -> x.uuid, (x, uuid, wrapperLookup) -> x.uuid = uuid)
            .add(Integer.class, "color", (x, wrapperLookup) -> x.color == null ? -1 : x.color.getId(), (x, color, wrapperLookup) -> x.color = color == -1 ? null : DyeColor.byId(color))
            .add(Float.class, "progress", (x, wrapperLookup) -> x.progress, (x, progress, wrapperLookup) -> x.progress = progress)
            .add(Boolean.class, "isArtificial", (x, wrapperLookup) -> x.isArtificial, (x, isArtificial, wrapperLookup) -> x.isArtificial = isArtificial)

            .add(UUID.class, "ownerUuid", (x, wrapperLookup) -> x.ownerUuid, (x, ownerUuid, wrapperLookup) -> x.ownerUuid = ownerUuid)
            .add(String.class, "ownerName", (x, wrapperLookup) -> x.ownerName, (x, ownerName, wrapperLookup) -> x.ownerName = ownerName)
            .add(Float.class, "health", (x, wrapperLookup) -> x.health, (x, health, wrapperLookup) -> x.health = health)
            .add(Integer.class, "gameMode", (x, wrapperLookup) -> x.gameMode, (x, gameMode, wrapperLookup) -> x.gameMode = gameMode)
            .add(NbtList.class, "inventory", (x, wrapperLookup) -> x.inventory.writeNbt(new NbtList(), wrapperLookup), (x, inventory, wrapperLookup) -> { x.inventory = new SimpleInventory(); x.inventory.readNbt(inventory, wrapperLookup); })
            .add(NbtCompound.class, "components", (x, wrapperLookup) -> x.component.writeNbt(new NbtCompound(), wrapperLookup), (x, component, wrapperLookup) -> { x.component = ShellStateComponent.empty(); if (component != null) { x.component.readNbt(component, wrapperLookup); } })

            .add(Integer.class, "foodLevel", (x, wrapperLookup) -> x.foodLevel, (x, foodLevel, wrapperLookup) -> x.foodLevel = foodLevel)
            .add(Float.class, "saturationLevel", (x, wrapperLookup) -> x.saturationLevel, (x, saturationLevel, wrapperLookup) -> x.saturationLevel = saturationLevel)
            .add(Float.class, "exhaustion", (x, wrapperLookup) -> x.exhaustion, (x, exhaustion, wrapperLookup) -> x.exhaustion = exhaustion)

            .add(Integer.class, "experienceLevel", (x, wrapperLookup) -> x.experienceLevel, (x, experienceLevel, wrapperLookup) -> x.experienceLevel = experienceLevel)
            .add(Float.class, "experienceProgress", (x, wrapperLookup) -> x.experienceProgress, (x, experienceProgress, wrapperLookup) -> x.experienceProgress = experienceProgress)
            .add(Integer.class, "totalExperience", (x, wrapperLookup) -> x.totalExperience, (x, totalExperience, wrapperLookup) -> x.totalExperience = totalExperience)

            .add(Identifier.class, "world", (x, wrapperLookup) -> x.world, (x, world, wrapperLookup) -> x.world = world)
            .add(BlockPos.class, "pos", (x, wrapperLookup) -> x.pos, (x, pos, wrapperLookup) -> x.pos = pos)
            .build();
    }
}