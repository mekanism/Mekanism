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
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.Coord4D;
import mekanism.api.DataHandlerUtils;
import mekanism.api.MekanismAPI;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NonNull;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.inventory.AutomationType;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.api.math.FloatingLong;
import mekanism.api.providers.IRobitSkinProvider;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.ItemStackToItemStackCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.api.robit.IRobit;
import mekanism.api.robit.RobitSkin;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.config.MekanismConfig;
import mekanism.common.entity.ai.RobitAIFollow;
import mekanism.common.entity.ai.RobitAIPickup;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.inventory.container.sync.SyncableFloatingLong;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.item.ItemRobit;
import mekanism.common.lib.security.ISecurityObject;
import mekanism.common.lib.security.SecurityMode;
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
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.block.PortalInfo;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.datasync.IDataSerializer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.TicketType;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.items.ItemHandlerHelper;

//TODO: When Galacticraft gets ported make it so the robit can "breath" without a mask
public class EntityRobit extends CreatureEntity implements IRobit, IMekanismInventory, ISustainedInventory, ISecurityObject, IMekanismStrictEnergyHandler,
      ItemRecipeLookupHandler<ItemStackToItemStackRecipe> {

    public static final ModelProperty<ResourceLocation> SKIN_TEXTURE_PROPERTY = new ModelProperty<>();

    private static <T> DataParameter<T> define(IDataSerializer<T> dataSerializer) {
        return EntityDataManager.defineId(EntityRobit.class, dataSerializer);
    }

    private static final TicketType<Integer> ROBIT_CHUNK_UNLOAD = TicketType.create("robit_chunk_unload", Integer::compareTo, 20);
    private static final DataParameter<UUID> OWNER_UUID = define(MekanismDataSerializers.UUID.getSerializer());
    private static final DataParameter<String> OWNER_NAME = define(DataSerializers.STRING);
    private static final DataParameter<Boolean> FOLLOW = define(DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> DROP_PICKUP = define(DataSerializers.BOOLEAN);
    private static final DataParameter<RobitSkin> SKIN = define(MekanismDataSerializers.<RobitSkin>getRegistryEntrySerializer());
    public static final FloatingLong MAX_ENERGY = FloatingLong.createConst(100_000);
    private static final FloatingLong DISTANCE_MULTIPLIER = FloatingLong.createConst(1.5);
    //TODO: Note the robit smelts at double normal speed, we may want to make this configurable
    //TODO: Allow for upgrades in the robit?
    private static final int ticksRequired = 100;
    private SecurityMode securityMode = SecurityMode.PUBLIC;
    public Coord4D homeLocation;
    private int lastTextureUpdate;
    private int textureIndex;
    private int progress;

    /**
     * The players currently using this robit.
     */
    private final Set<PlayerEntity> playersUsing = new ObjectOpenHashSet<>();

    private final RecipeCacheLookupMonitor<ItemStackToItemStackRecipe> recipeCacheLookupMonitor;

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

    public EntityRobit(EntityType<EntityRobit> type, World world) {
        super(type, world);
        getNavigation().setCanFloat(false);
        setCustomNameVisible(true);
        recipeCacheLookupMonitor = new RecipeCacheLookupMonitor<>(this);
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

        mainContainerSlots = Collections.singletonList(energySlot);
        smeltingContainerSlots = Arrays.asList(smeltingInputSlot, smeltingOutputSlot);

        inputHandler = InputHelper.getInputHandler(smeltingInputSlot);
        outputHandler = OutputHelper.getOutputHandler(smeltingOutputSlot);
    }

    @Nullable
    public static EntityRobit create(World world, double x, double y, double z) {
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
        goalSelector.addGoal(3, new LookAtGoal(this, PlayerEntity.class, 8));
        goalSelector.addGoal(3, new LookRandomlyGoal(this));
        goalSelector.addGoal(4, new SwimGoal(this));
    }

    public static AttributeModifierMap.MutableAttribute getDefaultAttributes() {
        return MobEntity.createMobAttributes().add(Attributes.MAX_HEALTH, 1.0D).add(Attributes.MOVEMENT_SPEED, 0.3F);
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
            ((ServerWorld) level).getChunkSource().addRegionTicket(ROBIT_CHUNK_UNLOAD, new ChunkPos(blockPosition()), 2, getId());
        }
        super.onRemovedFromWorld();
    }

    @Override
    public void baseTick() {
        if (!level.isClientSide) {
            if (getFollowing()) {
                PlayerEntity owner = getOwner();
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
                remove();
                return;
            }

            if (tickCount % 20 == 0) {
                World serverWorld = ServerLifecycleHooks.getCurrentServer().getLevel(homeLocation.dimension);
                BlockPos homePos = homeLocation.getPos();
                if (WorldUtils.isBlockLoaded(serverWorld, homePos) && WorldUtils.getTileEntity(TileEntityChargepad.class, serverWorld, homePos) == null) {
                    drop();
                    remove();
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
                            item.remove();
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
                                item.remove();
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
            ServerWorld newWorld = ((ServerWorld) this.level).getServer().getLevel(homeLocation.dimension);
            if (newWorld != null) {
                Vector3d destination = new Vector3d(homeLocation.getX() + 0.5, homeLocation.getY() + 0.3, homeLocation.getZ() + 0.5);
                changeDimension(newWorld, new ITeleporter() {
                    @Override
                    public Entity placeEntity(Entity entity, ServerWorld currentWorld, ServerWorld destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
                        return repositionEntity.apply(false);
                    }

                    @Override
                    public PortalInfo getPortalInfo(Entity entity, ServerWorld destWorld, Function<ServerWorld, PortalInfo> defaultPortalInfo) {
                        return new PortalInfo(destination, Vector3d.ZERO, entity.yRot, entity.xRot);
                    }

                    @Override
                    public boolean playTeleportSound(ServerPlayerEntity player, ServerWorld sourceWorld, ServerWorld destWorld) {
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
    public ActionResultType interactAt(PlayerEntity player, @Nonnull Vector3d vec, @Nonnull Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (!stack.isEmpty() && stack.getItem() instanceof ItemConfigurator) {
                if (!level.isClientSide) {
                    drop();
                }
                remove();
                player.swing(hand);
                return ActionResultType.SUCCESS;
            }
        } else {
            if (!SecurityUtils.canAccess(player, this)) {
                if (!level.isClientSide) {
                    SecurityUtils.displayNoAccess(player);
                }
                return ActionResultType.FAIL;
            }
            if (!level.isClientSide) {
                INamedContainerProvider provider = MekanismContainerTypes.MAIN_ROBIT.getProvider(MekanismLang.ROBIT, this);
                if (provider != null) {
                    //Validate the provider isn't null, it shouldn't be but just in case
                    NetworkHooks.openGui((ServerPlayerEntity) player, provider, buf -> buf.writeVarInt(getId()));
                }
            }
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
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
        item.setOwnerUUID(stack, getOwnerUUID());
        item.setSecurity(stack, getSecurityMode());
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
    public void addAdditionalSaveData(@Nonnull CompoundNBT nbtTags) {
        super.addAdditionalSaveData(nbtTags);
        nbtTags.putUUID(NBTConstants.OWNER_UUID, getOwnerUUID());
        nbtTags.putInt(NBTConstants.SECURITY_MODE, securityMode.ordinal());
        nbtTags.putBoolean(NBTConstants.FOLLOW, getFollowing());
        nbtTags.putBoolean(NBTConstants.PICKUP_DROPS, getDropPickup());
        if (homeLocation != null) {
            homeLocation.write(nbtTags);
        }
        nbtTags.put(NBTConstants.ITEMS, DataHandlerUtils.writeContainers(getInventorySlots(null)));
        nbtTags.put(NBTConstants.ENERGY_CONTAINERS, DataHandlerUtils.writeContainers(getEnergyContainers(null)));
        nbtTags.putInt(NBTConstants.PROGRESS, getOperatingTicks());
        nbtTags.putString(NBTConstants.SKIN, getSkin().getRegistryName().toString());
    }

    @Override
    public void readAdditionalSaveData(@Nonnull CompoundNBT nbtTags) {
        super.readAdditionalSaveData(nbtTags);
        NBTUtils.setUUIDIfPresent(nbtTags, NBTConstants.OWNER_UUID, this::setOwnerUUID);
        NBTUtils.setEnumIfPresent(nbtTags, NBTConstants.SECURITY_MODE, SecurityMode::byIndexStatic, mode -> securityMode = mode);
        setFollowing(nbtTags.getBoolean(NBTConstants.FOLLOW));
        setDropPickup(nbtTags.getBoolean(NBTConstants.PICKUP_DROPS));
        homeLocation = Coord4D.read(nbtTags);
        DataHandlerUtils.readContainers(getInventorySlots(null), nbtTags.getList(NBTConstants.ITEMS, NBT.TAG_COMPOUND));
        DataHandlerUtils.readContainers(getEnergyContainers(null), nbtTags.getList(NBTConstants.ENERGY_CONTAINERS, NBT.TAG_COMPOUND));
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
        if (damageSource == DamageSource.FALL) {
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

    public PlayerEntity getOwner() {
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

    @Override
    public SecurityMode getSecurityMode() {
        return securityMode;
    }

    @Override
    public void setSecurityMode(SecurityMode mode) {
        if (securityMode != mode) {
            SecurityMode old = securityMode;
            securityMode = mode;
            onSecurityChanged(old, securityMode);
        }
    }

    @Override
    public void onSecurityChanged(SecurityMode old, SecurityMode mode) {
        //If the mode changed and the new security mode is more restrictive than the old one
        if (old != mode && (old == SecurityMode.PUBLIC || (old == SecurityMode.TRUSTED && mode == SecurityMode.PRIVATE))) {
            //and there are players using this tile
            if (!playersUsing.isEmpty()) {
                //then double check that all the players are actually supposed to be able to access the GUI
                for (PlayerEntity player : new ObjectOpenHashSet<>(playersUsing)) {
                    if (!SecurityUtils.canAccess(player, this)) {
                        //and if they can't then boot them out
                        player.closeContainer();
                    }
                }
            }
        }
    }

    public void open(PlayerEntity player) {
        playersUsing.add(player);
    }

    public void close(PlayerEntity player) {
        playersUsing.remove(player);
    }

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
    public void setInventory(ListNBT nbtTags, Object... data) {
        if (nbtTags != null && !nbtTags.isEmpty()) {
            DataHandlerUtils.readContainers(getInventorySlots(null), nbtTags);
        }
    }

    @Override
    public ListNBT getInventory(Object... data) {
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
    public List<IInventorySlot> getContainerInventorySlots(@Nonnull ContainerType<?> containerType) {
        if (!hasInventory()) {
            return Collections.emptyList();
        } else if (containerType == MekanismContainerTypes.INVENTORY_ROBIT.getContainerType()) {
            return inventoryContainerSlots;
        } else if (containerType == MekanismContainerTypes.MAIN_ROBIT.getContainerType()) {
            return mainContainerSlots;
        } else if (containerType == MekanismContainerTypes.SMELTING_ROBIT.getContainerType()) {
            return smeltingContainerSlots;
        }
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public MekanismRecipeType<ItemStackToItemStackRecipe, SingleItem<ItemStackToItemStackRecipe>> getRecipeType() {
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
    public ItemStack getPickedResult(RayTraceResult target) {
        return getItemVariant();
    }

    @Nonnull
    @Override
    public CachedRecipe<ItemStackToItemStackRecipe> createNewCachedRecipe(@Nonnull ItemStackToItemStackRecipe recipe, int cacheIndex) {
        //TODO: Make a robit specific smelting energy usage config
        return new ItemStackToItemStackCachedRecipe(recipe, inputHandler, outputHandler)
              .setEnergyRequirements(MekanismConfig.usage.energizedSmelter, energyContainer)
              .setRequiredTicks(() -> ticksRequired)
              .setOnFinish(this::onContentsChanged)
              .setOperatingTicksChanged(operatingTicks -> progress = operatingTicks);
    }

    public void addContainerTrackers(MekanismContainer container) {
        ContainerType<?> containerType = container.getType();
        container.track(SyncableEnum.create(SecurityMode::byIndexStatic, SecurityMode.PUBLIC, this::getSecurityMode, this::setSecurityMode));
        if (containerType == MekanismContainerTypes.MAIN_ROBIT.getContainerType()) {
            container.track(SyncableFloatingLong.create(energyContainer::getEnergy, energyContainer::setEnergy));
        } else if (containerType == MekanismContainerTypes.SMELTING_ROBIT.getContainerType()) {
            container.track(SyncableInt.create(() -> progress, value -> progress = value));
        }
    }

    public IWorldPosCallable getWorldPosCallable() {
        return new IWorldPosCallable() {
            @Nonnull
            @Override
            public <T> Optional<T> evaluate(@Nonnull BiFunction<World, BlockPos, T> worldBlockPosTBiFunction) {
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
    public boolean setSkin(@Nonnull IRobitSkinProvider skinProvider, @Nullable PlayerEntity player) {
        Objects.requireNonNull(skinProvider, "Robit skin cannot be null.");
        RobitSkin skin = skinProvider.getSkin();
        if (player != null) {
            if (!SecurityUtils.canAccess(player, this) || !skin.isUnlocked(player)) {
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
}