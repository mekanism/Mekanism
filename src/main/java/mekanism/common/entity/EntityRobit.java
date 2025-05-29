package mekanism.common.entity;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.MekanismAPI;
import mekanism.api.SerializationConstants;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.event.MekanismTeleportEvent;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.api.math.MathUtils;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.api.recipes.cache.OneInputCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.api.robit.IRobit;
import mekanism.api.robit.RobitSkin;
import mekanism.api.security.IEntitySecurityUtils;
import mekanism.api.security.IItemSecurityUtils;
import mekanism.api.security.ISecurityObject;
import mekanism.api.security.SecurityMode;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.client.recipe_viewer.type.RecipeViewerRecipeType;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.base.holiday.HolidayManager;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.config.MekanismConfig;
import mekanism.common.entity.ai.RobitAIFollow;
import mekanism.common.entity.ai.RobitAIPickup;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.container.sync.SyncableLong;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.item.ItemRobit;
import mekanism.common.lib.security.EntitySecurityUtils;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.ISingleRecipeLookupHandler.ItemRecipeLookupHandler;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.SingleItem;
import mekanism.common.recipe.lookup.monitor.RecipeCacheLookupMonitor;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.registries.MekanismDataSerializers;
import mekanism.common.registries.MekanismEntityTypes;
import mekanism.common.registries.MekanismItems;
import mekanism.common.registries.MekanismRobitSkins;
import mekanism.common.registries.MekanismRobitSkins.SkinLookup;
import mekanism.common.tile.TileEntityChargepad;
import mekanism.common.tile.prefab.TileEntityRecipeMachine;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//TODO: When Galacticraft gets ported make it so the robit can "breath" without a mask
public class EntityRobit extends PathfinderMob implements IRobit, IMekanismInventory, IMekanismStrictEnergyHandler, ItemRecipeLookupHandler<ItemStackToItemStackRecipe> {

    public static AttributeSupplier.Builder getDefaultAttributes() {
        return createMobAttributes().add(Attributes.MAX_HEALTH, 1.0D).add(Attributes.MOVEMENT_SPEED, 0.3F);
    }

    public static final ModelProperty<ResourceLocation> SKIN_TEXTURE_PROPERTY = new ModelProperty<>();

    private static <T> EntityDataAccessor<T> define(EntityDataSerializer<T> dataSerializer) {
        return SynchedEntityData.defineId(EntityRobit.class, dataSerializer);
    }

    private static final TicketType<Integer> ROBIT_CHUNK_UNLOAD = TicketType.create("robit_chunk_unload", Integer::compareTo, SharedConstants.TICKS_PER_SECOND);
    private static final EntityDataAccessor<UUID> OWNER_UUID = define(MekanismDataSerializers.UUID.value());
    //TODO: Ensure this properly updates if the user's name changes but uuid is the same
    private static final EntityDataAccessor<String> OWNER_NAME = define(EntityDataSerializers.STRING);
    private static final EntityDataAccessor<SecurityMode> SECURITY = define(MekanismDataSerializers.SECURITY.value());
    private static final EntityDataAccessor<Boolean> FOLLOW = define(EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DROP_PICKUP = define(EntityDataSerializers.BOOLEAN);
    //Note: We sync the default skin part, so that pick item on the robit will properly persist this
    private static final EntityDataAccessor<Boolean> DEFAULT_SKIN_MANUALLY_SELECTED = define(EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<ResourceKey<RobitSkin>> SKIN = define(MekanismDataSerializers.ROBIT_SKIN.value());

    private static final List<RecipeError> TRACKED_ERROR_TYPES = List.of(
          RecipeError.NOT_ENOUGH_ENERGY,
          RecipeError.NOT_ENOUGH_INPUT,
          RecipeError.NOT_ENOUGH_OUTPUT_SPACE,
          RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT
    );

    public static final long MAX_ENERGY = 100_000L;
    private static final double DISTANCE_MULTIPLIER = 1.5D;
    //TODO: Note the robit smelts at double normal speed, we may want to make this configurable
    //TODO: Allow for upgrades in the robit?
    private static final int ticksRequired = 5 * SharedConstants.TICKS_PER_SECOND;

    @Nullable
    private GlobalPos homeLocation;
    private int lastTextureUpdate;
    private int textureIndex;
    private int progress;

    /**
     * The players currently using this robit.
     */
    private final Set<Player> playersUsing = new ObjectOpenHashSet<>();

    private final RecipeCacheLookupMonitor<ItemStackToItemStackRecipe> recipeCacheLookupMonitor;
    private final BooleanSupplier recheckAllRecipeErrors;
    private final boolean[] trackedErrors = new boolean[TRACKED_ERROR_TYPES.size()];

    private final IInputHandler<@NotNull ItemStack> inputHandler;
    private final IOutputHandler<@NotNull ItemStack> outputHandler;

    @NotNull
    private final List<IInventorySlot> inventorySlots;
    @NotNull
    private final List<IInventorySlot> mainContainerSlots;
    @NotNull
    private final List<IInventorySlot> smeltingContainerSlots;
    @NotNull
    private final List<IInventorySlot> inventoryContainerSlots;
    private final EnergyInventorySlot energySlot;
    private final InputInventorySlot smeltingInputSlot;
    private final OutputInventorySlot smeltingOutputSlot;
    private final List<IEnergyContainer> energyContainers;
    private final BasicEnergyContainer energyContainer;

    public EntityRobit(EntityType<EntityRobit> type, Level world) {
        super(type, world);
        getNavigation().setCanFloat(false);
        setPathfindingMalus(PathType.UNPASSABLE_RAIL, 0.0F);
        setCustomNameVisible(true);
        recipeCacheLookupMonitor = new RecipeCacheLookupMonitor<>(this);
        // Choose a random offset to check for all errors. We do this to ensure that not every tile tries to recheck errors for every
        // recipe the same tick and thus create uneven spikes of CPU usage
        int checkOffset = level().random.nextInt(TileEntityRecipeMachine.RECIPE_CHECK_FREQUENCY);
        recheckAllRecipeErrors = () -> !playersUsing.isEmpty() && level().getGameTime() % TileEntityRecipeMachine.RECIPE_CHECK_FREQUENCY == checkOffset;
        IContentsListener recipeCacheUnpauseListener = () -> {
            onContentsChanged();
            recipeCacheLookupMonitor.unpause();
        };
        energyContainers = Collections.singletonList(energyContainer = BasicEnergyContainer.input(MAX_ENERGY, recipeCacheUnpauseListener));

        inventorySlots = new ArrayList<>();
        inventoryContainerSlots = new ArrayList<>();
        for (int slotY = 0; slotY < 3; slotY++) {
            for (int slotX = 0; slotX < 9; slotX++) {
                IInventorySlot slot = BasicInventorySlot.at(this, 8 + slotX * 18, 18 + slotY * 18);
                inventorySlots.add(slot);
                inventoryContainerSlots.add(slot);
            }
        }
        inventorySlots.add(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::level, this, 153, 17));
        inventorySlots.add(smeltingInputSlot = InputInventorySlot.at(this::containsRecipe, recipeCacheLookupMonitor, 51, 35));
        //TODO: Previously used FurnaceResultSlot, check if we need to replicate any special logic it had (like if it had xp logic or something)
        // Yes we probably do want this to allow for experience. Though maybe we should allow for experience for all our recipes/smelting recipes? V10
        inventorySlots.add(smeltingOutputSlot = OutputInventorySlot.at(recipeCacheUnpauseListener, 116, 35));
        smeltingInputSlot.tracksWarnings(slot -> slot.warning(WarningType.NO_MATCHING_RECIPE, getWarningCheck(RecipeError.NOT_ENOUGH_INPUT)));
        smeltingOutputSlot.tracksWarnings(slot -> slot.warning(WarningType.NO_SPACE_IN_OUTPUT, getWarningCheck(RecipeError.NOT_ENOUGH_OUTPUT_SPACE)));

        mainContainerSlots = Collections.singletonList(energySlot);
        smeltingContainerSlots = List.of(smeltingInputSlot, smeltingOutputSlot);

        inputHandler = InputHelper.getInputHandler(smeltingInputSlot, RecipeError.NOT_ENOUGH_INPUT);
        outputHandler = OutputHelper.getOutputHandler(smeltingOutputSlot, RecipeError.NOT_ENOUGH_OUTPUT_SPACE);
    }

    @Nullable
    public static EntityRobit create(Level world, double x, double y, double z) {
        EntityRobit robit = MekanismEntityTypes.ROBIT.get().create(world);
        if (robit == null) {
            return null;
        }
        robit.setPos(x, y, z);
        robit.xo = x;
        robit.yo = y;
        robit.zo = z;
        return robit;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(1, new RobitAIPickup(this, 1));
        goalSelector.addGoal(2, new RobitAIFollow(this, 1, 4, 2));
        goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8));
        goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        goalSelector.addGoal(4, new FloatGoal(this));
    }

    @Nullable
    @Override
    public Level getLevel() {
        return level();
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected void defineSynchedData(@NotNull SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        //Default before it has a brief chance to get set the owner to mekanism's fake player
        builder.define(OWNER_UUID, Mekanism.gameProfile.getId());
        builder.define(OWNER_NAME, "");
        builder.define(SECURITY, SecurityMode.PUBLIC);
        builder.define(FOLLOW, false);
        builder.define(DROP_PICKUP, false);
        builder.define(DEFAULT_SKIN_MANUALLY_SELECTED, false);
        builder.define(SKIN, MekanismRobitSkins.BASE);
    }

    private long getRoundedTravelEnergy() {
        return MathUtils.ceilToLong(DISTANCE_MULTIPLIER * Math.sqrt(distanceToSqr(xo, yo, zo)));
    }

    @Override
    public void onRemovedFromLevel() {
        if (level() != null && !level().isClientSide && getFollowing() && getOwner() != null) {
            //If this robit is currently following its owner and is being removed from the world (due to chunk unloading)
            // register a ticket that loads the chunk for a second, so that it has time to have its following check run again
            // (as it runs every 10 ticks, half a second), and then teleport to the owner.
            ((ServerLevel) level()).getChunkSource().addRegionTicket(ROBIT_CHUNK_UNLOAD, new ChunkPos(blockPosition()), 2, getId());
        }
        super.onRemovedFromLevel();
    }

    @Override
    public void tick() {
        Level level = level();
        if (!level.isClientSide) {
            if (homeLocation == null) {
                discard();
                return;
            }
            if (tickCount % SharedConstants.TICKS_PER_SECOND == 0) {
                Level serverWorld;
                if (level.dimension() == homeLocation.dimension()) {
                    serverWorld = level;
                } else {
                    MinecraftServer server = getServer();
                    serverWorld = server == null ? null : server.getLevel(homeLocation.dimension());
                }
                BlockPos homePos = homeLocation.pos();
                if (WorldUtils.isBlockLoaded(serverWorld, homePos) && WorldUtils.getTileEntity(TileEntityChargepad.class, serverWorld, homePos) == null) {
                    drop();
                    discard();
                    return;
                }
            }
        }
        super.tick();
    }

    @Override
    public void baseTick() {
        if (!level().isClientSide) {
            if (getFollowing()) {
                Player owner = getOwner();
                if (owner != null && distanceToSqr(owner) > 4 && !getNavigation().isDone() && !energyContainer.isEmpty()) {
                    energyContainer.extract(getRoundedTravelEnergy(), Action.EXECUTE, AutomationType.INTERNAL);
                }
            }
        }

        super.baseTick();

        if (!level().isClientSide) {
            if (getDropPickup()) {
                collectItems();
            }

            if (energyContainer.isEmpty() && !isOnChargepad()) {
                goHome();
            }

            energySlot.fillContainerOrConvert();
            recipeCacheLookupMonitor.updateAndProcess();

            if (!isDefaultSkinManuallySelected() && HolidayManager.hasRobitSkinsToday() && getSkin() == MekanismRobitSkins.BASE) {
                //Randomize the robit's skin
                setSkin(HolidayManager.getRandomBaseSkin(level().random), null);
            }
        }
    }

    public boolean isItemValid(ItemEntity item) {
        return item.isAlive() && !item.hasPickUpDelay() && !(item.getItem().getItem() instanceof ItemRobit);
    }

    private void collectItems() {
        List<ItemEntity> items = level().getEntitiesOfClass(ItemEntity.class, getBoundingBox().inflate(1.5, 1.5, 1.5));
        if (!items.isEmpty()) {
            for (ItemEntity item : items) {
                if (isItemValid(item)) {
                    for (IInventorySlot slot : inventoryContainerSlots) {
                        if (slot.isEmpty()) {
                            slot.setStack(item.getItem());
                            take(item, item.getItem().getCount());
                            item.discard();
                            playSound(SoundEvents.ITEM_PICKUP, 1, ((random.nextFloat() - random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                            break;
                        }
                        ItemStack itemStack = slot.getStack();
                        int maxSize = slot.getLimit(itemStack);
                        if (ItemStack.isSameItemSameComponents(itemStack, item.getItem()) && itemStack.getCount() < maxSize) {
                            int needed = maxSize - itemStack.getCount();
                            int toAdd = Math.min(needed, item.getItem().getCount());
                            MekanismUtils.logMismatchedStackSize(slot.growStack(toAdd, Action.EXECUTE), toAdd);
                            item.getItem().shrink(toAdd);
                            take(item, toAdd);
                            if (item.getItem().isEmpty()) {
                                item.discard();
                            }
                            playSound(SoundEvents.ITEM_PICKUP, 1, ((random.nextFloat() - random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                            break;
                        }
                    }
                }
            }
        }
    }

    public void goHome() {
        if (level().isClientSide() || homeLocation == null) {
            return;
        }
        MekanismTeleportEvent.Robit event = new MekanismTeleportEvent.Robit(this);
        if (NeoForge.EVENT_BUS.post(event).isCanceled()) {
            //Fail if the event was cancelled
            return;
        }
        setFollowing(false);
        if (!event.isTransDimensional()) {
            setDeltaMovement(0, 0, 0);
            teleportTo(event.getTargetX(), event.getTargetY(), event.getTargetZ());
        } else {
            ServerLevel newWorld = ((ServerLevel) this.level()).getServer().getLevel(event.getTargetDimension());
            if (newWorld != null) {
                Vec3 destination = event.getTarget();
                changeDimension(new DimensionTransition(newWorld, destination, Vec3.ZERO, getYRot(), getXRot(), DimensionTransition.DO_NOTHING));
            }
        }
    }

    private boolean isOnChargepad() {
        return WorldUtils.getTileEntity(TileEntityChargepad.class, level(), blockPosition()) != null;
    }

    @NotNull
    @Override
    public InteractionResult interactAt(@NotNull Player player, @NotNull Vec3 vec, @NotNull InteractionHand hand) {
        if (!IEntitySecurityUtils.INSTANCE.canAccessOrDisplayError(player, this)) {
            return InteractionResult.FAIL;
        } else if (player.isShiftKeyDown()) {
            ItemStack stack = player.getItemInHand(hand);
            if (!stack.isEmpty() && stack.getItem() instanceof ItemConfigurator) {
                if (!level().isClientSide) {
                    drop();
                }
                discard();
                player.swing(hand);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        } else if (!level().isClientSide) {
            MenuProvider provider = MekanismContainerTypes.MAIN_ROBIT.getProvider(MekanismLang.ROBIT, this, true);
            if (provider != null) {
                gameEvent(GameEvent.ENTITY_INTERACT, player);
                //Validate the provider isn't null, it shouldn't be but just in case
                player.openMenu(provider, buf -> buf.writeVarInt(getId()));
            }
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    private ItemStack getItemVariant() {
        ItemStack stack = MekanismItems.ROBIT.asStack();
        IStrictEnergyHandler energyHandlerItem = Capabilities.STRICT_ENERGY.getCapability(stack);
        if (energyHandlerItem != null && energyHandlerItem.getEnergyContainerCount() > 0) {
            energyHandlerItem.setEnergy(0, energyContainer.getEnergy());
        }
        ContainerType.ITEM.copyToStack(level().registryAccess(), getInventorySlots(null), stack);
        if (hasCustomName()) {
            stack.set(MekanismDataComponents.ROBIT_NAME, getName());
        }
        ISecurityObject security = IItemSecurityUtils.INSTANCE.securityCapability(stack);
        if (security != null) {
            security.setOwnerUUID(getOwnerUUID());
            security.setSecurityMode(getSecurityMode());
        }
        stack.set(MekanismDataComponents.DEFAULT_MANUALLY_SELECTED, isDefaultSkinManuallySelected());
        stack.set(MekanismDataComponents.ROBIT_SKIN, getSkin());
        return stack;
    }

    public void drop() {
        //TODO: Move this to loot table?
        ItemEntity entityItem = new ItemEntity(level(), getX(), getY() + 0.3, getZ(), getItemVariant());
        entityItem.setDeltaMovement(0, random.nextGaussian() * 0.05F + 0.2F, 0);
        level().addFreshEntity(entityItem);
    }

    public double getScaledProgress() {
        return getOperatingTicks() / (double) ticksRequired;
    }

    public int getOperatingTicks() {
        return progress;
    }

    @Override
    public int getSavedOperatingTicks(int cacheIndex) {
        return getOperatingTicks();
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag nbtTags) {
        super.addAdditionalSaveData(nbtTags);
        HolderLookup.Provider provider = registryAccess();
        nbtTags.putUUID(SerializationConstants.OWNER_UUID, getOwnerUUID());
        NBTUtils.writeEnum(nbtTags, SerializationConstants.SECURITY_MODE, getSecurityMode());
        nbtTags.putBoolean(SerializationConstants.FOLLOW, getFollowing());
        nbtTags.putBoolean(SerializationConstants.PICKUP_DROPS, getDropPickup());
        if (homeLocation != null) {
            Optional<Tag> result = GlobalPos.CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), homeLocation).result();
            //noinspection OptionalIsPresent - Capturing lambda
            if (result.isPresent()) {
                nbtTags.put(SerializationConstants.HOME_LOCATION, result.get());
            }
        }
        ContainerType.ITEM.saveTo(provider, nbtTags, getInventorySlots(null));
        ContainerType.ENERGY.saveTo(provider, nbtTags, getEnergyContainers(null));
        nbtTags.putInt(SerializationConstants.PROGRESS, getOperatingTicks());
        NBTUtils.writeResourceKey(nbtTags, SerializationConstants.SKIN, getSkin());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag nbtTags) {
        super.readAdditionalSaveData(nbtTags);
        HolderLookup.Provider provider = registryAccess();
        NBTUtils.setUUIDIfPresent(nbtTags, SerializationConstants.OWNER_UUID, this::setOwnerUUID);
        NBTUtils.setEnumIfPresent(nbtTags, SerializationConstants.SECURITY_MODE, SecurityMode.BY_ID, this::setSecurityMode);
        setFollowing(nbtTags.getBoolean(SerializationConstants.FOLLOW));
        setDropPickup(nbtTags.getBoolean(SerializationConstants.PICKUP_DROPS));
        NBTUtils.setCompoundIfPresent(nbtTags, SerializationConstants.HOME_LOCATION, home -> homeLocation = GlobalPos.CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), home).result().orElse(null));
        ContainerType.ITEM.readFrom(provider, nbtTags, getInventorySlots(null));
        ContainerType.ENERGY.readFrom(provider, nbtTags, getEnergyContainers(null));
        progress = nbtTags.getInt(SerializationConstants.PROGRESS);
        NBTUtils.setResourceKeyIfPresentElse(nbtTags, SerializationConstants.SKIN, MekanismAPI.ROBIT_SKIN_REGISTRY_NAME, skin -> setSkin(skin, null),
              () -> setSkin(MekanismRobitSkins.BASE, null));
    }

    @Override
    public void onDamageTaken(@NotNull DamageContainer damageContainer) {
        energyContainer.extract(MathUtils.clampToLong(1_000D * damageContainer.getNewDamage()), Action.EXECUTE, AutomationType.INTERNAL);
        //Don't actually allow taking damage to reduce the robit's health
        setHealth(getMaxHealth());
    }

    @Override
    protected void tickDeath() {
    }

    public void setHome(GlobalPos home) {
        homeLocation = home;
    }

    @Nullable
    @Override
    public GlobalPos getHome() {
        return homeLocation;
    }

    @Override
    public boolean isPushable() {
        return !energyContainer.isEmpty();
    }

    public Player getOwner() {
        return level().getPlayerByUUID(getOwnerUUID());
    }

    @NotNull
    @Override
    public String getOwnerName() {
        return entityData.get(OWNER_NAME);
    }

    @NotNull
    @Override
    public UUID getOwnerUUID() {
        return entityData.get(OWNER_UUID);
    }

    @NotNull
    @Override
    public SecurityMode getSecurityMode() {
        return entityData.get(SECURITY);
    }

    @Override
    public void setSecurityMode(@NotNull SecurityMode mode) {
        SecurityMode current = getSecurityMode();
        if (current != mode) {
            entityData.set(SECURITY, mode);
            onSecurityChanged(current, mode);
        }
    }

    @Override
    public void onSecurityChanged(@NotNull SecurityMode old, @NotNull SecurityMode mode) {
        if (!level().isClientSide) {
            EntitySecurityUtils.get().securityChanged(playersUsing, this, old, mode);
        }
    }

    public void open(Player player) {
        playersUsing.add(player);
    }

    public void close(Player player) {
        playersUsing.remove(player);
    }

    @Override
    public void setOwnerUUID(UUID uuid) {
        entityData.set(OWNER_UUID, uuid);
        entityData.set(OWNER_NAME, MekanismUtils.getLastKnownUsername(uuid));
    }

    public boolean getFollowing() {
        return entityData.get(FOLLOW);
    }

    public void setFollowing(boolean follow) {
        entityData.set(FOLLOW, follow);
    }

    public boolean getDropPickup() {
        return entityData.get(DROP_PICKUP);
    }

    public void setDropPickup(boolean pickup) {
        entityData.set(DROP_PICKUP, pickup);
    }

    @NotNull
    @Override
    public List<IInventorySlot> getInventorySlots(@Nullable Direction side) {
        return hasInventory() ? inventorySlots : Collections.emptyList();
    }

    @NotNull
    @Override
    public List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) {
        return canHandleEnergy() ? energyContainers : Collections.emptyList();
    }

    @Override
    public void onContentsChanged() {
        //TODO: Do we need to save the things? Probably, if not remove the call to here from createNewCachedRecipe
    }

    @NotNull
    public List<IInventorySlot> getContainerInventorySlots(@NotNull MenuType<?> containerType) {
        if (!hasInventory()) {
            return Collections.emptyList();
        } else if (containerType == MekanismContainerTypes.INVENTORY_ROBIT.get()) {
            return inventoryContainerSlots;
        } else if (containerType == MekanismContainerTypes.MAIN_ROBIT.get()) {
            return mainContainerSlots;
        } else if (containerType == MekanismContainerTypes.SMELTING_ROBIT.get()) {
            return smeltingContainerSlots;
        }
        return Collections.emptyList();
    }

    @NotNull
    @Override
    public IMekanismRecipeTypeProvider<SingleRecipeInput, ItemStackToItemStackRecipe, SingleItem<ItemStackToItemStackRecipe>> getRecipeType() {
        return MekanismRecipeType.SMELTING;
    }

    @Override
    public IRecipeViewerRecipeType<ItemStackToItemStackRecipe> recipeViewerType() {
        return RecipeViewerRecipeType.SMELTING;
    }

    @Nullable
    @Override
    public ItemStackToItemStackRecipe getRecipe(int cacheIndex) {
        return findFirstRecipe(inputHandler);
    }

    public IEnergyContainer getEnergyContainer() {
        return energyContainer;
    }

    @Override
    public ItemStack getPickedResult(@NotNull HitResult target) {
        return getItemVariant();
    }

    @Override
    public void clearRecipeErrors(int cacheIndex) {
        Arrays.fill(trackedErrors, false);
    }

    @NotNull
    @Override
    public CachedRecipe<ItemStackToItemStackRecipe> createNewCachedRecipe(@NotNull ItemStackToItemStackRecipe recipe, int cacheIndex) {
        //TODO: Make a robit specific smelting energy usage config
        return OneInputCachedRecipe.itemToItem(recipe, recheckAllRecipeErrors, inputHandler, outputHandler)
              .setErrorsChanged(errors -> {
                  for (int i = 0; i < trackedErrors.length; i++) {
                      trackedErrors[i] = errors.contains(TRACKED_ERROR_TYPES.get(i));
                  }
              })
              .setEnergyRequirements(MekanismConfig.usage.energizedSmelter, energyContainer)
              .setRequiredTicks(() -> ticksRequired)
              .setOnFinish(this::onContentsChanged)
              .setOperatingTicksChanged(operatingTicks -> progress = operatingTicks);
    }

    public BooleanSupplier getWarningCheck(RecipeError error) {
        int errorIndex = TRACKED_ERROR_TYPES.indexOf(error);
        if (errorIndex == -1) {
            //Something went wrong
            return () -> false;
        }
        return () -> trackedErrors[errorIndex];
    }

    public void addContainerTrackers(MekanismContainer container) {
        MenuType<?> containerType = container.getType();
        if (containerType == MekanismContainerTypes.MAIN_ROBIT.get()) {
            container.track(SyncableLong.create(energyContainer::getEnergy, energyContainer::setEnergy));
        } else if (containerType == MekanismContainerTypes.SMELTING_ROBIT.get()) {
            container.track(SyncableInt.create(() -> progress, value -> progress = value));
            container.trackArray(trackedErrors);
        }
    }

    public ContainerLevelAccess getWorldPosCallable() {
        if (level().isClientSide) {
            //Note: Mojang just uses a null level access for containers on the client side. We mirror this here so that
            // we don't play multiple sounds when taking items out of the robit's repair screen
            return ContainerLevelAccess.NULL;
        }
        return new ContainerLevelAccess() {
            @NotNull
            @Override
            public <T> Optional<T> evaluate(@NotNull BiFunction<Level, BlockPos, T> worldBlockPosTBiFunction) {
                //Note: We use an anonymous class implementation rather than using IWorldPosCallable.of, so that if the robit moves
                // this uses the proper updated position
                return Optional.ofNullable(worldBlockPosTBiFunction.apply(level(), blockPosition()));
            }
        };
    }

    public boolean isDefaultSkinManuallySelected() {
        return entityData.get(DEFAULT_SKIN_MANUALLY_SELECTED);
    }

    public void setDefaultSkinManuallySelected(boolean value) {
        entityData.set(DEFAULT_SKIN_MANUALLY_SELECTED, value);
    }

    @NotNull
    @Override
    public ResourceKey<RobitSkin> getSkin() {
        return entityData.get(SKIN);
    }

    @Override
    public boolean setSkin(@NotNull ResourceKey<RobitSkin> skinKey, @Nullable Player player) {
        Objects.requireNonNull(skinKey, "Robit skin cannot be null.");
        if (getSkin() == skinKey) {
            //Don't do anything if the robit already has that skin selected
            return true;
        }
        if (player != null) {
            if (!IEntitySecurityUtils.INSTANCE.canAccess(player, this)) {
                return false;
            }
            SkinLookup skinLookup = MekanismRobitSkins.lookup(level().registryAccess(), skinKey);
            skinKey = skinLookup.name();
            if (getSkin() == skinKey) {
                //Don't do anything if the robit already has that skin selected
                //Note: We double-check this in case we ended up being changed to the default skin due to it not existing
                return true;
            } else if (!skinLookup.skin().isUnlocked(player)) {
                return false;
            } else if (player instanceof ServerPlayer serverPlayer) {
                MekanismCriteriaTriggers.CHANGE_ROBIT_SKIN.value().trigger(serverPlayer, skinKey);
            }
        }
        entityData.set(SKIN, skinKey);
        if (skinKey == MekanismRobitSkins.BASE) {
            setDefaultSkinManuallySelected(true);
        }
        return true;
    }

    /**
     * @apiNote Only call on the client.
     */
    public ModelData getModelData() {
        //TODO: Eventually we might want to evaluate caching this model data object
        return ModelData.of(SKIN_TEXTURE_PROPERTY, getModelTexture());
    }

    /**
     * @apiNote Only call on the client.
     */
    private ResourceLocation getModelTexture() {
        Registry<RobitSkin> robitSkins = level().registryAccess().registryOrThrow(MekanismAPI.ROBIT_SKIN_REGISTRY_NAME);
        ResourceKey<RobitSkin> skinKey = getSkin();
        Optional<RobitSkin> optionalSkin = robitSkins.getOptional(skinKey);
        RobitSkin skin;
        if (optionalSkin.isPresent()) {
            skin = optionalSkin.get();
        } else {
            Mekanism.logger.error("Unknown Robit Skin: {}; resetting skin to base.", skinKey.location());
            setSkin(skinKey = MekanismRobitSkins.BASE, null);
            skin = robitSkins.getOrThrow(skinKey);
        }
        List<ResourceLocation> textures = skin.textures();
        if (textures.isEmpty()) {
            //Note: Should not really happen but in case a custom impl has no textures handle it
            textureIndex = 0;
            if (skinKey != MekanismRobitSkins.BASE) {
                Mekanism.logger.error("Robit Skin: {}, has no textures; resetting skin to base.", skinKey.location());
                setSkin(skinKey = MekanismRobitSkins.BASE, null);
                skin = robitSkins.getOrThrow(skinKey);
            }
            if (skin.textures().isEmpty()) {
                //This should not happen but if it does throw a cleaner error than a stack overflow
                throw new IllegalStateException("Base robit skin has no textures defined.");
            }
            return getModelTexture();
        }
        int textureCount = textures.size();
        if (textureCount == 1) {
            textureIndex = 0;
        } else {
            if (lastTextureUpdate < tickCount) {
                //Only check for movement and if the texture index needs to update if we haven't already done so this tick
                lastTextureUpdate = tickCount;
                if (Math.abs(getX() - xo) + Math.abs(getZ() - zo) > 0.001) {
                    //If the robit moved and the ticks are such that it should update, update the index
                    if (tickCount % 3 == 0) {
                        textureIndex++;
                    }
                }
            }
            if (textureIndex >= textureCount) {
                textureIndex = textureIndex % textureCount;
            }
        }
        return textures.get(textureIndex);
    }
}
