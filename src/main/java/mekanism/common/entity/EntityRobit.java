package mekanism.common.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.energy.EnergizedItemManager;
import mekanism.api.energy.IEnergizedItem;
import mekanism.common.MekanismItem;
import mekanism.common.base.ISustainedInventory;
import mekanism.common.config.MekanismConfig;
import mekanism.common.entity.ai.RobitAIFollow;
import mekanism.common.entity.ai.RobitAIPickup;
import mekanism.common.integration.forgeenergy.ForgeEnergyIntegration;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.item.ItemRobit;
import mekanism.common.tile.TileEntityChargepad;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
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
public class EntityRobit extends CreatureEntity implements IInventory, ISustainedInventory {

    private static final DataParameter<Float> ELECTRICITY = EntityDataManager.createKey(EntityRobit.class, DataSerializers.FLOAT);
    private static final DataParameter<String> OWNER_UUID = EntityDataManager.createKey(EntityRobit.class, DataSerializers.STRING);
    private static final DataParameter<String> OWNER_NAME = EntityDataManager.createKey(EntityRobit.class, DataSerializers.STRING);
    private static final DataParameter<Boolean> FOLLOW = EntityDataManager.createKey(EntityRobit.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> DROP_PICKUP = EntityDataManager.createKey(EntityRobit.class, DataSerializers.BOOLEAN);
    public double MAX_ELECTRICITY = 100_000;
    public Coord4D homeLocation;
    public NonNullList<ItemStack> inventory = NonNullList.withSize(31, ItemStack.EMPTY);
    public int furnaceBurnTime = 0;
    public int currentItemBurnTime = 0;
    public int furnaceCookTime = 0;
    public boolean texTick;

    public EntityRobit(World world) {
        super(world);
        setSize(0.5F, 0.5F);
        getNavigator().setCanSwim(false);
        tasks.addTask(1, new RobitAIPickup(this, 1.0F));
        tasks.addTask(2, new RobitAIFollow(this, 1.0F, 4.0F, 2.0F));
        tasks.addTask(3, new LookAtGoal(this, PlayerEntity.class, 8.0F));
        tasks.addTask(3, new LookRandomlyGoal(this));
        tasks.addTask(4, new SwimGoal(this));
        setAlwaysRenderNameTag(true);
    }

    public EntityRobit(World world, double x, double y, double z) {
        this(world);
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
    protected void registerAttributes() {
        super.registerAttributes();
        getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3);
        getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(1);
    }

    @Override
    protected boolean canDespawn() {
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
        return new BigDecimal(getDistance(prevPosX, prevPosY, prevPosZ) * 1.5).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
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

            ItemStack stack = inventory.get(27);

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
                    stack.shrink(1);
                }
            }

            if (furnaceBurnTime > 0) {
                furnaceBurnTime--;
            }

            if (!world.isRemote) {
                if (furnaceBurnTime == 0 && canSmelt()) {
                    currentItemBurnTime = furnaceBurnTime = inventory.get(29).getBurnTime();
                    if (furnaceBurnTime > 0) {
                        if (!inventory.get(29).isEmpty()) {
                            inventory.get(29).shrink(1);
                            if (inventory.get(29).getCount() == 0) {
                                inventory.set(29, inventory.get(29).getItem().getContainerItem(inventory.get(29)));
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
                    ItemStack itemStack = inventory.get(i);
                    if (itemStack.isEmpty()) {
                        inventory.set(i, item.getItem());
                        onItemPickup(item, item.getItem().getCount());
                        item.remove();
                        playSound(SoundEvents.ENTITY_ITEM_PICKUP, 1.0F, ((rand.nextFloat() - rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                        break;
                    } else if (ItemHandlerHelper.canItemStacksStack(itemStack, item.getItem()) && itemStack.getCount() < itemStack.getMaxStackSize()) {
                        int needed = itemStack.getMaxStackSize() - itemStack.getCount();
                        int toAdd = Math.min(needed, item.getItem().getCount());
                        itemStack.grow(toAdd);
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
            changeDimension(homeLocation.dimension, (world1, entity, yaw) ->
                  entity.setLocationAndAngles(homeLocation.x + 0.5, homeLocation.y + 0.3, homeLocation.z + 0.5, yaw, rotationPitch));
        }
        setMotion(0, 0, 0);
    }

    private boolean canSmelt() {
        ItemStack input = inventory.get(28);
        if (input.isEmpty()) {
            return false;
        }
        ItemStack result = FurnaceRecipes.instance().getSmeltingResult(input);
        if (result.isEmpty()) {
            return false;
        }
        ItemStack currentOutput = inventory.get(30);
        if (currentOutput.isEmpty()) {
            return true;
        }
        if (!ItemHandlerHelper.canItemStacksStack(currentOutput, result)) {
            return false;
        }
        int newAmount = currentOutput.getCount() + result.getCount();
        return newAmount <= getInventoryStackLimit() && newAmount <= result.getMaxStackSize();
    }

    public void smeltItem() {
        if (canSmelt()) {
            ItemStack input = inventory.get(28);
            ItemStack result = FurnaceRecipes.instance().getSmeltingResult(input);
            ItemStack currentOutput = inventory.get(30);
            if (currentOutput.isEmpty()) {
                inventory.set(30, result.copy());
            } else if (ItemHandlerHelper.canItemStacksStack(currentOutput, result)) {
                currentOutput.grow(result.getCount());
            }
            //There shouldn't be any other case where the item doesn't stack but should we double check it anyways
            input.shrink(1);
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
            MekanismUtils.openEntityGui(entityplayer, this, 21);
            return ActionResultType.SUCCESS;
        }

        return ActionResultType.PASS;
    }

    public void drop() {
        ItemEntity entityItem = new ItemEntity(world, posX, posY + 0.3, posZ, MekanismItem.ROBIT.getItemStack());
        ItemRobit item = (ItemRobit) entityItem.getItem().getItem();
        item.setEnergy(entityItem.getItem(), getEnergy());
        item.setInventory(((ISustainedInventory) this).getInventory(), entityItem.getItem());
        item.setName(entityItem.getItem(), getName());

        float k = 0.05F;
        entityItem.setMotion(0, rand.nextGaussian() * k + 0.2F, 0);
        world.addEntity(entityItem);
    }

    @Override
    public void writeAdditional(CompoundNBT nbtTags) {
        super.writeAdditional(nbtTags);
        nbtTags.putDouble("electricityStored", getEnergy());
        nbtTags.putString("name", getName());
        if (getOwnerUUID() != null) {
            nbtTags.putString("ownerUUID", getOwnerUUID().toString());
        }
        nbtTags.putBoolean("follow", getFollowing());
        nbtTags.putBoolean("dropPickup", getDropPickup());
        if (homeLocation != null) {
            homeLocation.write(nbtTags);
        }
        ListNBT tagList = new ListNBT();
        for (int slotCount = 0; slotCount < inventory.size(); slotCount++) {
            if (!inventory.get(slotCount).isEmpty()) {
                CompoundNBT tagCompound = new CompoundNBT();
                tagCompound.putByte("Slot", (byte) slotCount);
                inventory.get(slotCount).write(tagCompound);
                tagList.add(tagCompound);
            }
        }
        nbtTags.put("Items", tagList);
    }

    @Override
    public void readAdditional(CompoundNBT nbtTags) {
        super.readAdditional(nbtTags);
        setEnergy(nbtTags.getDouble("electricityStored"));
        setCustomNameTag(nbtTags.getString("name"));
        if (nbtTags.contains("ownerUUID")) {
            setOwnerUUID(UUID.fromString(nbtTags.getString("ownerUUID")));
        }
        setFollowing(nbtTags.getBoolean("follow"));
        setDropPickup(nbtTags.getBoolean("dropPickup"));
        homeLocation = Coord4D.read(nbtTags);
        ListNBT tagList = nbtTags.getList("Items", Constants.NBT.TAG_COMPOUND);
        inventory = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);
        for (int tagCount = 0; tagCount < tagList.size(); tagCount++) {
            CompoundNBT tagCompound = tagList.getCompound(tagCount);
            byte slotID = tagCompound.getByte("Slot");

            if (slotID >= 0 && slotID < inventory.size()) {
                inventory.set(slotID, ItemStack.read(tagCompound));
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
    public int getSizeInventory() {
        return inventory.size();
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slotID) {
        return inventory.get(slotID);
    }

    @Nonnull
    @Override
    public ItemStack decrStackSize(int slotID, int amount) {
        return ItemStackHelper.getAndSplit(inventory, slotID, amount);
    }

    @Nonnull
    @Override
    public ItemStack removeStackFromSlot(int slotID) {
        return ItemStackHelper.getAndRemove(inventory, slotID);
    }

    @Override
    public void setInventorySlotContents(int slotID, @Nonnull ItemStack itemstack) {
        inventory.set(slotID, itemstack);
        if (!itemstack.isEmpty() && itemstack.getCount() > getInventoryStackLimit()) {
            itemstack.setCount(getInventoryStackLimit());
        }
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {
    }

    @Override
    public boolean isUsableByPlayer(@Nonnull PlayerEntity entityplayer) {
        return true;
    }

    @Override
    public void openInventory(@Nonnull PlayerEntity player) {
    }

    @Override
    public void closeInventory(@Nonnull PlayerEntity player) {
    }

    @Override
    public boolean isItemValidForSlot(int i, @Nonnull ItemStack itemstack) {
        return true;
    }

    @Override
    public void clear() {
    }

    @Override
    public void setInventory(ListNBT nbtTags, Object... data) {
        if (nbtTags == null || nbtTags.isEmpty()) {
            return;
        }
        inventory = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);
        for (int slots = 0; slots < nbtTags.size(); slots++) {
            CompoundNBT tagCompound = nbtTags.getCompound(slots);
            byte slotID = tagCompound.getByte("Slot");
            if (slotID >= 0 && slotID < inventory.size()) {
                inventory.set(slotID, ItemStack.read(tagCompound));
            }
        }
    }

    @Override
    public ListNBT getInventory(Object... data) {
        ListNBT tagList = new ListNBT();
        for (int slots = 0; slots < inventory.size(); slots++) {
            if (!inventory.get(slots).isEmpty()) {
                CompoundNBT tagCompound = new CompoundNBT();
                tagCompound.putByte("Slot", (byte) slots);
                inventory.get(slots).write(tagCompound);
                tagList.add(tagCompound);
            }
        }
        return tagList;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    //TODO: Galacticraft
    /*@Override
    public boolean canBreath() {
        return true;
    }*/
}