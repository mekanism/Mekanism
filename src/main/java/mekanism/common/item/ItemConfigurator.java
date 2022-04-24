package mekanism.common.item;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IConfigurable;
import mekanism.api.MekanismAPI;
import mekanism.api.NBTConstants;
import mekanism.api.RelativeSide;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeStateFacing;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.ItemConfigurator.ConfiguratorMode;
import mekanism.common.item.interfaces.IItemHUDProvider;
import mekanism.common.item.interfaces.IRadialModeItem;
import mekanism.common.item.interfaces.IRadialSelectorEnum;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ItemConfigurator extends ItemEnergized implements IRadialModeItem<ConfiguratorMode>, IItemHUDProvider {

    public ItemConfigurator(Properties properties) {
        super(MekanismConfig.gear.configuratorChargeRate, MekanismConfig.gear.configuratorMaxEnergy, properties.rarity(Rarity.UNCOMMON));
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, Level world, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);
        tooltip.add(MekanismLang.STATE.translateColored(EnumColor.PINK, getMode(stack)));
    }

    @Nonnull
    @Override
    public Component getName(@Nonnull ItemStack stack) {
        return TextComponentUtil.build(EnumColor.AQUA, super.getName(stack));
    }

    @Nonnull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level world = context.getLevel();
        if (!world.isClientSide && player != null) {
            BlockPos pos = context.getClickedPos();
            Direction side = context.getClickedFace();
            ItemStack stack = context.getItemInHand();
            BlockEntity tile = WorldUtils.getTileEntity(world, pos);
            ConfiguratorMode mode = getMode(stack);
            if (mode.isConfigurating()) { //Configurate
                TransmissionType transmissionType = Objects.requireNonNull(mode.getTransmission(), "Configurating state requires transmission type");
                if (tile instanceof ISideConfiguration config && config.getConfig().supports(transmissionType)) {
                    ConfigInfo info = config.getConfig().getConfig(transmissionType);
                    if (info != null) {
                        RelativeSide relativeSide = RelativeSide.fromDirections(config.getDirection(), side);
                        DataType dataType = info.getDataType(relativeSide);
                        if (!player.isShiftKeyDown()) {
                            player.sendMessage(MekanismUtils.logFormat(MekanismLang.CONFIGURATOR_VIEW_MODE.translate(transmissionType, dataType.getColor(), dataType,
                                  dataType.getColor().getColoredName())), Util.NIL_UUID);
                        } else if (!MekanismAPI.getSecurityUtils().canAccessOrDisplayError(player, tile)) {
                            return InteractionResult.FAIL;
                        } else {
                            if (!player.isCreative()) {
                                IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
                                FloatingLong energyPerConfigure = MekanismConfig.gear.configuratorEnergyPerConfigure.get();
                                if (energyContainer == null || energyContainer.extract(energyPerConfigure, Action.SIMULATE, AutomationType.MANUAL).smallerThan(energyPerConfigure)) {
                                    return InteractionResult.FAIL;
                                }
                                energyContainer.extract(energyPerConfigure, Action.EXECUTE, AutomationType.MANUAL);
                            }
                            DataType old = dataType;
                            dataType = info.incrementDataType(relativeSide);
                            if (dataType != old) {
                                player.sendMessage(MekanismUtils.logFormat(MekanismLang.CONFIGURATOR_TOGGLE_MODE.translate(transmissionType, dataType.getColor(), dataType,
                                      dataType.getColor().getColoredName())), Util.NIL_UUID);
                                config.getConfig().sideChanged(transmissionType, relativeSide);
                            }
                        }
                    }
                    return InteractionResult.SUCCESS;
                }
                if (!MekanismAPI.getSecurityUtils().canAccessOrDisplayError(player, tile)) {
                    return InteractionResult.FAIL;
                }
                Optional<IConfigurable> capability = CapabilityUtils.getCapability(tile, Capabilities.CONFIGURABLE_CAPABILITY, side).resolve();
                if (capability.isPresent()) {
                    IConfigurable config = capability.get();
                    if (player.isShiftKeyDown()) {
                        return config.onSneakRightClick(player);
                    }
                    return config.onRightClick(player);
                }
            } else if (mode == ConfiguratorMode.EMPTY) { //Empty
                if (tile instanceof IMekanismInventory inv && inv.hasInventory()) {
                    if (!MekanismAPI.getSecurityUtils().canAccessOrDisplayError(player, tile)) {
                        return InteractionResult.FAIL;
                    }
                    boolean creative = player.isCreative();
                    IEnergyContainer energyContainer = creative ? null : StorageUtils.getEnergyContainer(stack, 0);
                    if (!creative && energyContainer == null) {
                        return InteractionResult.FAIL;
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
                            Block.popResource(world, pos, inventorySlot.getStack().copy());
                            inventorySlot.setEmpty();
                        }
                    }
                    return InteractionResult.SUCCESS;
                }
            } else if (mode == ConfiguratorMode.ROTATE) { //Rotate
                if (tile instanceof TileEntityMekanism tileMekanism) {
                    if (!MekanismAPI.getSecurityUtils().canAccessOrDisplayError(player, tile)) {
                        return InteractionResult.FAIL;
                    }
                    if (Attribute.get(tileMekanism.getBlockType(), AttributeStateFacing.class).canRotate()) {
                        if (!player.isShiftKeyDown()) {
                            tileMekanism.setFacing(side);
                        } else if (player.isShiftKeyDown()) {
                            tileMekanism.setFacing(side.getOpposite());
                        }
                    }
                }
                return InteractionResult.SUCCESS;
            } else if (mode == ConfiguratorMode.WRENCH) { //Wrench
                return InteractionResult.PASS;
            }
        }
        return InteractionResult.PASS;
    }

    public EnumColor getColor(ConfiguratorMode mode) {
        return mode.getColor();
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader world, BlockPos pos, Player player) {
        return getMode(stack) == ConfiguratorMode.WRENCH;
    }

    @Override
    public void addHUDStrings(List<Component> list, Player player, ItemStack stack, EquipmentSlot slotType) {
        list.add(MekanismLang.MODE.translateColored(EnumColor.PINK, getMode(stack)));
    }

    @Override
    public void changeMode(@Nonnull Player player, @Nonnull ItemStack stack, int shift, boolean displayChangeMessage) {
        ConfiguratorMode mode = getMode(stack);
        ConfiguratorMode newMode = mode.adjust(shift);
        if (mode != newMode) {
            setMode(stack, player, newMode);
            if (displayChangeMessage) {
                player.sendMessage(MekanismUtils.logFormat(MekanismLang.CONFIGURE_STATE.translate(newMode)), Util.NIL_UUID);
            }
        }
    }

    @Nonnull
    @Override
    public Component getScrollTextComponent(@Nonnull ItemStack stack) {
        return getMode(stack).getTextComponent();
    }

    @Override
    public void setMode(ItemStack stack, Player player, ConfiguratorMode mode) {
        ItemDataUtils.setInt(stack, NBTConstants.STATE, mode.ordinal());
    }

    @Override
    public ConfiguratorMode getMode(ItemStack stack) {
        return ConfiguratorMode.byIndexStatic(ItemDataUtils.getInt(stack, NBTConstants.STATE));
    }

    @Override
    public Class<ConfiguratorMode> getModeClass() {
        return ConfiguratorMode.class;
    }

    @Override
    public ConfiguratorMode getModeByIndex(int ordinal) {
        return ConfiguratorMode.byIndexStatic(ordinal);
    }

    @FieldsAreNonnullByDefault
    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    public enum ConfiguratorMode implements IRadialSelectorEnum<ConfiguratorMode>, IHasTextComponent {
        CONFIGURATE_ITEMS(MekanismLang.CONFIGURATOR_CONFIGURATE, TransmissionType.ITEM, EnumColor.BRIGHT_GREEN, true, null),
        CONFIGURATE_FLUIDS(MekanismLang.CONFIGURATOR_CONFIGURATE, TransmissionType.FLUID, EnumColor.BRIGHT_GREEN, true, null),
        CONFIGURATE_GASES(MekanismLang.CONFIGURATOR_CONFIGURATE, TransmissionType.GAS, EnumColor.BRIGHT_GREEN, true, null),
        CONFIGURATE_INFUSE_TYPES(MekanismLang.CONFIGURATOR_CONFIGURATE, TransmissionType.INFUSION, EnumColor.BRIGHT_GREEN, true, null),
        CONFIGURATE_PIGMENTS(MekanismLang.CONFIGURATOR_CONFIGURATE, TransmissionType.PIGMENT, EnumColor.BRIGHT_GREEN, true, null),
        CONFIGURATE_SLURRIES(MekanismLang.CONFIGURATOR_CONFIGURATE, TransmissionType.SLURRY, EnumColor.BRIGHT_GREEN, true, null),
        CONFIGURATE_ENERGY(MekanismLang.CONFIGURATOR_CONFIGURATE, TransmissionType.ENERGY, EnumColor.BRIGHT_GREEN, true, null),
        CONFIGURATE_HEAT(MekanismLang.CONFIGURATOR_CONFIGURATE, TransmissionType.HEAT, EnumColor.BRIGHT_GREEN, true, null),
        EMPTY(MekanismLang.CONFIGURATOR_EMPTY, null, EnumColor.DARK_RED, false, MekanismUtils.getResource(ResourceType.GUI, "empty.png")),
        ROTATE(MekanismLang.CONFIGURATOR_ROTATE, null, EnumColor.YELLOW, false, MekanismUtils.getResource(ResourceType.GUI, "rotate.png")),
        WRENCH(MekanismLang.CONFIGURATOR_WRENCH, null, EnumColor.PINK, false, MekanismUtils.getResource(ResourceType.GUI, "wrench.png"));

        public static final ConfiguratorMode[] MODES = values();
        private final ILangEntry langEntry;
        @Nullable
        private final TransmissionType transmissionType;
        private final EnumColor color;
        private final boolean configurating;
        private final ResourceLocation icon;

        ConfiguratorMode(ILangEntry langEntry, @Nullable TransmissionType transmissionType, EnumColor color, boolean configurating, @Nullable ResourceLocation icon) {
            this.langEntry = langEntry;
            this.transmissionType = transmissionType;
            this.color = color;
            this.configurating = configurating;
            if (transmissionType != null) {
                this.icon = MekanismUtils.getResource(ResourceType.GUI, transmissionType.getTransmission() + ".png");
            } else {
                this.icon = icon;
            }
        }

        @Override
        public Component getTextComponent() {
            if (transmissionType != null) {
                return langEntry.translateColored(color, transmissionType);
            }
            return langEntry.translateColored(color);
        }

        @Override
        public EnumColor getColor() {
            return color;
        }

        public boolean isConfigurating() {
            return configurating;
        }

        @Nullable
        public TransmissionType getTransmission() {
            return switch (this) {
                case CONFIGURATE_ITEMS -> TransmissionType.ITEM;
                case CONFIGURATE_FLUIDS -> TransmissionType.FLUID;
                case CONFIGURATE_GASES -> TransmissionType.GAS;
                case CONFIGURATE_INFUSE_TYPES -> TransmissionType.INFUSION;
                case CONFIGURATE_PIGMENTS -> TransmissionType.PIGMENT;
                case CONFIGURATE_SLURRIES -> TransmissionType.SLURRY;
                case CONFIGURATE_ENERGY -> TransmissionType.ENERGY;
                case CONFIGURATE_HEAT -> TransmissionType.HEAT;
                default -> null;
            };
        }

        @Nonnull
        @Override
        public ConfiguratorMode byIndex(int index) {
            return byIndexStatic(index);
        }

        public static ConfiguratorMode byIndexStatic(int index) {
            return MathUtils.getByIndexMod(MODES, index);
        }

        @Override
        public Component getShortText() {
            return configurating && transmissionType != null ? transmissionType.getLangEntry().translateColored(color) : getTextComponent();
        }

        @Override
        public ResourceLocation getIcon() {
            return icon;
        }
    }
}