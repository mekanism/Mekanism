package mekanism.common.item;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.IConfigurable;
import mekanism.api.IIncrementalEnum;
import mekanism.api.IMekWrench;
import mekanism.api.NBTConstants;
import mekanism.api.RelativeSide;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.inventory.AutomationType;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.MekanismLang;
import mekanism.common.base.ILangEntry;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemConfigurator extends ItemEnergized implements IMekWrench, IModeItem, IItemHUDProvider {

    public ItemConfigurator(Properties properties) {
        super(MekanismConfig.gear.configuratorChargeRate, MekanismConfig.gear.configuratorMaxEnergy, properties);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        tooltip.add(MekanismLang.STATE.translateColored(EnumColor.PINK, getState(stack)));
    }

    @Nonnull
    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        World world = context.getWorld();
        if (!world.isRemote && player != null) {
            BlockPos pos = context.getPos();
            Direction side = context.getFace();
            Hand hand = context.getHand();
            ItemStack stack = player.getHeldItem(hand);
            TileEntity tile = MekanismUtils.getTileEntity(world, pos);

            if (getState(stack).isConfigurating()) { //Configurate
                TransmissionType transmissionType = Objects.requireNonNull(getState(stack).getTransmission(), "Configurating state requires transmission type");
                if (tile instanceof ISideConfiguration && ((ISideConfiguration) tile).getConfig().supports(transmissionType)) {
                    ISideConfiguration config = (ISideConfiguration) tile;
                    ConfigInfo info = config.getConfig().getConfig(transmissionType);
                    if (info != null) {
                        RelativeSide relativeSide = RelativeSide.fromDirections(config.getOrientation(), side);
                        DataType dataType = info.getDataType(relativeSide);
                        if (!player.isSneaking()) {
                            player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM,
                                  MekanismLang.CONFIGURATOR_VIEW_MODE.translateColored(EnumColor.GRAY, transmissionType, dataType.getColor(), dataType,
                                        dataType.getColor().getColoredName())));
                        } else if (SecurityUtils.canAccess(player, tile)) {
                            if (!player.isCreative()) {
                                IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
                                FloatingLong energyPerConfigure = MekanismConfig.gear.configuratorEnergyPerConfigure.get();
                                if (energyContainer == null || energyContainer.extract(energyPerConfigure, Action.SIMULATE, AutomationType.MANUAL).smallerThan(energyPerConfigure)) {
                                    return ActionResultType.FAIL;
                                }
                                energyContainer.extract(energyPerConfigure, Action.EXECUTE, AutomationType.MANUAL);
                            }
                            dataType = info.incrementDataType(relativeSide);
                            player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM,
                                  MekanismLang.CONFIGURATOR_TOGGLE_MODE.translateColored(EnumColor.GRAY, transmissionType,
                                        dataType.getColor(), dataType, dataType.getColor().getColoredName())));
                            config.getConfig().sideChanged(transmissionType, relativeSide);
                        } else {
                            SecurityUtils.displayNoAccess(player);
                        }
                    }
                    return ActionResultType.SUCCESS;
                }
                if (SecurityUtils.canAccess(player, tile)) {
                    Optional<IConfigurable> capability = MekanismUtils.toOptional(CapabilityUtils.getCapability(tile, Capabilities.CONFIGURABLE_CAPABILITY, side));
                    if (capability.isPresent()) {
                        IConfigurable config = capability.get();
                        if (player.isSneaking()) {
                            return config.onSneakRightClick(player, side);
                        }
                        return config.onRightClick(player, side);
                    }
                } else {
                    SecurityUtils.displayNoAccess(player);
                    return ActionResultType.SUCCESS;
                }
            } else if (getState(stack) == ConfiguratorMode.EMPTY) { //Empty
                if (tile instanceof IMekanismInventory) {
                    IMekanismInventory inv = (IMekanismInventory) tile;
                    if (inv.hasInventory()) {
                        if (SecurityUtils.canAccess(player, tile)) {
                            boolean creative = player.isCreative();
                            IEnergyContainer energyContainer = creative ? null : StorageUtils.getEnergyContainer(stack, 0);
                            if (!creative && energyContainer == null) {
                                return ActionResultType.FAIL;
                            }
                            //TODO: Switch this to items being handled by TileEntityMekanism, energy handled here (via lambdas?)
                            FloatingLong energyPerItemDump = MekanismConfig.gear.configuratorEnergyPerItem.get();
                            for (IInventorySlot inventorySlot : inv.getInventorySlots(null)) {
                                if (!inventorySlot.isEmpty()) {
                                    if (!creative) {
                                        if (energyContainer.extract(energyPerItemDump, Action.SIMULATE, AutomationType.MANUAL).smallerThan(energyPerItemDump)) {
                                            break;
                                        }
                                        energyContainer.extract(energyPerItemDump, Action.EXECUTE, AutomationType.MANUAL);
                                    }
                                    Block.spawnAsEntity(world, pos, inventorySlot.getStack().copy());
                                    inventorySlot.setStack(ItemStack.EMPTY);
                                }
                            }
                            return ActionResultType.SUCCESS;
                        } else {
                            SecurityUtils.displayNoAccess(player);
                            return ActionResultType.FAIL;
                        }
                    }
                }
            } else if (getState(stack) == ConfiguratorMode.ROTATE) { //Rotate
                if (tile instanceof TileEntityMekanism) {
                    if (SecurityUtils.canAccess(player, tile)) {
                        TileEntityMekanism tileMekanism = (TileEntityMekanism) tile;
                        if (!player.isSneaking()) {
                            tileMekanism.setFacing(side);
                        } else if (player.isSneaking()) {
                            tileMekanism.setFacing(side.getOpposite());
                        }
                    } else {
                        SecurityUtils.displayNoAccess(player);
                    }
                }
                return ActionResultType.SUCCESS;
            } else if (getState(stack) == ConfiguratorMode.WRENCH) { //Wrench
                return ActionResultType.PASS;
            }
        }
        return ActionResultType.PASS;
    }

    public EnumColor getColor(ConfiguratorMode mode) {
        return mode.getColor();
    }

    public void setState(ItemStack stack, ConfiguratorMode state) {
        ItemDataUtils.setInt(stack, NBTConstants.STATE, state.ordinal());
    }

    public ConfiguratorMode getState(ItemStack stack) {
        return ConfiguratorMode.byIndexStatic(ItemDataUtils.getInt(stack, NBTConstants.STATE));
    }

    @Override
    public boolean canUseWrench(ItemStack stack, PlayerEntity player, BlockPos pos) {
        return getState(stack) == ConfiguratorMode.WRENCH;
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, IWorldReader world, BlockPos pos, PlayerEntity player) {
        return getState(stack) == ConfiguratorMode.WRENCH;
    }

    @Override
    public void addHUDStrings(List<ITextComponent> list, ItemStack stack, EquipmentSlotType slotType) {
        list.add(MekanismLang.MODE.translateColored(EnumColor.PINK, getState(stack)));
    }

    @Override
    public void changeMode(@Nonnull PlayerEntity player, @Nonnull ItemStack stack, int shift, boolean displayChangeMessage) {
        ConfiguratorMode mode = getState(stack);
        ConfiguratorMode newMode = mode.adjust(shift);
        if (mode != newMode) {
            setState(stack, newMode);
            if (displayChangeMessage) {
                player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM,
                      MekanismLang.CONFIGURE_STATE.translateColored(EnumColor.GRAY, newMode)));
            }
        }
    }

    @Nonnull
    @Override
    public ITextComponent getScrollTextComponent(@Nonnull ItemStack stack) {
        return getState(stack).getTextComponent();
    }

    @FieldsAreNonnullByDefault
    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    public enum ConfiguratorMode implements IIncrementalEnum<ConfiguratorMode>, IHasTextComponent {
        CONFIGURATE_ITEMS(MekanismLang.CONFIGURATOR_CONFIGURATE, TransmissionType.ITEM, EnumColor.BRIGHT_GREEN, true),
        CONFIGURATE_FLUIDS(MekanismLang.CONFIGURATOR_CONFIGURATE, TransmissionType.FLUID, EnumColor.BRIGHT_GREEN, true),
        CONFIGURATE_GASES(MekanismLang.CONFIGURATOR_CONFIGURATE, TransmissionType.GAS, EnumColor.BRIGHT_GREEN, true),
        CONFIGURATE_ENERGY(MekanismLang.CONFIGURATOR_CONFIGURATE, TransmissionType.ENERGY, EnumColor.BRIGHT_GREEN, true),
        CONFIGURATE_HEAT(MekanismLang.CONFIGURATOR_CONFIGURATE, TransmissionType.HEAT, EnumColor.BRIGHT_GREEN, true),
        EMPTY(MekanismLang.CONFIGURATOR_EMPTY, null, EnumColor.DARK_RED, false),
        ROTATE(MekanismLang.CONFIGURATOR_ROTATE, null, EnumColor.YELLOW, false),
        WRENCH(MekanismLang.CONFIGURATOR_WRENCH, null, EnumColor.PINK, false);

        private static final ConfiguratorMode[] MODES = values();
        private final ILangEntry langEntry;
        @Nullable
        private final TransmissionType transmissionType;
        private final EnumColor color;
        private final boolean configurating;

        ConfiguratorMode(ILangEntry langEntry, @Nullable TransmissionType transmissionType, EnumColor color, boolean configurating) {
            this.langEntry = langEntry;
            this.transmissionType = transmissionType;
            this.color = color;
            this.configurating = configurating;
        }

        @Override
        public ITextComponent getTextComponent() {
            if (transmissionType != null) {
                return langEntry.translateColored(color, transmissionType);
            }
            return langEntry.translateColored(color);
        }

        public EnumColor getColor() {
            return color;
        }

        public boolean isConfigurating() {
            return configurating;
        }

        @Nullable
        public TransmissionType getTransmission() {
            switch (this) {
                case CONFIGURATE_ITEMS:
                    return TransmissionType.ITEM;
                case CONFIGURATE_FLUIDS:
                    return TransmissionType.FLUID;
                case CONFIGURATE_GASES:
                    return TransmissionType.GAS;
                case CONFIGURATE_ENERGY:
                    return TransmissionType.ENERGY;
                case CONFIGURATE_HEAT:
                    return TransmissionType.HEAT;
                default:
                    return null;
            }
        }

        @Nonnull
        @Override
        public ConfiguratorMode byIndex(int index) {
            return byIndexStatic(index);
        }

        public static ConfiguratorMode byIndexStatic(int index) {
            return MathUtils.getByIndexMod(MODES, index);
        }
    }
}