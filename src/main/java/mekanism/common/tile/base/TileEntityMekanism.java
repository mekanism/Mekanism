package mekanism.common.tile.base;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.IMekWrench;
import mekanism.api.TileNetworkList;
import mekanism.api.Upgrade;
import mekanism.api.block.IBlockElectric;
import mekanism.api.block.IBlockSound;
import mekanism.api.block.IHasInventory;
import mekanism.api.block.IHasSecurity;
import mekanism.api.block.IHasTileEntity;
import mekanism.api.block.ISupportsComparator;
import mekanism.api.block.ISupportsRedstone;
import mekanism.api.block.ISupportsUpgrades;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.IMekanismGasHandler;
import mekanism.api.infuse.IInfusionHandler;
import mekanism.api.infuse.IMekanismInfusionHandler;
import mekanism.api.infuse.InfuseType;
import mekanism.api.infuse.InfusionStack;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.sustained.ISustainedInventory;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.base.IChemicalTankHolder;
import mekanism.common.base.IComparatorSupport;
import mekanism.common.base.IEnergyWrapper;
import mekanism.common.base.ITileComponent;
import mekanism.common.base.ITileNetwork;
import mekanism.common.block.interfaces.IHasGui;
import mekanism.common.block.interfaces.IUpgradeableBlock;
import mekanism.common.block.states.IStateActive;
import mekanism.common.block.states.IStateFacing;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.CapabilityWrapperManager;
import mekanism.common.capabilities.IToggleableCapability;
import mekanism.common.capabilities.proxy.ProxyGasHandler;
import mekanism.common.capabilities.proxy.ProxyInfusionHandler;
import mekanism.common.capabilities.proxy.ProxyItemHandler;
import mekanism.common.config.MekanismConfig;
import mekanism.common.frequency.Frequency;
import mekanism.common.frequency.FrequencyManager;
import mekanism.common.frequency.IFrequencyHandler;
import mekanism.common.integration.forgeenergy.ForgeEnergyIntegration;
import mekanism.common.integration.wrenches.Wrenches;
import mekanism.common.inventory.container.ITrackableContainer;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.inventory.container.sync.SyncableGasStack;
import mekanism.common.inventory.container.sync.SyncableInfusionStack;
import mekanism.common.inventory.slot.UpgradeInventorySlot;
import mekanism.common.inventory.slot.holder.IInventorySlotHolder;
import mekanism.common.item.ItemConfigurationCard;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.network.PacketDataRequest;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.security.ISecurityTile;
import mekanism.common.tile.component.TileComponentSecurity;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.tile.interfaces.ITileActive;
import mekanism.common.tile.interfaces.ITileDirectional;
import mekanism.common.tile.interfaces.ITileElectric;
import mekanism.common.tile.interfaces.ITileRedstone;
import mekanism.common.tile.interfaces.ITileSound;
import mekanism.common.tile.interfaces.ITileUpgradable;
import mekanism.common.tile.interfaces.ITierUpgradable;
import mekanism.common.upgrade.IUpgradeData;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

//TODO: Should methods that TileEntityMekanism implements but aren't used because of the block this tile is for
// does not support them throw an UnsupportedMethodException to make it easier to track down potential bugs
// rather than silently "fail" and just do nothing
//TODO: We need to move the "supports" methods into the source interfaces so that we make sure they get checked before being used
public abstract class TileEntityMekanism extends TileEntity implements ITileNetwork, IFrequencyHandler, ITickableTileEntity, IToggleableCapability, ITileDirectional,
      ITileElectric, ITileActive, ITileSound, ITileRedstone, ISecurityTile, IMekanismInventory, ISustainedInventory, ITileUpgradable, ITierUpgradable,
      IComparatorSupport, ITrackableContainer, IMekanismGasHandler, IMekanismInfusionHandler {
    //TODO: Make sure we have a way of saving the inventory to disk and a way to load it, basically what ISustainedInventory was before

    //TODO: Should the implementations of the various stuff be extracted into TileComponents?

    /**
     * The players currently using this block.
     */
    public Set<PlayerEntity> playersUsing = new ObjectOpenHashSet<>();

    /**
     * A timer used to send packets to clients.
     */
    //TODO: Evaluate this
    public int ticker;

    //TODO: Remove the need for this boolean to exist. It currently is used for some things that
    // would have a hard time using the new container sync system such as multiblocks where the client doesn't
    // have a place to store the values after transferring
    protected boolean doAutoSync;

    private List<ITileComponent> components = new ArrayList<>();

    protected IBlockProvider blockProvider;

    private boolean supportsComparator;
    private boolean supportsUpgrades;
    private boolean supportsRedstone;
    private boolean canBeUpgraded;
    private boolean isDirectional;
    private boolean isActivatable;
    private boolean hasInventory;
    private boolean hasSecurity;
    private boolean isElectric;
    private boolean hasSound;
    private boolean hasGui;

    //Variables for handling ITileRedstone
    //TODO: Move these to private variables?
    public boolean redstone = false;
    private boolean redstoneLastTick = false;
    /**
     * This machine's current RedstoneControl type.
     */
    private RedstoneControl controlType = RedstoneControl.DISABLED;
    //End variables ITileRedstone

    //Variables for handling IComparatorSupport
    private int currentRedstoneLevel;
    //End variables IComparatorSupport

    //Variables for handling ITileUpgradable
    //TODO: Convert this to being private
    protected TileComponentUpgrade upgradeComponent;
    //End variables ITileUpgradable

    //Variables for handling ITileContainer
    @Nullable
    private IInventorySlotHolder slotHolder;

    private ProxyItemHandler readOnlyItemHandler;
    private Map<Direction, ProxyItemHandler> itemHandlers;
    //End variables ITileContainer

    //Variables for handling IMekanismGasHandler
    @Nullable
    private IChemicalTankHolder<Gas, GasStack> gasTankHolder;

    private ProxyGasHandler readOnlyGasHandler;
    private Map<Direction, ProxyGasHandler> gasHandlers;
    //End variables IMekanismGasHandler

    //Variables for handling IMekanismInfusionHandler
    @Nullable
    private IChemicalTankHolder<InfuseType, InfusionStack> infusionTankHolder;

    private ProxyInfusionHandler readOnlyInfusionHandler;
    private Map<Direction, ProxyInfusionHandler> infusionHandlers;
    //End variables IMekanismInfusionHandler

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
    private double lastEnergyReceived;
    //End variables ITileElectric

    //Variables for handling ITileSecurity
    private TileComponentSecurity securityComponent;
    //End variables ITileSecurity

    //Variables for handling ITileActive
    private long lastActive = -1;

    // Number of ticks that the block can be inactive before it's considered not recently active
    //TODO: MekanismConfig.current().general.UPDATE_DELAY.val() except the default has to be changed to 100
    private final int RECENT_THRESHOLD = 100;
    //End variables ITileActive

    //Variables for handling ITileSound
    @Nullable
    private final SoundEvent soundEvent;

    /**
     * Only used on the client
     */
    private ISound activeSound;
    private int playSoundCooldown = 0;
    protected int rapidChangeThreshold = 10;
    //End variables ITileSound

    public TileEntityMekanism(IBlockProvider blockProvider) {
        super(((IHasTileEntity<? extends TileEntity>) blockProvider.getBlock()).getTileType());
        this.blockProvider = blockProvider;
        setSupportedTypes(this.blockProvider.getBlock());
        presetVariables();
        gasTankHolder = getInitialGasTanks();
        if (canHandleGas()) {
            gasHandlers = new EnumMap<>(Direction.class);
        }
        infusionTankHolder = getInitialInfusionTanks();
        if (canHandleInfusion()) {
            infusionHandlers = new EnumMap<>(Direction.class);
        }
        if (hasInventory()) {
            itemHandlers = new EnumMap<>(Direction.class);
            slotHolder = getInitialInventory();
        }
        if (supportsUpgrades()) {
            upgradeComponent = new TileComponentUpgrade(this, UpgradeInventorySlot.of(this, getSupportedUpgrade()));
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
        } else {
            soundEvent = null;
        }
    }

    /**
     * Like getWorld(), but for when you _know_ world won't be null
     *
     * @return The world!
     */
    @Nonnull
    protected World getWorldNN() {
        return Objects.requireNonNull(getWorld(), "getWorldNN called before world set");
    }

    private void setSupportedTypes(Block block) {
        //Used to get any data we may need
        isElectric = block instanceof IBlockElectric;
        supportsUpgrades = block instanceof ISupportsUpgrades;
        canBeUpgraded = block instanceof IUpgradeableBlock;
        isDirectional = block instanceof IStateFacing;
        supportsRedstone = block instanceof ISupportsRedstone;
        hasSound = block instanceof IBlockSound;
        hasGui = block instanceof IHasGui;
        hasInventory = block instanceof IHasInventory;
        hasSecurity = block instanceof IHasSecurity;
        //TODO: Is this the proper way of doing it
        isActivatable = hasSound || block instanceof IStateActive;
        supportsComparator = block instanceof ISupportsComparator;
    }

    /**
     * Sets variables up, called immediately after {@link #setSupportedTypes(Block)} but before any things start being created.
     *
     * @implNote This method should be used for setting any variables that would normally be set directly, except that gets run to late to set things up properly in our
     * constructor.
     */
    protected void presetVariables() {
    }

    public boolean isRemote() {
        return getWorldNN().isRemote();
    }

    public Block getBlockType() {
        //TODO: Should this be getBlockState().getBlock()
        return blockProvider.getBlock();
    }

    @Override
    public final boolean supportsUpgrades() {
        return supportsUpgrades;
    }

    @Override
    public final boolean supportsComparator() {
        return supportsComparator;
    }

    @Override
    public final boolean canBeUpgraded() {
        return canBeUpgraded;
    }

    @Override
    public final boolean isDirectional() {
        return isDirectional;
    }

    @Override
    public final boolean supportsRedstone() {
        return supportsRedstone;
    }

    @Override
    public final boolean isElectric() {
        return isElectric;
    }

    @Override
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

    @Override
    public final boolean isActivatable() {
        return isActivatable;
    }

    @Override
    public final boolean hasInventory() {
        return hasInventory;
    }

    @Override
    public boolean canHandleInfusion() {
        return infusionTankHolder != null;
    }

    @Override
    public boolean canHandleGas() {
        return gasTankHolder != null;
    }

    public void addComponent(ITileComponent component) {
        components.add(component);
    }

    public List<ITileComponent> getComponents() {
        return components;
    }

    @Nonnull
    public ITextComponent getName() {
        //TODO: Is this useful or should the gui title be got a different way
        // We can probably do it via the containers name
        return TextComponentUtil.translate(getBlockType().getTranslationKey());
    }

    @Override
    public void markDirty() {
        //Copy of the base impl of markDirty in TileEntity, except only updates comparator state when something changed
        // and if our block supports having a comparator signal, instead of always doing it
        if (world != null) {
            //TODO: Do we even really need to be updating the cachedBlockState?
            cachedBlockState = world.getBlockState(pos);
            world.markChunkDirty(pos, this);
            //Only update the comparator state if we are on the server and support comparators
            if (!isRemote() && supportsComparator() && !cachedBlockState.isAir(world, pos)) {
                int newRedstoneLevel = getRedstoneLevel();
                if (newRedstoneLevel != currentRedstoneLevel) {
                    world.updateComparatorOutputLevel(pos, getBlockType());
                    currentRedstoneLevel = newRedstoneLevel;
                }
            }
        }
    }

    public WrenchResult tryWrench(BlockState state, PlayerEntity player, Hand hand, BlockRayTraceResult rayTrace) {
        ItemStack stack = player.getHeldItem(hand);
        if (!stack.isEmpty()) {
            IMekWrench wrenchHandler = Wrenches.getHandler(stack);
            if (wrenchHandler != null) {
                if (wrenchHandler.canUseWrench(stack, player, rayTrace.getPos())) {
                    if (hasSecurity() && !SecurityUtils.canAccess(player, this)) {
                        SecurityUtils.displayNoAccess(player);
                        return WrenchResult.NO_SECURITY;
                    }
                    if (player.isShiftKeyDown()) {
                        MekanismUtils.dismantleBlock(state, getWorld(), pos, this);
                        return WrenchResult.DISMANTLED;
                    }
                    //Special ITileDirectional handling
                    if (isDirectional()) {
                        //TODO: Extract this out into a handleRotation method?
                        setFacing(getDirection().rotateY());
                        //TODO: I believe this is no longer needed, verify
                        //world.notifyNeighborsOfStateChange(pos, getBlockType());
                    }
                    return WrenchResult.SUCCESS;
                }
            }
        }
        return WrenchResult.PASS;
    }

    public ActionResultType openGui(PlayerEntity player) {
        //Everything that calls this has isRemote being false but add the check just in case anyways
        if (hasGui() && !isRemote() && !player.isShiftKeyDown()) {
            if (hasSecurity() && !SecurityUtils.canAccess(player, this)) {
                SecurityUtils.displayNoAccess(player);
                return ActionResultType.FAIL;
            }
            //Pass on this activation if the player is rotating with a configurator
            ItemStack stack = player.getHeldItemMainhand();
            if (isDirectional() && !stack.isEmpty() && stack.getItem() instanceof ItemConfigurator) {
                ItemConfigurator configurator = (ItemConfigurator) stack.getItem();
                if (configurator.getState(stack) == ItemConfigurator.ConfiguratorMode.ROTATE) {
                    return ActionResultType.PASS;
                }
            }
            //Pass on this activation if the player is using a configuration card (and this tile supports the capability)
            if (CapabilityUtils.getCapability(this, Capabilities.CONFIG_CARD_CAPABILITY, null).isPresent()) {
                if(!stack.isEmpty() && stack.getItem() instanceof ItemConfigurationCard) {
                    return ActionResultType.PASS;
                }
            }

            NetworkHooks.openGui((ServerPlayerEntity) player, ((IHasGui<TileEntityMekanism>) blockProvider.getBlock()).getProvider(this), pos);
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (isRemote()) {
            Mekanism.packetHandler.sendToServer(new PacketDataRequest(Coord4D.get(this)));
        }
    }

    @Override
    public void tick() {
        for (ITileComponent component : components) {
            component.tick();
        }

        if (isRemote() && hasSound()) {
            updateSound();
        }
        if (isActivatable()) {
            //Update the block if the specified amount of time has passed
            if (!getActive() && lastActive > 0) {
                long updateDiff = getWorldNN().getDayTime() - lastActive;
                if (updateDiff > RECENT_THRESHOLD) {
                    MekanismUtils.updateBlock(world, getPos());
                    lastActive = -1;
                }
            }
        }

        onUpdate();
        if (!isRemote() && doAutoSync) {
            sendToAllUsing(() -> new PacketTileEntity(this));
        }
        ticker++;
        if (supportsRedstone()) {
            redstoneLastTick = redstone;
        }

        if(!isRemote()) {
            lastEnergyReceived = 0;
        }
    }

    public <MSG> void sendToAllUsing(Supplier<MSG> packetSupplier) {
        //TODO: Can we get the container sync system to handle all use cases of this
        if (!playersUsing.isEmpty()) {
            MSG packet = packetSupplier.get();
            for (PlayerEntity player : playersUsing) {
                Mekanism.packetHandler.sendTo(packet, (ServerPlayerEntity) player);
            }
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
    public void handlePacketData(PacketBuffer dataStream) {
        if (isRemote()) {
            for (ITileComponent component : components) {
                component.read(dataStream);
            }
            if (isElectric()) {
                setEnergy(dataStream.readDouble());
                if (supportsUpgrades()) {
                    setEnergyPerTick(dataStream.readDouble());
                    setMaxEnergy(dataStream.readDouble());
                }
            }
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        //TODO: Should there be a hasComponents?
        for (ITileComponent component : components) {
            component.write(data);
        }
        //TODO: Move this into classes of things that need energy info for rendering, and let the rest be handled by the container sync
        if (isElectric()) {
            data.add(getEnergy());
            if (supportsUpgrades()) {
                data.add(getEnergyPerTick());
                data.add(getMaxEnergy());
            }
        }
        return data;
    }

    @Override
    public void remove() {
        super.remove();
        for (ITileComponent component : components) {
            component.invalidate();
        }
        if (isRemote() && hasSound()) {
            updateSound();
        }
    }

    @Override
    public void validate() {
        super.validate();
        if (isRemote()) {
            Mekanism.packetHandler.sendToServer(new PacketDataRequest(Coord4D.get(this)));
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
        if (supportsRedstone() && nbtTags.contains("controlType")) {
            controlType = RedstoneControl.byIndexStatic(nbtTags.getInt("controlType"));
        }
        if (hasInventory() && handleInventory()) {
            ListNBT tagList = nbtTags.getList("Items", NBT.TAG_COMPOUND);
            List<IInventorySlot> inventorySlots = getInventorySlots(null);
            int size = inventorySlots.size();
            for (int tagCount = 0; tagCount < tagList.size(); tagCount++) {
                CompoundNBT tagCompound = tagList.getCompound(tagCount);
                byte slotID = tagCompound.getByte("Slot");
                if (slotID >= 0 && slotID < size) {
                    //TODO: Re-evaluate the slot id stuff
                    inventorySlots.get(slotID).deserializeNBT(tagCompound);
                }
            }
        }
        if (canHandleGas() && handlesGas()) {
            readChemicalTanks(getGasTanks(null), nbtTags.getList("GasTanks", NBT.TAG_COMPOUND));
        }
        if (canHandleInfusion() && handlesInfusion()) {
            readChemicalTanks(getInfusionTanks(null), nbtTags.getList("InfusionTanks", NBT.TAG_COMPOUND));
        }
        if (isElectric()) {
            electricityStored = nbtTags.getDouble("electricityStored");
        }
    }

    private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> void readChemicalTanks(List<? extends IChemicalTank<CHEMICAL, STACK>> tanks,
          ListNBT storedTanks) {
        int size = tanks.size();
        for (int tagCount = 0; tagCount < storedTanks.size(); tagCount++) {
            CompoundNBT tagCompound = storedTanks.getCompound(tagCount);
            byte slotID = tagCompound.getByte("Tank");
            if (slotID >= 0 && slotID < size) {
                //TODO: Re-evaluate the slot id stuff
                tanks.get(slotID).deserializeNBT(tagCompound);
            }
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
        if (supportsRedstone()) {
            nbtTags.putInt("controlType", controlType.ordinal());
        }
        if (hasInventory() && handleInventory()) {
            ListNBT tagList = new ListNBT();
            List<IInventorySlot> inventorySlots = getInventorySlots(null);
            for (int slotCount = 0; slotCount < inventorySlots.size(); slotCount++) {
                CompoundNBT tagCompound = inventorySlots.get(slotCount).serializeNBT();
                if (!tagCompound.isEmpty()) {
                    //TODO: Re-evaluate how the slot works like this
                    tagCompound.putByte("Slot", (byte) slotCount);
                    tagList.add(tagCompound);
                }
            }
            nbtTags.put("Items", tagList);
        }
        if (canHandleGas() && handlesGas()) {
            nbtTags.put("GasTanks", writeChemicalTanks(getGasTanks(null)));
        }
        if (canHandleInfusion() && handlesInfusion()) {
            nbtTags.put("InfusionTanks", writeChemicalTanks(getInfusionTanks(null)));
        }
        if (isElectric()) {
            nbtTags.putDouble("electricityStored", getEnergy());
        }
        return nbtTags;
    }

    private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> ListNBT writeChemicalTanks(List<? extends IChemicalTank<CHEMICAL, STACK>> tanks) {
        ListNBT tagList = new ListNBT();
        for (int tank = 0; tank < tanks.size(); tank++) {
            CompoundNBT tagCompound = tanks.get(tank).serializeNBT();
            if (!tagCompound.isEmpty()) {
                tagCompound.putByte("Tank", (byte) tank);
                tagList.add(tagCompound);
            }
        }
        return tagList;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        //TODO: Allow components to define what things they need to sync for when viewing the main gui of a tile
        // Currently we just manually do the security mode, though if security override changes then I am not sure we are catching it
        // maybe make a method addMainContainerTrackers or something for the ITileComponents so they can add data they care about
        if (hasSecurity()) {
            container.track(SyncableEnum.create(SecurityMode::byIndexStatic, SecurityMode.PUBLIC, () -> getSecurity().getMode(), value -> getSecurity().setMode(value)));
        }
        if (supportsRedstone()) {
            container.track(SyncableEnum.create(RedstoneControl::byIndexStatic, RedstoneControl.DISABLED, () -> controlType, value -> controlType = value));
        }
        if (isElectric()) {
            container.track(SyncableDouble.create(this::getEnergy, this::setEnergy));
            container.track(SyncableDouble.create(this::getInputRate, this::setInputRate));
            if (supportsUpgrades()) {
                container.track(SyncableDouble.create(this::getEnergyPerTick, this::setEnergyPerTick));
                container.track(SyncableDouble.create(this::getMaxEnergy, this::setMaxEnergy));
            }
        }
        if (canHandleGas() && handlesGas()) {
            //TODO: Make it so we are not needlessly syncing this for the fusion reactor controller
            List<? extends IChemicalTank<Gas, GasStack>> gasTanks = getGasTanks(null);
            for (IChemicalTank<Gas, GasStack> gasTank : gasTanks) {
                container.track(SyncableGasStack.create(gasTank));
            }
        }
        if (canHandleInfusion() && handlesInfusion()) {
            List<? extends IChemicalTank<InfuseType, InfusionStack>> infusionTanks = getInfusionTanks(null);
            for (IChemicalTank<InfuseType, InfusionStack> infusionTank : infusionTanks) {
                container.track(SyncableInfusionStack.create(infusionTank));
            }
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        //TODO: Cache the LazyOptional where possible as recommended in ICapabilityProvider
        if (isCapabilityDisabled(capability, side)) {
            return LazyOptional.empty();
        }
        if (hasInventory()) {
            if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
                List<IInventorySlot> inventorySlots = getInventorySlots(side);
                //Don't return an item handler if we don't actually even have any slots for that side
                //TODO: Should we actually return the item handler regardless??? And then just everything fails?
                LazyOptional<IItemHandler> lazyItemHandler = inventorySlots.isEmpty() ? LazyOptional.empty() : LazyOptional.of(() -> getItemHandler(side));
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.orEmpty(capability, lazyItemHandler);
            }
        }
        if (canHandleGas()) {
            if (capability == Capabilities.GAS_HANDLER_CAPABILITY) {
                List<? extends IChemicalTank<Gas, GasStack>> gasTanks = getGasTanks(side);
                //Don't return an item handler if we don't actually even have any slots for that side
                //TODO: Should we actually return the item handler regardless??? And then just everything fails?
                LazyOptional<IGasHandler> lazyGasHandler = gasTanks.isEmpty() ? LazyOptional.empty() : LazyOptional.of(() -> getGasHandler(side));
                return Capabilities.GAS_HANDLER_CAPABILITY.orEmpty(capability, lazyGasHandler);
            }
        }
        if (canHandleInfusion()) {
            if (capability == Capabilities.INFUSION_HANDLER_CAPABILITY) {
                List<? extends IChemicalTank<InfuseType, InfusionStack>> infusionTanks = getInfusionTanks(side);
                //Don't return an item handler if we don't actually even have any slots for that side
                //TODO: Should we actually return the item handler regardless??? And then just everything fails?
                LazyOptional<IInfusionHandler> lazyInfusionHandler = infusionTanks.isEmpty() ? LazyOptional.empty() : LazyOptional.of(() -> getInfusionHandler(side));
                return Capabilities.INFUSION_HANDLER_CAPABILITY.orEmpty(capability, lazyInfusionHandler);
            }
        }
        if (capability == Capabilities.TILE_NETWORK_CAPABILITY) {
            return Capabilities.TILE_NETWORK_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        if (isElectric()) {
            if (capability == Capabilities.ENERGY_STORAGE_CAPABILITY) {
                return Capabilities.ENERGY_STORAGE_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
            }
            if (capability == Capabilities.ENERGY_ACCEPTOR_CAPABILITY) {
                return Capabilities.ENERGY_ACCEPTOR_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
            }
            if (capability == Capabilities.ENERGY_OUTPUTTER_CAPABILITY) {
                return Capabilities.ENERGY_OUTPUTTER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
            }
            if (capability == CapabilityEnergy.ENERGY) {
                return CapabilityEnergy.ENERGY.orEmpty(capability, LazyOptional.of(() -> forgeEnergyManager.getWrapper(this, side)));
            }
        }
        return super.getCapability(capability, side);
    }

    //TODO: Go through and re-evaluate all the capabilities, as there are cases when we should have the item handler cap disabled where it is not in the future
    // As other ones are being handled this is becoming less of a problem, as things like multiblocks are returning no slots accessible for when they are not formed
    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, @Nullable Direction side) {
        //TODO: Disable these caps if it is not electric?
        if (isElectric() && (isStrictEnergy(capability) || capability == CapabilityEnergy.ENERGY)) {
            return side != null && !canReceiveEnergy(side) && !canOutputEnergy(side);
        }
        return false;
    }

    public void onNeighborChange(Block block) {
        if (!isRemote() && supportsRedstone()) {
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
        super.read(tag);
    }

    //Methods pertaining to IUpgradeableTile
    public void parseUpgradeData(@Nonnull IUpgradeData data) {
        Mekanism.logger.warn("Unhandled upgrade data.", new Throwable());
    }
    //End methods IUpgradeableTile

    //Methods for implementing ITileDirectional
    @Nonnull
    @Override
    public Direction getDirection() {
        if (isDirectional()) {
            BlockState state = getBlockState();
            Block block = state.getBlock();
            if (block instanceof IStateFacing) {
                return ((IStateFacing) block).getDirection(state);
            }
        }
        //TODO: Remove, give it some better default, or allow it to be null
        return Direction.NORTH;
    }

    @Override
    public void setFacing(@Nonnull Direction direction) {
        //TODO: Remove this method or cleanup how it is a wrapper for setting the blockstate direction
        if (isDirectional()) {
            BlockState state = getBlockState();
            Block block = state.getBlock();
            if (block instanceof IStateFacing) {
                state = ((IStateFacing) block).setDirection(state, direction);
                World world = getWorld();
                if (world != null) {
                    world.setBlockState(pos, state);
                }
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

    //Methods for implementing IComparatorSupport
    @Override
    public int getRedstoneLevel() {
        if (supportsComparator()) {
            if (hasInventory()) {
                return ItemHandlerHelper.calcRedstoneFromInventory(this);
            }
            //TODO: Do we want some other defaults as well?
        }
        return 0;
    }

    @Override
    public int getCurrentRedstoneLevel() {
        if (supportsComparator()) {
            //TODO: Should we just always return currentRedstoneLevel as it gets initialized to zero
            // so cannot be anything else if we don't support a comparator
            return currentRedstoneLevel;
        }
        return 0;
    }
    //End methods IComparatorSupport

    //Methods for implementing ITileUpgradable
    @Nonnull
    @Override
    public Set<Upgrade> getSupportedUpgrade() {
        if (supportsUpgrades()) {
            return ((ISupportsUpgrades) getBlockType()).getSupportedUpgrade();
        }
        return Collections.emptySet();
    }

    @Override
    public TileComponentUpgrade getComponent() {
        return upgradeComponent;
    }

    @Override
    public void recalculateUpgrades(Upgrade upgrade) {
        //TODO: Defaults for each of the types based on what other things this machine supports??
        if (upgrade == Upgrade.SPEED) {
            if (isElectric()) {
                setEnergyPerTick(MekanismUtils.getEnergyPerTick(this, getBaseUsage()));
            }
        } else if (upgrade == Upgrade.ENERGY) {
            //TODO: Is there any case this is not a required sub req?
            if (isElectric()) {
                setMaxEnergy(MekanismUtils.getMaxEnergy(this, getBaseStorage()));
                setEnergyPerTick(MekanismUtils.getEnergyPerTick(this, getBaseUsage()));
                setEnergy(Math.min(getMaxEnergy(), getEnergy()));
            }
        }
    }
    //End methods ITileUpgradable

    //Methods for implementing ITileContainer
    @Nullable
    protected IInventorySlotHolder getInitialInventory() {
        //TODO: Go back through and verify all sides are correct/make sense now that it is easier to tell what slot is what
        // Also reorder the slots to be more logical when the order does not make much sense
        return null;
    }

    @Nonnull
    @Override
    public List<IInventorySlot> getInventorySlots(@Nullable Direction side) {
        if (!hasInventory() || slotHolder == null) {
            return Collections.emptyList();
        }
        return slotHolder.getInventorySlots(side);
    }

    @Override
    public void onContentsChanged() {
        markDirty();
    }

    @Override
    public void setInventory(ListNBT nbtTags, Object... data) {
        if (nbtTags == null || nbtTags.isEmpty() || !handleInventory()) {
            return;
        }
        List<IInventorySlot> inventorySlots = getInventorySlots(null);
        int size = inventorySlots.size();
        for (int slots = 0; slots < nbtTags.size(); slots++) {
            CompoundNBT tagCompound = nbtTags.getCompound(slots);
            byte slotID = tagCompound.getByte("Slot");
            if (slotID >= 0 && slotID < size) {
                //TODO: Re-evaluate the slot id stuff
                inventorySlots.get(slotID).deserializeNBT(tagCompound);
            }
        }
    }

    @Override
    public ListNBT getInventory(Object... data) {
        ListNBT tagList = new ListNBT();
        if (handleInventory()) {
            List<IInventorySlot> inventorySlots = getInventorySlots(null);
            for (int slots = 0; slots < inventorySlots.size(); slots++) {
                IInventorySlot inventorySlot = inventorySlots.get(slots);
                CompoundNBT tagCompound = inventorySlot.serializeNBT();
                if (!tagCompound.isEmpty()) {
                    //TODO: Re-evaluate how the slot works like this
                    tagCompound.putByte("Slot", (byte) slots);
                    tagList.add(tagCompound);
                }
            }
        }
        return tagList;
    }

    //TODO: Remove?? Maybe rename to something like shouldSaveInventory
    public boolean handleInventory() {
        return hasInventory();
    }

    /**
     * Lazily get and cache an ItemHandler instance for the given side, and make it be read only if something else is trying to interact with us using the null side
     */
    protected IItemHandler getItemHandler(@Nullable Direction side) {
        if (!hasInventory()) {
            return null;
        }
        if (side == null) {
            if (readOnlyItemHandler == null) {
                readOnlyItemHandler = new ProxyItemHandler(this, null);
            }
            return readOnlyItemHandler;
        }
        ProxyItemHandler itemHandler = itemHandlers.get(side);
        if (itemHandler == null) {
            itemHandlers.put(side, itemHandler = new ProxyItemHandler(this, side));
        }
        return itemHandler;
    }
    //End methods ITileContainer

    //Methods for implementing IMekanismGasHandler
    @Nullable
    protected IChemicalTankHolder<Gas, GasStack> getInitialGasTanks() {
        return null;
    }

    @Nonnull
    @Override
    public List<? extends IChemicalTank<Gas, GasStack>> getGasTanks(@Nullable Direction side) {
        if (!canHandleGas() || gasTankHolder == null) {
            return Collections.emptyList();
        }
        return gasTankHolder.getTanks(side);
    }

    //TODO: Re-evaluate this is mainly for quantum entangloporter to not save it to the tile
    public boolean handlesGas() {
        return canHandleGas();
    }

    /**
     * Lazily get and cache an IGasHandler instance for the given side, and make it be read only if something else is trying to interact with us using the null side
     */
    protected IGasHandler getGasHandler(@Nullable Direction side) {
        if (!canHandleGas()) {
            return null;
        }
        if (side == null) {
            if (readOnlyGasHandler == null) {
                readOnlyGasHandler = new ProxyGasHandler(this, null);
            }
            return readOnlyGasHandler;
        }
        ProxyGasHandler gasHandler = gasHandlers.get(side);
        if (gasHandler == null) {
            gasHandlers.put(side, gasHandler = new ProxyGasHandler(this, side));
        }
        return gasHandler;
    }
    //End methods IMekanismGasHandler

    //Methods for implementing IMekanismInfusionHandler
    @Nullable
    protected IChemicalTankHolder<InfuseType, InfusionStack> getInitialInfusionTanks() {
        return null;
    }

    @Nonnull
    @Override
    public List<? extends IChemicalTank<InfuseType, InfusionStack>> getInfusionTanks(@Nullable Direction side) {
        if (!canHandleGas() || infusionTankHolder == null) {
            return Collections.emptyList();
        }
        return infusionTankHolder.getTanks(side);
    }

    //TODO: Re-evaluate this is mainly for quantum entangloporter to not save it to the tile
    public boolean handlesInfusion() {
        return canHandleInfusion();
    }

    /**
     * Lazily get and cache an IInfusionHandler instance for the given side, and make it be read only if something else is trying to interact with us using the null side
     */
    protected IInfusionHandler getInfusionHandler(@Nullable Direction side) {
        if (!canHandleInfusion()) {
            return null;
        }
        if (side == null) {
            if (readOnlyInfusionHandler == null) {
                readOnlyInfusionHandler = new ProxyInfusionHandler(this, null);
            }
            return readOnlyInfusionHandler;
        }
        ProxyInfusionHandler infusionHandler = infusionHandlers.get(side);
        if (infusionHandler == null) {
            infusionHandlers.put(side, infusionHandler = new ProxyInfusionHandler(this, side));
        }
        return infusionHandler;
    }
    //End methods IMekanismInfusionHandler

    //Methods for implementing ITileElectric
    protected boolean isStrictEnergy(@Nonnull Capability<?> capability) {
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
            markDirty();
        }
    }

    @Override
    public double getMaxEnergy() {
        return maxEnergy;
    }

    public double getInputRate() {
        return lastEnergyReceived;
    }

    public void setInputRate(double inputRate) {
        this.lastEnergyReceived = inputRate;
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
            this.lastEnergyReceived += toUse;
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

    //TODO: Remove the need for this?
    protected void setMaxEnergy(double maxEnergy) {
        if (isElectric()) {
            this.maxEnergy = maxEnergy;
        }
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
        if (isActivatable()) {
            BlockState state = getBlockState();
            Block block = state.getBlock();
            if (block instanceof IStateActive) {
                return ((IStateActive) block).isActive(state);
            }
        }
        return false;
    }

    @Override
    public void setActive(boolean active) {
        if (isActivatable()) {
            boolean stateChange = getActive() != active;
            if (stateChange) {
                BlockState state = getBlockState();
                Block block = state.getBlock();
                if (block instanceof IStateActive) {
                    int flags = 3;
                    //TODO: Check if this is done correctly
                    if (!active) {
                        //Switched off; note the time
                        lastActive = world.getDayTime();
                        //TODO: Is there any case we don't want a rendering update here?
                    } else {
                        //Switching on; if lastActive is not currently set, trigger a lighting update
                        // and make sure lastActive is clear
                        if (lastActive != -1 || !lightUpdate() || !MekanismConfig.client.machineEffects.get()) {
                            //Mark that we don't want a rendering update
                            flags |= 4;
                        }
                        lastActive = -1;
                    }
                    //TODO: Should we also check renderUpdate() for building flags
                    //Set the state
                    state = ((IStateActive) block).setActive(state, active);
                    world.setBlockState(pos, state, flags);
                }
            }
        }
    }

    @Override
    public boolean wasActiveRecently() {
        // If the machine is currently active or it flipped off within our threshold,
        // we'll consider it recently active.
        return isActivatable() && (getActive() || (lastActive > 0 && (world.getDayTime() - lastActive) < RECENT_THRESHOLD));
    }
    //End methods ITileActive

    //Methods for implementing ITileSound

    /**
     * Only call this from the client
     */
    private void updateSound() {
        // If machine sounds are disabled, noop
        if (!hasSound() || !MekanismConfig.client.enableMachineSounds.get() || soundEvent == null) {
            return;
        }

        if (getActive() && !isRemoved()) {
            // If sounds are being muted, we can attempt to start them on every tick, only to have them
            // denied by the event bus, so use a cooldown period that ensures we're only trying once every
            // second or so to start a sound.
            if (--playSoundCooldown > 0) {
                return;
            }

            // If this machine isn't fully muffled and we don't seem to be playing a sound for it, go ahead and
            // play it
            if (!isFullyMuffled() && (activeSound == null || !Minecraft.getInstance().getSoundHandler().isPlaying(activeSound))) {
                activeSound = SoundHandler.startTileSound(soundEvent, getSoundCategory(), getInitialVolume(), getPos());
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
            long downtime = world.getDayTime() - lastActive;
            if (activeSound != null && downtime > rapidChangeThreshold) {
                SoundHandler.stopTileSound(getPos());
                activeSound = null;
                playSoundCooldown = 0;
            }
        }
    }

    private boolean isFullyMuffled() {
        if (!hasSound() || !supportsUpgrades()) {
            return false;
        }
        if (getComponent().supports(Upgrade.MUFFLING)) {
            return getComponent().getUpgrades(Upgrade.MUFFLING) == Upgrade.MUFFLING.getMax();
        }
        return false;
    }
    //End methods ITileSound
}