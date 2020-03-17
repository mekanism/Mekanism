package mekanism.common.tile.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.IntSupplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mekanism.api.DataHandlerUtils;
import mekanism.api.IMekWrench;
import mekanism.api.NBTConstants;
import mekanism.api.Upgrade;
import mekanism.api.block.IHasTileEntity;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.gas.IMekanismGasHandler;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.infuse.IMekanismInfusionHandler;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.fluid.IExtendedFluidHandler;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.sustained.ISustainedInventory;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.base.IComparatorSupport;
import mekanism.common.base.IEnergyWrapper;
import mekanism.common.base.ITileComponent;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeEnergy;
import mekanism.common.block.attribute.AttributeGui;
import mekanism.common.block.attribute.AttributeSound;
import mekanism.common.block.attribute.AttributeStateActive;
import mekanism.common.block.attribute.AttributeStateFacing;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.block.attribute.AttributeUpgradeable;
import mekanism.common.block.attribute.Attributes.AttributeComparator;
import mekanism.common.block.attribute.Attributes.AttributeInventory;
import mekanism.common.block.attribute.Attributes.AttributeRedstone;
import mekanism.common.block.attribute.Attributes.AttributeSecurity;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.CapabilityWrapperManager;
import mekanism.common.capabilities.IToggleableCapability;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.proxy.ProxyFluidHandler;
import mekanism.common.capabilities.proxy.ProxyGasHandler;
import mekanism.common.capabilities.proxy.ProxyInfusionHandler;
import mekanism.common.capabilities.proxy.ProxyItemHandler;
import mekanism.common.config.MekanismConfig;
import mekanism.common.frequency.Frequency;
import mekanism.common.frequency.FrequencyManager;
import mekanism.common.frequency.IFrequencyHandler;
import mekanism.common.integration.forgeenergy.ForgeEnergyIntegration;
import mekanism.common.inventory.container.ITrackableContainer;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.inventory.container.sync.SyncableFluidStack;
import mekanism.common.inventory.container.sync.SyncableGasStack;
import mekanism.common.inventory.container.sync.SyncableInfusionStack;
import mekanism.common.inventory.slot.UpgradeInventorySlot;
import mekanism.common.item.ItemConfigurationCard;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.security.ISecurityTile;
import mekanism.common.tile.component.TileComponentSecurity;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.tile.interfaces.ITierUpgradable;
import mekanism.common.tile.interfaces.ITileActive;
import mekanism.common.tile.interfaces.ITileDirectional;
import mekanism.common.tile.interfaces.ITileElectric;
import mekanism.common.tile.interfaces.ITileRedstone;
import mekanism.common.tile.interfaces.ITileSound;
import mekanism.common.tile.interfaces.ITileUpgradable;
import mekanism.common.upgrade.IUpgradeData;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
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
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

//TODO: Should methods that TileEntityMekanism implements but aren't used because of the block this tile is for
// does not support them throw an UnsupportedMethodException to make it easier to track down potential bugs
// rather than silently "fail" and just do nothing
//TODO: We need to move the "supports" methods into the source interfaces so that we make sure they get checked before being used
public abstract class TileEntityMekanism extends TileEntityUpdateable implements IFrequencyHandler, ITickableTileEntity, IToggleableCapability, ITileDirectional,
      ITileElectric, ITileActive, ITileSound, ITileRedstone, ISecurityTile, IMekanismInventory, ISustainedInventory, ITileUpgradable, ITierUpgradable,
      IComparatorSupport, ITrackableContainer, IMekanismGasHandler, IMekanismInfusionHandler, IMekanismFluidHandler {
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

    //Variables for handling IMekanismFluidHandler
    @Nullable
    private IFluidTankHolder fluidTankHolder;

    private ProxyFluidHandler readOnlyFluidHandler;
    private Map<Direction, ProxyFluidHandler> fluidHandlers;
    //End variables IMekanismFluidHandler

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
    private boolean currentActive;
    private int updateDelay;
    protected IntSupplier delaySupplier = MekanismConfig.general.UPDATE_DELAY;
    //End variables ITileActive

    //Variables for handling ITileSound
    @Nullable
    private final SoundEvent soundEvent;

    /**
     * Only used on the client
     */
    private ISound activeSound;
    private int playSoundCooldown = 0;
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
        fluidTankHolder = getInitialFluidTanks();
        if (canHandleFluid()) {
            fluidHandlers = new EnumMap<>(Direction.class);
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
            soundEvent = Attribute.get(blockProvider.getBlock(), AttributeSound.class).getSoundEvent();
        } else {
            soundEvent = null;
        }
    }

    private void setSupportedTypes(Block block) {
        //Used to get any data we may need
        isElectric = Attribute.has(block, AttributeEnergy.class);
        supportsUpgrades = Attribute.has(block, AttributeUpgradeSupport.class);
        canBeUpgraded = Attribute.has(block, AttributeUpgradeable.class);
        isDirectional = Attribute.has(block, AttributeStateFacing.class);
        supportsRedstone = Attribute.has(block, AttributeRedstone.class);
        hasSound = Attribute.has(block, AttributeSound.class);
        hasGui = Attribute.has(block, AttributeGui.class);
        hasInventory = Attribute.has(block, AttributeInventory.class);
        hasSecurity = Attribute.has(block, AttributeSecurity.class);
        isActivatable = hasSound || Attribute.has(block, AttributeSound.class);
        supportsComparator = Attribute.has(block, AttributeComparator.class);
    }

    /**
     * Sets variables up, called immediately after {@link #setSupportedTypes(Block)} but before any things start being created.
     *
     * @implNote This method should be used for setting any variables that would normally be set directly, except that gets run to late to set things up properly in our
     * constructor.
     */
    protected void presetVariables() {}

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
    public boolean canHandleFluid() {
        return fluidTankHolder != null;
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
    protected void markDirtyComparator() {
        //Only update the comparator state if we support comparators
        if (supportsComparator() && !cachedBlockState.isAir(world, pos)) {
            int newRedstoneLevel = getRedstoneLevel();
            if (newRedstoneLevel != currentRedstoneLevel) {
                world.updateComparatorOutputLevel(pos, getBlockType());
                currentRedstoneLevel = newRedstoneLevel;
            }
        }
    }

    public WrenchResult tryWrench(BlockState state, PlayerEntity player, Hand hand, BlockRayTraceResult rayTrace) {
        ItemStack stack = player.getHeldItem(hand);
        if (!stack.isEmpty()) {
            IMekWrench wrenchHandler = MekanismUtils.getWrench(stack);
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
                if (!stack.isEmpty() && stack.getItem() instanceof ItemConfigurationCard) {
                    return ActionResultType.PASS;
                }
            }

            NetworkHooks.openGui((ServerPlayerEntity) player, Attribute.get(blockProvider.getBlock(), AttributeGui.class).getProvider(this), pos);
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

    @Override
    public void tick() {
        for (ITileComponent component : components) {
            component.tick();
        }
        if (isRemote()) {
            if (hasSound()) {
                updateSound();
            }
            if (isActivatable()) {
                if (ticker == 0) {
                    MekanismUtils.updateBlock(getWorld(), getPos());
                }
            }
            onUpdateClient();
        } else {
            if (isActivatable()) {
                if (updateDelay > 0) {
                    updateDelay--;
                    if (updateDelay == 0 && getClientActive() != currentActive) {
                        setActive(currentActive);
                    }
                }
            }
            onUpdateServer();
            lastEnergyReceived = 0;
        }
        ticker++;
        if (supportsRedstone()) {
            redstoneLastTick = redstone;
        }
    }

    public void open(PlayerEntity player) {
        playersUsing.add(player);
    }

    public void close(PlayerEntity player) {
        playersUsing.remove(player);
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

    /**
     * Update call for machines. Use instead of updateEntity -- it's called every tick on the client side.
     */
    protected void onUpdateClient() {
    }

    /**
     * Update call for machines. Use instead of updateEntity -- it's called every tick on the server side.
     */
    protected void onUpdateServer() {
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        redstone = nbtTags.getBoolean(NBTConstants.REDSTONE);
        for (ITileComponent component : components) {
            component.read(nbtTags);
        }
        if (supportsRedstone()) {
            NBTUtils.setEnumIfPresent(nbtTags, NBTConstants.CONTROL_TYPE, RedstoneControl::byIndexStatic, type -> controlType = type);
        }
        if (hasInventory() && persistInventory()) {
            DataHandlerUtils.readSlots(getInventorySlots(null), nbtTags.getList(NBTConstants.ITEMS, NBT.TAG_COMPOUND));
        }
        if (canHandleGas() && persistGas()) {
            DataHandlerUtils.readTanks(getGasTanks(null), nbtTags.getList(NBTConstants.GAS_TANKS, NBT.TAG_COMPOUND));
        }
        if (canHandleInfusion() && persistInfusion()) {
            DataHandlerUtils.readTanks(getInfusionTanks(null), nbtTags.getList(NBTConstants.INFUSION_TANKS, NBT.TAG_COMPOUND));
        }
        if (canHandleFluid() && persistFluid()) {
            DataHandlerUtils.readTanks(getFluidTanks(null), nbtTags.getList(NBTConstants.FLUID_TANKS, NBT.TAG_COMPOUND));
        }
        if (isElectric()) {
            electricityStored = nbtTags.getDouble(NBTConstants.ENERGY_STORED);
        }
        if (isActivatable()) {
            currentActive = nbtTags.getBoolean(NBTConstants.ACTIVE_STATE);
            updateDelay = nbtTags.getInt(NBTConstants.UPDATE_DELAY);
        }
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putBoolean(NBTConstants.REDSTONE, redstone);
        for (ITileComponent component : components) {
            component.write(nbtTags);
        }
        if (supportsRedstone()) {
            nbtTags.putInt(NBTConstants.CONTROL_TYPE, controlType.ordinal());
        }
        if (hasInventory() && persistInventory()) {
            nbtTags.put(NBTConstants.ITEMS, DataHandlerUtils.writeSlots(getInventorySlots(null)));
        }
        if (canHandleGas() && persistGas()) {
            nbtTags.put(NBTConstants.GAS_TANKS, DataHandlerUtils.writeTanks(getGasTanks(null)));
        }
        if (canHandleInfusion() && persistInfusion()) {
            nbtTags.put(NBTConstants.INFUSION_TANKS, DataHandlerUtils.writeTanks(getInfusionTanks(null)));
        }
        if (canHandleFluid() && persistFluid()) {
            nbtTags.put(NBTConstants.FLUID_TANKS, DataHandlerUtils.writeTanks(getFluidTanks(null)));
        }
        if (isElectric()) {
            nbtTags.putDouble(NBTConstants.ENERGY_STORED, getEnergy());
        }
        if (isActivatable()) {
            nbtTags.putBoolean(NBTConstants.ACTIVE_STATE, currentActive);
            nbtTags.putInt(NBTConstants.UPDATE_DELAY, updateDelay);
        }
        return nbtTags;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        for (ITileComponent component : components) {
            component.trackForMainContainer(container);
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
        if (canHandleFluid() && handlesFluid()) {
            List<IExtendedFluidTank> fluidTanks = getFluidTanks(null);
            for (IExtendedFluidTank fluidTank : fluidTanks) {
                container.track(SyncableFluidStack.create(fluidTank));
            }
        }
    }

    @Nonnull
    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT updateTag = super.getUpdateTag();
        for (ITileComponent component : components) {
            component.addToUpdateTag(updateTag);
        }
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundNBT tag) {
        super.handleUpdateTag(tag);
        for (ITileComponent component : components) {
            component.readFromUpdateTag(tag);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Do not override this method if you are implementing {@link IToggleableCapability}, instead override {@link #getCapabilityIfEnabled(Capability,
     * Direction)}, calling this method is fine.
     */
    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        //Due to TileEntity implementing ICapabilityProvider we have to manually copy the logic from IToggleableCapability
        // that reroutes getCapability to check if it is disabled and otherwise use getCapabilityIfEnabled
        return isCapabilityDisabled(capability, side) ? LazyOptional.empty() : getCapabilityIfEnabled(capability, side);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapabilityIfEnabled(@Nonnull Capability<T> capability, @Nullable Direction side) {
        //TODO: Cache the LazyOptional where possible as recommended in ICapabilityProvider
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
                //Don't return a gas handler if we don't actually even have any gas tanks for that side
                //TODO: Should we actually return the gas handler regardless??? And then just everything fails?
                LazyOptional<IGasHandler> lazyGasHandler = gasTanks.isEmpty() ? LazyOptional.empty() : LazyOptional.of(() -> getGasHandler(side));
                return Capabilities.GAS_HANDLER_CAPABILITY.orEmpty(capability, lazyGasHandler);
            }
        }
        if (canHandleInfusion()) {
            if (capability == Capabilities.INFUSION_HANDLER_CAPABILITY) {
                List<? extends IChemicalTank<InfuseType, InfusionStack>> infusionTanks = getInfusionTanks(side);
                //Don't return an infusion handler if we don't actually even have any infusion tanks for that side
                //TODO: Should we actually return the infusion handler regardless??? And then just everything fails?
                LazyOptional<IInfusionHandler> lazyInfusionHandler = infusionTanks.isEmpty() ? LazyOptional.empty() : LazyOptional.of(() -> getInfusionHandler(side));
                return Capabilities.INFUSION_HANDLER_CAPABILITY.orEmpty(capability, lazyInfusionHandler);
            }
        }
        if (canHandleFluid()) {
            if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
                List<IExtendedFluidTank> fluidTanks = getFluidTanks(side);
                //Don't return a fluid handler if we don't actually even have any fluid tanks for that side
                //TODO: Should we actually return fluid item handler regardless??? And then just everything fails?
                LazyOptional<IFluidHandler> lazyFluidHandler = fluidTanks.isEmpty() ? LazyOptional.empty() : LazyOptional.of(() -> getFluidHandler(side));
                return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(capability, lazyFluidHandler);
            }
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
        //Call to the TileEntity's Implementation of getCapability if we could not find a capability ourselves
        return super.getCapability(capability, side);
    }

    //TODO: Go through and re-evaluate all the capabilities, as there are cases when we should have the item handler cap disabled where it is not in the future
    // As other ones are being handled this is becoming less of a problem, as things like multiblocks are returning no slots accessible for when they are not formed
    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, @Nullable Direction side) {
        if (isStrictEnergy(capability) || capability == CapabilityEnergy.ENERGY) {
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
            if (Attribute.has(block, AttributeStateFacing.class)) {
                return Attribute.get(block, AttributeStateFacing.class).getDirection(state);
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
            if (Attribute.has(block, AttributeStateFacing.class)) {
                state = Attribute.get(block, AttributeStateFacing.class).setDirection(state, direction);
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
            markDirty();
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
            return Attribute.get(blockProvider.getBlock(), AttributeUpgradeSupport.class).getSupportedUpgrades();
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
        if (nbtTags != null && !nbtTags.isEmpty() && persistInventory()) {
            DataHandlerUtils.readSlots(getInventorySlots(null), nbtTags);
        }
    }

    @Override
    public ListNBT getInventory(Object... data) {
        return persistInventory() ? DataHandlerUtils.writeSlots(getInventorySlots(null)) : new ListNBT();
    }

    /**
     * Should the inventory be persisted in this tile save
     */
    public boolean persistInventory() {
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
                readOnlyItemHandler = new ProxyItemHandler(this, null, slotHolder);
            }
            return readOnlyItemHandler;
        }
        ProxyItemHandler itemHandler = itemHandlers.get(side);
        if (itemHandler == null) {
            itemHandlers.put(side, itemHandler = new ProxyItemHandler(this, side, slotHolder));
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
        return canHandleGas() && gasTankHolder != null ? gasTankHolder.getTanks(side) : Collections.emptyList();
    }

    /**
     * Should gas be persisted in this tile save
     */
    public boolean persistGas() {
        return canHandleGas();
    }

    /**
     * Should gas be saved to the item, and synced to the client in the GUI
     */
    public boolean handlesGas() {
        return persistGas();
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
                readOnlyGasHandler = new ProxyGasHandler(this, null, gasTankHolder);
            }
            return readOnlyGasHandler;
        }
        ProxyGasHandler gasHandler = gasHandlers.get(side);
        if (gasHandler == null) {
            gasHandlers.put(side, gasHandler = new ProxyGasHandler(this, side, gasTankHolder));
        }
        return gasHandler;
    }

    public void loadGas(ListNBT nbtTags) {
        if (nbtTags != null && !nbtTags.isEmpty() && canHandleGas()) {
            DataHandlerUtils.readTanks(getGasTanks(null), nbtTags);
        }
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
        return canHandleInfusion() && infusionTankHolder != null ? infusionTankHolder.getTanks(side) : Collections.emptyList();
    }

    /**
     * Should infusion be persisted in this tile save
     */
    public boolean persistInfusion() {
        return canHandleInfusion();
    }

    /**
     * Should infusion be saved to the item, and synced to the client in the GUI
     */
    public boolean handlesInfusion() {
        return persistInfusion();
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
                readOnlyInfusionHandler = new ProxyInfusionHandler(this, null, infusionTankHolder);
            }
            return readOnlyInfusionHandler;
        }
        ProxyInfusionHandler infusionHandler = infusionHandlers.get(side);
        if (infusionHandler == null) {
            infusionHandlers.put(side, infusionHandler = new ProxyInfusionHandler(this, side, infusionTankHolder));
        }
        return infusionHandler;
    }

    public void loadInfusion(ListNBT nbtTags) {
        if (nbtTags != null && !nbtTags.isEmpty() && canHandleInfusion()) {
            DataHandlerUtils.readTanks(getInfusionTanks(null), nbtTags);
        }
    }
    //End methods IMekanismInfusionHandler

    //Methods for implementing IMekanismFluidHandler
    @Nullable
    protected IFluidTankHolder getInitialFluidTanks() {
        return null;
    }

    @Nonnull
    @Override
    public List<IExtendedFluidTank> getFluidTanks(@Nullable Direction side) {
        return canHandleFluid() && fluidTankHolder != null ? fluidTankHolder.getTanks(side) : Collections.emptyList();
    }

    /**
     * Should fluid be persisted in this tile save
     */
    public boolean persistFluid() {
        return canHandleFluid();
    }

    /**
     * Should fluid be saved to the item, and synced to the client in the GUI
     */
    public boolean handlesFluid() {
        return persistFluid();
    }

    /**
     * Lazily get and cache an IExtendedFluidHandler instance for the given side, and make it be read only if something else is trying to interact with us using the null
     * side
     */
    protected IExtendedFluidHandler getFluidHandler(@Nullable Direction side) {
        if (!canHandleFluid()) {
            return null;
        }
        if (side == null) {
            if (readOnlyFluidHandler == null) {
                readOnlyFluidHandler = new ProxyFluidHandler(this, null, fluidTankHolder);
            }
            return readOnlyFluidHandler;
        }
        ProxyFluidHandler fluidHandler = fluidHandlers.get(side);
        if (fluidHandler == null) {
            fluidHandlers.put(side, fluidHandler = new ProxyFluidHandler(this, side, fluidTankHolder));
        }
        return fluidHandler;
    }

    public void loadFluid(ListNBT nbtTags) {
        if (nbtTags != null && !nbtTags.isEmpty() && canHandleFluid()) {
            DataHandlerUtils.readTanks(getFluidTanks(null), nbtTags);
        }
    }
    //End methods IMekanismFluidHandler

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
            return Attribute.get(blockProvider.getBlock(), AttributeEnergy.class).getUsage();
        }
        return 0;
    }

    public double getBaseStorage() {
        if (isElectric()) {
            return Attribute.get(blockProvider.getBlock(), AttributeEnergy.class).getStorage();
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
        return isRemote() ? getClientActive() : currentActive;
    }

    private boolean getClientActive() {
        BlockState state = getBlockState();
        Block block = state.getBlock();
        if (Attribute.has(block, AttributeStateActive.class)) {
            return Attribute.get(block, AttributeStateActive.class).isActive(state);
        }
        return false;
    }

    @Override
    public void setActive(boolean active) {
        if (isActivatable()) {
            BlockState state = getBlockState();
            Block block = state.getBlock();
            if (Attribute.has(block, AttributeStateActive.class)) {
                currentActive = active;

                if (updateDelay == 0 && getClientActive() != active) {
                    state = Attribute.get(block, AttributeStateActive.class).setActive(state, active);
                    world.setBlockState(pos, state);
                    updateDelay = delaySupplier.getAsInt();
                }
            }
        }
    }
    //End methods ITileActive

    //Methods for implementing ITileSound
    /**
     * Used to check if this tile should attempt to play its sound
     */
    protected boolean canPlaySound() {
        return getActive();
    }

    /**
     * Only call this from the client
     */
    private void updateSound() {
        // If machine sounds are disabled, noop
        if (!hasSound() || !MekanismConfig.client.enableMachineSounds.get() || soundEvent == null) {
            return;
        }
        if (canPlaySound() && !isRemoved()) {
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
        } else if (activeSound != null) {
            SoundHandler.stopTileSound(getPos());
            activeSound = null;
            playSoundCooldown = 0;
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