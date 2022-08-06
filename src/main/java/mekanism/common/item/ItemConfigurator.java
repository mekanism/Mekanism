package mekanism.common.item;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IConfigurable;
import mekanism.api.IIncrementalEnum;
import mekanism.api.MekanismAPI;
import mekanism.api.NBTConstants;
import mekanism.api.RelativeSide;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeStateFacing;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.ItemConfigurator.ConfiguratorMode;
import mekanism.common.item.interfaces.IItemHUDProvider;
import mekanism.api.radial.RadialData;
import mekanism.api.radial.mode.IRadialMode;
import mekanism.common.lib.radial.IRadialEnumModeItem;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.WorldUtils;
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
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemConfigurator extends ItemEnergized implements IRadialEnumModeItem<ConfiguratorMode>, IItemHUDProvider {

    public static final Lazy<RadialData<ConfiguratorMode>> LAZY_RADIAL_DATA = Lazy.of(() ->
          MekanismAPI.getRadialDataHelper().dataForEnum(Mekanism.rl("configurator_mode"), ConfiguratorMode.class));

    public ItemConfigurator(Properties properties) {
        super(MekanismConfig.gear.configuratorChargeRate, MekanismConfig.gear.configuratorMaxEnergy, properties.rarity(Rarity.UNCOMMON));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);
        tooltip.add(MekanismLang.STATE.translateColored(EnumColor.PINK, getMode(stack)));
    }

    @NotNull
    @Override
    public Component getName(@NotNull ItemStack stack) {
        return TextComponentUtil.build(EnumColor.AQUA, super.getName(stack));
    }

    @NotNull
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
                            player.sendSystemMessage(MekanismUtils.logFormat(MekanismLang.CONFIGURATOR_VIEW_MODE.translate(transmissionType, dataType.getColor(), dataType,
                                  dataType.getColor().getColoredName())));
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
                                player.sendSystemMessage(MekanismUtils.logFormat(MekanismLang.CONFIGURATOR_TOGGLE_MODE.translate(transmissionType, dataType.getColor(), dataType,
                                      dataType.getColor().getColoredName())));
                                config.getConfig().sideChanged(transmissionType, relativeSide);
                            }
                        }
                    }
                    return InteractionResult.SUCCESS;
                }
                if (!MekanismAPI.getSecurityUtils().canAccessOrDisplayError(player, tile)) {
                    return InteractionResult.FAIL;
                }
                Optional<IConfigurable> capability = CapabilityUtils.getCapability(tile, Capabilities.CONFIGURABLE, side).resolve();
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
                    if (!tileMekanism.isDirectional()) {
                        return InteractionResult.PASS;
                    } else if (!MekanismAPI.getSecurityUtils().canAccessOrDisplayError(player, tile)) {
                        return InteractionResult.FAIL;
                    } else if (Attribute.get(tileMekanism.getBlockType(), AttributeStateFacing.class).canRotate()) {
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

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader world, BlockPos pos, Player player) {
        return getMode(stack) == ConfiguratorMode.WRENCH;
    }

    @Override
    public void addHUDStrings(List<Component> list, Player player, ItemStack stack, EquipmentSlot slotType) {
        list.add(MekanismLang.MODE.translateColored(EnumColor.PINK, getMode(stack)));
    }

    @Override
    public void changeMode(@NotNull Player player, @NotNull ItemStack stack, int shift, boolean displayChangeMessage) {
        ConfiguratorMode mode = getMode(stack);
        ConfiguratorMode newMode = mode.adjust(shift);
        if (mode != newMode) {
            setMode(stack, player, newMode);
            if (displayChangeMessage) {
                player.sendSystemMessage(MekanismUtils.logFormat(MekanismLang.CONFIGURE_STATE.translate(newMode)));
            }
        }
    }

    @NotNull
    @Override
    public Component getScrollTextComponent(@NotNull ItemStack stack) {
        return getMode(stack).getTextComponent();
    }

    @Override
    public String getModeSaveKey() {
        return NBTConstants.STATE;
    }

    @NotNull
    @Override
    public RadialData<ConfiguratorMode> getRadialData(ItemStack stack) {
        return LAZY_RADIAL_DATA.get();
    }

    @Override
    public ConfiguratorMode getModeByIndex(int ordinal) {
        return ConfiguratorMode.byIndexStatic(ordinal);
    }

    @NothingNullByDefault
    public enum ConfiguratorMode implements IIncrementalEnum<ConfiguratorMode>, IHasTextComponent, IRadialMode {
        CONFIGURATE_ITEMS(MekanismLang.CONFIGURATOR_CONFIGURATE, TransmissionType.ITEM, EnumColor.BRIGHT_GREEN, true, null),
        CONFIGURATE_FLUIDS(MekanismLang.CONFIGURATOR_CONFIGURATE, TransmissionType.FLUID, EnumColor.BRIGHT_GREEN, true, null),
        CONFIGURATE_GASES(MekanismLang.CONFIGURATOR_CONFIGURATE, TransmissionType.GAS, EnumColor.BRIGHT_GREEN, true, null),
        CONFIGURATE_INFUSE_TYPES(MekanismLang.CONFIGURATOR_CONFIGURATE, TransmissionType.INFUSION, EnumColor.BRIGHT_GREEN, true, null),
        CONFIGURATE_PIGMENTS(MekanismLang.CONFIGURATOR_CONFIGURATE, TransmissionType.PIGMENT, EnumColor.BRIGHT_GREEN, true, null),
        CONFIGURATE_SLURRIES(MekanismLang.CONFIGURATOR_CONFIGURATE, TransmissionType.SLURRY, EnumColor.BRIGHT_GREEN, true, null),
        CONFIGURATE_ENERGY(MekanismLang.CONFIGURATOR_CONFIGURATE, TransmissionType.ENERGY, EnumColor.BRIGHT_GREEN, true, null),
        CONFIGURATE_HEAT(MekanismLang.CONFIGURATOR_CONFIGURATE, TransmissionType.HEAT, EnumColor.BRIGHT_GREEN, true, null),
        EMPTY(MekanismLang.CONFIGURATOR_EMPTY, null, EnumColor.DARK_RED, false, MekanismUtils.getResource(ResourceType.GUI_RADIAL, "empty.png")),
        ROTATE(MekanismLang.CONFIGURATOR_ROTATE, null, EnumColor.YELLOW, false, MekanismUtils.getResource(ResourceType.GUI_RADIAL, "rotate.png")),
        WRENCH(MekanismLang.CONFIGURATOR_WRENCH, null, EnumColor.PINK, false, MekanismUtils.getResource(ResourceType.GUI_RADIAL, "wrench.png"));

        private static final ConfiguratorMode[] MODES = values();
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
            if (transmissionType == null) {
                this.icon = Objects.requireNonNull(icon, "Icon should only be null if there is a transmission type present.");
            } else {
                this.icon = MekanismUtils.getResource(ResourceType.GUI, transmissionType.getTransmission() + ".png");
            }
        }

        @Override
        public Component getTextComponent() {
            if (transmissionType == null) {
                return langEntry.translateColored(color);
            }
            return langEntry.translateColored(color, transmissionType);
        }

        @Override
        public EnumColor color() {
            return color;
        }

        public boolean isConfigurating() {
            return configurating;
        }

        @Nullable
        public TransmissionType getTransmission() {
            return transmissionType;
        }

        @NotNull
        @Override
        public ConfiguratorMode byIndex(int index) {
            return byIndexStatic(index);
        }

        public static ConfiguratorMode byIndexStatic(int index) {
            return MathUtils.getByIndexMod(MODES, index);
        }

        @NotNull
        @Override
        public Component sliceName() {
            return configurating && transmissionType != null ? transmissionType.getLangEntry().translateColored(color) : getTextComponent();
        }

        @NotNull
        @Override
        public ResourceLocation icon() {
            return icon;
        }
    }
}