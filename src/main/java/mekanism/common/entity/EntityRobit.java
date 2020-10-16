package mekanism.common.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.Coord4D;
import mekanism.api.DataHandlerUtils;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NonNull;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.inventory.AutomationType;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.ICachedRecipeHolder;
import mekanism.api.recipes.cache.ItemStackToItemStackCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.common.CommonWorldTickHandler;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.config.MekanismConfig;
import mekanism.common.entity.ai.RobitAIFollow;
import mekanism.common.entity.ai.RobitAIPickup;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.entity.robit.MainRobitContainer;
import mekanism.common.inventory.container.sync.SyncableFloatingLong;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.item.ItemRobit;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.registries.MekanismDamageSource;
import mekanism.common.registries.MekanismEntityTypes;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tile.TileEntityChargepad;
import mekanism.common.tile.component.TileComponentChunkLoader;
import mekanism.common.tile.interfaces.ISustainedInventory;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
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
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.TicketType;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.items.ItemHandlerHelper;

//TODO: When Galacticraft gets ported make it so the robit can "breath" without a mask
public class EntityRobit extends CreatureEntity implements IMekanismInventory, ISustainedInventory, ICachedRecipeHolder<ItemStackToItemStackRecipe>,
      IMekanismStrictEnergyHandler {

    private static final TicketType<Integer> ROBIT_CHUNK_UNLOAD = TicketType.create("robit_chunk_unload", Integer::compareTo, 20);
    private static final DataParameter<String> OWNER_UUID = EntityDataManager.createKey(EntityRobit.class, DataSerializers.STRING);
    private static final DataParameter<String> OWNER_NAME = EntityDataManager.createKey(EntityRobit.class, DataSerializers.STRING);
    private static final DataParameter<Boolean> FOLLOW = EntityDataManager.createKey(EntityRobit.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> DROP_PICKUP = EntityDataManager.createKey(EntityRobit.class, DataSerializers.BOOLEAN);
    public static final FloatingLong MAX_ENERGY = FloatingLong.createConst(100_000);
    private static final FloatingLong DISTANCE_MULTIPLIER = FloatingLong.createConst(1.5);
    //TODO: Note the robit smelts at double normal speed, we may want to make this configurable
    //TODO: Allow for upgrades in the robit?
    private static final int ticksRequired = 100;
    public Coord4D homeLocation;
    public boolean texTick;
    private int progress;

    private CachedRecipe<ItemStackToItemStackRecipe> cachedRecipe = null;

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
        getNavigator().setCanSwim(false);
        setCustomNameVisible(true);
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
        inventorySlots.add(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getEntityWorld, this, 153, 17));
        inventorySlots.add(smeltingInputSlot = InputInventorySlot.at(item -> containsRecipe(recipe -> recipe.getInput().testType(item)), this, 51, 35));
        //TODO: Previously used FurnaceResultSlot, check if we need to replicate any special logic it had (like if it had xp logic or something)
        // Yes we probably do want this to allow for experience. Though maybe we should allow for experience for all our recipes/smelting recipes? V10
        inventorySlots.add(smeltingOutputSlot = OutputInventorySlot.at(this, 116, 35));

        mainContainerSlots = Collections.singletonList(energySlot);
        smeltingContainerSlots = Arrays.asList(smeltingInputSlot, smeltingOutputSlot);

        inputHandler = InputHelper.getInputHandler(smeltingInputSlot);
        outputHandler = OutputHelper.getOutputHandler(smeltingOutputSlot);
    }

    public EntityRobit(World world, double x, double y, double z) {
        this(MekanismEntityTypes.ROBIT.getEntityType(), world);
        setPosition(x, y, z);
        prevPosX = x;
        prevPosY = y;
        prevPosZ = z;
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
        return MobEntity.func_233666_p_().createMutableAttribute(Attributes.MAX_HEALTH, 1.0D).createMutableAttribute(Attributes.MOVEMENT_SPEED, 0.3F);
    }

    @Override
    public boolean canDespawn(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected void registerData() {
        super.registerData();
        dataManager.register(OWNER_UUID, "");
        dataManager.register(OWNER_NAME, "");
        dataManager.register(FOLLOW, false);
        dataManager.register(DROP_PICKUP, false);
    }

    private FloatingLong getRoundedTravelEnergy() {
        return DISTANCE_MULTIPLIER.multiply(Math.sqrt(getDistanceSq(prevPosX, prevPosY, prevPosZ)));
    }

    @Override
    public void onRemovedFromWorld() {
        if (world != null && !world.isRemote && getFollowing() && getOwner() != null) {
            //If this robit is currently following its owner and is being removed from the world (due to chunk unloading)
            // register a ticket that loads the chunk for a second, so that it has time to have its following check run again
            // (as it runs every 10 ticks, half a second), and then teleport to the owner.
            ((ServerWorld) world).getChunkProvider().registerTicket(ROBIT_CHUNK_UNLOAD, new ChunkPos(getPosition()), TileComponentChunkLoader.TICKET_DISTANCE, getEntityId());
        }
        super.onRemovedFromWorld();
    }

    @Override
    public void baseTick() {
        if (!world.isRemote) {
            if (getFollowing()) {
                PlayerEntity owner = getOwner();
                if (owner != null && getDistanceSq(owner) > 4 && !getNavigator().noPath() && !energyContainer.isEmpty()) {
                    energyContainer.extract(getRoundedTravelEnergy(), Action.EXECUTE, AutomationType.INTERNAL);
                }
            }
        }

        super.baseTick();

        if (!world.isRemote) {
            if (getDropPickup()) {
                collectItems();
            }
            if (homeLocation == null) {
                remove();
                return;
            }

            if (ticksExisted % 20 == 0) {
                World serverWorld = ServerLifecycleHooks.getCurrentServer().getWorld(homeLocation.dimension);
                BlockPos homePos = homeLocation.getPos();
                if (serverWorld.isBlockPresent(homePos)) {
                    if (MekanismUtils.getTileEntity(TileEntityChargepad.class, serverWorld, homePos) == null) {
                        drop();
                        remove();
                    }
                }
            }

            if (energyContainer.isEmpty() && !isOnChargepad()) {
                goHome();
            }

            energySlot.fillContainerOrConvert();
            cachedRecipe = getUpdatedCache(0);
            if (cachedRecipe != null) {
                cachedRecipe.process();
            }
        }
    }

    private void collectItems() {
        List<ItemEntity> items = world.getEntitiesWithinAABB(ItemEntity.class, getBoundingBox().grow(1.5, 1.5, 1.5));
        if (!items.isEmpty()) {
            for (ItemEntity item : items) {
                if (item.cannotPickup() || item.getItem().getItem() instanceof ItemRobit || !item.isAlive()) {
                    continue;
                }
                for (IInventorySlot slot : inventoryContainerSlots) {
                    if (slot.isEmpty()) {
                        slot.setStack(item.getItem());
                        onItemPickup(item, item.getItem().getCount());
                        item.remove();
                        playSound(SoundEvents.ENTITY_ITEM_PICKUP, 1, ((rand.nextFloat() - rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                        break;
                    }
                    ItemStack itemStack = slot.getStack();
                    int maxSize = slot.getLimit(itemStack);
                    if (ItemHandlerHelper.canItemStacksStack(itemStack, item.getItem()) && itemStack.getCount() < maxSize) {
                        int needed = maxSize - itemStack.getCount();
                        int toAdd = Math.min(needed, item.getItem().getCount());
                        MekanismUtils.logMismatchedStackSize(slot.growStack(toAdd, Action.EXECUTE), toAdd);
                        item.getItem().shrink(toAdd);
                        onItemPickup(item, toAdd);
                        if (item.getItem().isEmpty()) {
                            item.remove();
                        }
                        playSound(SoundEvents.ENTITY_ITEM_PICKUP, 1, ((rand.nextFloat() - rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                        break;
                    }
                }
            }
        }
    }

    public void goHome() {
        if (world.isRemote()) {
            return;
        }
        setFollowing(false);
        if (world.getDimensionKey() == homeLocation.dimension) {
            setMotion(0, 0, 0);
            setPositionAndUpdate(homeLocation.getX() + 0.5, homeLocation.getY() + 0.3, homeLocation.getZ() + 0.5);
        } else {
            ServerWorld newWorld = ((ServerWorld) this.world).getServer().getWorld(homeLocation.dimension);
            if (newWorld != null) {
                Vector3d destination = new Vector3d(homeLocation.getX() +  0.5, homeLocation.getY() +  0.3, homeLocation.getZ() +  0.5);
                changeDimension(newWorld, new ITeleporter() {
                    @Override
                    public Entity placeEntity(Entity entity, ServerWorld currentWorld, ServerWorld destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
                        return repositionEntity.apply(false);
                    }

                    @Override
                    public PortalInfo getPortalInfo(Entity entity, ServerWorld destWorld, Function<ServerWorld, PortalInfo> defaultPortalInfo) {
                        return new PortalInfo(destination, Vector3d.ZERO, entity.rotationYaw, entity.rotationPitch);
                    }
                });
            }
        }
    }

    private boolean isOnChargepad() {
        return MekanismUtils.getTileEntity(TileEntityChargepad.class, world, getPosition()) != null;
    }

    @Nonnull
    @Override
    public ActionResultType applyPlayerInteraction(PlayerEntity player, @Nonnull Vector3d vec, @Nonnull Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (player.isSneaking()) {
            if (!stack.isEmpty() && stack.getItem() instanceof ItemConfigurator) {
                if (!world.isRemote) {
                    drop();
                }
                remove();
                player.swingArm(hand);
                return ActionResultType.SUCCESS;
            }
        } else {
            if (!world.isRemote) {
                NetworkHooks.openGui((ServerPlayerEntity) player, new ContainerProvider(MekanismLang.ROBIT, (i, inv, p) -> new MainRobitContainer(i, inv, this)),
                      buf -> buf.writeVarInt(getEntityId()));
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
        return stack;
    }

    public void drop() {
        //TODO: Move this to loot table?
        ItemEntity entityItem = new ItemEntity(world, getPosX(), getPosY() + 0.3, getPosZ(), getItemVariant());
        entityItem.setMotion(0, rand.nextGaussian() * 0.05F + 0.2F, 0);
        world.addEntity(entityItem);
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
    public void writeAdditional(@Nonnull CompoundNBT nbtTags) {
        super.writeAdditional(nbtTags);
        if (getOwnerUUID() != null) {
            nbtTags.putUniqueId(NBTConstants.OWNER_UUID, getOwnerUUID());
        }
        nbtTags.putBoolean(NBTConstants.FOLLOW, getFollowing());
        nbtTags.putBoolean(NBTConstants.PICKUP_DROPS, getDropPickup());
        if (homeLocation != null) {
            homeLocation.write(nbtTags);
        }
        nbtTags.put(NBTConstants.ITEMS, DataHandlerUtils.writeContainers(getInventorySlots(null)));
        nbtTags.put(NBTConstants.ENERGY_CONTAINERS, DataHandlerUtils.writeContainers(getEnergyContainers(null)));
        nbtTags.putInt(NBTConstants.PROGRESS, getOperatingTicks());
    }

    @Override
    public void readAdditional(@Nonnull CompoundNBT nbtTags) {
        super.readAdditional(nbtTags);
        NBTUtils.setUUIDIfPresent(nbtTags, NBTConstants.OWNER_UUID, this::setOwnerUUID);
        setFollowing(nbtTags.getBoolean(NBTConstants.FOLLOW));
        setDropPickup(nbtTags.getBoolean(NBTConstants.PICKUP_DROPS));
        homeLocation = Coord4D.read(nbtTags);
        DataHandlerUtils.readContainers(getInventorySlots(null), nbtTags.getList(NBTConstants.ITEMS, NBT.TAG_COMPOUND));
        DataHandlerUtils.readContainers(getEnergyContainers(null), nbtTags.getList(NBTConstants.ENERGY_CONTAINERS, NBT.TAG_COMPOUND));
        progress = nbtTags.getInt(NBTConstants.PROGRESS);
    }

    @Override
    public boolean isInvulnerableTo(@Nonnull DamageSource source) {
        return source == MekanismDamageSource.RADIATION || super.isInvulnerableTo(source);
    }

    @Override
    protected void damageEntity(@Nonnull DamageSource damageSource, float amount) {
        amount = ForgeHooks.onLivingHurt(this, damageSource, amount);
        if (amount <= 0) {
            return;
        }
        amount = applyArmorCalculations(damageSource, amount);
        amount = applyPotionDamageCalculations(damageSource, amount);
        float j = getHealth();

        energyContainer.extract(FloatingLong.create(1_000 * amount), Action.EXECUTE, AutomationType.INTERNAL);
        getCombatTracker().trackDamage(damageSource, j, amount);
    }

    @Override
    protected void onDeathUpdate() {
    }

    public void setHome(Coord4D home) {
        homeLocation = home;
    }

    @Override
    public boolean canBePushed() {
        return !energyContainer.isEmpty();
    }

    public PlayerEntity getOwner() {
        return world.getPlayerByUuid(getOwnerUUID());
    }

    public String getOwnerName() {
        return dataManager.get(OWNER_NAME);
    }

    public UUID getOwnerUUID() {
        return UUID.fromString(dataManager.get(OWNER_UUID));
    }

    public void setOwnerUUID(UUID uuid) {
        dataManager.set(OWNER_UUID, uuid.toString());
        dataManager.set(OWNER_NAME, MekanismUtils.getLastKnownUsername(uuid));
    }

    public boolean getFollowing() {
        return dataManager.get(FOLLOW);
    }

    public void setFollowing(boolean follow) {
        dataManager.set(FOLLOW, follow);
    }

    public boolean getDropPickup() {
        return dataManager.get(DROP_PICKUP);
    }

    public void setDropPickup(boolean pickup) {
        dataManager.set(DROP_PICKUP, pickup);
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
    public MekanismRecipeType<ItemStackToItemStackRecipe> getRecipeType() {
        return MekanismRecipeType.SMELTING;
    }

    public boolean containsRecipe(@Nonnull Predicate<ItemStackToItemStackRecipe> matchCriteria) {
        return getRecipeType().contains(getEntityWorld(), matchCriteria);
    }

    @Nullable
    public ItemStackToItemStackRecipe findFirstRecipe(@Nonnull Predicate<ItemStackToItemStackRecipe> matchCriteria) {
        return getRecipeType().findFirst(getEntityWorld(), matchCriteria);
    }

    @Nullable
    @Override
    public CachedRecipe<ItemStackToItemStackRecipe> getCachedRecipe(int cacheIndex) {
        return cachedRecipe;
    }

    @Nullable
    @Override
    public ItemStackToItemStackRecipe getRecipe(int cacheIndex) {
        ItemStack stack = inputHandler.getInput();
        return stack.isEmpty() ? null : findFirstRecipe(recipe -> recipe.test(stack));
    }

    public IEnergyContainer getEnergyContainer() {
        return energyContainer;
    }

    @Override
    public boolean invalidateCache() {
        return CommonWorldTickHandler.flushTagAndRecipeCaches;
    }

    @Override
    public ItemStack getPickedResult(RayTraceResult target) {
        return getItemVariant();
    }

    @Nullable
    @Override
    public CachedRecipe<ItemStackToItemStackRecipe> createNewCachedRecipe(@Nonnull ItemStackToItemStackRecipe recipe, int cacheIndex) {
        //TODO: Make a robit specific smelting energy usage config
        return new ItemStackToItemStackCachedRecipe(recipe, inputHandler, outputHandler)
              .setEnergyRequirements(MekanismConfig.usage.energizedSmelter, energyContainer)
              .setRequiredTicks(() -> ticksRequired)
              .setOnFinish(this::onContentsChanged)
              .setOperatingTicksChanged(operatingTicks -> progress = operatingTicks);
    }

    public void addContainerTrackers(@Nonnull ContainerType<?> containerType, MekanismContainer container) {
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
            public <T> Optional<T> apply(@Nonnull BiFunction<World, BlockPos, T> worldBlockPosTBiFunction) {
                //Note: We use an anonymous class implementation rather than using IWorldPosCallable.of, so that if the robit moves
                // this uses the proper updated position
                return Optional.ofNullable(worldBlockPosTBiFunction.apply(getEntityWorld(), getPosition()));
            }
        };
    }
}