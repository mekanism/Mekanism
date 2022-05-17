package mekanism.common.tile.base;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntSupplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.DataHandlerUtils;
import mekanism.api.IConfigCardAccess;
import mekanism.api.IContentsListener;
import mekanism.api.MekanismAPI;
import mekanism.api.NBTConstants;
import mekanism.api.Upgrade;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.heat.IHeatHandler;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.api.math.FloatingLong;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.security.SecurityMode;
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
import mekanism.common.block.attribute.Attributes.AttributeComputerIntegration;
import mekanism.common.block.attribute.Attributes.AttributeRedstone;
import mekanism.common.block.attribute.Attributes.AttributeSecurity;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.heat.BasicHeatCapacitor;
import mekanism.common.capabilities.heat.CachedAmbientTemperature;
import mekanism.common.capabilities.heat.ITileHeatHandler;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.heat.IHeatCapacitorHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
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
import mekanism.common.integration.computer.BoundComputerMethod;
import mekanism.common.integration.computer.ComputerCapabilityHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodMapper;
import mekanism.common.integration.computer.ComputerMethodMapper.MethodRestriction;
import mekanism.common.integration.computer.IComputerTile;
import mekanism.common.integration.computer.annotation.ComputerMethod;
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
import mekanism.common.item.ItemConfigurationCard;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.lib.chunkloading.IChunkLoader;
import mekanism.common.lib.frequency.IFrequencyHandler;
import mekanism.common.lib.frequency.TileComponentFrequency;
import mekanism.common.lib.security.ISecurityTile;
import mekanism.common.tile.component.ITileComponent;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentSecurity;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.tile.interfaces.IComparatorSupport;
import mekanism.common.tile.interfaces.ISustainedData;
import mekanism.common.tile.interfaces.ISustainedInventory;
import mekanism.common.tile.interfaces.ITierUpgradable;
import mekanism.common.tile.interfaces.ITileActive;
import mekanism.common.tile.interfaces.ITileDirectional;
import mekanism.common.tile.interfaces.ITileRadioactive;
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
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

//TODO: We need to move the "supports" methods into the source interfaces so that we make sure they get checked before being used
public abstract class TileEntityMekanism extends CapabilityTileEntity implements IFrequencyHandler, ITileDirectional, IConfigCardAccess, ITileActive, ITileSound,
      ITileRedstone, ISecurityTile, IMekanismInventory, ISustainedInventory, ITileUpgradable, ITierUpgradable, IComparatorSupport, ITrackableContainer,
      IMekanismFluidHandler, IMekanismStrictEnergyHandler, ITileHeatHandler, IGasTile, IInfusionTile, IPigmentTile, ISlurryTile, IComputerTile, ITileRadioactive,
      Nameable {

    /**
     * The players currently using this block.
     */
    public final Set<Player> playersUsing = new ObjectOpenHashSet<>();

    /**
     * A timer used to send packets to clients.
     */
    public int ticker;
    private final List<ICapabilityHandlerManager<?>> capabilityHandlerManagers = new ArrayList<>();
    private final List<ITileComponent> components = new ArrayList<>();

    protected final IBlockProvider blockProvider;

    private boolean supportsComparator;
    private boolean supportsComputers;
    private boolean supportsUpgrades;
    private boolean supportsRedstone;
    private boolean canBeUpgraded;
    private boolean isDirectional;
    private boolean isActivatable;
    private boolean hasSecurity;
    private boolean hasSound;
    private boolean hasGui;
    private boolean hasChunkloader;
    private boolean nameable;

    @Nullable
    private Component customName;

    //Methods for implementing ITileDirectional
    @Nullable
    private Direction cachedDirection;
    //End variables ITileRedstone

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
    private boolean updateComparators;
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
    private float radiationScale;
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
    private final FluidHandlerManager fluidHandlerManager;
    //End variables IMekanismFluidHandler

    //Variables for handling IMekanismStrictEnergyHandler
    private final EnergyHandlerManager energyHandlerManager;
    private FloatingLong lastEnergyReceived = FloatingLong.ZERO;
    //End variables IMekanismStrictEnergyHandler

    //Variables for handling IMekanismHeatHandler
    protected final CachedAmbientTemperature ambientTemperature;
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
    private SoundInstance activeSound;
    private int playSoundCooldown = 0;
    //End variables ITileSound

    public TileEntityMekanism(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
        super(((IHasTileEntity<? extends BlockEntity>) blockProvider.getBlock()).getTileType(), pos, state);
        this.blockProvider = blockProvider;
        Block block = this.blockProvider.getBlock();
        setSupportedTypes(block);
        presetVariables();
        IContentsListener saveOnlyListener = this::markForSave;
        capabilityHandlerManagers.add(gasHandlerManager = getInitialGasManager(getListener(SubstanceType.GAS, saveOnlyListener)));
        capabilityHandlerManagers.add(infusionHandlerManager = getInitialInfusionManager(getListener(SubstanceType.INFUSION, saveOnlyListener)));
        capabilityHandlerManagers.add(pigmentHandlerManager = getInitialPigmentManager(getListener(SubstanceType.PIGMENT, saveOnlyListener)));
        capabilityHandlerManagers.add(slurryHandlerManager = getInitialSlurryManager(getListener(SubstanceType.SLURRY, saveOnlyListener)));
        capabilityHandlerManagers.add(fluidHandlerManager = new FluidHandlerManager(getInitialFluidTanks(getListener(SubstanceType.FLUID, saveOnlyListener)), this));
        capabilityHandlerManagers.add(energyHandlerManager = new EnergyHandlerManager(getInitialEnergyContainers(getListener(SubstanceType.ENERGY, saveOnlyListener)), this));
        capabilityHandlerManagers.add(itemHandlerManager = new ItemHandlerManager(getInitialInventory(getListener(null, saveOnlyListener)), this));
        CachedAmbientTemperature ambientTemperature = new CachedAmbientTemperature(this::getLevel, this::getBlockPos);
        capabilityHandlerManagers.add(heatHandlerManager = new HeatHandlerManager(getInitialHeatCapacitors(getListener(SubstanceType.HEAT, saveOnlyListener), ambientTemperature), this));
        this.ambientTemperature = canHandleHeat() ? ambientTemperature : null;
        addCapabilityResolvers(capabilityHandlerManagers);
        frequencyComponent = new TileComponentFrequency(this);
        if (supportsUpgrades()) {
            upgradeComponent = new TileComponentUpgrade(this);
        }
        if (hasSecurity()) {
            securityComponent = new TileComponentSecurity(this);
            addCapabilityResolver(BasicCapabilityResolver.security(this));
        }
        if (hasSound()) {
            soundEvent = Attribute.get(block, AttributeSound.class).getSoundEvent();
        } else {
            soundEvent = null;
        }
        ComputerCapabilityHelper.addComputerCapabilities(this, this::addCapabilityResolver);
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
        supportsComputers = Mekanism.hooks.computerCompatEnabled() && Attribute.has(block, AttributeComputerIntegration.class);
        hasChunkloader = this instanceof IChunkLoader;
        nameable = hasGui() && !Attribute.get(getBlockType(), AttributeGui.class).hasCustomName();
    }

    /**
     * Sets variables up, called immediately after {@link #setSupportedTypes(Block)} but before any things start being created.
     *
     * @implNote This method should be used for setting any variables that would normally be set directly, except that gets run too late to set things up properly in our
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
    public final boolean hasComputerSupport() {
        return supportsComputers;
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
        if (component instanceof TileComponentConfig config) {
            addConfigComponent(config);
        }
    }

    public List<ITileComponent> getComponents() {
        return components;
    }

    @Nonnull
    @Override
    @SuppressWarnings("ConstantConditions")
    public Component getName() {
        return hasCustomName() ? getCustomName() : TextComponentUtil.build(getBlockType());
    }

    @Nonnull
    @Override
    @SuppressWarnings("ConstantConditions")
    public Component getDisplayName() {
        if (isNameable()) {
            return hasCustomName() ? getCustomName() : TextComponentUtil.translate(Util.makeDescriptionId("container", getBlockType().getRegistryName()));
        }
        return TextComponentUtil.build(getBlockType());
    }

    @Nullable
    @Override
    public Component getCustomName() {
        return isNameable() ? customName : null;
    }

    public void setCustomName(@Nullable Component name) {
        if (isNameable()) {
            this.customName = name;
        }
    }

    /**
     * This should return false if naming it would be pointless, in order to save on NBT data on both the tile entity and the block item.
     *
     * @return if the tile entity can be named
     */
    public boolean isNameable() {
        return nameable;
    }

    @Override
    public void markDirtyComparator() {
        //Only mark our comparators as needing update if we support comparators
        if (supportsComparator()) {
            updateComparators = true;
        }
    }

    protected void notifyComparatorChange() {
        level.updateNeighbourForOutputSignal(worldPosition, getBlockType());
    }

    public WrenchResult tryWrench(BlockState state, Player player, InteractionHand hand, BlockHitResult rayTrace) {
        ItemStack stack = player.getItemInHand(hand);
        if (MekanismUtils.canUseAsWrench(stack)) {
            if (hasSecurity() && !MekanismAPI.getSecurityUtils().canAccessOrDisplayError(player, this)) {
                return WrenchResult.NO_SECURITY;
            }
            if (player.isShiftKeyDown()) {
                WorldUtils.dismantleBlock(state, getLevel(), worldPosition, this);
                return WrenchResult.DISMANTLED;
            }
            //Special ITileDirectional handling
            if (isDirectional() && Attribute.get(getBlockType(), AttributeStateFacing.class).canRotate()) {
                setFacing(getDirection().getClockWise());
            }
            return WrenchResult.SUCCESS;
        }
        return WrenchResult.PASS;
    }

    public InteractionResult openGui(Player player) {
        //Everything that calls this has isRemote being false but add the check just in case anyway
        if (hasGui() && !isRemote() && !player.isShiftKeyDown()) {
            if (hasSecurity() && !MekanismAPI.getSecurityUtils().canAccessOrDisplayError(player, this)) {
                return InteractionResult.FAIL;
            }
            //Pass on this activation if the player is rotating with a configurator
            ItemStack stack = player.getMainHandItem();
            if (isDirectional() && !stack.isEmpty() && stack.getItem() instanceof ItemConfigurator configurator) {
                if (configurator.getMode(stack) == ItemConfigurator.ConfiguratorMode.ROTATE) {
                    return InteractionResult.PASS;
                }
            }
            //Pass on this activation if the player is using a configuration card (and this tile supports the capability)
            if (getCapability(Capabilities.CONFIG_CARD_CAPABILITY, null).isPresent()) {
                if (!stack.isEmpty() && stack.getItem() instanceof ItemConfigurationCard) {
                    return InteractionResult.PASS;
                }
            }

            NetworkHooks.openGui((ServerPlayer) player, Attribute.get(getBlockType(), AttributeGui.class).getProvider(this), worldPosition);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    //TODO - 1.18: Optimize what gets ticks registered to it
    public static void tickClient(Level level, BlockPos pos, BlockState state, TileEntityMekanism tile) {
        if (tile.hasSound()) {
            tile.updateSound();
        }
        tile.onUpdateClient();
        tile.ticker++;
        if (tile.supportsRedstone()) {
            tile.redstoneLastTick = tile.redstone;
        }
    }

    public static void tickServer(Level level, BlockPos pos, BlockState state, TileEntityMekanism tile) {
        tile.frequencyComponent.tickServer();
        if (tile.supportsUpgrades()) {
            tile.upgradeComponent.tickServer();
        }
        if (tile.hasSecurity()) {
            tile.securityComponent.tickServer();
        }
        if (tile.hasChunkloader) {
            ((IChunkLoader) tile).getChunkLoader().tickServer();
        }
        if (tile.isActivatable()) {
            if (tile.updateDelay > 0) {
                tile.updateDelay--;
                if (tile.updateDelay == 0 && tile.getClientActive() != tile.currentActive) {
                    //If it doesn't match, and we are done with the delay period, then update it
                    level.setBlockAndUpdate(pos, Attribute.setActive(state, tile.currentActive));
                }
            }
        }
        tile.onUpdateServer();
        tile.updateRadiationScale();
        //TODO - 1.18: More generic "needs update" flag that we set that then means we don't end up sending an update packet more than once per tick
        if (tile.persists(SubstanceType.HEAT)) {
            // update heat after server tick as we now have simulated changes
            // we use persists, as only one reference should update
            tile.updateHeatCapacitors(null);
        }
        tile.lastEnergyReceived = FloatingLong.ZERO;
        //Only update the comparator state if we support comparators and need to update comparators
        if (tile.supportsComparator() && tile.updateComparators && !state.isAir()) {
            int newRedstoneLevel = tile.getRedstoneLevel();
            if (newRedstoneLevel != tile.currentRedstoneLevel) {
                tile.currentRedstoneLevel = newRedstoneLevel;
                tile.notifyComparatorChange();
            }
            tile.updateComparators = false;
        }
        tile.ticker++;
        if (tile.supportsRedstone()) {
            tile.redstoneLastTick = tile.redstone;
        }
    }

    public void open(Player player) {
        playersUsing.add(player);
    }

    public void close(Player player) {
        playersUsing.remove(player);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        for (ITileComponent component : components) {
            component.invalidate();
        }
        if (isRemote() && hasSound()) {
            updateSound();
        }
    }

    @Override
    public void blockRemoved() {
        super.blockRemoved();
        for (ITileComponent component : components) {
            component.removed();
        }
        if (!isRemote() && MekanismAPI.getRadiationManager().isRadiationEnabled() && shouldDumpRadiation()) {
            //If we are on a server and radiation is enabled dump all gas tanks with radioactive materials
            // Note: we handle clearing radioactive contents later in drop calculation due to when things are written to NBT
            MekanismAPI.getRadiationManager().dumpRadiation(getTileCoord(), getGasTanks(null), false);
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
    @Deprecated
    public void setBlockState(@Nonnull BlockState newState) {
        super.setBlockState(newState);
        if (isDirectional()) {
            //Clear the cached direction so that we can lazily get it when we need it again
            cachedDirection = null;
        }
    }

    @Override
    public void load(@Nonnull CompoundTag nbt) {
        super.load(nbt);
        NBTUtils.setBooleanIfPresent(nbt, NBTConstants.REDSTONE, value -> redstone = value);
        for (ITileComponent component : components) {
            component.read(nbt);
        }
        loadGeneralPersistentData(nbt);
        if (hasInventory() && persistInventory()) {
            DataHandlerUtils.readContainers(getInventorySlots(null), nbt.getList(NBTConstants.ITEMS, Tag.TAG_COMPOUND));
        }
        for (SubstanceType type : EnumUtils.SUBSTANCES) {
            if (type.canHandle(this) && persists(type)) {
                type.read(this, nbt);
            }
        }
        if (isActivatable()) {
            NBTUtils.setBooleanIfPresent(nbt, NBTConstants.ACTIVE_STATE, value -> currentActive = value);
            NBTUtils.setIntIfPresent(nbt, NBTConstants.UPDATE_DELAY, value -> updateDelay = value);
        }
        if (supportsComparator()) {
            NBTUtils.setIntIfPresent(nbt, NBTConstants.CURRENT_REDSTONE, value -> currentRedstoneLevel = value);
        }
        if (isNameable()) {
            NBTUtils.setStringIfPresent(nbt, NBTConstants.CUSTOM_NAME, value -> customName = Component.Serializer.fromJson(value));
        }
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag nbtTags) {
        super.saveAdditional(nbtTags);
        nbtTags.putBoolean(NBTConstants.REDSTONE, redstone);
        for (ITileComponent component : components) {
            component.write(nbtTags);
        }
        addGeneralPersistentData(nbtTags);
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

        // Save the custom name, only if it exists and the tile can be named
        if (this.customName != null && isNameable()) {
            nbtTags.putString(NBTConstants.CUSTOM_NAME, Component.Serializer.toJson(this.customName));
        }
    }

    protected void addGeneralPersistentData(CompoundTag data) {
        if (supportsRedstone()) {
            NBTUtils.writeEnum(data, NBTConstants.CONTROL_TYPE, controlType);
        }
        if (this instanceof ISustainedData sustainedData) {
            sustainedData.writeSustainedData(data);
        }
    }

    protected void loadGeneralPersistentData(CompoundTag data) {
        if (supportsRedstone()) {
            NBTUtils.setEnumIfPresent(data, NBTConstants.CONTROL_TYPE, RedstoneControl::byIndexStatic, type -> controlType = type);
        }
        if (this instanceof ISustainedData sustainedData) {
            sustainedData.readSustainedData(data);
        }
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        // setup dynamic container syncing
        SyncMapper.INSTANCE.setup(container, getClass(), () -> this);

        for (ITileComponent component : components) {
            component.trackForMainContainer(container);
        }
        if (supportsRedstone()) {
            container.track(SyncableEnum.create(RedstoneControl::byIndexStatic, RedstoneControl.DISABLED, () -> controlType, value -> controlType = value));
        }
        boolean isClient = isRemote();
        if (canHandleGas() && handles(SubstanceType.GAS)) {
            List<IGasTank> gasTanks = getGasTanks(null);
            for (IGasTank gasTank : gasTanks) {
                container.track(SyncableGasStack.create(gasTank, isClient));
            }
        }
        if (canHandleInfusion() && handles(SubstanceType.INFUSION)) {
            List<IInfusionTank> infusionTanks = getInfusionTanks(null);
            for (IInfusionTank infusionTank : infusionTanks) {
                container.track(SyncableInfusionStack.create(infusionTank, isClient));
            }
        }
        if (canHandlePigment() && handles(SubstanceType.PIGMENT)) {
            List<IPigmentTank> pigmentTanks = getPigmentTanks(null);
            for (IPigmentTank pigmentTank : pigmentTanks) {
                container.track(SyncablePigmentStack.create(pigmentTank, isClient));
            }
        }
        if (canHandleSlurry() && handles(SubstanceType.SLURRY)) {
            List<ISlurryTank> slurryTanks = getSlurryTanks(null);
            for (ISlurryTank slurryTank : slurryTanks) {
                container.track(SyncableSlurryStack.create(slurryTank, isClient));
            }
        }
        if (canHandleFluid() && handles(SubstanceType.FLUID)) {
            List<IExtendedFluidTank> fluidTanks = getFluidTanks(null);
            for (IExtendedFluidTank fluidTank : fluidTanks) {
                container.track(SyncableFluidStack.create(fluidTank, isClient));
            }
        }
        if (canHandleHeat() && handles(SubstanceType.HEAT)) {
            List<IHeatCapacitor> heatCapacitors = getHeatCapacitors(null);
            for (IHeatCapacitor capacitor : heatCapacitors) {
                container.track(SyncableDouble.create(capacitor::getHeat, capacitor::setHeat));
                if (capacitor instanceof BasicHeatCapacitor heatCapacitor) {
                    container.track(SyncableDouble.create(capacitor::getHeatCapacity, capacity -> heatCapacitor.setHeatCapacity(capacity, false)));
                }
            }
        }
        if (canHandleEnergy() && handles(SubstanceType.ENERGY)) {
            container.track(SyncableFloatingLong.create(this::getInputRate, this::setInputRate));
            List<IEnergyContainer> energyContainers = getEnergyContainers(null);
            for (IEnergyContainer energyContainer : energyContainers) {
                container.track(SyncableFloatingLong.create(energyContainer::getEnergy, energyContainer::setEnergy));
                if (energyContainer instanceof MachineEnergyContainer<?> machineEnergy) {
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
    public CompoundTag getReducedUpdateTag() {
        CompoundTag updateTag = super.getReducedUpdateTag();
        for (ITileComponent component : components) {
            component.addToUpdateTag(updateTag);
        }
        updateTag.putFloat(NBTConstants.RADIATION, radiationScale);
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundTag tag) {
        super.handleUpdateTag(tag);
        for (ITileComponent component : components) {
            component.readFromUpdateTag(tag);
        }
        radiationScale = tag.getFloat(NBTConstants.RADIATION);
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
    @ComputerMethod(restriction = MethodRestriction.DIRECTIONAL)
    public final Direction getDirection() {
        if (isDirectional()) {
            if (cachedDirection != null) {
                return cachedDirection;
            }
            BlockState state = getBlockState();
            cachedDirection = Attribute.getFacing(state);
            if (cachedDirection != null) {
                return cachedDirection;
            } else if (!getType().isValid(state)) {
                //This is probably always true if we couldn't get the direction it is facing
                // but double check just in case before logging
                Mekanism.logger.warn("Error invalid block for tile {} at {} in {}. Unable to get direction, falling back to north, "
                                     + "things will probably not work correctly. This is almost certainly due to another mod incorrectly "
                                     + "trying to move this tile and not properly updating the position.", getType().getRegistryName(), worldPosition, level);
            }
        }
        //TODO: Remove, give it some better default, or allow it to be null
        return Direction.NORTH;
    }

    @Override
    public void setFacing(@Nonnull Direction direction) {
        if (isDirectional() && direction != cachedDirection && level != null) {
            cachedDirection = direction;
            BlockState state = Attribute.setFacing(getBlockState(), direction);
            if (state != null) {
                level.setBlockAndUpdate(worldPosition, state);
            }
        }
    }
    //End methods ITileDirectional

    //Methods for implementing ITileRedstone
    @Override
    @ComputerMethod(nameOverride = "getRedstoneMode", restriction = MethodRestriction.REDSTONE_CONTROL)
    public RedstoneControl getControlType() {
        return controlType;
    }

    @Override
    public void setControlType(@Nonnull RedstoneControl type) {
        if (supportsRedstone()) {
            controlType = Objects.requireNonNull(type);
            markForSave();
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
        boolean power = level.hasNeighborSignal(getBlockPos());
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

    /**
     * @param type Type or {@code null} for items.
     *
     * @implNote It can be assumed {@link #supportsComparator()} is true before this is called.
     */
    protected boolean makesComparatorDirty(@Nullable SubstanceType type) {
        //Assume that items make it dirty unless otherwise overridden, as we use this before we can call hasInventory
        // and if we aren't using an inventory as our comparator thing we will be overriding this method anyway
        // and if we don't have an inventory we can't assign this listener to anything as adding slots and assigning it
        // is what binds the listener to the main tile
        return type == null;
    }

    protected final IContentsListener getListener(@Nullable SubstanceType type, IContentsListener saveOnlyListener) {
        //If we don't support comparators we can just skip having a special one that only marks for save as our
        // setChanged won't actually do anything so there is no reason to bother creating a save only listener
        return !supportsComparator() || makesComparatorDirty(type) ? this : saveOnlyListener;
    }

    @Override
    @ComputerMethod(nameOverride = "getComparatorLevel", restriction = MethodRestriction.COMPARATOR)
    public int getCurrentRedstoneLevel() {
        return currentRedstoneLevel;
    }
    //End methods IComparatorSupport

    //Methods for implementing ITileUpgradable
    @Nonnull
    @Override
    public Set<Upgrade> getSupportedUpgrade() {
        if (supportsUpgrades()) {
            return Attribute.get(getBlockType(), AttributeUpgradeSupport.class).supportedUpgrades();
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
                if (energyContainer instanceof MachineEnergyContainer<?> machineEnergy) {
                    machineEnergy.updateEnergyPerTick();
                }
            }
        } else if (upgrade == Upgrade.ENERGY) {
            for (IEnergyContainer energyContainer : getEnergyContainers(null)) {
                if (energyContainer instanceof MachineEnergyContainer<?> machineEnergy) {
                    machineEnergy.updateMaxEnergy();
                    machineEnergy.updateEnergyPerTick();
                }
            }
        }
    }
    //End methods ITileUpgradable

    //Methods for implementing ITileContainer
    @Nullable
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        return null;
    }

    @Nonnull
    @Override
    public final List<IInventorySlot> getInventorySlots(@Nullable Direction side) {
        return itemHandlerManager.getContainers(side);
    }

    @Override
    public void onContentsChanged() {
        setChanged();
    }

    @Override
    public void setInventory(ListTag nbtTags, Object... data) {
        if (nbtTags != null && !nbtTags.isEmpty() && persistInventory()) {
            DataHandlerUtils.readContainers(getInventorySlots(null), nbtTags);
        }
    }

    @Override
    public ListTag getInventory(Object... data) {
        return persistInventory() ? DataHandlerUtils.writeContainers(getInventorySlots(null)) : new ListTag();
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

    public boolean shouldDumpRadiation() {
        return true;
    }

    /**
     * @apiNote Only call on server.
     */
    private void updateRadiationScale() {
        if (shouldDumpRadiation()) {
            float scale = ITileRadioactive.calculateRadiationScale(getGasTanks(null));
            if (Math.abs(scale - radiationScale) > 0.05F) {
                radiationScale = scale;
                sendUpdatePacket();
            }
        }
    }

    @Override
    public float getRadiationScale() {
        return MekanismAPI.getRadiationManager().isRadiationEnabled() ? radiationScale : 0;
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
    protected IFluidTankHolder getInitialFluidTanks(IContentsListener listener) {
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
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
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
    protected IHeatCapacitorHolder getInitialHeatCapacitors(IContentsListener listener, CachedAmbientTemperature ambientTemperature) {
        return null;
    }

    @Override
    public double getAmbientTemperature(@Nonnull Direction side) {
        if (canHandleHeat() && ambientTemperature != null) {
            return ambientTemperature.getTemperature(side);
        }
        return ITileHeatHandler.super.getAmbientTemperature(side);
    }

    @Nullable
    @Override
    public IHeatHandler getAdjacent(@Nonnull Direction side) {
        if (canHandleHeat() && getHeatCapacitorCount(side) > 0) {
            BlockEntity adj = WorldUtils.getTileEntity(getLevel(), getBlockPos().relative(side));
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

    //Methods for implementing IConfigCardAccess
    @Override
    public String getConfigCardName() {
        return getBlockType().getDescriptionId();
    }

    @Override
    public CompoundTag getConfigurationData(Player player) {
        CompoundTag data = new CompoundTag();
        addGeneralPersistentData(data);
        getFrequencyComponent().writeConfiguredFrequencies(data);
        return data;
    }

    @Override
    public void setConfigurationData(Player player, CompoundTag data) {
        loadGeneralPersistentData(data);
        getFrequencyComponent().readConfiguredFrequencies(player, data);
    }

    @Override
    public BlockEntityType<?> getConfigurationDataType() {
        return getType();
    }

    @Override
    public void configurationDataSet() {
        setChanged();
        invalidateCachedCapabilities();
        sendUpdatePacket();
        WorldUtils.notifyLoadedNeighborsOfTileChange(getLevel(), getTilePos());
    }
    //End methods IConfigCardAccess

    //Methods for implementing ITileSecurity
    @Override
    public TileComponentSecurity getSecurity() {
        return securityComponent;
    }

    @Override
    public void onSecurityChanged(@Nonnull SecurityMode old, @Nonnull SecurityMode mode) {
        if (!isRemote() && hasGui()) {
            SecurityUtils.INSTANCE.securityChanged(playersUsing, this, old, mode);
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
                        level.setBlockAndUpdate(worldPosition, state);
                    } else {
                        // if the update delay is already zero, we can go ahead and set the state
                        if (updateDelay == 0) {
                            level.setBlockAndUpdate(worldPosition, Attribute.setActive(getBlockState(), currentActive));
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

            // If this machine isn't fully muffled, and we don't seem to be playing a sound for it, go ahead and
            // play it
            if (!isFullyMuffled() && (activeSound == null || !Minecraft.getInstance().getSoundManager().isActive(activeSound))) {
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
        if (hasSound() && supportsUpgrade(Upgrade.MUFFLING)) {
            return getComponent().getUpgrades(Upgrade.MUFFLING) == Upgrade.MUFFLING.getMax();
        }
        return false;
    }
    //End methods ITileSound

    //Methods relating to IComputerTile
    // Note: Some methods are elsewhere if we are exposing pre-existing implementations
    @Override
    public String getComputerName() {
        if (hasComputerSupport()) {
            return Attribute.get(getBlockType(), AttributeComputerIntegration.class).name();
        }
        return "";
    }

    public void validateSecurityIsPublic() throws ComputerException {
        if (hasSecurity() && MekanismAPI.getSecurityUtils().getSecurityMode(this, isRemote()) != SecurityMode.PUBLIC) {
            throw new ComputerException("Setter not available due to machine security not being public.");
        }
    }

    @Override
    public void getComputerMethods(Map<String, BoundComputerMethod> methods) {
        IComputerTile.super.getComputerMethods(methods);
        for (ITileComponent component : components) {
            //Allow any supported components to add their computer methods as well
            // For example side config, ejector, and upgrade components
            ComputerMethodMapper.INSTANCE.getAndBindToHandler(component, methods);
        }
    }

    //TODO: If we ever end up using the part of our API that allows for multiple energy containers, it may be worth exposing
    // overloaded versions of these methods that take the container index as a parameter if anyone ends up running into a case
    // where being able to get a specific container's stored energy would be useful to their program. Alternatively we could
    // probably make use of our synthetic computer method wrapper to just add extra methods so then have it basically create
    // getEnergy, getEnergyFE for us with us only having to define getEnergy
    @ComputerMethod(nameOverride = "getEnergy", restriction = MethodRestriction.ENERGY)
    private FloatingLong getTotalEnergy() {
        return getTotalEnergy(IEnergyContainer::getEnergy);
    }

    @ComputerMethod(nameOverride = "getMaxEnergy", restriction = MethodRestriction.ENERGY)
    private FloatingLong getTotalMaxEnergy() {
        return getTotalEnergy(IEnergyContainer::getMaxEnergy);
    }

    @ComputerMethod(nameOverride = "getEnergyNeeded", restriction = MethodRestriction.ENERGY)
    private FloatingLong getTotalEnergyNeeded() {
        return getTotalEnergy(IEnergyContainer::getNeeded);
    }

    private FloatingLong getTotalEnergy(Function<IEnergyContainer, FloatingLong> getter) {
        FloatingLong total = FloatingLong.ZERO;
        List<IEnergyContainer> energyContainers = getEnergyContainers(null);
        for (IEnergyContainer energyContainer : energyContainers) {
            total = total.plusEqual(getter.apply(energyContainer));
        }
        return total;
    }

    @ComputerMethod(nameOverride = "getEnergyFilledPercentage", restriction = MethodRestriction.ENERGY)
    private double getTotalEnergyFilledPercentage() {
        FloatingLong stored = FloatingLong.ZERO;
        FloatingLong max = FloatingLong.ZERO;
        List<IEnergyContainer> energyContainers = getEnergyContainers(null);
        for (IEnergyContainer energyContainer : energyContainers) {
            stored = stored.plusEqual(energyContainer.getEnergy());
            max = max.plusEqual(energyContainer.getMaxEnergy());
        }
        return stored.divideToLevel(max);
    }

    @ComputerMethod(restriction = MethodRestriction.REDSTONE_CONTROL)
    private void setRedstoneMode(RedstoneControl type) throws ComputerException {
        validateSecurityIsPublic();
        if (type == RedstoneControl.PULSE && !canPulse()) {
            throw new ComputerException("Unsupported redstone control mode: %s", RedstoneControl.PULSE);
        }
        setControlType(type);
    }
    //End methods IComputerTile
}