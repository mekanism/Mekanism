package mekanism.common.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.energy.EnergizedItemManager;
import mekanism.api.energy.IEnergizedItem;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.api.inventory.slot.IInventorySlot;
import mekanism.api.sustained.ISustainedInventory;
import mekanism.common.Mekanism;
import mekanism.common.MekanismItem;
import mekanism.common.config.MekanismConfig;
import mekanism.common.entity.ai.RobitAIFollow;
import mekanism.common.entity.ai.RobitAIPickup;
import mekanism.common.integration.forgeenergy.ForgeEnergyIntegration;
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
import net.minecraft.item.Items;
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
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.items.ItemHandlerHelper;

//TODO: Galaticraft
//@Interface(iface = "micdoodle8.mods.galacticraft.api.entity.IEntityBreathable", modid = MekanismHooks.GALACTICRAFT_MOD_ID)
public class EntityRobit extends CreatureEntity implements IMekanismInventory, ISustainedInventory {

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
                IInventorySlot slot = BasicInventorySlot.at(8 + slotX * 18, 18 + slotY * 18);
                inventorySlots.add(slot);
                inventoryContainerSlots.add(slot);
            }
        }
        inventorySlots.add(energySlot = EnergyInventorySlot.discharge(153, 17));
        //TODO: FIX THIS INPUT AND OUTPUT SLOT DECLARATION
        inventorySlots.add(smeltingInputSlot = InputInventorySlot.at(56, 17));
        inventorySlots.add(fuelSlot = FuelInventorySlot.at(56, 53));
        //TODO: Previously used FurnaceResultSlot, check if we need to replicate any special logic it had
        inventorySlots.add(smeltingOutputSlot = OutputInventorySlot.at(116, 35));

        mainContainerSlots = Collections.singletonList(energySlot);
        smeltingContainerSlots = Arrays.asList(smeltingInputSlot, fuelSlot, smeltingOutputSlot);
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
                if (homeLocation.exists(serverWorld)) {
                    if (!(homeLocation.getTileEntity(serverWorld) instanceof TileEntityChargepad)) {
                        drop();
                        remove();
                    }
                }
            }

            if (getEnergy() == 0 && !isOnChargepad()) {
                goHome();
            }

            ItemStack stack = energySlot.getStack();
            if (!stack.isEmpty() && getEnergy() < MAX_ELECTRICITY) {
                if (stack.getItem() instanceof IEnergizedItem) {
                    setEnergy(getEnergy() + EnergizedItemManager.discharge(stack, MAX_ELECTRICITY - getEnergy()));
                } else if (MekanismUtils.useForge() && stack.getCapability(CapabilityEnergy.ENERGY).isPresent()) {
                    stack.getCapability(CapabilityEnergy.ENERGY).ifPresent(storage -> {
                        if (storage.canExtract()) {
                            int needed = ForgeEnergyIntegration.toForge(MAX_ELECTRICITY - getEnergy());
                            setEnergy(getEnergy() + ForgeEnergyIntegration.fromForge(storage.extractEnergy(needed, false)));
                        }
                    });
                }
                //TODO: IC2
                /*else if (MekanismUtils.useIC2() && stack.getItem() instanceof IElectricItem) {
                    IElectricItem item = (IElectricItem) stack.getItem();
                    if (item.canProvideEnergy(stack)) {
                        double gain = IC2Integration.fromEU(ElectricItem.manager.discharge(stack, IC2Integration.toEU(MAX_ELECTRICITY - getEnergy()), 4, true, true, false));
                        setEnergy(getEnergy() + gain);
                    }
                }*/
                else if (stack.getItem() == Items.REDSTONE && getEnergy() + MekanismConfig.general.ENERGY_PER_REDSTONE.get() <= MAX_ELECTRICITY) {
                    setEnergy(getEnergy() + MekanismConfig.general.ENERGY_PER_REDSTONE.get());
                    if (energySlot.shrinkStack(1) != 1) {
                        //TODO: Print error that something went wrong
                    }
                }
            }

            if (furnaceBurnTime > 0) {
                furnaceBurnTime--;
            }

            if (!world.isRemote) {
                if (furnaceBurnTime == 0 && canSmelt()) {
                    ItemStack fuel = fuelSlot.getStack();
                    currentItemBurnTime = furnaceBurnTime = ForgeHooks.getBurnTime(fuel);
                    if (furnaceBurnTime > 0) {
                        if (!fuel.isEmpty()) {
                            if (fuelSlot.shrinkStack(1) != 1) {
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
                    ItemStack itemStack = slot.getStack();
                    if (itemStack.isEmpty()) {
                        slot.setStack(item.getItem());
                        onItemPickup(item, item.getItem().getCount());
                        item.remove();
                        playSound(SoundEvents.ENTITY_ITEM_PICKUP, 1.0F, ((rand.nextFloat() - rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                        break;
                    }
                    int maxStackSize = slot.getStackLimit();
                    if (ItemHandlerHelper.canItemStacksStack(itemStack, item.getItem()) && itemStack.getCount() < maxStackSize) {
                        int needed = maxStackSize - itemStack.getCount();
                        int toAdd = Math.min(needed, item.getItem().getCount());
                        if (slot.growStack(toAdd) != toAdd) {
                            //TODO: Print warning that something went wrong
                        }
                        item.getItem().shrink(toAdd);
                        onItemPickup(item, toAdd);
                        if (item.getItem().getCount() == 0) {
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
        ItemStack input = smeltingInputSlot.getStack();
        if (input.isEmpty()) {
            return false;
        }
        //TODO: Should we make the robit go off of the energized smelter recipes instead?? It would allow for reducing a lot of this code
        // as then it could do it all via the CachedRecipe system
        // The decision is yes, so we need to kill a bunch of these methods and replace them with using the CachedRecipe stuff
        Optional<FurnaceRecipe> recipe = world.getRecipeManager().getRecipe(IRecipeType.SMELTING, new Inventory(input), world);
        if (!recipe.isPresent()) {
            return false;
        }
        ItemStack result = recipe.get().getRecipeOutput();
        if (result.isEmpty()) {
            return false;
        }
        ItemStack currentOutput = smeltingOutputSlot.getStack();
        if (currentOutput.isEmpty()) {
            return true;
        }
        if (!ItemHandlerHelper.canItemStacksStack(currentOutput, result)) {
            return false;
        }
        return currentOutput.getCount() + result.getCount() <= smeltingOutputSlot.getStackLimit();
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
                if (smeltingOutputSlot.growStack(result.getCount()) != result.getCount()) {
                    //TODO: Print error that something went wrong
                }
            }
            //There shouldn't be any other case where the item doesn't stack but should we double check it anyways
            if (smeltingInputSlot.shrinkStack(1) != 1) {
                //TODO: Print error that something went wrong
            }
        }
    }

    public boolean isOnChargepad() {
        BlockPos pos = new BlockPos(this);
        return world.getTileEntity(pos) instanceof TileEntityChargepad;
    }

    @Nonnull
    @Override
    public ActionResultType applyPlayerInteraction(PlayerEntity entityplayer, Vec3d vec, Hand hand) {
        ItemStack stack = entityplayer.getHeldItem(hand);
        if (entityplayer.isSneaking()) {
            if (!stack.isEmpty() && stack.getItem() instanceof ItemConfigurator) {
                if (!world.isRemote) {
                    drop();
                }
                remove();
                entityplayer.swingArm(hand);
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
        ItemEntity entityItem = new ItemEntity(world, posX, posY + 0.3, posZ, MekanismItem.ROBIT.getItemStack());
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
            IInventorySlot slot = inventorySlots.get(slotCount);
            if (!slot.isEmpty()) {
                CompoundNBT tagCompound = new CompoundNBT();
                tagCompound.putByte("Slot", (byte) slotCount);
                slot.getStack().write(tagCompound);
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
        int size = getSlots(null);
        for (int tagCount = 0; tagCount < tagList.size(); tagCount++) {
            CompoundNBT tagCompound = tagList.getCompound(tagCount);
            byte slotID = tagCompound.getByte("Slot");
            if (slotID >= 0 && slotID < size) {
                setStackInSlot(slotID, ItemStack.read(tagCompound));
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

    public double getEnergy() {
        return dataManager.get(ELECTRICITY);
    }

    public void setEnergy(double energy) {
        dataManager.set(ELECTRICITY, (float) Math.max(Math.min(energy, MAX_ELECTRICITY), 0));
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
        int size = getSlots(null);
        for (int slots = 0; slots < nbtTags.size(); slots++) {
            CompoundNBT tagCompound = nbtTags.getCompound(slots);
            byte slotID = tagCompound.getByte("Slot");
            if (slotID >= 0 && slotID < size) {
                setStackInSlot(slotID, ItemStack.read(tagCompound));
            }
        }
    }

    @Override
    public ListNBT getInventory(Object... data) {
        ListNBT tagList = new ListNBT();
        for (int i = 0; i < inventorySlots.size(); i++) {
            IInventorySlot slot = inventorySlots.get(i);
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty()) {
                CompoundNBT tagCompound = new CompoundNBT();
                tagCompound.putByte("Slot", (byte) i);
                stack.write(tagCompound);
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

    @Nonnull
    public List<IInventorySlot> getInventorySlots(@Nonnull ContainerType<?> containerType) {
        if (!hasInventory()) {
            return Collections.emptyList();
        } else if (containerType == MekanismContainerTypes.INVENTORY_ROBIT) {
            return inventoryContainerSlots;
        } else if (containerType == MekanismContainerTypes.MAIN_ROBIT) {
            return mainContainerSlots;
        } else if (containerType == MekanismContainerTypes.SMELTING_ROBIT) {
            return smeltingContainerSlots;
        }
        return Collections.emptyList();
    }

    //TODO: Galacticraft
    /*@Override
    public boolean canBreath() {
        return true;
    }*/
}