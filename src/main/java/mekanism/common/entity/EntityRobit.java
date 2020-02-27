package mekanism.common.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.Coord4D;
import mekanism.api.annotations.NonNull;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.ICachedRecipeHolder;
import mekanism.api.recipes.cache.ItemStackToItemStackCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.api.sustained.ISustainedInventory;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.entity.ai.RobitAIFollow;
import mekanism.common.entity.ai.RobitAIPickup;
import mekanism.common.inventory.container.ITrackableContainer;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.item.ItemRobit;
import mekanism.common.network.PacketGuiButtonPress;
import mekanism.common.network.PacketGuiButtonPress.ClickedEntityButton;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.registries.MekanismEntityTypes;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tile.TileEntityChargepad;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.items.ItemHandlerHelper;

//TODO: When Galaticraft gets ported make it so the robit can "breath" without a mask
public class EntityRobit extends CreatureEntity implements IMekanismInventory, ISustainedInventory, IStrictEnergyStorage, ICachedRecipeHolder<ItemStackToItemStackRecipe>,
      ITrackableContainer {

    private static final DataParameter<Float> ELECTRICITY = EntityDataManager.createKey(EntityRobit.class, DataSerializers.FLOAT);
    private static final DataParameter<String> OWNER_UUID = EntityDataManager.createKey(EntityRobit.class, DataSerializers.STRING);
    private static final DataParameter<String> OWNER_NAME = EntityDataManager.createKey(EntityRobit.class, DataSerializers.STRING);
    private static final DataParameter<Boolean> FOLLOW = EntityDataManager.createKey(EntityRobit.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> DROP_PICKUP = EntityDataManager.createKey(EntityRobit.class, DataSerializers.BOOLEAN);
    public double MAX_ELECTRICITY = 100_000;
    public Coord4D homeLocation;
    public boolean texTick;
    private int progress;
    //TODO: Note the robit smelts at double normal speed, we may want to make this configurable/
    //TODO: Allow for upgrades in the robit?
    private final int ticksRequired = 100;

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

    public EntityRobit(EntityType<EntityRobit> type, World world) {
        super(type, world);
        getNavigator().setCanSwim(false);
        setCustomNameVisible(true);
        //TODO: Go through all this and clean it up properly
        inventorySlots = new ArrayList<>();
        inventoryContainerSlots = new ArrayList<>();
        for (int slotY = 0; slotY < 3; slotY++) {
            for (int slotX = 0; slotX < 9; slotX++) {
                IInventorySlot slot = BasicInventorySlot.at(this, 8 + slotX * 18, 18 + slotY * 18);
                inventorySlots.add(slot);
                inventoryContainerSlots.add(slot);
            }
        }
        inventorySlots.add(energySlot = EnergyInventorySlot.discharge(this, 153, 17));
        inventorySlots.add(smeltingInputSlot = InputInventorySlot.at(item -> containsRecipe(recipe -> recipe.getInput().testType(item)), this, 51, 35));
        //TODO: Previously used FurnaceResultSlot, check if we need to replicate any special logic it had (like if it had xp logic or something)
        // Yes we probably do want this to allow for experience. Though maybe we should allow for experience for all our recipes/smelting recipes?
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

    @Nonnull
    @Override
    public GroundPathNavigator getNavigator() {
        return (GroundPathNavigator) navigator;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(1, new RobitAIPickup(this, 1.0F));
        goalSelector.addGoal(2, new RobitAIFollow(this, 1.0F, 4.0F, 2.0F));
        goalSelector.addGoal(3, new LookAtGoal(this, PlayerEntity.class, 8.0F));
        goalSelector.addGoal(3, new LookRandomlyGoal(this));
        goalSelector.addGoal(4, new SwimGoal(this));
    }

    @Override
    protected void registerAttributes() {
        super.registerAttributes();
        getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3);
        getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(1);
    }

    @Override
    public boolean canDespawn(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected void registerData() {
        super.registerData();
        dataManager.register(ELECTRICITY, 0F);
        dataManager.register(OWNER_UUID, "");
        dataManager.register(OWNER_NAME, "");
        dataManager.register(FOLLOW, false);
        dataManager.register(DROP_PICKUP, false);
    }

    public double getRoundedTravelEnergy() {
        double distance = Math.sqrt(getDistanceSq(prevPosX, prevPosY, prevPosZ));
        return new BigDecimal(distance * 1.5).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
    }

    @Override
    public void baseTick() {
        if (!world.isRemote) {
            if (getFollowing() && getOwner() != null && getDistanceSq(getOwner()) > 4 && !getNavigator().noPath() && getEnergy() > 0) {
                setEnergy(getEnergy() - getRoundedTravelEnergy());
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
                if (serverWorld.isBlockLoaded(homePos)) {
                    if (MekanismUtils.getTileEntity(TileEntityChargepad.class, serverWorld, homePos) == null) {
                        drop();
                        remove();
                    }
                }
            }

            if (getEnergy() == 0 && !isOnChargepad()) {
                goHome();
            }

            energySlot.discharge(this);
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
                for (int i = 0; i < 27; i++) {
                    IInventorySlot slot = inventorySlots.get(i);
                    if (slot.isEmpty()) {
                        slot.setStack(item.getItem());
                        onItemPickup(item, item.getItem().getCount());
                        item.remove();
                        playSound(SoundEvents.ENTITY_ITEM_PICKUP, 1.0F, ((rand.nextFloat() - rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                        break;
                    }
                    ItemStack itemStack = slot.getStack();
                    int maxSize = slot.getLimit(itemStack);
                    if (ItemHandlerHelper.canItemStacksStack(itemStack, item.getItem()) && itemStack.getCount() < maxSize) {
                        int needed = maxSize - itemStack.getCount();
                        int toAdd = Math.min(needed, item.getItem().getCount());
                        if (slot.growStack(toAdd, Action.EXECUTE) != toAdd) {
                            //TODO: Print warning that something went wrong
                        }
                        item.getItem().shrink(toAdd);
                        onItemPickup(item, toAdd);
                        if (item.getItem().isEmpty()) {
                            item.remove();
                        }
                        playSound(SoundEvents.ENTITY_ITEM_PICKUP, 1.0F, ((rand.nextFloat() - rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                        break;
                    }
                }
            }
        }
    }

    public void goHome() {
        setFollowing(false);
        if (dimension != homeLocation.dimension) {
            changeDimension(homeLocation.dimension, new ITeleporter() {
                @Override
                public Entity placeEntity(Entity entity, ServerWorld currentWorld, ServerWorld destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
                    Entity repositionedEntity = repositionEntity.apply(false);
                    repositionedEntity.setPositionAndUpdate(homeLocation.x + 0.5, homeLocation.y + 0.3, homeLocation.z + 0.5);
                    return repositionedEntity;
                }
            });
        } else {
            setPositionAndUpdate(homeLocation.x + 0.5, homeLocation.y + 0.3, homeLocation.z + 0.5);
        }
        setMotion(0, 0, 0);
    }

    private boolean isOnChargepad() {
        return MekanismUtils.getTileEntity(TileEntityChargepad.class, world, getPosition()) != null;
    }

    @Nonnull
    @Override
    public ActionResultType applyPlayerInteraction(PlayerEntity player, Vec3d vec, Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (player.isShiftKeyDown()) {
            if (!stack.isEmpty() && stack.getItem() instanceof ItemConfigurator) {
                if (!world.isRemote) {
                    drop();
                }
                remove();
                player.swingArm(hand);
                return ActionResultType.SUCCESS;
            }
        } else {
            Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedEntityButton.ROBIT_MAIN, getEntityId()));
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

    public void drop() {
        //TODO: Move this to loot table?
        ItemEntity entityItem = new ItemEntity(world, getPosX(), getPosY() + 0.3, getPosZ(), MekanismItems.ROBIT.getItemStack());
        ItemRobit item = (ItemRobit) entityItem.getItem().getItem();
        item.setEnergy(entityItem.getItem(), getEnergy());
        item.setInventory(((ISustainedInventory) this).getInventory(), entityItem.getItem());
        item.setName(entityItem.getItem(), getName().getFormattedText());
        entityItem.setMotion(0, rand.nextGaussian() * 0.05F + 0.2F, 0);
        world.addEntity(entityItem);
    }

    public double getScaledProgress() {
        return (double) getOperatingTicks() / (double) ticksRequired;
    }

    public int getOperatingTicks() {
        if (getEntityWorld().isRemote()) {
            return progress;
        }
        if (cachedRecipe == null) {
            return 0;
        }
        return cachedRecipe.getOperatingTicks();
    }

    @Override
    public void writeAdditional(CompoundNBT nbtTags) {
        super.writeAdditional(nbtTags);
        nbtTags.putDouble("electricityStored", getEnergy());
        //TODO: Is this necessary or is it handled by the main entity class
        //nbtTags.putString("name", getName());
        if (getOwnerUUID() != null) {
            nbtTags.putString("ownerUUID", getOwnerUUID().toString());
        }
        nbtTags.putBoolean("follow", getFollowing());
        nbtTags.putBoolean("dropPickup", getDropPickup());
        if (homeLocation != null) {
            homeLocation.write(nbtTags);
        }
        ListNBT tagList = new ListNBT();
        for (int slotCount = 0; slotCount < inventorySlots.size(); slotCount++) {
            CompoundNBT tagCompound = inventorySlots.get(slotCount).serializeNBT();
            if (!tagCompound.isEmpty()) {
                tagCompound.putByte("Slot", (byte) slotCount);
                tagList.add(tagCompound);
            }
        }
        nbtTags.put("Items", tagList);
    }

    @Override
    public void readAdditional(CompoundNBT nbtTags) {
        super.readAdditional(nbtTags);
        setEnergy(nbtTags.getDouble("electricityStored"));
        //TODO: Is this necessary or is it handled by the main entity class
        //setCustomNameTag(nbtTags.getString("name"));
        if (nbtTags.contains("ownerUUID")) {
            setOwnerUUID(UUID.fromString(nbtTags.getString("ownerUUID")));
        }
        setFollowing(nbtTags.getBoolean("follow"));
        setDropPickup(nbtTags.getBoolean("dropPickup"));
        homeLocation = Coord4D.read(nbtTags);
        ListNBT tagList = nbtTags.getList("Items", Constants.NBT.TAG_COMPOUND);
        List<IInventorySlot> inventorySlots = getInventorySlots((Direction) null);
        int size = inventorySlots.size();
        for (int tagCount = 0; tagCount < tagList.size(); tagCount++) {
            CompoundNBT tagCompound = tagList.getCompound(tagCount);
            byte slotID = tagCompound.getByte("Slot");
            if (slotID >= 0 && slotID < size) {
                inventorySlots.get(slotID).deserializeNBT(tagCompound);
            }
        }
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
        setEnergy(Math.max(0, getEnergy() - (amount * 1000)));
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
        return getEnergy() > 0;
    }

    @Override
    public double getEnergy() {
        return dataManager.get(ELECTRICITY);
    }

    @Override
    public void setEnergy(double energy) {
        dataManager.set(ELECTRICITY, (float) Math.max(Math.min(energy, MAX_ELECTRICITY), 0));
    }

    @Override
    public double getMaxEnergy() {
        return MAX_ELECTRICITY;
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
        if (nbtTags == null || nbtTags.isEmpty()) {
            return;
        }
        List<IInventorySlot> inventorySlots = getInventorySlots((Direction) null);
        int size = inventorySlots.size();
        for (int slots = 0; slots < nbtTags.size(); slots++) {
            CompoundNBT tagCompound = nbtTags.getCompound(slots);
            byte slotID = tagCompound.getByte("Slot");
            if (slotID >= 0 && slotID < size) {
                inventorySlots.get(slotID).deserializeNBT(tagCompound);
            }
        }
    }

    @Override
    public ListNBT getInventory(Object... data) {
        ListNBT tagList = new ListNBT();
        for (int i = 0; i < inventorySlots.size(); i++) {
            CompoundNBT tagCompound = inventorySlots.get(i).serializeNBT();
            if (!tagCompound.isEmpty()) {
                tagCompound.putByte("Slot", (byte) i);
                tagList.add(tagCompound);
            }
        }
        return tagList;
    }

    @Nonnull
    @Override
    public List<IInventorySlot> getInventorySlots(@Nullable Direction side) {
        return hasInventory() ? inventorySlots : Collections.emptyList();
    }

    @Override
    public void onContentsChanged() {
        //TODO: Do we need to save the things? Probably, if not remove the call to here from createNewCachedRecipe
    }

    @Nonnull
    public List<IInventorySlot> getInventorySlots(@Nonnull ContainerType<?> containerType) {
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

    @Nullable
    @Override
    public CachedRecipe<ItemStackToItemStackRecipe> createNewCachedRecipe(@Nonnull ItemStackToItemStackRecipe recipe, int cacheIndex) {
        //TODO: Make a robit specific smelting energy usage config
        return new ItemStackToItemStackCachedRecipe(recipe, inputHandler, outputHandler)
              .setEnergyRequirements(MekanismConfig.usage.energizedSmelter::get, this::getEnergy, energy -> setEnergy(getEnergy() - energy))
              .setRequiredTicks(() -> ticksRequired)
              .setOnFinish(this::onContentsChanged)
              .setOperatingTicksChanged(operatingTicks -> progress = operatingTicks);
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        container.track(SyncableInt.create(() -> progress, value -> progress = value));
    }
}