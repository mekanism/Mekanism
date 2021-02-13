package mekanism.common.tile.base;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.IntSupplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.Coord4D;
import mekanism.api.DataHandlerUtils;
import mekanism.api.IMekWrench;
import mekanism.api.NBTConstants;
import mekanism.api.Upgrade;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.gas.attribute.GasAttributes;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.heat.IHeatHandler;
import mekanism.api.inventory.AutomationType;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.api.math.FloatingLong;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeGui;
import mekanism.common.block.attribute.AttributeSound;
import mekanism.common.block.attribute.AttributeStateActive;
import mekanism.common.block.attribute.AttributeStateFacing;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.block.attribute.AttributeUpgradeable;
import mekanism.common.block.attribute.Attributes.AttributeComparator;
import mekanism.common.block.attribute.Attributes.AttributeRedstone;
import mekanism.common.block.attribute.Attributes.AttributeSecurity;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.heat.BasicHeatCapacitor;
import mekanism.common.capabilities.heat.ITileHeatHandler;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.heat.IHeatCapacitorHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.resolver.manager.ChemicalHandlerManager.GasHandlerManager;
import mekanism.common.capabilities.resolver.manager.ChemicalHandlerManager.InfusionHandlerManager;
import mekanism.common.capabilities.resolver.manager.ChemicalHandlerManager.PigmentHandlerManager;
import mekanism.common.capabilities.resolver.manager.ChemicalHandlerManager.SlurryHandlerManager;
import mekanism.common.capabilities.resolver.manager.EnergyHandlerManager;
import mekanism.common.capabilities.resolver.manager.FluidHandlerManager;
import mekanism.common.capabilities.resolver.manager.HeatHandlerManager;
import mekanism.common.capabilities.resolver.manager.ICapabilityHandlerManager;
import mekanism.common.capabilities.resolver.manager.ItemHandlerManager;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.ITrackableContainer;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.inventory.container.sync.SyncableFloatingLong;
import mekanism.common.inventory.container.sync.SyncableFluidStack;
import mekanism.common.inventory.container.sync.chemical.SyncableGasStack;
import mekanism.common.inventory.container.sync.chemical.SyncableInfusionStack;
import mekanism.common.inventory.container.sync.chemical.SyncablePigmentStack;
import mekanism.common.inventory.container.sync.chemical.SyncableSlurryStack;
import mekanism.common.inventory.container.sync.dynamic.SyncMapper;
import mekanism.common.inventory.slot.UpgradeInventorySlot;
import mekanism.common.item.ItemConfigurationCard;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.lib.frequency.IFrequencyHandler;
import mekanism.common.lib.frequency.TileComponentFrequency;
import mekanism.common.lib.security.ISecurityTile;
import mekanism.common.lib.security.SecurityMode;
import mekanism.common.tile.component.ITileComponent;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentSecurity;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.tile.interfaces.IComparatorSupport;
import mekanism.common.tile.interfaces.ISustainedInventory;
import mekanism.common.tile.interfaces.ITierUpgradable;
import mekanism.common.tile.interfaces.ITileActive;
import mekanism.common.tile.interfaces.ITileDirectional;
import mekanism.common.tile.interfaces.ITileRedstone;
import mekanism.common.tile.interfaces.ITileSound;
import mekanism.common.tile.interfaces.ITileUpgradable;
import mekanism.common.tile.interfaces.chemical.IGasTile;
import mekanism.common.tile.interfaces.chemical.IInfusionTile;
import mekanism.common.tile.interfaces.chemical.IPigmentTile;
import mekanism.common.tile.interfaces.chemical.ISlurryTile;
import mekanism.common.upgrade.IUpgradeData;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.WorldUtils;
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
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.network.NetworkHooks;

//TODO: We need to move the "supports" methods into the source interfaces so that we make sure they get checked before being used
public abstract class TileEntityMekanism extends CapabilityTileEntity implements IFrequencyHandler, ITickableTileEntity, ITileDirectional,
      ITileActive, ITileSound, ITileRedstone, ISecurityTile, IMekanismInventory, ISustainedInventory, ITileUpgradable, ITierUpgradable, IComparatorSupport,
      ITrackableContainer, IMekanismFluidHandler, IMekanismStrictEnergyHandler, ITileHeatHandler, IGasTile, IInfusionTile, IPigmentTile, ISlurryTile {

    /**
     * The players currently using this block.
     */
    public final Set<PlayerEntity> playersUsing = new ObjectOpenHashSet<>();

    /**
     * A timer used to send packets to clients.
     */
    public int ticker;
    private final List<ICapabilityHandlerManager<?>> capabilityHandlerManagers = new ArrayList<>();
    private final List<ITileComponent> components = new ArrayList<>();

    protected final IBlockProvider blockProvider;

    private boolean supportsComparator;
    private boolean supportsUpgrades;
    private boolean supportsRedstone;
    private boolean canBeUpgraded;
    private boolean isDirectional;
    private boolean isActivatable;
    private boolean hasSecurity;
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
    protected int currentRedstoneLevel;
    //End variables IComparatorSupport

    //Variables for handling ITileUpgradable
    //TODO: Convert this to being private
    protected TileComponentUpgrade upgradeComponent;
    //End variables ITileUpgradable

    //Variables for handling IFrequencyHandler
    protected final TileComponentFrequency frequencyComponent;
    //End variables IFrequencyHandler

    //Variables for handling ITileContainer
    protected final ItemHandlerManager itemHandlerManager;
    //End variables ITileContainer

    //Variables for handling IGasTile
    private final GasHandlerManager gasHandlerManager;
    //End variables IGasTile

    //Variables for handling IInfusionTile
    private final InfusionHandlerManager infusionHandlerManager;
    //End variables IInfusionTile

    //Variables for handling IPigmentTile
    private final PigmentHandlerManager pigmentHandlerManager;
    //End variables IPigmentTile

    //Variables for handling ISlurryTile
    private final SlurryHandlerManager slurryHandlerManager;
    //End variables ISlurryTile

    //Variables for handling IMekanismFluidHandler
    protected final FluidHandlerManager fluidHandlerManager;
    //End variables IMekanismFluidHandler

    //Variables for handling IMekanismStrictEnergyHandler
    protected final EnergyHandlerManager energyHandlerManager;
    private FloatingLong lastEnergyReceived = FloatingLong.ZERO;
    //End variables IMekanismStrictEnergyHandler

    //Variables for handling IMekanismHeatHandler
    protected final HeatHandlerManager heatHandlerManager;
    //End variables for IMekanismHeatHandler

    //Variables for handling ITileSecurity
    private TileComponentSecurity securityComponent;
    //End variables ITileSecurity

    //Variables for handling ITileActive
    private boolean currentActive;
    private int updateDelay;
    protected IntSupplier delaySupplier = MekanismConfig.general.blockDeactivationDelay;
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
        capabilityHandlerManagers.add(gasHandlerManager = getInitialGasManager());
        capabilityHandlerManagers.add(infusionHandlerManager = getInitialInfusionManager());
        capabilityHandlerManagers.add(pigmentHandlerManager = getInitialPigmentManager());
        capabilityHandlerManagers.add(slurryHandlerManager = getInitialSlurryManager());
        capabilityHandlerManagers.add(fluidHandlerManager = new FluidHandlerManager(getInitialFluidTanks(), this));
        capabilityHandlerManagers.add(energyHandlerManager = new EnergyHandlerManager(getInitialEnergyContainers(), this));
        capabilityHandlerManagers.add(heatHandlerManager = new HeatHandlerManager(getInitialHeatCapacitors(), this));
        capabilityHandlerManagers.add(itemHandlerManager = new ItemHandlerManager(getInitialInventory(), this));
        addCapabilityResolvers(capabilityHandlerManagers);
        frequencyComponent = new TileComponentFrequency(this);
        if (supportsUpgrades()) {
            upgradeComponent = new TileComponentUpgrade(this, UpgradeInventorySlot.of(this, getSupportedUpgrade()));
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
        supportsUpgrades = Attribute.has(block, AttributeUpgradeSupport.class);
        canBeUpgraded = Attribute.has(block, AttributeUpgradeable.class);
        isDirectional = Attribute.has(block, AttributeStateFacing.class);
        supportsRedstone = Attribute.has(block, AttributeRedstone.class);
        hasSound = Attribute.has(block, AttributeSound.class);
        hasGui = Attribute.has(block, AttributeGui.class);
        hasSecurity = Attribute.has(block, AttributeSecurity.class);
        isActivatable = hasSound || Attribute.has(block, AttributeStateActive.class);
        supportsComparator = Attribute.has(block, AttributeComparator.class);
    }

    /**
     * Sets variables up, called immediately after {@link #setSupportedTypes(Block)} but before any things start being created.
     *
     * @implNote This method should be used for setting any variables that would normally be set directly, except that gets run to late to set things up properly in our
     * constructor.
     */
    protected void presetVariables() {
    }

    public Block getBlockType() {
        return blockProvider.getBlock();
    }

    /**
     * Should data related to the given type be persisted in this tile save
     */
    public boolean persists(SubstanceType type) {
        return type.canHandle(this);
    }

    /**
     * Should data related to the given type be saved to the item and synced to the client in the GUI
     */
    public boolean handles(SubstanceType type) {
        return persists(type);
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
        return itemHandlerManager.canHandle();
    }

    @Override
    public final boolean canHandleGas() {
        return gasHandlerManager.canHandle();
    }

    @Override
    public final boolean canHandleInfusion() {
        return infusionHandlerManager.canHandle();
    }

    @Override
    public final boolean canHandlePigment() {
        return pigmentHandlerManager.canHandle();
    }

    @Override
    public final boolean canHandleSlurry() {
        return slurryHandlerManager.canHandle();
    }

    @Override
    public final boolean canHandleFluid() {
        return fluidHandlerManager.canHandle();
    }

    @Override
    public final boolean canHandleEnergy() {
        return energyHandlerManager.canHandle();
    }

    @Override
    public final boolean canHandleHeat() {
        return heatHandlerManager.canHandle();
    }

    public void addComponent(ITileComponent component) {
        components.add(component);
        if (component instanceof TileComponentConfig) {
            addConfigComponent((TileComponentConfig) component);
        }
    }

    public List<ITileComponent> getComponents() {
        return components;
    }

    @Nonnull
    public ITextComponent getName() {
        return TextComponentUtil.translate(Util.makeTranslationKey("container", getBlockType().getRegistryName()));
    }

    @Override
    public void markDirtyComparator() {
        //Only update the comparator state if we support comparators
        if (supportsComparator() && !getBlockState().isAir(world, pos)) {
            int newRedstoneLevel = getRedstoneLevel();
            if (newRedstoneLevel != currentRedstoneLevel) {
                currentRedstoneLevel = newRedstoneLevel;
                notifyComparatorChange();
            }
        }
    }

    protected void notifyComparatorChange() {
        world.updateComparatorOutputLevel(pos, getBlockType());
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
                    if (player.isSneaking()) {
                        WorldUtils.dismantleBlock(state, getWorld(), pos, this);
                        return WrenchResult.DISMANTLED;
                    }
                    //Special ITileDirectional handling
                    if (isDirectional() && Attribute.get(getBlockType(), AttributeStateFacing.class).canRotate()) {
                        setFacing(getDirection().rotateY());
                    }
                    return WrenchResult.SUCCESS;
                }
            }
        }
        return WrenchResult.PASS;
    }

    public ActionResultType openGui(PlayerEntity player) {
        //Everything that calls this has isRemote being false but add the check just in case anyways
        if (hasGui() && !isRemote() && !player.isSneaking()) {
            if (hasSecurity() && !SecurityUtils.canAccess(player, this)) {
                SecurityUtils.displayNoAccess(player);
                return ActionResultType.FAIL;
            }
            //Pass on this activation if the player is rotating with a configurator
            ItemStack stack = player.getHeldItemMainhand();
            if (isDirectional() && !stack.isEmpty() && stack.getItem() instanceof ItemConfigurator) {
                ItemConfigurator configurator = (ItemConfigurator) stack.getItem();
                if (configurator.getMode(stack) == ItemConfigurator.ConfiguratorMode.ROTATE) {
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
                    WorldUtils.updateBlock(getWorld(), getPos(), this);
                }
            }
            onUpdateClient();
        } else {
            if (isActivatable()) {
                if (updateDelay > 0) {
                    updateDelay--;
                    if (updateDelay == 0 && getClientActive() != currentActive) {
                        //If it doesn't match and we are done with the delay period, then update it
                        world.setBlockState(pos, Attribute.setActive(getBlockState(), currentActive));
                    }
                }
            }
            onUpdateServer();
            if (persists(SubstanceType.HEAT)) {
                // update heat after server tick as we now have simulate changes
                // we use persists, as only one reference should update
                updateHeatCapacitors(null);
            }
            lastEnergyReceived = FloatingLong.ZERO;
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
        if (!isRemote() && MekanismConfig.general.radiationEnabled.get()) {
            //If we are on a server and radiation is enabled dump all gas tanks with radioactive materials
            dumpRadiation();
        }
    }

    protected void dumpRadiation() {
        List<IGasTank> gasTanks = getGasTanks(null);
        for (IGasTank gasTank : gasTanks) {
            //For each tank in this tile, if it isn't empty and the stored gas is radioactive
            // then dump that radioactivity into the air
            if (!gasTank.isEmpty()) {
                GasStack stack = gasTank.getStack();
                if (stack.has(GasAttributes.Radiation.class)) {
                    double radioactivity = stack.get(GasAttributes.Radiation.class).getRadioactivity();
                    Mekanism.radiationManager.radiate(Coord4D.get(this), radioactivity * stack.getAmount());
                }
            }
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        for (ITileComponent component : components) {
            component.onChunkUnload();
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

    public void onPlace() {
    }

    @Override
    public void read(@Nonnull BlockState state, @Nonnull CompoundNBT nbtTags) {
        super.read(state, nbtTags);
        NBTUtils.setBooleanIfPresent(nbtTags, NBTConstants.REDSTONE, value -> redstone = value);
        for (ITileComponent component : components) {
            component.read(nbtTags);
        }
        if (supportsRedstone()) {
            NBTUtils.setEnumIfPresent(nbtTags, NBTConstants.CONTROL_TYPE, RedstoneControl::byIndexStatic, type -> controlType = type);
        }
        if (hasInventory() && persistInventory()) {
            DataHandlerUtils.readContainers(getInventorySlots(null), nbtTags.getList(NBTConstants.ITEMS, NBT.TAG_COMPOUND));
        }
        for (SubstanceType type : EnumUtils.SUBSTANCES) {
            if (type.canHandle(this) && persists(type)) {
                type.read(this, nbtTags);
            }
        }
        if (isActivatable()) {
            NBTUtils.setBooleanIfPresent(nbtTags, NBTConstants.ACTIVE_STATE, value -> currentActive = value);
            NBTUtils.setIntIfPresent(nbtTags, NBTConstants.UPDATE_DELAY, value -> updateDelay = value);
        }
        if (supportsComparator()) {
            NBTUtils.setIntIfPresent(nbtTags, NBTConstants.CURRENT_REDSTONE, value -> currentRedstoneLevel = value);
        }
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putBoolean(NBTConstants.REDSTONE, redstone);
        for (ITileComponent component : components) {
            component.write(nbtTags);
        }
        if (supportsRedstone()) {
            nbtTags.putInt(NBTConstants.CONTROL_TYPE, controlType.ordinal());
        }
        if (hasInventory() && persistInventory()) {
            nbtTags.put(NBTConstants.ITEMS, DataHandlerUtils.writeContainers(getInventorySlots(null)));
        }

        for (SubstanceType type : EnumUtils.SUBSTANCES) {
            if (type.canHandle(this) && persists(type)) {
                type.write(this, nbtTags);
            }
        }

        if (isActivatable()) {
            nbtTags.putBoolean(NBTConstants.ACTIVE_STATE, currentActive);
            nbtTags.putInt(NBTConstants.UPDATE_DELAY, updateDelay);
        }
        if (supportsComparator()) {
            nbtTags.putInt(NBTConstants.CURRENT_REDSTONE, currentRedstoneLevel);
        }
        return nbtTags;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        // setup dynamic container syncing
        SyncMapper.setup(container, getClass(), () -> this);

        for (ITileComponent component : components) {
            component.trackForMainContainer(container);
        }
        if (supportsRedstone()) {
            container.track(SyncableEnum.create(RedstoneControl::byIndexStatic, RedstoneControl.DISABLED, () -> controlType, value -> controlType = value));
        }
        if (canHandleGas() && handles(SubstanceType.GAS)) {
            List<IGasTank> gasTanks = getGasTanks(null);
            for (IGasTank gasTank : gasTanks) {
                container.track(SyncableGasStack.create(gasTank));
            }
        }
        if (canHandleInfusion() && handles(SubstanceType.INFUSION)) {
            List<IInfusionTank> infusionTanks = getInfusionTanks(null);
            for (IInfusionTank infusionTank : infusionTanks) {
                container.track(SyncableInfusionStack.create(infusionTank));
            }
        }
        if (canHandlePigment() && handles(SubstanceType.PIGMENT)) {
            List<IPigmentTank> pigmentTanks = getPigmentTanks(null);
            for (IPigmentTank pigmentTank : pigmentTanks) {
                container.track(SyncablePigmentStack.create(pigmentTank));
            }
        }
        if (canHandleSlurry() && handles(SubstanceType.SLURRY)) {
            List<ISlurryTank> slurryTanks = getSlurryTanks(null);
            for (ISlurryTank slurryTank : slurryTanks) {
                container.track(SyncableSlurryStack.create(slurryTank));
            }
        }
        if (canHandleFluid() && handles(SubstanceType.FLUID)) {
            List<IExtendedFluidTank> fluidTanks = getFluidTanks(null);
            for (IExtendedFluidTank fluidTank : fluidTanks) {
                container.track(SyncableFluidStack.create(fluidTank));
            }
        }
        if (canHandleHeat() && handles(SubstanceType.HEAT)) {
            List<IHeatCapacitor> heatCapacitors = getHeatCapacitors(null);
            for (IHeatCapacitor capacitor : heatCapacitors) {
                container.track(SyncableDouble.create(capacitor::getHeat, capacitor::setHeat));
                if (capacitor instanceof BasicHeatCapacitor) {
                    container.track(SyncableDouble.create(capacitor::getHeatCapacity, capacity -> ((BasicHeatCapacitor) capacitor).setHeatCapacity(capacity, false)));
                }
            }
        }
        if (canHandleEnergy() && handles(SubstanceType.ENERGY)) {
            container.track(SyncableFloatingLong.create(this::getInputRate, this::setInputRate));
            List<IEnergyContainer> energyContainers = getEnergyContainers(null);
            for (IEnergyContainer energyContainer : energyContainers) {
                container.track(SyncableFloatingLong.create(energyContainer::getEnergy, energyContainer::setEnergy));
                if (energyContainer instanceof MachineEnergyContainer) {
                    MachineEnergyContainer<?> machineEnergy = (MachineEnergyContainer<?>) energyContainer;
                    if (supportsUpgrades() || machineEnergy.adjustableRates()) {
                        container.track(SyncableFloatingLong.create(machineEnergy::getMaxEnergy, machineEnergy::setMaxEnergy));
                        container.track(SyncableFloatingLong.create(machineEnergy::getEnergyPerTick, machineEnergy::setEnergyPerTick));
                    }
                }
            }
        }
    }

    @Nonnull
    @Override
    public CompoundNBT getReducedUpdateTag() {
        CompoundNBT updateTag = super.getReducedUpdateTag();
        for (ITileComponent component : components) {
            component.addToUpdateTag(updateTag);
        }
        return updateTag;
    }

    @Override
    public void handleUpdateTag(BlockState state, @Nonnull CompoundNBT tag) {
        super.handleUpdateTag(state, tag);
        for (ITileComponent component : components) {
            component.readFromUpdateTag(tag);
        }
    }

    public void onNeighborChange(Block block, BlockPos neighborPos) {
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
    public TileComponentFrequency getFrequencyComponent() {
        return frequencyComponent;
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
            Direction facing = Attribute.getFacing(state);
            if (facing != null) {
                return facing;
            } else if (!getType().isValidBlock(state.getBlock())) {
                //This is probably always true if we couldn't get the direction it is facing
                // but double check just in case before logging
                Mekanism.logger.warn("Error invalid block for tile {} at {} in {}. Unable to get direction, falling back to north, "
                                     + "things will probably not work correctly. This is almost certainly due to another mod incorrectly "
                                     + "trying to move this tile and not properly updating the position.", getType().getRegistryName(), pos, world);
            }
        }
        //TODO: Remove, give it some better default, or allow it to be null
        return Direction.NORTH;
    }

    @Override
    public void setFacing(@Nonnull Direction direction) {
        if (isDirectional()) {
            BlockState state = Attribute.setFacing(getBlockState(), direction);
            if (world != null && state != null) {
                world.setBlockState(pos, state);
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
            markDirty(false);
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
                return MekanismUtils.redstoneLevelFromContents(getInventorySlots(null));
            }
            //TODO: Do we want some other defaults as well?
        }
        return 0;
    }

    @Override
    public int getCurrentRedstoneLevel() {
        return currentRedstoneLevel;
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
        if (upgrade == Upgrade.SPEED) {
            for (IEnergyContainer energyContainer : getEnergyContainers(null)) {
                if (energyContainer instanceof MachineEnergyContainer) {
                    ((MachineEnergyContainer<?>) energyContainer).updateEnergyPerTick();
                }
            }
        } else if (upgrade == Upgrade.ENERGY) {
            for (IEnergyContainer energyContainer : getEnergyContainers(null)) {
                if (energyContainer instanceof MachineEnergyContainer) {
                    MachineEnergyContainer<?> machineEnergy = (MachineEnergyContainer<?>) energyContainer;
                    machineEnergy.updateMaxEnergy();
                    machineEnergy.updateEnergyPerTick();
                }
            }
        }
    }
    //End methods ITileUpgradable

    //Methods for implementing ITileContainer
    @Nullable
    protected IInventorySlotHolder getInitialInventory() {
        return null;
    }

    @Nonnull
    @Override
    public final List<IInventorySlot> getInventorySlots(@Nullable Direction side) {
        return itemHandlerManager.getContainers(side);
    }

    @Override
    public void onContentsChanged() {
        markDirty(false);
    }

    @Override
    public void setInventory(ListNBT nbtTags, Object... data) {
        if (nbtTags != null && !nbtTags.isEmpty() && persistInventory()) {
            DataHandlerUtils.readContainers(getInventorySlots(null), nbtTags);
        }
    }

    @Override
    public ListNBT getInventory(Object... data) {
        return persistInventory() ? DataHandlerUtils.writeContainers(getInventorySlots(null)) : new ListNBT();
    }

    /**
     * Should the inventory be persisted in this tile save
     */
    public boolean persistInventory() {
        return hasInventory();
    }
    //End methods ITileContainer

    //Methods for implementing IGasTile
    @Nonnull
    @Override
    public GasHandlerManager getGasManager() {
        return gasHandlerManager;
    }
    //End methods IGasTile

    //Methods for implementing IInfusionTile
    @Nonnull
    @Override
    public InfusionHandlerManager getInfusionManager() {
        return infusionHandlerManager;
    }
    //End methods IInfusionTile

    //Methods for implementing IPigmentTile
    @Nonnull
    @Override
    public PigmentHandlerManager getPigmentManager() {
        return pigmentHandlerManager;
    }
    //End methods IPigmentTile

    //Methods for implementing ISlurryTile
    @Nonnull
    @Override
    public SlurryHandlerManager getSlurryManager() {
        return slurryHandlerManager;
    }
    //End methods ISlurryTile

    //Methods for implementing IMekanismFluidHandler
    @Nullable
    protected IFluidTankHolder getInitialFluidTanks() {
        return null;
    }

    @Nonnull
    @Override
    public final List<IExtendedFluidTank> getFluidTanks(@Nullable Direction side) {
        return fluidHandlerManager.getContainers(side);
    }
    //End methods IMekanismFluidHandler

    //Methods for implementing IMekanismStrictEnergyHandler
    @Nullable
    protected IEnergyContainerHolder getInitialEnergyContainers() {
        return null;
    }

    @Nonnull
    @Override
    public final List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) {
        return energyHandlerManager.getContainers(side);
    }

    @Nonnull
    @Override
    public FloatingLong insertEnergy(int container, @Nonnull FloatingLong amount, @Nullable Direction side, @Nonnull Action action) {
        IEnergyContainer energyContainer = getEnergyContainer(container, side);
        if (energyContainer == null) {
            return amount;
        }
        FloatingLong remainder = energyContainer.insert(amount, action, side == null ? AutomationType.INTERNAL : AutomationType.EXTERNAL);
        if (action.execute()) {
            lastEnergyReceived = lastEnergyReceived.plusEqual(amount.subtract(remainder));
        }
        return remainder;
    }

    public FloatingLong getInputRate() {
        return lastEnergyReceived;
    }

    protected void setInputRate(FloatingLong inputRate) {
        this.lastEnergyReceived = inputRate;
    }
    //End methods IMekanismStrictEnergyHandler

    //Methods for implementing IInWorldHeatHandler
    @Nullable
    protected IHeatCapacitorHolder getInitialHeatCapacitors() {
        return null;
    }

    @Nullable
    @Override
    public IHeatHandler getAdjacent(Direction side) {
        if (canHandleHeat() && getHeatCapacitorCount(side) > 0) {
            TileEntity adj = WorldUtils.getTileEntity(getWorld(), getPos().offset(side));
            return CapabilityUtils.getCapability(adj, Capabilities.HEAT_HANDLER_CAPABILITY, side.getOpposite()).resolve().orElse(null);
        }
        return null;
    }

    @Nonnull
    @Override
    public final List<IHeatCapacitor> getHeatCapacitors(@Nullable Direction side) {
        return heatHandlerManager.getContainers(side);
    }
    //End methods for IInWorldHeatHandler

    //Methods for implementing ITileSecurity
    @Override
    public TileComponentSecurity getSecurity() {
        return securityComponent;
    }

    @Override
    public void onSecurityChanged(SecurityMode old, SecurityMode mode) {
        //If the mode changed and this tile has a gui
        if (old != mode && hasGui()) {
            //and the new security mode is more restrictive than the old one
            if (old == SecurityMode.PUBLIC || (old == SecurityMode.TRUSTED && mode == SecurityMode.PRIVATE)) {
                //and there are players using this tile
                if (!playersUsing.isEmpty()) {
                    //then double check that all the players are actually supposed to be able to access the GUI
                    for (PlayerEntity player : new ObjectOpenHashSet<>(playersUsing)) {
                        if (!SecurityUtils.canAccess(player, this)) {
                            //and if they can't then boot them out
                            player.closeScreen();
                        }
                    }
                }
            }
        }
    }
    //End methods ITileSecurity

    //Methods for implementing ITileActive
    @Override
    public boolean getActive() {
        return isRemote() ? getClientActive() : currentActive;
    }

    private boolean getClientActive() {
        return isActivatable() && Attribute.isActive(getBlockState());
    }

    @Override
    public void setActive(boolean active) {
        if (isActivatable() && active != currentActive) {
            BlockState state = getBlockState();
            Block block = state.getBlock();
            if (Attribute.has(block, AttributeStateActive.class)) {
                currentActive = active;
                if (getClientActive() != active) {
                    if (active) {
                        //Always turn on instantly
                        state = Attribute.setActive(state, true);
                        world.setBlockState(pos, state);
                    } else {
                        // if the update delay is already zero, we can go ahead and set the state
                        if (updateDelay == 0) {
                            world.setBlockState(pos, Attribute.setActive(getBlockState(), currentActive));
                        }
                        // we always reset the update delay when turning off
                        updateDelay = delaySupplier.getAsInt();
                    }
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
                activeSound = SoundHandler.startTileSound(soundEvent, getSoundCategory(), getInitialVolume(), getSoundPos());
            }
            // Always reset the cooldown; either we just attempted to play a sound or we're fully muffled; either way
            // we don't want to try again
            playSoundCooldown = 20;
        } else if (activeSound != null) {
            SoundHandler.stopTileSound(getSoundPos());
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