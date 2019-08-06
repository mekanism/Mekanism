package mekanism.common.tile.base;

import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.IMekWrench;
import mekanism.api.TileNetworkList;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.Upgrade;
import mekanism.common.base.IBlockProvider;
import mekanism.common.base.IEnergyWrapper;
import mekanism.common.base.ITileComponent;
import mekanism.common.base.ITileNetwork;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.base.ItemHandlerWrapper;
import mekanism.common.block.interfaces.IBlockDisableable;
import mekanism.common.block.interfaces.IBlockElectric;
import mekanism.common.block.interfaces.IBlockSound;
import mekanism.common.block.interfaces.IHasGui;
import mekanism.common.block.interfaces.IHasInventory;
import mekanism.common.block.interfaces.IHasSecurity;
import mekanism.common.block.interfaces.ISupportsRedstone;
import mekanism.common.block.interfaces.ISupportsUpgrades;
import mekanism.common.block.states.IStateActive;
import mekanism.common.block.states.IStateFacing;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.CapabilityWrapperManager;
import mekanism.common.capabilities.IToggleableCapability;
import mekanism.common.config.MekanismConfig;
import mekanism.common.frequency.Frequency;
import mekanism.common.frequency.FrequencyManager;
import mekanism.common.frequency.IFrequencyHandler;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.integration.forgeenergy.ForgeEnergyIntegration;
import mekanism.common.integration.ic2.IC2Integration;
import mekanism.common.integration.wrenches.Wrenches;
import mekanism.common.network.PacketDataRequest.DataRequestMessage;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.security.ISecurityTile;
import mekanism.common.tile.component.TileComponentSecurity;
import mekanism.common.tile.interfaces.ITileActive;
import mekanism.common.tile.interfaces.ITileContainer;
import mekanism.common.tile.interfaces.ITileDirectional;
import mekanism.common.tile.interfaces.ITileElectric;
import mekanism.common.tile.interfaces.ITileRedstone;
import mekanism.common.tile.interfaces.ITileSound;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

//TODO: Should methods that TileEntityMekanism implements but aren't used because of the block this tile is for
// does not support them throw an UnsupportedMethodException to make it easier to track down potential bugs
// rather than silently "fail" and just do nothing
public abstract class TileEntityMekanism extends TileEntity implements ITileNetwork, IFrequencyHandler, ITickable, IToggleableCapability, ITileDirectional,
      ITileContainer, ITileElectric, ITileActive, ITileSound, ITileRedstone, ISecurityTile {

    //TODO: Should the implementations of the various stuff be extracted into TileComponents?

    /**
     * The players currently using this block.
     */
    public Set<PlayerEntity> playersUsing = new HashSet<>();

    /**
     * A timer used to send packets to clients.
     */
    //TODO: Evaluate this
    public int ticker;

    public boolean doAutoSync = true;

    private List<ITileComponent> components = new ArrayList<>();

    protected IBlockProvider blockProvider;

    private boolean supportsUpgrades;
    private boolean supportsRedstone;
    private boolean isDirectional;
    private boolean isActivatable;
    private boolean hasInventory;
    private boolean hasSecurity;
    private boolean isElectric;
    private boolean hasSound;
    private boolean hasGui;

    //Variables for handling ITileDirectional
    //TODO: Should this be null when we don't support rotations
    @Nonnull
    private Direction facing = Direction.NORTH;
    //End variables ITileDirectional

    //Variables for handling ITileRedstone
    public boolean redstone = false;
    public boolean redstoneLastTick = false;
    /**
     * This machine's current RedstoneControl type.
     */
    private RedstoneControl controlType = RedstoneControl.DISABLED;
    //End variables ITileRedstone

    //Variables for handling ITileContainer
    /**
     * The inventory slot itemstacks used by this block.
     */
    public NonNullList<ItemStack> inventory;

    private CapabilityWrapperManager<ISidedInventory, ItemHandlerWrapper> itemManager = new CapabilityWrapperManager<>(ISidedInventory.class, ItemHandlerWrapper.class);
    /**
     * Read only itemhandler for the null facing.
     */
    private IItemHandler nullHandler = new InvWrapper(this) {
        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            return stack;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
            //no
        }
    };
    //End variables ITileContainer

    //Variables for handling ITileElectric
    protected CapabilityWrapperManager<IEnergyWrapper, ForgeEnergyIntegration> forgeEnergyManager = new CapabilityWrapperManager<>(IEnergyWrapper.class, ForgeEnergyIntegration.class);
    /**
     * How much energy is stored in this block.
     */
    private double electricityStored;
    /**
     * Actual maximum energy storage, including upgrades
     */
    private double maxEnergy;
    private double energyPerTick;

    private boolean ic2Registered;
    //End variables ITileElectric

    //Variables for handling ITileSecurity
    private TileComponentSecurity securityComponent;
    //End variables ITileSecurity

    //Variables for handling ITileActive
    private boolean isActive;
    private long lastActive = -1;

    // Number of ticks that the block can be inactive before it's considered not recently active
    //TODO: MekanismConfig.current().general.UPDATE_DELAY.val() except the default has to be changed to 100
    private final int RECENT_THRESHOLD = 100;
    //End variables ITileActive

    //Variables for handling ITileSound
    //TODO: Make this final?
    @Nullable
    private SoundEvent soundEvent;

    @OnlyIn(Dist.CLIENT)
    private ISound activeSound;
    private int playSoundCooldown = 0;
    protected int rapidChangeThreshold = 10;
    //End variables ITileSound

    public TileEntityMekanism(IBlockProvider blockProvider) {
        this.blockProvider = blockProvider;
        setSupportedTypes(this.blockProvider.getBlock());
        if (hasInventory()) {
            inventory = NonNullList.withSize(((IHasInventory) blockProvider.getBlock()).getInventorySize(), ItemStack.EMPTY);
        }
        if (isElectric()) {
            maxEnergy = getBaseStorage();
            energyPerTick = getBaseUsage();
        }
        if (hasSecurity()) {
            securityComponent = new TileComponentSecurity(this);
        }
        if (hasSound()) {
            soundEvent = ((IBlockSound) blockProvider.getBlock()).getSoundEvent();
        }
    }

    protected void setSupportedTypes(Block block) {
        //Used to get any data we may need
        isElectric = block instanceof IBlockElectric;
        supportsUpgrades = block instanceof ISupportsUpgrades;
        isDirectional = block instanceof IStateFacing;
        supportsRedstone = block instanceof ISupportsRedstone;
        hasSound = block instanceof IBlockSound;
        hasGui = block instanceof IHasGui;
        hasInventory = block instanceof IHasInventory;
        hasSecurity = block instanceof IHasSecurity;
        //TODO: Is this the proper way of doing it
        isActivatable = hasSound || block instanceof IStateActive;
    }

    public final boolean supportsUpgrades() {
        return supportsUpgrades;
    }

    @Override
    public final boolean isDirectional() {
        return isDirectional;
    }

    public final boolean supportsRedstone() {
        return supportsRedstone;
    }

    public final boolean isElectric() {
        return isElectric;
    }

    public final boolean hasSound() {
        return hasSound;
    }

    public final boolean hasGui() {
        return hasGui;
    }

    @Override
    public final boolean hasSecurity() {
        return hasSecurity;
    }

    public final boolean isActivatable() {
        return isActivatable;
    }

    @Override
    public final boolean hasInventory() {
        return hasInventory;
    }

    public void addComponent(ITileComponent component) {
        components.add(component);
    }

    public List<ITileComponent> getComponents() {
        return components;
    }

    public WrenchResult tryWrench(BlockState state, PlayerEntity player, Hand hand, Supplier<RayTraceResult> rayTraceSupplier) {
        ItemStack stack = player.getHeldItem(hand);
        if (!stack.isEmpty()) {
            IMekWrench wrenchHandler = Wrenches.getHandler(stack);
            if (wrenchHandler != null) {
                RayTraceResult raytrace = rayTraceSupplier.get();
                if (wrenchHandler.canUseWrench(player, hand, stack, raytrace)) {
                    if (hasSecurity() && !SecurityUtils.canAccess(player, this)) {
                        SecurityUtils.displayNoAccess(player);
                        return WrenchResult.NO_SECURITY;
                    }
                    wrenchHandler.wrenchUsed(player, hand, stack, raytrace);
                    if (player.isSneaking()) {
                        MekanismUtils.dismantleBlock(getBlockType(), state, world, pos);
                        return WrenchResult.DISMANTLED;
                    }
                    //Special ITileDirectional handling
                    if (isDirectional()) {
                        //TODO: Extract this out into a handleRotation method?
                        setFacing(getDirection().rotateY());
                        world.notifyNeighborsOfStateChange(pos, getBlockType(), true);
                    }
                    return WrenchResult.SUCCESS;
                }
            }
        }
        return WrenchResult.PASS;
    }

    public boolean openGui(PlayerEntity player) {
        if (hasGui() && !player.isSneaking()) {
            if (hasSecurity() && !SecurityUtils.canAccess(player, this)) {
                SecurityUtils.displayNoAccess(player);
            } else {
                player.openGui(Mekanism.instance, ((IHasGui) blockProvider.getBlock()).getGuiID(), world, pos.getX(), pos.getY(), pos.getZ());
            }
            return true;
        }
        return false;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (world.isRemote) {
            Mekanism.packetHandler.sendToServer(new DataRequestMessage(Coord4D.get(this)));
        }
        if (isElectric() && MekanismUtils.useIC2()) {
            register();
        }
    }

    @Override
    public void onChunkUnload() {
        if (isElectric() && MekanismUtils.useIC2()) {
            deregister();
        }
        super.onChunkUnload();
    }

    @Override
    public void update() {
        if (!world.isRemote && MekanismConfig.current().general.destroyDisabledBlocks.val()) {
            Block block = getBlockType();
            if (block instanceof IBlockDisableable && !((IBlockDisableable) block).isEnabled()) {
                //TODO: Better way of doing name?
                Mekanism.logger.info("Destroying machine of type '" + block.getClass().getSimpleName() + "' at coords " + Coord4D.get(this) + " as according to config.");
                world.removeBlock(getPos(), false);
                return;
            }
        }

        for (ITileComponent component : components) {
            component.tick();
        }

        if (hasSound()) {
            updateSound();
        }
        if (isActivatable()) {
            //Update the block if the specified amount of time has passed
            if (!getActive() && lastActive > 0) {
                long updateDiff = world.getTotalWorldTime() - lastActive;
                if (updateDiff > RECENT_THRESHOLD) {
                    MekanismUtils.updateBlock(world, getPos());
                    lastActive = -1;
                }
            }
        }

        onUpdate();
        if (!world.isRemote) {
            if (doAutoSync && playersUsing.size() > 0) {
                TileEntityMessage updateMessage = new TileEntityMessage(this);
                for (PlayerEntity player : playersUsing) {
                    Mekanism.packetHandler.sendTo(updateMessage, (ServerPlayerEntity) player);
                }
            }
        }
        ticker++;
        if (supportsRedstone()) {
            redstoneLastTick = redstone;
        }
    }

    @Override
    public void updateContainingBlockInfo() {
        super.updateContainingBlockInfo();
        onAdded();
    }

    public void open(PlayerEntity player) {
        playersUsing.add(player);
    }

    public void close(PlayerEntity player) {
        playersUsing.remove(player);
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            redstone = dataStream.readBoolean();
            for (ITileComponent component : components) {
                component.read(dataStream);
            }
            if (isDirectional()) {
                Direction previousDirection = getDirection();
                facing = Direction.byIndex(dataStream.readInt());
                if (previousDirection != getDirection()) {
                    MekanismUtils.updateBlock(world, getPos());
                    world.notifyNeighborsOfStateChange(getPos(), world.getBlockState(getPos()).getBlock(), true);
                }
            }
            if (supportsRedstone()) {
                controlType = RedstoneControl.values()[dataStream.readInt()];
            }
            if (isElectric()) {
                setEnergy(dataStream.readDouble());
            }
            if (isActivatable()) {
                boolean newActive = dataStream.readBoolean();
                boolean stateChange = newActive != getActive();
                isActive = newActive;

                if (stateChange && !getActive()) {
                    // Switched off; note the time
                    lastActive = world.getTotalWorldTime();
                } else if (stateChange) { //&& getActive()
                    // Switching on; if lastActive is not currently set, trigger a lighting update
                    // and make sure lastActive is clear
                    if (lastActive == -1) {
                        MekanismUtils.updateBlock(world, getPos());
                    }
                    lastActive = -1;
                }
            }
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        //TODO: Should there be a hasRedstone?
        data.add(redstone);
        //TODO: Should there be a hasComponents?
        for (ITileComponent component : components) {
            component.write(data);
        }
        if (isDirectional()) {
            data.add(getDirection().ordinal());
        }
        if (supportsRedstone()) {
            data.add(controlType.ordinal());
        }
        if (isElectric()) {
            data.add(getEnergy());
        }
        if (isActivatable()) {
            data.add(getActive());
        }
        return data;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        for (ITileComponent component : components) {
            component.invalidate();
        }
        if (isElectric() && MekanismUtils.useIC2()) {
            deregister();
        }
        if (hasSound()) {
            updateSound();
        }
    }

    @Override
    public void validate() {
        boolean wasInvalid = this.tileEntityInvalid;//workaround for pending tile entity invalidate/revalidate cycle
        super.validate();
        if (world.isRemote) {
            Mekanism.packetHandler.sendToServer(new DataRequestMessage(Coord4D.get(this)));
        }
        if (isElectric() && wasInvalid && MekanismUtils.useIC2()) {//re-register if we got invalidated and are an electric block
            register();
        }
    }

    /**
     * Update call for machines. Use instead of updateEntity -- it's called every tick.
     */
    public abstract void onUpdate();

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        redstone = nbtTags.getBoolean("redstone");
        for (ITileComponent component : components) {
            component.read(nbtTags);
        }
        if (isDirectional() && nbtTags.contains("facing")) {
            facing = Direction.byIndex(nbtTags.getInt("facing"));
        }
        if (supportsRedstone() && nbtTags.contains("controlType")) {
            controlType = RedstoneControl.values()[nbtTags.getInt("controlType")];
        }
        if (hasInventory()) {
            if (handleInventory()) {
                ListNBT tagList = nbtTags.getList("Items", NBT.TAG_COMPOUND);
                inventory = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);
                for (int tagCount = 0; tagCount < tagList.size(); tagCount++) {
                    CompoundNBT tagCompound = tagList.getCompound(tagCount);
                    byte slotID = tagCompound.getByte("Slot");
                    if (slotID >= 0 && slotID < getSizeInventory()) {
                        setInventorySlotContents(slotID, ItemStack.read(tagCompound));
                    }
                }
            }
        }
        if (isElectric()) {
            electricityStored = nbtTags.getDouble("electricityStored");
        }
        if (isActivatable()) {
            isActive = nbtTags.getBoolean("isActive");
        }
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putBoolean("redstone", redstone);
        for (ITileComponent component : components) {
            component.write(nbtTags);
        }
        if (isDirectional()) {
            nbtTags.putInt("facing", getDirection().ordinal());
        }
        if (supportsRedstone()) {
            nbtTags.putInt("controlType", controlType.ordinal());
        }
        if (hasInventory()) {
            if (handleInventory()) {
                ListNBT tagList = new ListNBT();
                for (int slotCount = 0; slotCount < getSizeInventory(); slotCount++) {
                    ItemStack stackInSlot = getStackInSlot(slotCount);
                    if (!stackInSlot.isEmpty()) {
                        CompoundNBT tagCompound = new CompoundNBT();
                        tagCompound.putByte("Slot", (byte) slotCount);
                        stackInSlot.write(tagCompound);
                        tagList.add(tagCompound);
                    }
                }
                nbtTags.put("Items", tagList);
            }
        }
        if (isElectric()) {
            nbtTags.putDouble("electricityStored", getEnergy());
        }
        if (isActivatable()) {
            nbtTags.putBoolean("isActive", getActive());
        }
        return nbtTags;
    }


    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, Direction side) {
        if (isCapabilityDisabled(capability, side)) {
            return false;
        } else if (hasInventory() && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        } else if (isElectric() && (isStrictEnergy(capability) || capability == CapabilityEnergy.ENERGY)) {
            return true;
        }
        return capability == Capabilities.TILE_NETWORK_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, Direction side) {
        if (isCapabilityDisabled(capability, side)) {
            return null;
        } else if (hasInventory() && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(getItemHandler(side));
        } else if (capability == Capabilities.TILE_NETWORK_CAPABILITY) {
            return Capabilities.TILE_NETWORK_CAPABILITY.cast(this);
        } else if (isElectric() && isStrictEnergy(capability)) {
            return (T) this;
        } else if (isElectric() && capability == CapabilityEnergy.ENERGY) {
            return CapabilityEnergy.ENERGY.cast(forgeEnergyManager.getWrapper(this, side));
        }
        return super.getCapability(capability, side);
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, Direction side) {
        if (isElectric() && (isStrictEnergy(capability) || capability == CapabilityEnergy.ENERGY)) {
            return side != null && !canReceiveEnergy(side) && !canOutputEnergy(side);
        }
        return false;
    }

    public void onNeighborChange(Block block) {
        if (!world.isRemote && supportsRedstone()) {
            updatePower();
        }
    }

    /**
     * Called when block is placed in world
     */
    public void onAdded() {
        if (supportsRedstone()) {
            updatePower();
        }
    }

    @Override
    public Frequency getFrequency(FrequencyManager manager) {
        //TODO: I don't think this is needed, only thing that uses this method is querying the quantum entangloporter
        if (manager == Mekanism.securityFrequencies && hasSecurity) {
            return getSecurity().getFrequency();
        }
        return null;
    }

    @Nonnull
    @Override
    public CompoundNBT getUpdateTag() {
        // Forge writes only x/y/z/id info to a new NBT Tag Compound. This is fine, we have a custom network system
        // to send other data so we don't use this one (yet).
        return super.getUpdateTag();
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundNBT tag) {
        // The super implementation of handleUpdateTag is to call this readFromNBT. But, the given TagCompound
        // only has x/y/z/id data, so our readFromNBT will set a bunch of default values which are wrong.
        // So simply call the super's readFromNBT, to let Forge do whatever it wants, but don't treat this like
        // a full NBT object, don't pass it to our custom read methods.
        super.readFromNBT(tag);
    }


    //Methods for implementing ITileDirectional
    @Nonnull
    @Override
    public Direction getDirection() {
        return facing;
    }

    @Override
    public void setFacing(@Nonnull Direction direction) {
        if (canSetFacing(direction)) {
            Direction previousDirection = getDirection();
            facing = direction;
            if (!world.isRemote && previousDirection != getDirection()) {
                Mekanism.packetHandler.sendUpdatePacket(this);
                markDirty();
            }
        }
    }
    //End methods ITileDirectional

    //Methods for implementing ITileRedstone
    @Override
    public RedstoneControl getControlType() {
        return controlType;
    }

    @Override
    public void setControlType(@Nonnull RedstoneControl type) {
        if (supportsRedstone()) {
            controlType = Objects.requireNonNull(type);
            MekanismUtils.saveChunk(this);
        }
    }

    @Override
    public boolean isPowered() {
        return supportsRedstone() && redstone;
    }

    @Override
    public boolean wasPowered() {
        return supportsRedstone() && redstoneLastTick;
    }

    private void updatePower() {
        boolean power = world.isBlockPowered(getPos());
        if (redstone != power) {
            redstone = power;
            Mekanism.packetHandler.sendUpdatePacket(this);
            onPowerChange();
        }
    }
    //End methods ITileRedstone

    //Methods for implementing ITileContainer
    @Nonnull
    @Override
    public NonNullList<ItemStack> getInventory() {
        return inventory;
    }

    @Override
    public void setInventorySlotContents(int slotID, @Nonnull ItemStack itemstack) {
        if (hasInventory()) {
            getInventory().set(slotID, itemstack);
            if (!itemstack.isEmpty() && itemstack.getCount() > getInventoryStackLimit()) {
                itemstack.setCount(getInventoryStackLimit());
            }
            markDirty();
        }
    }

    @Override
    public boolean isUsableByPlayer(@Nonnull PlayerEntity entityplayer) {
        return hasInventory() && !isInvalid() && this.world.isBlockLoaded(this.pos);//prevent Containers from remaining valid after the chunk has unloaded;
    }

    @Nonnull
    @Override
    //TODO: Don't have this be abstract, get it from the block instead by default
    public int[] getSlotsForFace(@Nonnull Direction side) {
        //TODO
        return new int[0];
    }

    @Override
    public void setInventory(ListNBT nbtTags, Object... data) {
        if (nbtTags == null || nbtTags.isEmpty() || !handleInventory()) {
            return;
        }
        NonNullList<ItemStack> inventory = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);
        for (int slots = 0; slots < nbtTags.size(); slots++) {
            CompoundNBT tagCompound = nbtTags.getCompound(slots);
            byte slotID = tagCompound.getByte("Slot");
            if (slotID >= 0 && slotID < inventory.size()) {
                inventory.set(slotID, ItemStack.read(tagCompound));
            }
        }
        this.inventory = inventory;
    }

    @Override
    public ListNBT getInventory(Object... data) {
        ListNBT tagList = new ListNBT();
        if (handleInventory()) {
            NonNullList<ItemStack> inventory = getInventory();
            for (int slots = 0; slots < inventory.size(); slots++) {
                ItemStack itemStack = inventory.get(slots);
                if (!itemStack.isEmpty()) {
                    CompoundNBT tagCompound = new CompoundNBT();
                    tagCompound.putByte("Slot", (byte) slots);
                    itemStack.write(tagCompound);
                    tagList.add(tagCompound);
                }
            }
        }
        return tagList;
    }

    //TODO: Remove??
    public boolean handleInventory() {
        return hasInventory();
    }

    protected IItemHandler getItemHandler(Direction side) {
        return side == null ? nullHandler : itemManager.getWrapper(this, side);
    }

    @Nonnull
    @Override
    public String getName() {
        return LangUtils.localize(getBlockType().getTranslationKey() + ".name");
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return new StringTextComponent(getName());
    }
    //End methods ITileContainer

    //Methods for implementing ITileElectric
    protected boolean isStrictEnergy(@Nonnull Capability capability) {
        return capability == Capabilities.ENERGY_STORAGE_CAPABILITY || capability == Capabilities.ENERGY_ACCEPTOR_CAPABILITY || capability == Capabilities.ENERGY_OUTPUTTER_CAPABILITY;
    }

    @Override
    public boolean canOutputEnergy(Direction side) {
        return false;
    }

    @Override
    public boolean canReceiveEnergy(Direction side) {
        return isElectric();
    }

    @Override
    public double getMaxOutput() {
        return 0;
    }

    @Override
    public double getEnergy() {
        return isElectric() ? electricityStored : 0;
    }

    @Override
    public void setEnergy(double energy) {
        if (isElectric()) {
            electricityStored = Math.max(Math.min(energy, getMaxEnergy()), 0);
            MekanismUtils.saveChunk(this);
        }
    }

    @Override
    public double getMaxEnergy() {
        return maxEnergy;
    }

    public double getBaseUsage() {
        if (isElectric()) {
            return ((IBlockElectric) blockProvider.getBlock()).getUsage();
        }
        return 0;
    }

    public double getBaseStorage() {
        if (isElectric()) {
            return ((IBlockElectric) blockProvider.getBlock()).getStorage();
        }
        return 0;
    }

    public double getEnergyPerTick() {
        return isElectric() ? energyPerTick : 0;
    }

    public void setEnergyPerTick(double energyPerTick) {
        if (isElectric()) {
            this.energyPerTick = energyPerTick;
        }
    }

    @Override
    public double acceptEnergy(Direction side, double amount, boolean simulate) {
        if (!isElectric()) {
            return 0;
        }
        double toUse = Math.min(getNeededEnergy(), amount);
        if (toUse < 0.0001 || (side != null && !canReceiveEnergy(side))) {
            return 0;
        }
        if (!simulate) {
            setEnergy(getEnergy() + toUse);
        }
        return toUse;
    }

    @Override
    public double pullEnergy(Direction side, double amount, boolean simulate) {
        if (!isElectric()) {
            return 0;
        }
        double toGive = Math.min(getEnergy(), amount);
        if (toGive < 0.0001 || (side != null && !canOutputEnergy(side))) {
            return 0;
        }
        if (!simulate) {
            setEnergy(getEnergy() - toGive);
        }
        return toGive;
    }

    //TODO: Once upgrade handling is moved into this class, this can probably be removed
    protected void setMaxEnergy(double maxEnergy) {
        if (isElectric()) {
            this.maxEnergy = maxEnergy;
        }
    }

    //IC2
    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public void register() {
        if (!world.isRemote && !ic2Registered) {
            MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
            ic2Registered = true;
        }
    }

    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public void deregister() {
        if (!world.isRemote && ic2Registered) {
            MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
            ic2Registered = false;
        }
    }

    @Override
    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public int addEnergy(int amount) {
        if (!MekanismConfig.current().general.blacklistIC2.val()) {
            setEnergy(getEnergy() + IC2Integration.fromEU(amount));
            return IC2Integration.toEUAsInt(getEnergy());
        }
        return 0;
    }

    @Override
    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public double getDemandedEnergy() {
        return !MekanismConfig.current().general.blacklistIC2.val() ? IC2Integration.toEU(getNeededEnergy()) : 0;
    }

    @Override
    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public double injectEnergy(Direction pushDirection, double amount, double voltage) {
        // nb: the facing param contains the side relative to the pushing block
        TileEntity tile = MekanismUtils.getTileEntity(world, getPos().offset(pushDirection.getOpposite()));
        if (MekanismConfig.current().general.blacklistIC2.val() || CapabilityUtils.hasCapability(tile, Capabilities.GRID_TRANSMITTER_CAPABILITY, pushDirection)) {
            return amount;
        }
        return amount - IC2Integration.toEU(acceptEnergy(pushDirection.getOpposite(), IC2Integration.fromEU(amount), false));
    }

    @Override
    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public void drawEnergy(double amount) {
        setEnergy(Math.max(getEnergy() - IC2Integration.fromEU(amount), 0));
    }
    //End methods ITileElectric

    //Methods for implementing ITileSecurity
    @Override
    public TileComponentSecurity getSecurity() {
        return securityComponent;
    }
    //End methods ITileSecurity

    //Methods for implementing ITileActive
    @Override
    public boolean getActive() {
        return isActivatable() && isActive;
    }

    @Override
    public void setActive(boolean active) {
        if (isActivatable()) {
            boolean stateChange = getActive() != active;
            if (stateChange) {
                isActive = active;
                Mekanism.packetHandler.sendUpdatePacket(this);
            }
        }
    }

    @Override
    public boolean wasActiveRecently() {
        // If the machine is currently active or it flipped off within our threshold,
        // we'll consider it recently active.
        return isActivatable() && (getActive() || (lastActive > 0 && (world.getTotalWorldTime() - lastActive) < RECENT_THRESHOLD));
    }
    //End methods ITileActive

    //Methods for implementing ITileSound
    protected float getInitialVolume() {
        return 1.0f;
    }

    @OnlyIn(Dist.CLIENT)
    private void updateSound() {
        // If machine sounds are disabled, noop
        if (!hasSound() || !MekanismConfig.current().client.enableMachineSounds.val() || soundEvent == null) {
            return;
        }

        if (getActive() && !isInvalid()) {
            // If sounds are being muted, we can attempt to start them on every tick, only to have them
            // denied by the event bus, so use a cooldown period that ensures we're only trying once every
            // second or so to start a sound.
            if (--playSoundCooldown > 0) {
                return;
            }

            // If this machine isn't fully muffled and we don't seem to be playing a sound for it, go ahead and
            // play it
            if (!isFullyMuffled() && (activeSound == null || !Minecraft.getInstance().getSoundHandler().isSoundPlaying(activeSound))) {
                activeSound = SoundHandler.startTileSound(soundEvent.getSoundName(), getInitialVolume(), getPos());
            }
            // Always reset the cooldown; either we just attempted to play a sound or we're fully muffled; either way
            // we don't want to try again
            playSoundCooldown = 20;

        } else {
            // Determine how long the machine has been stopped (ala lighting changes). Don't try and stop the sound
            // unless machine has been stopped at least half-a-second, so that machines which are rapidly flipping on/off
            // just sound like they are continuously on.
            // Some machines call the constructor where they can change rapidChangeThreshold,
            // because their sound is intended to be turned on/off rapidly, eg. the clicking of LogisticalSorter.
            long downtime = world.getTotalWorldTime() - lastActive;
            if (activeSound != null && downtime > rapidChangeThreshold) {
                SoundHandler.stopTileSound(getPos());
                activeSound = null;
                playSoundCooldown = 0;
            }
        }
    }

    private boolean isFullyMuffled() {
        if (!hasSound() || !(this instanceof IUpgradeTile)) {
            return false;
        }
        IUpgradeTile tile = (IUpgradeTile) this;
        if (tile.getComponent().supports(Upgrade.MUFFLING)) {
            return tile.getComponent().getUpgrades(Upgrade.MUFFLING) == Upgrade.MUFFLING.getMax();
        }
        return false;
    }
    //End methods ITileSound
}