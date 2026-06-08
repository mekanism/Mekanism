package mekanism.common.tile.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import mekanism.api.IConfigCardAccess;
import mekanism.api.IContentsListener;
import mekanism.api.MekanismItemAbilities;
import mekanism.api.RelativeSide;
import mekanism.api.SerializationConstants;
import mekanism.api.Upgrade;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.heat.IHeatHandler;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.radiation.IRadiationManager;
import mekanism.api.security.IBlockSecurityUtils;
import mekanism.api.security.SecurityMode;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.attachments.FilterAware;
import mekanism.common.attachments.containers.type.ContainerType;
import mekanism.common.attachments.containers.type.IContainerType;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeGui;
import mekanism.common.block.attribute.AttributeHasBounding;
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
import mekanism.common.capabilities.holder.container.IContainerHolder;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.resolver.ICapabilityResolver;
import mekanism.common.capabilities.resolver.manager.EnergyHandlerManager;
import mekanism.common.capabilities.resolver.manager.HeatHandlerManager;
import mekanism.common.capabilities.resolver.manager.ResourceHandlerManager;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.filter.FilterManager;
import mekanism.common.integration.computer.BoundMethodHolder;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.FactoryRegistry;
import mekanism.common.integration.computer.IComputerTile;
import mekanism.common.integration.computer.MethodRestriction;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.inventory.container.ITrackableContainer;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.container.sync.SyncableLargeResourceStack;
import mekanism.common.inventory.container.sync.SyncableLong;
import mekanism.common.inventory.container.sync.dynamic.SyncMapper;
import mekanism.common.item.ItemConfigurationCard;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.lib.transaction.LastEnergyTracker;
import mekanism.common.lib.chunkloading.IChunkLoader;
import mekanism.common.lib.frequency.IFrequencyHandler;
import mekanism.common.lib.frequency.TileComponentFrequency;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.lib.security.BlockSecurityUtils;
import mekanism.common.lib.security.ISecurityTile;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tile.component.ITileComponent;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentSecurity;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.tile.interfaces.IComparatorSupport;
import mekanism.common.tile.interfaces.ITierUpgradable;
import mekanism.common.tile.interfaces.ITileActive;
import mekanism.common.tile.interfaces.ITileDirectional;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import mekanism.common.tile.interfaces.ITileRadioactive;
import mekanism.common.tile.interfaces.ITileRedstone;
import mekanism.common.tile.interfaces.ITileSound;
import mekanism.common.tile.interfaces.ITileUpgradable;
import mekanism.common.upgrade.IUpgradeData;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.RegistryUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Util;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.redstone.Redstone;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

//TODO: We need to move the "supports" methods into the source interfaces so that we make sure they get checked before being used
public abstract class TileEntityMekanism extends CapabilityTileEntity implements IFrequencyHandler, ITileDirectional, IConfigCardAccess, ITileActive, ITileSound,
      ITileRedstone, ISecurityTile, ITileUpgradable, ITierUpgradable, IComparatorSupport, ITrackableContainer, ITileHeatHandler, IComputerTile, ITileRadioactive, Nameable,
      IContentsListener {

    protected static final Set<RelativeSide> BACK_ONLY = Set.of(RelativeSide.BACK);

    /**
     * The players currently using this block.
     */
    public final Set<Player> playersUsing = new HashSet<>();

    /**
     * A timer used to send packets to clients.
     */
    public int ticker;
    private final List<ITileComponent> components = new ArrayList<>();

    private final Holder<Block> blockProvider;

    private boolean supportsComparator;
    private boolean supportsComputers;
    private boolean supportsUpgrades;
    private boolean supportsRedstone;
    private boolean canBeUpgraded;
    private boolean isDirectional;
    private boolean isActivatable;
    private AttributeStateActive activeAttribute;
    private boolean hasBounding;
    private boolean hasSecurity;
    private boolean hasSound;
    private boolean hasGui;
    private boolean hasChunkloader;
    private boolean nameable;

    @Nullable
    private Component customName;
    @Nullable
    private String containerDescription;

    private boolean syncMasterToBounding;

    //Methods for implementing ITileDirectional
    @Nullable
    private Direction cachedDirection;

    //TODO: Re-evaluate if we should have this be null when we are not a directional tile?
    public final Supplier<Direction> facingSupplier = this::getDirection;
    //End variables ITileRedstone

    //Variables for handling ITileRedstone
    //TODO: Move these to private variables?
    protected boolean redstone = false;
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

    @Nullable
    protected final ResourceHandlerManager<ItemResource, IInventorySlot> itemHandlerManager;
    @Nullable
    protected final ResourceHandlerManager<FluidResource, IFluidTank> fluidHandlerManager;
    @Nullable
    protected final ResourceHandlerManager<ChemicalResource, IChemicalTank> chemicalHandlerManager;
    @Nullable
    protected final EnergyHandlerManager energyHandlerManager;

    //Variables for handling IMekanismChemicalHandler
    private float radiationScale;
    //End variables IMekanismChemicalHandler

    //Variables for handling IMekanismHeatHandler
    protected final Map<Direction, BlockCapabilityCache<IHeatHandler, @Nullable Direction>> adjacentHeatCaps;
    protected final CachedAmbientTemperature ambientTemperature;
    @Nullable
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
    protected final Supplier<SoundEvent> soundEvent;
    @Nullable
    protected SoundEvent lastSoundEvent;

    /**
     * Only used on the client
     */
    private SoundInstance activeSound;
    private int playSoundCooldown = 0;
    //End variables ITileSound

    public TileEntityMekanism(Holder<Block> blockProvider, BlockPos pos, BlockState state) {
        super(((IHasTileEntity<? extends BlockEntity>) blockProvider.value()).getTileType(), pos, state);
        this.blockProvider = blockProvider;
        setSupportedTypes(this.blockProvider);
        presetVariables();
        IContentsListener saveOnlyListener = this::markForSave;

        List<ICapabilityResolver<@Nullable Direction>> capabilityHandlerManagers = new ArrayList<>();

        IContainerHolder<IChemicalTank> initialChemicalTanks = getInitialChemicalTanks(getListener(ContainerType.CHEMICAL, saveOnlyListener));
        if (initialChemicalTanks != null) {
            capabilityHandlerManagers.add(chemicalHandlerManager = new ResourceHandlerManager<>(Capabilities.CHEMICAL, initialChemicalTanks));
        } else {
            chemicalHandlerManager = null;
        }

        IContainerHolder<IFluidTank> initialFluidTanks = getInitialFluidTanks(getListener(ContainerType.FLUID, saveOnlyListener));
        if (initialFluidTanks != null) {
            capabilityHandlerManagers.add(fluidHandlerManager = new ResourceHandlerManager<>(Capabilities.FLUID, initialFluidTanks));
        } else {
            fluidHandlerManager = null;
        }

        IEnergyContainerHolder initialEnergyContainers = getInitialEnergyContainer(getListener(ContainerType.ENERGY, saveOnlyListener));
        if (initialEnergyContainers != null) {
            capabilityHandlerManagers.add(energyHandlerManager = new EnergyHandlerManager(initialEnergyContainers, MekanismUtils.getGameTimeSupplier(this)));
        } else {
            energyHandlerManager = null;
        }

        IContainerHolder<IInventorySlot> initialInventory = getInitialInventory(getListener(ContainerType.ITEM, saveOnlyListener));
        if (initialInventory != null) {
            capabilityHandlerManagers.add(itemHandlerManager = new ResourceHandlerManager<>(Capabilities.ITEM, initialInventory));
        } else {
            itemHandlerManager = null;
        }

        CachedAmbientTemperature ambientTemperature = new CachedAmbientTemperature(this::getLevel, this::getBlockPos);
        IContainerHolder<IHeatCapacitor> initialHeatCapacitors = getInitialHeatCapacitors(getListener(ContainerType.HEAT, saveOnlyListener), ambientTemperature);
        if (initialHeatCapacitors != null) {
            capabilityHandlerManagers.add(heatHandlerManager = new HeatHandlerManager(initialHeatCapacitors, this));
        } else {
            heatHandlerManager = null;
        }
        if (canHandleHeat()) {
            adjacentHeatCaps = new EnumMap<>(Direction.class);
            this.ambientTemperature = ambientTemperature;
        } else {
            adjacentHeatCaps = Collections.emptyMap();
            this.ambientTemperature = null;
        }

        addCapabilityResolvers(capabilityHandlerManagers);
        frequencyComponent = new TileComponentFrequency(this);
        if (supportsUpgrades()) {
            upgradeComponent = new TileComponentUpgrade(this);
        }
        if (hasSecurity()) {
            securityComponent = new TileComponentSecurity(this);
        }
        soundEvent = hasSound() ? Attribute.getOrThrow(this.blockProvider, AttributeSound.class).getSound() : null;
    }

    private void setSupportedTypes(Holder<Block> block) {
        //Used to get any data we may need
        supportsUpgrades = Attribute.has(block, AttributeUpgradeSupport.class);
        canBeUpgraded = Attribute.has(block, AttributeUpgradeable.class);
        isDirectional = Attribute.has(block, AttributeStateFacing.class);
        supportsRedstone = Attribute.has(block, AttributeRedstone.class);
        hasSound = Attribute.has(block, AttributeSound.class);
        hasGui = Attribute.has(block, AttributeGui.class);
        hasBounding = Attribute.has(block, AttributeHasBounding.class);
        hasSecurity = Attribute.has(block, AttributeSecurity.class);
        activeAttribute = Attribute.get(block, AttributeStateActive.class);
        isActivatable = hasSound || activeAttribute != null;
        supportsComparator = Attribute.has(block, AttributeComparator.class);
        supportsComputers = Mekanism.hooks.computerCompatEnabled() && Attribute.has(block, AttributeComputerIntegration.class);
        hasChunkloader = this instanceof IChunkLoader;
        nameable = hasGui() && !Attribute.getOrThrow(getBlockHolder(), AttributeGui.class).hasCustomName();
    }

    /**
     * Sets variables up, called immediately after {@link #setSupportedTypes(Holder)} but before any things start being created.
     *
     * @implNote This method should be used for setting any variables that would normally be set directly, except that gets run too late to set things up properly in our
     * constructor.
     */
    protected void presetVariables() {
    }

    public final Holder<Block> getBlockHolder() {
        return blockProvider;
    }

    /**
     * Should data related to the given type be persisted in this tile save
     */
    public boolean persists(@UnknownNullability IContainerType<?, ?> type) {
        return type.canHandle(this);
    }

    /**
     * Should data related to the given type be transferred to the item
     */
    public boolean persistsToItem(IContainerType<?, ?> type) {
        return persists(type);
    }

    /**
     * Should data related to the given type be synced to the client in the GUI
     */
    public boolean syncs(IContainerType<?, ?> type) {
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

    /**
     * Used to check if this tile actually has an inventory.
     *
     * @return True if we are actually an inventory.
     *
     * @implNote If this returns false the capability should not be exposed AND methods should turn reasonable defaults for not doing anything.
     */
    public final boolean hasInventory() {
        return itemHandlerManager != null;
    }

    public boolean canHandleChemicals() {
        return chemicalHandlerManager != null;
    }

    public final boolean canHandleFluid() {
        return fluidHandlerManager != null;
    }

    public final boolean canHandleEnergy() {
        return energyHandlerManager != null;
    }

    @Override
    public final boolean canHandleHeat() {
        return heatHandlerManager != null;
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

    @NotNull
    @Override
    @SuppressWarnings("ConstantConditions")
    public Component getName() {
        return hasCustomName() ? getCustomName() : TextComponentUtil.build(getBlockHolder());
    }

    @NotNull
    @Override
    @SuppressWarnings("ConstantConditions")
    public Component getDisplayName() {
        if (isNameable()) {
            return hasCustomName() ? getCustomName() : TextComponentUtil.translate(getContainerDescription());
        }
        return TextComponentUtil.build(getBlockHolder());
    }

    private String getContainerDescription() {
        if (containerDescription == null) {
            containerDescription = Util.makeDescriptionId("container", RegistryUtils.getName(getBlockHolder()));
        }
        return containerDescription;
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
        level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
    }

    protected WrenchResult tryWrenchDismantle(BlockState state, Player player, ItemStack stack) {
        if (player.isShiftKeyDown()) {
            if (RadiationManager.isGlobalRadiationEnabled() && getRadiationScale() > 0) {
                //Don't allow dismantling radioactive blocks
                return WrenchResult.RADIOACTIVE;
            }
            WorldUtils.dismantleBlock(state, getLevel(), worldPosition, this, player, stack);
            return WrenchResult.DISMANTLED;
        }
        return WrenchResult.PASS;
    }

    protected WrenchResult tryWrenchRotate(BlockState state, Player player, ItemStack stack) {
        //Special ITileDirectional handling
        if (isDirectional()) {
            AttributeStateFacing attribute = Attribute.getOrThrow(getBlockHolder(), AttributeStateFacing.class);
            if (attribute.canRotate()) {
                setFacing(MekanismUtils.rotate(getDirection(), attribute.facingProperty() == BlockStateProperties.FACING));
                return WrenchResult.SUCCESS;
            }
        }
        return WrenchResult.PASS;
    }

    public WrenchResult tryWrench(BlockState state, Player player, ItemStack stack) {
        if (stack.isEmpty()) {
            return WrenchResult.PASS;
        }
        WrenchResult result = WrenchResult.PASS;
        boolean canRotate = stack.canPerformAction(MekanismItemAbilities.WRENCH_ROTATE);
        boolean canDismantle = stack.canPerformAction(MekanismItemAbilities.WRENCH_DISMANTLE);
        if (!canRotate && !canDismantle) {
            if (stack.canPerformAction(MekanismItemAbilities.WRENCH_EMPTY) || stack.canPerformAction(MekanismItemAbilities.WRENCH_CONFIGURE)) {
                //The stack provides some wrench actions, it is likely intentional that it can't rotate or dismantle blocks
                return result;
            }
            //If the item doesn't explicitly declare the ability to rotate or dismantle,
            // then mark that it can do both if it is in the configurator tag
            canRotate = canDismantle = stack.is(MekanismTags.Items.CONFIGURATORS);
        }
        if (canRotate || canDismantle) {
            if (hasSecurity() && !IBlockSecurityUtils.INSTANCE.canAccessOrDisplayError(player, getWorldNN(), worldPosition, this)) {
                return WrenchResult.NO_SECURITY;
            } else if (canDismantle) {
                result = tryWrenchDismantle(state, player, stack);
            }
            if (result == WrenchResult.PASS && canRotate) {
                result = tryWrenchRotate(state, player, stack);
            }
        }
        return result;
    }

    public InteractionResult openGui(Player player) {
        //Everything that calls this has isRemote being false but add the check just in case anyway
        if (hasGui() && !isRemote() && !player.isShiftKeyDown()) {
            if (hasSecurity() && !IBlockSecurityUtils.INSTANCE.canAccessOrDisplayError(player, player.level(), worldPosition, this)) {
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
            if (!stack.isEmpty() && stack.getItem() instanceof ItemConfigurationCard &&
                WorldUtils.getCapability(level, Capabilities.CONFIG_CARD, worldPosition, null, this, null) != null) {
                return InteractionResult.PASS;
            }

            player.openMenu(Attribute.getOrThrow(getBlockHolder(), AttributeGui.class).getProvider(this, true), buffer -> {
                buffer.writeBlockPos(worldPosition);
                encodeExtraContainerData(buffer);
            });
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    public void encodeExtraContainerData(RegistryFriendlyByteBuf buffer) {
    }

    //TODO - 1.18: Optimize what gets ticks registered to it
    public static void tickClient(Level level, BlockPos pos, BlockState state, TileEntityMekanism tile) {
        if (tile.hasSound()) {
            tile.updateSound();
        }
        tile.onUpdateClient();
        //None of our impls currently care about the ticker in their onUpdateClient methods
        //tile.ticker++;
    }

    public static void tickServer(Level level, BlockPos pos, BlockState state, TileEntityMekanism tile) {
        if (tile.hasBounding && tile.syncMasterToBounding) {
            //TODO: Evaluate checking every x ticks to make sure we have bounding blocks (at least if we haven't already checked) in case we are missing them
            // for example if someone set the main block by using a command
            tile.syncMasterToBounding = false;
            AttributeHasBounding hasBounding = Attribute.get(state, AttributeHasBounding.class);
            if (hasBounding != null) {
                //Note: In theory we only ever set syncMasterToBounding if we know this has bounding blocks, but validate it
                hasBounding.syncMasterPosition(level, pos, state);
            }
        }
        tile.frequencyComponent.tickServer(level, pos);
        if (tile.supportsUpgrades()) {
            tile.upgradeComponent.tickServer(null);
        }
        if (tile.hasChunkloader) {
            ((IChunkLoader) tile).getChunkLoader().tickServer();
        }
        if (tile.isActivatable()) {
            if (tile.updateDelay > 0) {
                tile.updateDelay--;
                if (tile.updateDelay == 0 && tile.getClientActive() != tile.currentActive) {
                    //If it doesn't match, and we are done with the delay period, then update it
                    level.setBlockAndUpdate(pos, tile.activeAttribute.setActive(state, tile.currentActive));
                }
            }
        }
        boolean sendUpdatePacket = tile.onUpdateServer();
        if (tile.updateRadiationScale()) {
            sendUpdatePacket = true;
        }
        //TODO - 1.18: More generic "needs update" flag that we set that then means we don't end up sending an update packet more than once per tick
        if (tile.canHandleHeat()) {
            // update heat after server tick as we now have simulated changes
            // we use persists, as only one reference should update
            tile.updateHeatCapacitors(null);
        }
        //Set that we received zero energy so if it is a different tick than we last had,
        // and we don't actually receive anything then we will properly update it to zero
        LastEnergyTracker lastEnergyTracker = tile.getLastEnergyTracker();
        if (lastEnergyTracker != null) {
            //Note: This should always be the case that the tick is considered changed
            lastEnergyTracker.checkTickChanged();
        }
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
        if (sendUpdatePacket) {
            tile.sendUpdatePacket();
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
    public void preRemoveSideEffects(@NotNull BlockPos pos, @NotNull BlockState state) {
        super.preRemoveSideEffects(pos, state);
        for (ITileComponent component : components) {
            component.removed();
        }
        if (!isRemote() && RadiationManager.isGlobalRadiationEnabled() && shouldDumpRadiation()) {
            //If we are on a server and radiation is enabled dump all gas tanks with radioactive materials
            // Note: we handle clearing radioactive contents later in drop calculation due to when things are written to NBT
            IRadiationManager.INSTANCE.dumpRadiation(getWorldNN(), worldPosition, getChemicalTanks(), false, null);
        }
    }

    /**
     * Update call for machines. Use instead of updateEntity -- it's called every tick on the client side.
     */
    protected void onUpdateClient() {
    }

    /**
     * Update call for machines. Use instead of updateEntity -- it's called every tick on the server side.
     *
     * @return {@code true} if an update packet needs to be sent to the client.
     */
    protected boolean onUpdateServer() {
        return false;
    }

    public void resyncMasterToBounding() {
        if (hasBounding) {
            syncMasterToBounding = true;
        }
    }

    @Override
    @Deprecated
    public void setBlockState(@NotNull BlockState newState) {
        super.setBlockState(newState);
        if (isDirectional()) {
            //Note: We get the new cached direction from the state as hopefully the state is not changing super often
            // and that way we can properly clear things that only should happen when the direction actually changes and not when we go from active to inactive
            Direction newDirection = Attribute.getFacing(newState);
            if (cachedDirection != newDirection) {
                invalidateDirectionCaches(newDirection);
            }
        }
    }

    @Override
    public void loadAdditional(@NotNull ValueInput input) {
        super.loadAdditional(input);
        redstone = input.getBooleanOr(SerializationConstants.REDSTONE, redstone);
        for (ITileComponent component : components) {
            component.read(input);
        }
        if (supportsUpgrades()) {
            recalculateUpgrades(Upgrade.SPEED);//force buffer to update
        }
        readSustainedData(input);
        for (IContainerType<?, ?> type : ContainerType.TYPES) {
            if (type.canHandle(this) && persists(type)) {
                type.readFrom(input, this);
            }
        }
        if (isActivatable()) {
            currentActive = input.getBooleanOr(SerializationConstants.ACTIVE_STATE, currentActive);
            updateDelay = input.getIntOr(SerializationConstants.UPDATE_DELAY, updateDelay);
        }
        if (supportsComparator()) {
            currentRedstoneLevel = input.getIntOr(SerializationConstants.CURRENT_REDSTONE, currentRedstoneLevel);
        }
        if (isNameable()) {
            customName = parseCustomNameSafe(input, SerializationConstants.CUSTOM_NAME);
        }
    }

    @Override
    public void saveAdditional(@NotNull ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean(SerializationConstants.REDSTONE, redstone);
        for (ITileComponent component : components) {
            component.write(output);
        }
        writeSustainedData(output);

        for (IContainerType<?, ?> type : ContainerType.TYPES) {
            if (type.canHandle(this) && persists(type)) {
                type.saveTo(output, this);
            }
        }

        if (isActivatable()) {
            output.putBoolean(SerializationConstants.ACTIVE_STATE, currentActive);
            output.putInt(SerializationConstants.UPDATE_DELAY, updateDelay);
        }
        if (supportsComparator()) {
            output.putInt(SerializationConstants.CURRENT_REDSTONE, currentRedstoneLevel);
        }

        // Save the custom name, if the tile can be named. storeNullable will handle ensuring it doesn't write it when there is no name
        if (isNameable()) {
            output.storeNullable(SerializationConstants.CUSTOM_NAME, ComponentSerialization.CODEC, this.customName);
        }
    }

    public void writeSustainedData(@NotNull ValueOutput output) {
        if (supportsRedstone()) {
            NBTUtils.writeEnum(output, SerializationConstants.CONTROL_TYPE, controlType);
        }
    }

    public void readSustainedData(@NotNull ValueInput input) {
        if (supportsRedstone()) {
            NBTUtils.setEnumIfPresent(input, SerializationConstants.CONTROL_TYPE, RedstoneControl.BY_ID, type -> controlType = supportedOrNextType(type));
        }
    }

    //TODO: Re-evaluate the entirety of this method and see what parts potentially should not be getting called at all when on the client side.
    // We previously had issues in readSustainedData regarding frequencies when on the client side so that is why the frequency data has this check
    // but there is a good chance a lot of this stuff has no real reason to need to be set on the client side at all
    @Override
    protected void applyImplicitComponents(@NotNull DataComponentGetter input) {
        super.applyImplicitComponents(input);
        // Check if the stack has a custom name, and if the tile supports naming, name it
        if (isNameable()) {
            setCustomName(input.get(DataComponents.CUSTOM_NAME));
        }

        for (ITileComponent component : components) {
            component.applyImplicitComponents(input);
        }
        if (supportsUpgrades()) {
            //Recalculate upgrades before setting types so that we don't clamp the stored energy
            for (Upgrade upgrade : getSupportedUpgrade()) {
                recalculateUpgrades(upgrade);
            }
        }

        for (IContainerType<?, ?> type : ContainerType.TYPES) {
            if (persistsToItem(type)) {
                type.copyToTile(this, input);
            }
        }
        if (this instanceof ITileFilterHolder<?> filterHolder) {
            FilterAware filterAware = input.get(MekanismDataComponents.FILTER_AWARE);
            if (filterAware != null) {
                //TODO - 1.20.4: Do we need to copy these or can we just pass the raw instance?
                filterHolder.getFilterManager().trySetFilters(filterAware.filters());
            }
        }
        if (supportsRedstone()) {
            setControlType(input.getOrDefault(MekanismDataComponents.REDSTONE_CONTROL, getControlType()));
        }
    }

    @Override
    public List<DataComponentType<?>> getRemapEntries() {
        List<DataComponentType<?>> remapEntries = super.getRemapEntries();
        for (ITileComponent component : components) {
            component.addRemapEntries(remapEntries);
        }
        for (IContainerType<?, ?> type : ContainerType.TYPES) {
            if (persistsToItem(type)) {
                DataComponentType<?> componentType = type.getComponentType().get();
                if (!remapEntries.contains(componentType)) {
                    //Ensure we add any container types that we only conditionally added
                    remapEntries.add(componentType);
                }
            }
        }
        if (this instanceof ITileFilterHolder<?> && !remapEntries.contains(MekanismDataComponents.FILTER_AWARE.get())) {
            remapEntries.add(MekanismDataComponents.FILTER_AWARE.get());
        }
        return remapEntries;
    }

    @Override
    @Deprecated
    public void removeComponentsFromTag(@NotNull ValueOutput output) {
        super.removeComponentsFromTag(output);
        for (ITileComponent component : components) {
            output.discard(component.getComponentKey());
        }
        output.discard(SerializationConstants.REDSTONE);
        if (supportsComparator()) {
            output.discard(SerializationConstants.CURRENT_REDSTONE);
        }
        if (isActivatable()) {
            output.discard(SerializationConstants.ACTIVE_STATE);
            output.discard(SerializationConstants.UPDATE_DELAY);
        }
        if (supportsRedstone()) {
            output.discard(SerializationConstants.CONTROL_TYPE);
        }
    }

    @Override
    protected void collectImplicitComponents(@NotNull DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        //TODO: Some of the data doesn't get properly "picked", because there are cases such as before opening the GUI where
        // the server doesn't bother syncing the data to the client. For example with what frequencies there are
        for (ITileComponent component : components) {
            component.collectImplicitComponents(builder);
        }
        for (IContainerType<?, ?> type : ContainerType.TYPES) {
            if (persistsToItem(type)) {
                type.copyFromTile(this, builder);
            }
        }
        if (this instanceof ITileFilterHolder<?> filterHolder) {
            FilterManager<?> filterManager = filterHolder.getFilterManager();
            if (!filterManager.getFilters().isEmpty()) {
                builder.set(MekanismDataComponents.FILTER_AWARE, new FilterAware(List.copyOf(filterManager.getFilters())));
            }
        }
        if (supportsRedstone()) {
            builder.set(MekanismDataComponents.REDSTONE_CONTROL, controlType);
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
            container.track(SyncableEnum.create(RedstoneControl.BY_ID, RedstoneControl.DISABLED, () -> controlType, value -> controlType = value));
            container.track(SyncableBoolean.create(this::isPowered, value -> redstone = value));
            container.track(SyncableBoolean.create(this::wasPowered, value -> redstoneLastTick = value));
        }
        if (canHandleChemicals() && syncs(ContainerType.CHEMICAL)) {
            for (IChemicalTank chemicalTank : getChemicalTanks()) {
                container.track(SyncableLargeResourceStack.create(chemicalTank));
            }
        }
        if (canHandleFluid() && syncs(ContainerType.FLUID)) {
            for (IFluidTank fluidTank : getFluidTanks()) {
                container.track(SyncableLargeResourceStack.create(fluidTank));
            }
        }
        if (canHandleHeat() && syncs(ContainerType.HEAT)) {
            for (IHeatCapacitor capacitor : getHeatCapacitors()) {
                container.track(SyncableDouble.create(capacitor::getHeat, capacitor::setHeat));
                if (capacitor instanceof BasicHeatCapacitor heatCapacitor) {
                    container.track(SyncableDouble.create(capacitor::getHeatCapacity, capacity -> heatCapacitor.setHeatCapacity(capacity, false)));
                }
            }
        }
        if (canHandleEnergy() && syncs(ContainerType.ENERGY)) {
            trackLastEnergy(container);
            IEnergyContainer energyContainer = getEnergyContainer();
            if (energyContainer != null) {
                if (energyContainer instanceof MachineEnergyContainer<?> machineEnergy) {
                    if (supportsUpgrades() || machineEnergy.adjustableRates()) {
                        container.track(SyncableLong.create(machineEnergy::getCapacityAsLong, machineEnergy::setMaxEnergy));
                        container.track(SyncableInt.create(machineEnergy::getEnergyPerTick, machineEnergy::setEnergyPerTick));
                    }
                }
                //Ensure energy is synced after the max energy adjustment is synced so that the client doesn't try to clamp what the energy is to the max value
                container.track(SyncableLong.create(energyContainer));
            }
        }
    }

    protected void trackLastEnergy(MekanismContainer container) {
        LastEnergyTracker lastEnergyTracker = getLastEnergyTracker();
        if (lastEnergyTracker != null) {
            container.track(SyncableLong.create(lastEnergyTracker::getLastEnergyReceived, lastEnergyTracker::setLastEnergyReceived));
        }
    }

    @Override
    public void writeReducedUpdatedTag(@NotNull ValueOutput output) {
        super.writeReducedUpdatedTag(output);
        for (ITileComponent component : components) {
            //TODO - 26.1: Do we want to be passing a child?
            component.addToUpdateTag(output);
        }
        output.putFloat(SerializationConstants.RADIATION, radiationScale);
    }

    @Override
    public void handleUpdateTag(@NotNull ValueInput input) {
        super.loadAdditional(input);//we do NOT call super directly, as it will call a load (like from disk) and BEs will never see their changes
        for (ITileComponent component : components) {
            component.readFromUpdateTag(input);
        }
        radiationScale = input.getFloatOr(SerializationConstants.RADIATION, radiationScale);
    }

    public void onNeighborChange(BlockPos neighborPos) {
        if (!isRemote()) {
            updatePower();
        }
    }

    @Override
    public void onAdded() {
        super.onAdded();
        updatePower();
        if (getClientActive()) {
            currentActive = true;
        }
    }

    @Override
    public TileComponentFrequency getFrequencyComponent() {
        return frequencyComponent;
    }

    //Methods pertaining to IUpgradeableTile
    public void parseUpgradeData(@NotNull IUpgradeData data, Provider provider, TransactionContext transaction) {
        Mekanism.logger.warn("Unhandled upgrade data.", new Throwable());
    }
    //End methods IUpgradeableTile

    //Methods for implementing ITileDirectional
    @NotNull
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
                                     + "trying to move this tile and not properly updating the position.",
                      Util.getRegisteredName(BuiltInRegistries.BLOCK_ENTITY_TYPE, getType()), worldPosition, level);
            }
        }
        //TODO: Remove, give it some better default, or allow it to be null
        // (this is used by some things like non directional blocks with energy configs)
        return Direction.NORTH;
    }

    protected void invalidateDirectionCaches(Direction newDirection) {
        cachedDirection = newDirection;
    }

    @Override
    public void setFacing(@NotNull Direction direction) {
        setFacing(direction, true);
    }

    public void setFacing(@NotNull Direction direction, boolean notifyCaps) {
        if (isDirectional() && direction != cachedDirection && level != null) {
            invalidateDirectionCaches(direction);
            BlockState state = Attribute.setFacing(getBlockState(), direction);
            if (state != null) {
                level.setBlockAndUpdate(worldPosition, state);
                if (notifyCaps) {
                    //Clear cached capabilities as it is possible it changed on one of the sides
                    invalidateCapabilitiesFull();
                }
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
    public void setControlType(@NotNull RedstoneControl type) {
        if (supportsRedstone()) {
            type = supportedOrNextType(type);
            if (type != controlType) {
                controlType = type;
                markForSave();
            }
        }
    }

    private RedstoneControl supportedOrNextType(@NotNull RedstoneControl type) {
        Objects.requireNonNull(type);
        if (!supportsMode(type)) {
            //Validate we support the mode that is being set
            type = type.getNext(this::supportsMode);
        }
        return type;
    }

    @Override
    public boolean isPowered() {
        return supportsRedstone() && redstone;
    }

    @Override
    public final boolean wasPowered() {
        return supportsRedstone() && redstoneLastTick;
    }

    public final void updatePower() {
        if (supportsRedstone()) {
            boolean power = level.hasNeighborSignal(getBlockPos());
            if (redstone != power) {
                redstone = power;
                onPowerChange();
            }
        }
    }

    public final boolean isRedstoneActivated() {
        return !supportsRedstone() ||
               switch (controlType) {
                   case DISABLED -> true;
                   case HIGH -> isPowered();
                   case LOW -> !isPowered();
                   case PULSE -> isPowered() && !redstoneLastTick;
               };
    }

    public boolean canFunction() {
        return isRedstoneActivated();
    }
    //End methods ITileRedstone

    //Methods for implementing IComparatorSupport
    @Override
    public int getRedstoneLevel() {
        if (supportsComparator()) {
            if (hasInventory()) {
                return ContainerType.ITEM.getRedstoneSignalFromContainers(getInventorySlots());
            }
            //TODO: Do we want some other defaults as well?
        }
        return Redstone.SIGNAL_NONE;
    }

    /**
     * @param type Type of container that got updated
     *
     * @implNote It can be assumed {@link #supportsComparator()} is true before this is called.
     */
    protected boolean makesComparatorDirty(IContainerType<?, ?> type) {
        //Assume that items make it dirty unless otherwise overridden, as we use this before we can call hasInventory
        // and if we aren't using an inventory as our comparator thing we will be overriding this method anyway
        // and if we don't have an inventory we can't assign this listener to anything as adding slots and assigning it
        // is what binds the listener to the main tile
        return type == ContainerType.ITEM;
    }

    private IContentsListener getListener(IContainerType<?, ?> type, IContentsListener saveOnlyListener) {
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
    @NotNull
    @Override
    public Set<Upgrade> getSupportedUpgrade() {
        if (supportsUpgrades()) {
            return Attribute.getOrThrow(getBlockHolder(), AttributeUpgradeSupport.class).supportedUpgrades();
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
            if (getEnergyContainer() instanceof MachineEnergyContainer<?> machineEnergy) {
                machineEnergy.updateEnergyPerTick();
                machineEnergy.updateMaxEnergy();
            }
        } else if (upgrade == Upgrade.ENERGY) {
            if (getEnergyContainer() instanceof MachineEnergyContainer<?> machineEnergy) {
                machineEnergy.updateEnergyPerTick();
                machineEnergy.updateMaxEnergy();
            }
        }
    }
    //End methods ITileUpgradable

    //Methods for implementing ITileContainer
    @Nullable
    protected IContainerHolder<IInventorySlot> getInitialInventory(IContentsListener listener) {
        return null;
    }

    @NotNull
    public final List<IInventorySlot> getInventorySlots() {
        return itemHandlerManager == null ? Collections.emptyList() : itemHandlerManager.getContainers(null);
    }

    @Override
    public void onContentsChanged() {
        setChanged();
    }
    //End methods ITileContainer

    //Methods for implementing IMekanismChemicalHandler
    public boolean shouldDumpRadiation() {
        return canHandleChemicals();
    }

    /**
     * @apiNote Only call on server.
     */
    private boolean updateRadiationScale() {
        if (shouldDumpRadiation()) {
            float scale = ITileRadioactive.calculateRadiationScale(getChemicalTanks());
            if (Math.abs(scale - radiationScale) > 0.05F) {
                radiationScale = scale;
                return true;
            }
        }
        return false;
    }

    @Override
    public float getRadiationScale() {
        return RadiationManager.isGlobalRadiationEnabled() ? radiationScale : 0;
    }

    @Nullable
    public IContainerHolder<IChemicalTank> getInitialChemicalTanks(IContentsListener listener) {
        return null;
    }

    @NotNull
    public final List<IChemicalTank> getChemicalTanks() {
        return chemicalHandlerManager == null ? Collections.emptyList() : chemicalHandlerManager.getContainers(null);
    }
    //End methods IMekanismChemicalHandler

    //Methods for implementing IMekanismFluidHandler
    @Nullable
    protected IContainerHolder<IFluidTank> getInitialFluidTanks(IContentsListener listener) {
        return null;
    }

    @NotNull
    public final List<IFluidTank> getFluidTanks() {
        return fluidHandlerManager == null ? Collections.emptyList() : fluidHandlerManager.getContainers(null);
    }
    //End methods IMekanismFluidHandler

    //Methods for implementing IMekanismStrictEnergyHandler
    protected @Nullable IEnergyContainerHolder getInitialEnergyContainer(IContentsListener listener) {
        return null;
    }

    @Nullable
    public final IEnergyContainer getEnergyContainer() {
        return energyHandlerManager == null ? null : energyHandlerManager.getContainer(null);
    }

    @Nullable
    private LastEnergyTracker getLastEnergyTracker() {
        return energyHandlerManager == null ? null : energyHandlerManager.getLastEnergyTracker();
    }

    public final long getInputRate() {
        LastEnergyTracker lastEnergyTracker = getLastEnergyTracker();
        return lastEnergyTracker == null ? 0 : lastEnergyTracker.getLastEnergyReceived();
    }
    //End methods IMekanismStrictEnergyHandler

    //Methods for implementing IInWorldHeatHandler
    @Nullable
    protected IContainerHolder<IHeatCapacitor> getInitialHeatCapacitors(IContentsListener listener, CachedAmbientTemperature ambientTemperature) {
        return null;
    }

    @Override
    public double getAmbientTemperature(@NotNull Direction side) {
        if (canHandleHeat() && ambientTemperature != null) {
            return ambientTemperature.getTemperature(side);
        }
        return ITileHeatHandler.super.getAmbientTemperature(side);
    }

    @Nullable
    @Override
    public IHeatHandler getAdjacent(@NotNull Direction side) {
        if (canHandleHeat() && getHeatCapacitorCount(side) > 0) {
            return getAdjacentUnchecked(side);
        }
        return null;
    }

    @Nullable
    protected IHeatHandler getAdjacentUnchecked(@NotNull Direction side) {
        BlockCapabilityCache<IHeatHandler, @Nullable Direction> cache = adjacentHeatCaps.get(side);
        if (cache == null) {
            cache = BlockCapabilityCache.create(Capabilities.HEAT, (ServerLevel) level, worldPosition.relative(side), side.getOpposite());
            adjacentHeatCaps.put(side, cache);
        }
        return cache.getCapability();
    }

    @NotNull
    public final List<IHeatCapacitor> getHeatCapacitors() {
        return getHeatCapacitors(null);
    }

    @NotNull
    @Override
    public final List<IHeatCapacitor> getHeatCapacitors(@Nullable Direction side) {
        return heatHandlerManager == null ? Collections.emptyList() : heatHandlerManager.getContainers(side);
    }
    //End methods for IInWorldHeatHandler

    //Methods for implementing IConfigCardAccess
    @Override
    public void writeConfigurationData(ValueOutput output, Player player) {
        writeSustainedData(output);
        getFrequencyComponent().writeConfiguredFrequencies(output);
    }

    @Override
    public void setConfigurationData(ValueInput input, Player player) {
        readSustainedData(input);
        getFrequencyComponent().readConfiguredFrequencies(input, player);
    }

    @Override
    public Block getConfigurationDataType() {
        return getBlockState().getBlock();
    }

    @Override
    public void configurationDataSet() {
        setChanged();
        invalidateCapabilitiesFull();
        sendUpdatePacket();
        WorldUtils.notifyLoadedNeighborsOfTileChange(getLevel(), this.getBlockPos());
    }
    //End methods IConfigCardAccess

    //Methods for implementing ITileSecurity
    @Override
    public TileComponentSecurity getSecurity() {
        return securityComponent;
    }

    @Override
    public void onSecurityChanged(@NotNull SecurityMode old, @NotNull SecurityMode mode) {
        if (!isRemote() && hasGui() && level != null) {
            BlockSecurityUtils.get().securityChanged(playersUsing, level, worldPosition, this, old, mode);
        }
    }
    //End methods ITileSecurity

    //Methods for implementing ITileActive
    @Override
    public boolean getActive() {
        return isRemote() ? getClientActive() : currentActive;
    }

    private boolean getClientActive() {
        return activeAttribute != null && activeAttribute.isActive(getBlockState());
    }

    @Override
    public void setActive(boolean active) {
        if (isActivatable() && active != currentActive) {
            BlockState state = getBlockState();
            if (activeAttribute != null) {
                currentActive = active;
                if (getClientActive() != active) {
                    if (active) {
                        //Always turn on instantly
                        level.setBlockAndUpdate(worldPosition, activeAttribute.setActive(state, true));
                    } else {
                        // if the update delay is already zero, we can go ahead and set the state
                        if (updateDelay == 0) {
                            level.setBlockAndUpdate(worldPosition, activeAttribute.setActive(state, currentActive));
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
            SoundEvent sound = soundEvent.get();
            if (sound != lastSoundEvent) {
                if (activeSound != null) {
                    //The sound changed, stop it so that we can start it back up again
                    SoundHandler.stopTileSound(getSoundPos());
                    activeSound = null;
                }
                lastSoundEvent = sound;
            }

            // If this machine isn't fully muffled, and we don't seem to be playing a sound for it, go ahead and
            // play it
            if (!isFullyMuffled() && (activeSound == null || !Minecraft.getInstance().getSoundManager().isActive(activeSound))) {
                activeSound = SoundHandler.startTileSound(lastSoundEvent, getSoundCategory(), getInitialVolume(), level.getRandom(), getSoundPos());
            }
            // Always reset the cooldown; either we just attempted to play a sound or we're fully muffled; either way
            // we don't want to try again
            playSoundCooldown = SharedConstants.TICKS_PER_SECOND;
        } else if (activeSound != null) {
            SoundHandler.stopTileSound(getSoundPos());
            activeSound = null;
            playSoundCooldown = 0;
        }
    }

    protected boolean isFullyMuffled() {
        if (hasSound() && supportsUpgrade(Upgrade.MUFFLING)) {
            return getComponent().getUpgrades(Upgrade.MUFFLING) >= Upgrade.MUFFLING.getMax();
        }
        return false;
    }
    //End methods ITileSound

    //Methods relating to IComputerTile
    // Note: Some methods are elsewhere if we are exposing pre-existing implementations
    @Override
    public String getComputerName() {
        if (hasComputerSupport()) {
            return Attribute.getOrThrow(getBlockHolder(), AttributeComputerIntegration.class).name();
        }
        return "";
    }

    public void validateSecurityIsPublic() throws ComputerException {
        if (hasSecurity() && IBlockSecurityUtils.INSTANCE.getSecurityMode(getWorldNN(), worldPosition, this) != SecurityMode.PUBLIC) {
            throw new ComputerException("Setter not available due to machine security not being public.");
        }
    }

    @Override
    public void getComputerMethods(BoundMethodHolder holder) {
        IComputerTile.super.getComputerMethods(holder);
        for (ITileComponent component : components) {
            //Allow any supported components to add their computer methods as well
            // For example side config, ejector, and upgrade components
            FactoryRegistry.bindTo(holder, component);
        }
    }

    //TODO: If we ever end up using the part of our API that allows for multiple energy containers, it may be worth exposing
    // overloaded versions of these methods that take the container index as a parameter if anyone ends up running into a case
    // where being able to get a specific container's stored energy would be useful to their program. Alternatively we could
    // probably make use of our synthetic computer method wrapper to just add extra methods so then have it basically create
    // getEnergy, getEnergyFE for us with us only having to define getEnergy
    @ComputerMethod(restriction = MethodRestriction.ENERGY)
    long getEnergy() {
        IEnergyContainer energyContainer = getEnergyContainer();
        return energyContainer == null ? 0 : energyContainer.getAmountAsLong();
    }

    @ComputerMethod(restriction = MethodRestriction.ENERGY)
    long getMaxEnergy() {
        IEnergyContainer energyContainer = getEnergyContainer();
        return energyContainer == null ? 0 : energyContainer.getCapacityAsLong();
    }

    @ComputerMethod(restriction = MethodRestriction.ENERGY)
    long getEnergyNeeded() {
        IEnergyContainer energyContainer = getEnergyContainer();
        return energyContainer == null ? 0 : energyContainer.getNeededAsLong();
    }

    @ComputerMethod(restriction = MethodRestriction.ENERGY)
    double getEnergyFilledPercentage() {
        IEnergyContainer energyContainer = getEnergyContainer();
        if (energyContainer == null) {
            return 1;
        }
        return ContainerType.ENERGY.divideToLevel(energyContainer);
    }

    @ComputerMethod(restriction = MethodRestriction.REDSTONE_CONTROL, requiresPublicSecurity = true)
    void setRedstoneMode(RedstoneControl type) throws ComputerException {
        validateSecurityIsPublic();
        if (!supportsMode(type)) {
            throw new ComputerException("Unsupported redstone control mode: %s", type);
        }
        setControlType(type);
    }
    //End methods IComputerTile
}