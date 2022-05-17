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
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.Coord4D;
import mekanism.api.DataHandlerUtils;
import mekanism.api.MekanismAPI;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NonNull;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.api.math.FloatingLong;
import mekanism.api.providers.IRobitSkinProvider;
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
import mekanism.api.security.ISecurityObject;
import mekanism.api.security.SecurityMode;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.CapabilityCache;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.capabilities.resolver.ICapabilityResolver;
import mekanism.common.config.MekanismConfig;
import mekanism.common.entity.ai.RobitAIFollow;
import mekanism.common.entity.ai.RobitAIPickup;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableFloatingLong;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.item.ItemRobit;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.ISingleRecipeLookupHandler.ItemRecipeLookupHandler;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.SingleItem;
import mekanism.common.recipe.lookup.monitor.RecipeCacheLookupMonitor;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.registries.MekanismDamageSource;
import mekanism.common.registries.MekanismDataSerializers;
import mekanism.common.registries.MekanismEntityTypes;
import mekanism.common.registries.MekanismItems;
import mekanism.common.registries.MekanismRobitSkins;
import mekanism.common.tile.TileEntityChargepad;
import mekanism.common.tile.interfaces.ISustainedInventory;
import mekanism.common.tile.prefab.TileEntityRecipeMachine;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
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
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.server.ServerLifecycleHooks;

//TODO: When Galacticraft gets ported make it so the robit can "breath" without a mask
public class EntityRobit extends PathfinderMob implements IRobit, IMekanismInventory, ISustainedInventory, ISecurityObject, IMekanismStrictEnergyHandler,
      ItemRecipeLookupHandler<ItemStackToItemStackRecipe> {

    public static AttributeSupplier.Builder getDefaultAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 1.0D).add(Attributes.MOVEMENT_SPEED, 0.3F);
    }

    public static final ModelProperty<ResourceLocation> SKIN_TEXTURE_PROPERTY = new ModelProperty<>();

    private static <T> EntityDataAccessor<T> define(EntityDataSerializer<T> dataSerializer) {
        return SynchedEntityData.defineId(EntityRobit.class, dataSerializer);
    }

    private static final TicketType<Integer> ROBIT_CHUNK_UNLOAD = TicketType.create("robit_chunk_unload", Integer::compareTo, 20);
    private static final EntityDataAccessor<UUID> OWNER_UUID = define(MekanismDataSerializers.UUID.getSerializer());
    private static final EntityDataAccessor<String> OWNER_NAME = define(EntityDataSerializers.STRING);
    private static final EntityDataAccessor<SecurityMode> SECURITY = define(MekanismDataSerializers.SECURITY.getSerializer());
    private static final EntityDataAccessor<Boolean> FOLLOW = define(EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DROP_PICKUP = define(EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<RobitSkin> SKIN = define(MekanismDataSerializers.<RobitSkin>getRegistryEntrySerializer());

    private static final List<RecipeError> TRACKED_ERROR_TYPES = List.of(
          RecipeError.NOT_ENOUGH_ENERGY,
          RecipeError.NOT_ENOUGH_INPUT,
          RecipeError.NOT_ENOUGH_OUTPUT_SPACE,
          RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT
    );

    public static final FloatingLong MAX_ENERGY = FloatingLong.createConst(100_000);
    private static final FloatingLong DISTANCE_MULTIPLIER = FloatingLong.createConst(1.5);
    //TODO: Note the robit smelts at double normal speed, we may want to make this configurable
    //TODO: Allow for upgrades in the robit?
    private static final int ticksRequired = 100;

    private final CapabilityCache capabilityCache = new CapabilityCache();
    public Coord4D homeLocation;
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

    private final IInputHandler<@NonNull ItemStack> inputHandler;
    private final IOutputHandler<@NonNull ItemStack> outputHandler;

    @Nonnull
    private final List<IInventorySlot> inventorySlots;
    @Nonnull
    private final List<IInventorySlot> mainContainerSlots;
    @Nonnull
    private final List<IInventorySlot> smeltingContainerSlots;
    @Nonnull
    private final List<IInventorySlot> inventoryContainerSlots;
    private final EnergyInventorySlot energySlot;
    private final InputInventorySlot smeltingInputSlot;
    private final OutputInventorySlot smeltingOutputSlot;
    private final List<IEnergyContainer> energyContainers;
    private final BasicEnergyContainer energyContainer;

    public EntityRobit(EntityType<EntityRobit> type, Level world) {
        super(type, world);
        getNavigation().setCanFloat(false);
        setCustomNameVisible(true);
        addCapabilityResolver(BasicCapabilityResolver.security(this));
        recipeCacheLookupMonitor = new RecipeCacheLookupMonitor<>(this);
        // Choose a random offset to check for all errors. We do this to ensure that not every tile tries to recheck errors for every
        // recipe the same tick and thus create uneven spikes of CPU usage
        int checkOffset = level.random.nextInt(TileEntityRecipeMachine.RECIPE_CHECK_FREQUENCY);
        recheckAllRecipeErrors = () -> !playersUsing.isEmpty() && level.getGameTime() % TileEntityRecipeMachine.RECIPE_CHECK_FREQUENCY == checkOffset;
        energyContainers = Collections.singletonList(energyContainer = BasicEnergyContainer.input(MAX_ENERGY, this));

        inventorySlots = new ArrayList<>();
        inventoryContainerSlots = new ArrayList<>();
        for (int slotY = 0; slotY < 3; slotY++) {
            for (int slotX = 0; slotX < 9; slotX++) {
                IInventorySlot slot = BasicInventorySlot.at(this, 8 + slotX * 18, 18 + slotY * 18);
                inventorySlots.add(slot);
                inventoryContainerSlots.add(slot);
            }
        }
        inventorySlots.add(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getCommandSenderWorld, this, 153, 17));
        inventorySlots.add(smeltingInputSlot = InputInventorySlot.at(this::containsRecipe, recipeCacheLookupMonitor, 51, 35));
        //TODO: Previously used FurnaceResultSlot, check if we need to replicate any special logic it had (like if it had xp logic or something)
        // Yes we probably do want this to allow for experience. Though maybe we should allow for experience for all our recipes/smelting recipes? V10
        inventorySlots.add(smeltingOutputSlot = OutputInventorySlot.at(this, 116, 35));
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

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        //Default before it has a brief chance to get set the owner to mekanism's fake player
        entityData.define(OWNER_UUID, Mekanism.gameProfile.getId());
        entityData.define(OWNER_NAME, "");
        entityData.define(SECURITY, SecurityMode.PUBLIC);
        entityData.define(FOLLOW, false);
        entityData.define(DROP_PICKUP, false);
        entityData.define(SKIN, MekanismRobitSkins.BASE.get());
    }

    private FloatingLong getRoundedTravelEnergy() {
        return DISTANCE_MULTIPLIER.multiply(Math.sqrt(distanceToSqr(xo, yo, zo)));
    }

    @Override
    public void onRemovedFromWorld() {
        if (level != null && !level.isClientSide && getFollowing() && getOwner() != null) {
            //If this robit is currently following its owner and is being removed from the world (due to chunk unloading)
            // register a ticket that loads the chunk for a second, so that it has time to have its following check run again
            // (as it runs every 10 ticks, half a second), and then teleport to the owner.
            ((ServerLevel) level).getChunkSource().addRegionTicket(ROBIT_CHUNK_UNLOAD, new ChunkPos(blockPosition()), 2, getId());
        }
        super.onRemovedFromWorld();
    }

    @Override
    public void baseTick() {
        if (!level.isClientSide) {
            if (getFollowing()) {
                Player owner = getOwner();
                if (owner != null && distanceToSqr(owner) > 4 && !getNavigation().isDone() && !energyContainer.isEmpty()) {
                    energyContainer.extract(getRoundedTravelEnergy(), Action.EXECUTE, AutomationType.INTERNAL);
                }
            }
        }

        super.baseTick();

        if (!level.isClientSide) {
            if (getDropPickup()) {
                collectItems();
            }
            if (homeLocation == null) {
                discard();
                return;
            }

            if (tickCount % 20 == 0) {
                Level serverWorld = ServerLifecycleHooks.getCurrentServer().getLevel(homeLocation.dimension);
                BlockPos homePos = homeLocation.getPos();
                if (WorldUtils.isBlockLoaded(serverWorld, homePos) && WorldUtils.getTileEntity(TileEntityChargepad.class, serverWorld, homePos) == null) {
                    drop();
                    discard();
                }
            }

            if (energyContainer.isEmpty() && !isOnChargepad()) {
                goHome();
            }

            energySlot.fillContainerOrConvert();
            recipeCacheLookupMonitor.updateAndProcess();
        }
    }

    public boolean isItemValid(ItemEntity item) {
        return item.isAlive() && !item.hasPickUpDelay() && !(item.getItem().getItem() instanceof ItemRobit);
    }

    private void collectItems() {
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, getBoundingBox().inflate(1.5, 1.5, 1.5));
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
                        if (ItemHandlerHelper.canItemStacksStack(itemStack, item.getItem()) && itemStack.getCount() < maxSize) {
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
        if (level.isClientSide()) {
            return;
        }
        setFollowing(false);
        if (level.dimension() == homeLocation.dimension) {
            setDeltaMovement(0, 0, 0);
            teleportTo(homeLocation.getX() + 0.5, homeLocation.getY() + 0.3, homeLocation.getZ() + 0.5);
        } else {
            ServerLevel newWorld = ((ServerLevel) this.level).getServer().getLevel(homeLocation.dimension);
            if (newWorld != null) {
                Vec3 destination = new Vec3(homeLocation.getX() + 0.5, homeLocation.getY() + 0.3, homeLocation.getZ() + 0.5);
                changeDimension(newWorld, new ITeleporter() {
                    @Override
                    public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
                        return repositionEntity.apply(false);
                    }

                    @Override
                    public PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld, Function<ServerLevel, PortalInfo> defaultPortalInfo) {
                        return new PortalInfo(destination, Vec3.ZERO, entity.getYRot(), entity.getXRot());
                    }

                    @Override
                    public boolean playTeleportSound(ServerPlayer player, ServerLevel sourceWorld, ServerLevel destWorld) {
                        return false;
                    }
                });
            }
        }
    }

    private boolean isOnChargepad() {
        return WorldUtils.getTileEntity(TileEntityChargepad.class, level, blockPosition()) != null;
    }

    @Nonnull
    @Override
    public InteractionResult interactAt(@Nonnull Player player, @Nonnull Vec3 vec, @Nonnull InteractionHand hand) {
        if (!MekanismAPI.getSecurityUtils().canAccessOrDisplayError(player, this)) {
            return InteractionResult.FAIL;
        } else if (player.isShiftKeyDown()) {
            ItemStack stack = player.getItemInHand(hand);
            if (!stack.isEmpty() && stack.getItem() instanceof ItemConfigurator) {
                if (!level.isClientSide) {
                    drop();
                }
                discard();
                player.swing(hand);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        } else if (!level.isClientSide) {
            MenuProvider provider = MekanismContainerTypes.MAIN_ROBIT.getProvider(MekanismLang.ROBIT, this);
            if (provider != null) {
                //Validate the provider isn't null, it shouldn't be but just in case
                NetworkHooks.openGui((ServerPlayer) player, provider, buf -> buf.writeVarInt(getId()));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private ItemStack getItemVariant() {
        ItemStack stack = MekanismItems.ROBIT.getItemStack();
        Optional<IStrictEnergyHandler> capability = stack.getCapability(Capabilities.STRICT_ENERGY_CAPABILITY).resolve();
        if (capability.isPresent()) {
            IStrictEnergyHandler energyHandlerItem = capability.get();
            if (energyHandlerItem.getEnergyContainerCount() > 0) {
                energyHandlerItem.setEnergy(0, energyContainer.getEnergy());
            }
        }
        ItemRobit item = (ItemRobit) stack.getItem();
        item.setInventory(getInventory(), stack);
        item.setName(stack, getName());
        stack.getCapability(Capabilities.SECURITY_OBJECT).ifPresent(security -> {
            security.setOwnerUUID(getOwnerUUID());
            security.setSecurityMode(getSecurityMode());
        });
        item.setSkin(stack, getSkin());
        return stack;
    }

    public void drop() {
        //TODO: Move this to loot table?
        ItemEntity entityItem = new ItemEntity(level, getX(), getY() + 0.3, getZ(), getItemVariant());
        entityItem.setDeltaMovement(0, random.nextGaussian() * 0.05F + 0.2F, 0);
        level.addFreshEntity(entityItem);
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
    public void addAdditionalSaveData(@Nonnull CompoundTag nbtTags) {
        super.addAdditionalSaveData(nbtTags);
        nbtTags.putUUID(NBTConstants.OWNER_UUID, getOwnerUUID());
        NBTUtils.writeEnum(nbtTags, NBTConstants.SECURITY_MODE, getSecurityMode());
        nbtTags.putBoolean(NBTConstants.FOLLOW, getFollowing());
        nbtTags.putBoolean(NBTConstants.PICKUP_DROPS, getDropPickup());
        if (homeLocation != null) {
            homeLocation.write(nbtTags);
        }
        nbtTags.put(NBTConstants.ITEMS, DataHandlerUtils.writeContainers(getInventorySlots(null)));
        nbtTags.put(NBTConstants.ENERGY_CONTAINERS, DataHandlerUtils.writeContainers(getEnergyContainers(null)));
        nbtTags.putInt(NBTConstants.PROGRESS, getOperatingTicks());
        NBTUtils.writeRegistryEntry(nbtTags, NBTConstants.SKIN, getSkin());
    }

    @Override
    public void readAdditionalSaveData(@Nonnull CompoundTag nbtTags) {
        super.readAdditionalSaveData(nbtTags);
        NBTUtils.setUUIDIfPresent(nbtTags, NBTConstants.OWNER_UUID, this::setOwnerUUID);
        NBTUtils.setEnumIfPresent(nbtTags, NBTConstants.SECURITY_MODE, SecurityMode::byIndexStatic, this::setSecurityMode);
        setFollowing(nbtTags.getBoolean(NBTConstants.FOLLOW));
        setDropPickup(nbtTags.getBoolean(NBTConstants.PICKUP_DROPS));
        homeLocation = Coord4D.read(nbtTags);
        DataHandlerUtils.readContainers(getInventorySlots(null), nbtTags.getList(NBTConstants.ITEMS, Tag.TAG_COMPOUND));
        DataHandlerUtils.readContainers(getEnergyContainers(null), nbtTags.getList(NBTConstants.ENERGY_CONTAINERS, Tag.TAG_COMPOUND));
        progress = nbtTags.getInt(NBTConstants.PROGRESS);
        NBTUtils.setRegistryEntryIfPresentElse(nbtTags, NBTConstants.SKIN, MekanismAPI.robitSkinRegistry(), skin -> setSkin(skin, null),
              () -> setSkin(MekanismRobitSkins.BASE, null));
    }

    @Override
    public boolean isInvulnerableTo(@Nonnull DamageSource source) {
        return source == MekanismDamageSource.RADIATION || super.isInvulnerableTo(source);
    }

    @Override
    protected void actuallyHurt(@Nonnull DamageSource damageSource, float amount) {
        amount = ForgeHooks.onLivingHurt(this, damageSource, amount);
        if (amount <= 0) {
            return;
        }
        amount = getDamageAfterArmorAbsorb(damageSource, amount);
        amount = getDamageAfterMagicAbsorb(damageSource, amount);
        if (damageSource.isFall()) {
            //Half the "potential" damage the Robit can take from falling
            amount /= 2;
        }
        energyContainer.extract(FloatingLong.create(1_000 * amount), Action.EXECUTE, AutomationType.INTERNAL);
        getCombatTracker().recordDamage(damageSource, getHealth(), amount);
    }

    @Override
    protected void tickDeath() {
    }

    public void setHome(Coord4D home) {
        homeLocation = home;
    }

    @Override
    public boolean isPushable() {
        return !energyContainer.isEmpty();
    }

    public Player getOwner() {
        return level.getPlayerByUUID(getOwnerUUID());
    }

    @Nonnull
    @Override
    public String getOwnerName() {
        return entityData.get(OWNER_NAME);
    }

    @Nonnull
    @Override
    public UUID getOwnerUUID() {
        return entityData.get(OWNER_UUID);
    }

    @Nonnull
    @Override
    public SecurityMode getSecurityMode() {
        return entityData.get(SECURITY);
    }

    @Override
    public void setSecurityMode(@Nonnull SecurityMode mode) {
        SecurityMode current = getSecurityMode();
        if (current != mode) {
            entityData.set(SECURITY, mode);
            onSecurityChanged(current, mode);
        }
    }

    @Override
    public void onSecurityChanged(@Nonnull SecurityMode old, @Nonnull SecurityMode mode) {
        if (!level.isClientSide) {
            SecurityUtils.INSTANCE.securityChanged(playersUsing, this, old, mode);
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

    @Override
    public void setInventory(ListTag nbtTags, Object... data) {
        if (nbtTags != null && !nbtTags.isEmpty()) {
            DataHandlerUtils.readContainers(getInventorySlots(null), nbtTags);
        }
    }

    @Override
    public ListTag getInventory(Object... data) {
        return DataHandlerUtils.writeContainers(getInventorySlots(null));
    }

    @Nonnull
    @Override
    public List<IInventorySlot> getInventorySlots(@Nullable Direction side) {
        return hasInventory() ? inventorySlots : Collections.emptyList();
    }

    @Nonnull
    @Override
    public List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) {
        return canHandleEnergy() ? energyContainers : Collections.emptyList();
    }

    @Override
    public void onContentsChanged() {
        //TODO: Do we need to save the things? Probably, if not remove the call to here from createNewCachedRecipe
    }

    @Nonnull
    public List<IInventorySlot> getContainerInventorySlots(@Nonnull MenuType<?> containerType) {
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

    @Nonnull
    @Override
    public IMekanismRecipeTypeProvider<ItemStackToItemStackRecipe, SingleItem<ItemStackToItemStackRecipe>> getRecipeType() {
        return MekanismRecipeType.SMELTING;
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
    public ItemStack getPickedResult(HitResult target) {
        return getItemVariant();
    }

    @Override
    public void clearRecipeErrors(int cacheIndex) {
        Arrays.fill(trackedErrors, false);
    }

    @Nonnull
    @Override
    public CachedRecipe<ItemStackToItemStackRecipe> createNewCachedRecipe(@Nonnull ItemStackToItemStackRecipe recipe, int cacheIndex) {
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
            container.track(SyncableFloatingLong.create(energyContainer::getEnergy, energyContainer::setEnergy));
        } else if (containerType == MekanismContainerTypes.SMELTING_ROBIT.get()) {
            container.track(SyncableInt.create(() -> progress, value -> progress = value));
            container.trackArray(trackedErrors);
        }
    }

    public ContainerLevelAccess getWorldPosCallable() {
        return new ContainerLevelAccess() {
            @Nonnull
            @Override
            public <T> Optional<T> evaluate(@Nonnull BiFunction<Level, BlockPos, T> worldBlockPosTBiFunction) {
                //Note: We use an anonymous class implementation rather than using IWorldPosCallable.of, so that if the robit moves
                // this uses the proper updated position
                return Optional.ofNullable(worldBlockPosTBiFunction.apply(getCommandSenderWorld(), blockPosition()));
            }
        };
    }

    @Nonnull
    @Override
    public RobitSkin getSkin() {
        return entityData.get(SKIN);
    }

    @Override
    public boolean setSkin(@Nonnull IRobitSkinProvider skinProvider, @Nullable Player player) {
        Objects.requireNonNull(skinProvider, "Robit skin cannot be null.");
        RobitSkin skin = skinProvider.getSkin();
        if (player != null) {
            if (!MekanismAPI.getSecurityUtils().canAccess(player, this) || !skin.isUnlocked(player)) {
                return false;
            }
        }
        entityData.set(SKIN, skin);
        return true;
    }

    /**
     * @apiNote Only call on the client.
     */
    public IModelData getModelData() {
        //TODO: Eventually we might want to evaluate caching this model data object
        return new ModelDataMap.Builder().withInitial(SKIN_TEXTURE_PROPERTY, getModelTexture()).build();
    }

    /**
     * @apiNote Only call on the client.
     */
    private ResourceLocation getModelTexture() {
        RobitSkin skin = getSkin();
        List<ResourceLocation> textures = skin.getTextures();
        if (textures.isEmpty()) {
            textureIndex = 0;
            Mekanism.logger.error("Robit Skin: {}, has no textures; resetting skin to base.", skin.getRegistryName());
            setSkin(MekanismRobitSkins.BASE, null);
            if (getSkin().getTextures().isEmpty()) {
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

    protected final void addCapabilityResolver(ICapabilityResolver resolver) {
        capabilityCache.addCapabilityResolver(resolver);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capabilityCache.isCapabilityDisabled(capability, side)) {
            return LazyOptional.empty();
        } else if (capabilityCache.canResolve(capability)) {
            return capabilityCache.getCapabilityUnchecked(capability, side);
        }
        //Call to the TileEntity's Implementation of getCapability if we could not find a capability ourselves
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        //When the capabilities on our tile get invalidated, make sure to also invalidate all our cached ones
        capabilityCache.invalidateAll();
    }
}