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
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Rarity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemConfigurator extends ItemEnergized implements IMekWrench, IRadialModeItem<ConfiguratorMode>, IItemHUDProvider {

    public ItemConfigurator(Properties properties) {
        super(MekanismConfig.gear.configuratorChargeRate, MekanismConfig.gear.configuratorMaxEnergy, properties.rarity(Rarity.UNCOMMON));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        tooltip.add(MekanismLang.STATE.translateColored(EnumColor.PINK, getMode(stack)));
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName(@Nonnull ItemStack stack) {
        return TextComponentUtil.build(EnumColor.AQUA, super.getDisplayName(stack));
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
            TileEntity tile = WorldUtils.getTileEntity(world, pos);
            ConfiguratorMode mode = getMode(stack);
            if (mode.isConfigurating()) { //Configurate
                TransmissionType transmissionType = Objects.requireNonNull(mode.getTransmission(), "Configurating state requires transmission type");
                if (tile instanceof ISideConfiguration && ((ISideConfiguration) tile).getConfig().supports(transmissionType)) {
                    ISideConfiguration config = (ISideConfiguration) tile;
                    ConfigInfo info = config.getConfig().getConfig(transmissionType);
                    if (info != null) {
                        RelativeSide relativeSide = RelativeSide.fromDirections(config.getOrientation(), side);
                        DataType dataType = info.getDataType(relativeSide);
                        if (!player.isSneaking()) {
                            player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM, EnumColor.GRAY,
                                  MekanismLang.CONFIGURATOR_VIEW_MODE.translate(transmissionType, dataType.getColor(), dataType, dataType.getColor().getColoredName())),
                                  Util.DUMMY_UUID);
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
                            player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM, EnumColor.GRAY,
                                  MekanismLang.CONFIGURATOR_TOGGLE_MODE.translate(transmissionType, dataType.getColor(), dataType, dataType.getColor().getColoredName())),
                                  Util.DUMMY_UUID);
                            config.getConfig().sideChanged(transmissionType, relativeSide);
                        } else {
                            SecurityUtils.displayNoAccess(player);
                        }
                    }
                    return ActionResultType.SUCCESS;
                }
                if (SecurityUtils.canAccess(player, tile)) {
                    Optional<IConfigurable> capability = CapabilityUtils.getCapability(tile, Capabilities.CONFIGURABLE_CAPABILITY, side).resolve();
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
            } else if (mode == ConfiguratorMode.EMPTY) { //Empty
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
                                    inventorySlot.setEmpty();
                                }
                            }
                            return ActionResultType.SUCCESS;
                        } else {
                            SecurityUtils.displayNoAccess(player);
                            return ActionResultType.FAIL;
                        }
                    }
                }
            } else if (mode == ConfiguratorMode.ROTATE) { //Rotate
                if (tile instanceof TileEntityMekanism) {
                    if (SecurityUtils.canAccess(player, tile)) {
                        TileEntityMekanism tileMekanism = (TileEntityMekanism) tile;
                        if (Attribute.get(tileMekanism.getBlockType(), AttributeStateFacing.class).canRotate()) {
                            if (!player.isSneaking()) {
                                tileMekanism.setFacing(side);
                            } else if (player.isSneaking()) {
                                tileMekanism.setFacing(side.getOpposite());
                            }
                        }
                    } else {
                        SecurityUtils.displayNoAccess(player);
                    }
                }
                return ActionResultType.SUCCESS;
            } else if (mode == ConfiguratorMode.WRENCH) { //Wrench
                return ActionResultType.PASS;
            }
        }
        return ActionResultType.PASS;
    }

    public EnumColor getColor(ConfiguratorMode mode) {
        return mode.getColor();
    }

    @Override
    public boolean canUseWrench(ItemStack stack, PlayerEntity player, BlockPos pos) {
        return getMode(stack) == ConfiguratorMode.WRENCH;
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, IWorldReader world, BlockPos pos, PlayerEntity player) {
        return getMode(stack) == ConfiguratorMode.WRENCH;
    }

    @Override
    public void addHUDStrings(List<ITextComponent> list, ItemStack stack, EquipmentSlotType slotType) {
        list.add(MekanismLang.MODE.translateColored(EnumColor.PINK, getMode(stack)));
    }

    @Override
    public void changeMode(@Nonnull PlayerEntity player, @Nonnull ItemStack stack, int shift, boolean displayChangeMessage) {
        ConfiguratorMode mode = getMode(stack);
        ConfiguratorMode newMode = mode.adjust(shift);
        if (mode != newMode) {
            setMode(stack, player, newMode);
            if (displayChangeMessage) {
                player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM, EnumColor.GRAY,
                      MekanismLang.CONFIGURE_STATE.translate(newMode)), Util.DUMMY_UUID);
            }
        }
    }

    @Nonnull
    @Override
    public ITextComponent getScrollTextComponent(@Nonnull ItemStack stack) {
        return getMode(stack).getTextComponent();
    }

    @Override
    public void setMode(ItemStack stack, PlayerEntity player, ConfiguratorMode mode) {
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
        public ITextComponent getTextComponent() {
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
            switch (this) {
                case CONFIGURATE_ITEMS:
                    return TransmissionType.ITEM;
                case CONFIGURATE_FLUIDS:
                    return TransmissionType.FLUID;
                case CONFIGURATE_GASES:
                    return TransmissionType.GAS;
                case CONFIGURATE_INFUSE_TYPES:
                    return TransmissionType.INFUSION;
                case CONFIGURATE_PIGMENTS:
                    return TransmissionType.PIGMENT;
                case CONFIGURATE_SLURRIES:
                    return TransmissionType.SLURRY;
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

        @Override
        public ITextComponent getShortText() {
            return configurating && transmissionType != null ? transmissionType.getLangEntry().translateColored(color) : getTextComponent();
        }

        @Override
        public ResourceLocation getIcon() {
            return icon;
        }
    }
}