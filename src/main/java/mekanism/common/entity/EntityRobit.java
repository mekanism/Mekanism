package mekanism.common.entity;

import cofh.redstoneflux.api.IEnergyContainerItem;
import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.energy.EnergizedItemManager;
import mekanism.api.energy.IEnergizedItem;
import mekanism.common.Mekanism;
import mekanism.common.MekanismItems;
import mekanism.common.base.ISustainedInventory;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig.general;
import mekanism.common.entity.ai.RobitAIFollow;
import mekanism.common.entity.ai.RobitAIPickup;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.item.ItemRobit;
import mekanism.common.tile.TileEntityChargepad;
import mekanism.common.util.MekanismUtils;
import micdoodle8.mods.galacticraft.api.entity.IEntityBreathable;
import net.darkhax.tesla.api.ITeslaProducer;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Optional.Interface;

@Interface(iface = "micdoodle8.mods.galacticraft.api.entity.IEntityBreathable", modid = MekanismHooks.GALACTICRAFT_MOD_ID)
public class EntityRobit extends EntityCreature implements IInventory, ISustainedInventory, IEntityBreathable {

    private static final DataParameter<Float> ELECTRICITY = EntityDataManager
          .createKey(EntityRobit.class, DataSerializers.FLOAT);
    private static final DataParameter<String> OWNER_UUID = EntityDataManager
          .createKey(EntityRobit.class, DataSerializers.STRING);
    private static final DataParameter<String> OWNER_NAME = EntityDataManager
          .createKey(EntityRobit.class, DataSerializers.STRING);
    private static final DataParameter<Boolean> FOLLOW = EntityDataManager
          .createKey(EntityRobit.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> DROP_PICKUP = EntityDataManager
          .createKey(EntityRobit.class, DataSerializers.BOOLEAN);
    public double MAX_ELECTRICITY = 100000;
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
        tasks.addTask(3, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        tasks.addTask(3, new EntityAILookIdle(this));
        tasks.addTask(4, new EntityAISwimming(this));

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
    public PathNavigateGround getNavigator() {
        return (PathNavigateGround) navigator;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();

        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3);
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(1);
    }

    @Override
    protected boolean canDespawn() {
        return false;
    }

    @Override
    protected void entityInit() {
        super.entityInit();

        dataManager.register(ELECTRICITY, 0F);
        dataManager.register(OWNER_UUID, "");
        dataManager.register(OWNER_NAME, "");
        dataManager.register(FOLLOW, false);
        dataManager.register(DROP_PICKUP, false);
    }

    public double getRoundedTravelEnergy() {
        return new BigDecimal(getDistance(prevPosX, prevPosY, prevPosZ) * 1.5).setScale(2, RoundingMode.HALF_EVEN)
              .doubleValue();
    }

    @Override
    public void onEntityUpdate() {
        if (!world.isRemote) {
            if (getFollowing() && getOwner() != null && getDistanceSq(getOwner()) > 4 && !getNavigator().noPath()
                  && getEnergy() > 0) {
                setEnergy(getEnergy() - getRoundedTravelEnergy());
            }
        }

        super.onEntityUpdate();

        if (!world.isRemote) {
            if (getDropPickup()) {
                collectItems();
            }

            if (homeLocation == null) {
                setDead();

                return;
            }

            if (ticksExisted % 20 == 0) {
                World serverWorld = FMLCommonHandler.instance().getMinecraftServerInstance()
                      .getWorld(homeLocation.dimensionId);

                if (homeLocation.exists(serverWorld)) {
                    if (!(homeLocation.getTileEntity(serverWorld) instanceof TileEntityChargepad)) {
                        drop();
                        setDead();
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
                } else if (MekanismUtils.useTesla() && stack
                      .hasCapability(Capabilities.TESLA_PRODUCER_CAPABILITY, null)) {
                    ITeslaProducer producer = stack.getCapability(Capabilities.TESLA_PRODUCER_CAPABILITY, null);

                    long needed = Math.round((MAX_ELECTRICITY - getEnergy()) * general.TO_TESLA);
                    setEnergy(getEnergy() + producer.takePower(needed, false) * general.FROM_TESLA);
                } else if (MekanismUtils.useForge() && stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
                    IEnergyStorage storage = stack.getCapability(CapabilityEnergy.ENERGY, null);

                    if (storage.canExtract()) {
                        int needed = (int) Math
                              .round(Math.min(Integer.MAX_VALUE, (MAX_ELECTRICITY - getEnergy()) * general.TO_FORGE));
                        setEnergy(getEnergy() + storage.extractEnergy(needed, false) * general.FROM_FORGE);
                    }
                } else if (MekanismUtils.useRF() && stack.getItem() instanceof IEnergyContainerItem) {
                    IEnergyContainerItem item = (IEnergyContainerItem) stack.getItem();

                    int needed = (int) Math
                          .round(Math.min(Integer.MAX_VALUE, (MAX_ELECTRICITY - getEnergy()) * general.TO_RF));
                    setEnergy(getEnergy() + (item.extractEnergy(stack, needed, false) * general.FROM_RF));
                } else if (MekanismUtils.useIC2() && stack.getItem() instanceof IElectricItem) {
                    IElectricItem item = (IElectricItem) stack.getItem();

                    if (item.canProvideEnergy(stack)) {
                        double gain = ElectricItem.manager
                              .discharge(stack, (MAX_ELECTRICITY - getEnergy()) * general.TO_IC2, 4, true, true, false)
                              * general.FROM_IC2;
                        setEnergy(getEnergy() + gain);
                    }
                } else if (stack.getItem() == Items.REDSTONE
                      && getEnergy() + general.ENERGY_PER_REDSTONE <= MAX_ELECTRICITY) {
                    setEnergy(getEnergy() + general.ENERGY_PER_REDSTONE);
                    stack.shrink(1);
                }
            }

            if (furnaceBurnTime > 0) {
                furnaceBurnTime--;
            }

            if (!world.isRemote) {
                if (furnaceBurnTime == 0 && canSmelt()) {
                    currentItemBurnTime = furnaceBurnTime = TileEntityFurnace.getItemBurnTime(inventory.get(29));

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
        List<EntityItem> items = world
              .getEntitiesWithinAABB(EntityItem.class, getEntityBoundingBox().grow(1.5, 1.5, 1.5));

        if (!items.isEmpty()) {
            for (EntityItem item : items) {
                if (item.cannotPickup() || item.getItem().getItem() instanceof ItemRobit || item.isDead) {
                    continue;
                }

                for (int i = 0; i < 27; i++) {
                    ItemStack itemStack = inventory.get(i);

                    if (itemStack.isEmpty()) {
                        inventory.set(i, item.getItem());
                        onItemPickup(item, item.getItem().getCount());
                        item.setDead();

                        playSound(SoundEvents.ENTITY_ITEM_PICKUP, 1.0F,
                              ((rand.nextFloat() - rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);

                        break;
                    } else if (itemStack.isItemEqual(item.getItem()) && itemStack.getCount() < itemStack
                          .getMaxStackSize()) {
                        int needed = itemStack.getMaxStackSize() - itemStack.getCount();
                        int toAdd = Math.min(needed, item.getItem().getCount());

                        itemStack.grow(toAdd);
                        item.getItem().shrink(toAdd);

                        onItemPickup(item, toAdd);

                        if (item.getItem().getCount() == 0) {
                            item.setDead();
                        }

                        playSound(SoundEvents.ENTITY_ITEM_PICKUP, 1.0F,
                              ((rand.nextFloat() - rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);

                        break;
                    }
                }
            }
        }
    }

    public void goHome() {
        setFollowing(false);

        if (world.provider.getDimension() != homeLocation.dimensionId) {
            changeDimension(homeLocation.dimensionId);
        }

        setPositionAndUpdate(homeLocation.x + 0.5, homeLocation.y + 0.3, homeLocation.z + 0.5);

        motionX = 0;
        motionY = 0;
        motionZ = 0;
    }

    private boolean canSmelt() {
        if (inventory.get(28).isEmpty()) {
            return false;
        } else {
            ItemStack itemstack = FurnaceRecipes.instance().getSmeltingResult(inventory.get(28));
            if (itemstack.isEmpty()) {
                return false;
            }
            if (inventory.get(30).isEmpty()) {
                return true;
            }
            if (!inventory.get(30).isItemEqual(itemstack)) {
                return false;
            }
            int result = inventory.get(30).getCount() + itemstack.getCount();
            return (result <= getInventoryStackLimit() && result <= itemstack.getMaxStackSize());
        }
    }

    public void smeltItem() {
        if (canSmelt()) {
            ItemStack itemstack = FurnaceRecipes.instance().getSmeltingResult(inventory.get(28));

            if (inventory.get(30).isEmpty()) {
                inventory.set(30, itemstack.copy());
            } else if (inventory.get(30).isItemEqual(itemstack)) {
                inventory.get(30).grow(itemstack.getCount());
            }

            inventory.get(28).shrink(1);
        }
    }

    public boolean isOnChargepad() {
        BlockPos pos = new BlockPos(this);

        return world.getTileEntity(pos) instanceof TileEntityChargepad;

    }

    @Nonnull
    @Override
    public EnumActionResult applyPlayerInteraction(EntityPlayer entityplayer, Vec3d vec, EnumHand hand) {
        ItemStack stack = entityplayer.getHeldItem(hand);

        if (entityplayer.isSneaking()) {
            if (!stack.isEmpty() && stack.getItem() instanceof ItemConfigurator) {
                if (!world.isRemote) {
                    drop();
                }

                setDead();

                entityplayer.swingArm(hand);
                return EnumActionResult.SUCCESS;
            }
        } else {
            entityplayer.openGui(Mekanism.instance, 21, world, getEntityId(), 0, 0);
            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.PASS;
    }

    public void drop() {
        EntityItem entityItem = new EntityItem(world, posX, posY + 0.3, posZ, new ItemStack(MekanismItems.Robit));

        ItemRobit item = (ItemRobit) entityItem.getItem().getItem();
        item.setEnergy(entityItem.getItem(), getEnergy());
        item.setInventory(((ISustainedInventory) this).getInventory(), entityItem.getItem());
        item.setName(entityItem.getItem(), getName());

        float k = 0.05F;
        entityItem.motionX = 0;
        entityItem.motionY = rand.nextGaussian() * k + 0.2F;
        entityItem.motionZ = 0;

        world.spawnEntity(entityItem);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbtTags) {
        super.writeEntityToNBT(nbtTags);

        nbtTags.setDouble("electricityStored", getEnergy());

        nbtTags.setString("name", getName());

        if (getOwnerUUID() != null) {
            nbtTags.setString("ownerUUID", getOwnerUUID().toString());
        }

        nbtTags.setBoolean("follow", getFollowing());

        nbtTags.setBoolean("dropPickup", getDropPickup());

        if (homeLocation != null) {
            homeLocation.write(nbtTags);
        }

        NBTTagList tagList = new NBTTagList();

        for (int slotCount = 0; slotCount < inventory.size(); slotCount++) {
            if (!inventory.get(slotCount).isEmpty()) {
                NBTTagCompound tagCompound = new NBTTagCompound();
                tagCompound.setByte("Slot", (byte) slotCount);
                inventory.get(slotCount).writeToNBT(tagCompound);
                tagList.appendTag(tagCompound);
            }
        }

        nbtTags.setTag("Items", tagList);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbtTags) {
        super.readEntityFromNBT(nbtTags);

        setEnergy(nbtTags.getDouble("electricityStored"));

        setCustomNameTag(nbtTags.getString("name"));

        if (nbtTags.hasKey("ownerUUID")) {
            setOwnerUUID(UUID.fromString(nbtTags.getString("ownerUUID")));
        }

        setFollowing(nbtTags.getBoolean("follow"));

        setDropPickup(nbtTags.getBoolean("dropPickup"));

        homeLocation = Coord4D.read(nbtTags);

        NBTTagList tagList = nbtTags.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        inventory = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);

        for (int tagCount = 0; tagCount < tagList.tagCount(); tagCount++) {
            NBTTagCompound tagCompound = tagList.getCompoundTagAt(tagCount);
            byte slotID = tagCompound.getByte("Slot");

            if (slotID >= 0 && slotID < inventory.size()) {
                inventory.set(slotID, new ItemStack(tagCompound));
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

    public EntityPlayer getOwner() {
        return world.getPlayerEntityByUUID(getOwnerUUID());
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
    public boolean isUsableByPlayer(@Nonnull EntityPlayer entityplayer) {
        return true;
    }

    @Override
    public void openInventory(@Nonnull EntityPlayer player) {
    }

    @Override
    public void closeInventory(@Nonnull EntityPlayer player) {
    }

    @Override
    public boolean isItemValidForSlot(int i, @Nonnull ItemStack itemstack) {
        return true;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {
    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
    }

    @Override
    public void setInventory(NBTTagList nbtTags, Object... data) {
        if (nbtTags == null || nbtTags.tagCount() == 0) {
            return;
        }

        inventory = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);

        for (int slots = 0; slots < nbtTags.tagCount(); slots++) {
            NBTTagCompound tagCompound = nbtTags.getCompoundTagAt(slots);
            byte slotID = tagCompound.getByte("Slot");

            if (slotID >= 0 && slotID < inventory.size()) {
                inventory.set(slotID, new ItemStack(tagCompound));
            }
        }
    }

    @Override
    public NBTTagList getInventory(Object... data) {
        NBTTagList tagList = new NBTTagList();

        for (int slots = 0; slots < inventory.size(); slots++) {
            if (!inventory.get(slots).isEmpty()) {
                NBTTagCompound tagCompound = new NBTTagCompound();
                tagCompound.setByte("Slot", (byte) slots);
                inventory.get(slots).writeToNBT(tagCompound);
                tagList.appendTag(tagCompound);
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

    @Override
    public boolean canBreath() {
        return true;
    }
}
