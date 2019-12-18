package mekanism.common.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.Coord4D;
import mekanism.api.annotations.NonNull;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.api.inventory.slot.IInventorySlot;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.ICachedRecipeHolder;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.api.sustained.ISustainedInventory;
import mekanism.common.Mekanism;
import mekanism.common.MekanismItem;
import mekanism.common.entity.ai.RobitAIFollow;
import mekanism.common.entity.ai.RobitAIPickup;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.FuelInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.item.ItemRobit;
import mekanism.common.network.PacketGuiButtonPress;
import mekanism.common.network.PacketGuiButtonPress.ClickedEntityButton;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.tile.TileEntityChargepad;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.item.crafting.IRecipeType;
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
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.items.ItemHandlerHelper;

//TODO: Galaticraft
//@Interface(iface = "micdoodle8.mods.galacticraft.api.entity.IEntityBreathable", modid = MekanismHooks.GALACTICRAFT_MOD_ID)
public class EntityRobit extends CreatureEntity implements IMekanismInventory, ISustainedInventory, IStrictEnergyStorage, ICachedRecipeHolder<ItemStackToItemStackRecipe> {

    private static final DataParameter<Float> ELECTRICITY = EntityDataManager.createKey(EntityRobit.class, DataSerializers.FLOAT);
    private static final DataParameter<String> OWNER_UUID = EntityDataManager.createKey(EntityRobit.class, DataSerializers.STRING);
    private static final DataParameter<String> OWNER_NAME = EntityDataManager.createKey(EntityRobit.class, DataSerializers.STRING);
    private static final DataParameter<Boolean> FOLLOW = EntityDataManager.createKey(EntityRobit.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> DROP_PICKUP = EntityDataManager.createKey(EntityRobit.class, DataSerializers.BOOLEAN);
    public double MAX_ELECTRICITY = 100_000;
    public Coord4D homeLocation;
    public int furnaceBurnTime = 0;
    public int currentItemBurnTime = 0;
    public int furnaceCookTime = 0;
    public boolean texTick;

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
    private final FuelInventorySlot fuelSlot;
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
        inventorySlots.add(smeltingInputSlot = InputInventorySlot.at(item -> containsRecipe(recipe -> recipe.getInput().testType(item)), this, 56, 17));
        //TODO: Figure this out, do we want it using the fuel or not?
        inventorySlots.add(fuelSlot = FuelInventorySlot.forFuel(ForgeHooks::getBurnTime, this, 56, 53));
        //TODO: Previously used FurnaceResultSlot, check if we need to replicate any special logic it had (like if it had xp logic or something)
        inventorySlots.add(smeltingOutputSlot = OutputInventorySlot.at(this, 116, 35));

        mainContainerSlots = Collections.singletonList(energySlot);
        smeltingContainerSlots = Arrays.asList(smeltingInputSlot, fuelSlot, smeltingOutputSlot);

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
            //TODO: Use cached recipe system
            /*cachedRecipe = getUpdatedCache(0);
            if (cachedRecipe != null) {
                cachedRecipe.process();
            }*/

            //TODO: Remove starting here
            if (furnaceBurnTime > 0) {
                furnaceBurnTime--;
            }

            if (furnaceBurnTime == 0 && canSmelt()) {
                ItemStack fuel = fuelSlot.getStack();
                currentItemBurnTime = furnaceBurnTime = ForgeHooks.getBurnTime(fuel);
                if (furnaceBurnTime > 0) {
                    if (!fuel.isEmpty()) {
                        if (fuelSlot.shrinkStack(1, Action.EXECUTE) != 1) {
                            //TODO: Print error that something went wrong
                        }
                        if (fuelSlot.isEmpty()) {
                            fuelSlot.setStack(fuel.getItem().getContainerItem(fuel));
                        }
                    }
                }
            }

            if (furnaceBurnTime > 0 && canSmelt()) {
                furnaceCookTime++;
                if (furnaceCookTime == 200) {
                    furnaceCookTime = 0;
                    smeltItem();
                }
            } else {
                furnaceCookTime = 0;
            }
            //TODO: End remove here
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
        if (world.getDimension().getType().equals(homeLocation.dimension)) {
            setPositionAndUpdate(homeLocation.x + 0.5, homeLocation.y + 0.3, homeLocation.z + 0.5);
        } else {
            //TODO: Check if this is the correct way to change dimensions
            changeDimension(homeLocation.dimension);
            setLocationAndAngles(homeLocation.x + 0.5, homeLocation.y + 0.3, homeLocation.z + 0.5, rotationYaw, rotationPitch);
        }
        setMotion(0, 0, 0);
    }

    private boolean canSmelt() {
        if (smeltingInputSlot.isEmpty()) {
            return false;
        }
        //TODO: Should we make the robit go off of the energized smelter recipes instead?? It would allow for reducing a lot of this code
        // as then it could do it all via the CachedRecipe system
        // The decision is yes, so we need to kill a bunch of these methods and replace them with using the CachedRecipe stuff
        ItemStack input = smeltingInputSlot.getStack();
        Optional<FurnaceRecipe> recipe = world.getRecipeManager().getRecipe(IRecipeType.SMELTING, new Inventory(input), world);
        if (!recipe.isPresent()) {
            return false;
        }
        ItemStack result = recipe.get().getRecipeOutput();
        if (result.isEmpty()) {
            return false;
        }
        if (smeltingOutputSlot.isEmpty()) {
            return true;
        }
        ItemStack currentOutput = smeltingOutputSlot.getStack();
        if (!ItemHandlerHelper.canItemStacksStack(currentOutput, result)) {
            return false;
        }
        return currentOutput.getCount() + result.getCount() <= smeltingOutputSlot.getLimit(currentOutput);
    }

    public void smeltItem() {
        if (canSmelt()) {
            ItemStack input = smeltingInputSlot.getStack();
            Optional<FurnaceRecipe> recipe = world.getRecipeManager().getRecipe(IRecipeType.SMELTING, new Inventory(input), world);
            if (!recipe.isPresent()) {
                return;
            }
            ItemStack result = recipe.get().getRecipeOutput();
            ItemStack currentOutput = smeltingOutputSlot.getStack();
            if (currentOutput.isEmpty()) {
                smeltingOutputSlot.setStack(result.copy());
            } else if (ItemHandlerHelper.canItemStacksStack(currentOutput, result)) {
                if (smeltingOutputSlot.growStack(result.getCount(), Action.EXECUTE) != result.getCount()) {
                    //TODO: Print error that something went wrong
                }
            }
            //There shouldn't be any other case where the item doesn't stack but should we double check it anyways
            if (smeltingInputSlot.shrinkStack(1, Action.EXECUTE) != 1) {
                //TODO: Print error that something went wrong
            }
        }
    }

    public boolean isOnChargepad() {
        return MekanismUtils.getTileEntity(TileEntityChargepad.class, world, getPosition()) != null;
    }

    @Nonnull
    @Override
    public ActionResultType applyPlayerInteraction(PlayerEntity player, Vec3d vec, Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (player.func_225608_bj_()) {
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
        ItemEntity entityItem = new ItemEntity(world, func_226277_ct_(), func_226278_cu_() + 0.3, func_226281_cx_(), MekanismItem.ROBIT.getItemStack());
        ItemRobit item = (ItemRobit) entityItem.getItem().getItem();
        item.setEnergy(entityItem.getItem(), getEnergy());
        item.setInventory(((ISustainedInventory) this).getInventory(), entityItem.getItem());
        item.setName(entityItem.getItem(), getName().getFormattedText());

        float k = 0.05F;
        entityItem.setMotion(0, rand.nextGaussian() * k + 0.2F, 0);
        world.addEntity(entityItem);
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
        //TODO: Use this
        /*return new ItemStackToItemStackCachedRecipe(recipe, inputHandler, outputHandler)
              .setEnergyRequirements(this::getEnergyPerTick, this::getEnergy, energy -> setEnergy(getEnergy() - energy))
              .setRequiredTicks(() -> ticksRequired)
              .setOnFinish(this::onContentsChanged);*/
        return null;
    }

    //TODO: Galacticraft
    /*@Override
    public boolean canBreath() {
        return true;
    }*/
}