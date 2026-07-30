package mekanism.common.item;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import mekanism.api.IConfigurable;
import mekanism.api.IIncrementalEnum;
import mekanism.api.MekanismItemAbilities;
import mekanism.api.RelativeSide;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.radial.IRadialDataHelper;
import mekanism.api.radial.RadialData;
import mekanism.api.radial.mode.IRadialMode;
import mekanism.api.security.IBlockSecurityUtils;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent.IHasEnumNameTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeStateFacing;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.item.ItemConfigurator.ConfiguratorMode;
import mekanism.common.item.interfaces.IItemHUDProvider;
import mekanism.common.lib.radial.IRadialModeItem;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public class ItemConfigurator extends Item implements IRadialModeItem<ConfiguratorMode>, IItemHUDProvider {

    private static final Lazy<RadialData<ConfiguratorMode>> LAZY_RADIAL_DATA = Lazy.of(() ->
          IRadialDataHelper.INSTANCE.dataForEnum(Mekanism.rl("configurator_mode"), ConfiguratorMode.class));

    public ItemConfigurator(Properties properties) {
        super(properties.rarity(Rarity.UNCOMMON).stacksTo(1)
              .component(MekanismDataComponents.CONFIGURATOR_MODE, ConfiguratorMode.CONFIGURATE_ITEMS)
        );
    }

    @Override
    public Component getName(ItemStack stack) {
        return TextComponentUtil.build(EnumColor.AQUA, super.getName(stack));
    }

    @Override
    public boolean canPerformAction(ItemInstance instance, ItemAbility action) {
        if (action == MekanismItemAbilities.WRENCH_CONFIGURE) {
            return getMode(instance).isConfigurating();
        } else if (action == MekanismItemAbilities.WRENCH_CONFIGURE_CHEMICALS) {
            return getMode(instance) == ConfiguratorMode.CONFIGURATE_CHEMICALS;
        } else if (action == MekanismItemAbilities.WRENCH_CONFIGURE_ENERGY) {
            return getMode(instance) == ConfiguratorMode.CONFIGURATE_ENERGY;
        } else if (action == MekanismItemAbilities.WRENCH_CONFIGURE_FLUIDS) {
            return getMode(instance) == ConfiguratorMode.CONFIGURATE_FLUIDS;
        } else if (action == MekanismItemAbilities.WRENCH_CONFIGURE_HEAT) {
            return getMode(instance) == ConfiguratorMode.CONFIGURATE_HEAT;
        } else if (action == MekanismItemAbilities.WRENCH_CONFIGURE_ITEMS) {
            return getMode(instance) == ConfiguratorMode.CONFIGURATE_ITEMS;
        } else if (action == MekanismItemAbilities.WRENCH_DISMANTLE) {
            return getMode(instance) == ConfiguratorMode.WRENCH;
        } else if (action == MekanismItemAbilities.WRENCH_EMPTY) {
            return getMode(instance) == ConfiguratorMode.EMPTY;
        } else if (action == MekanismItemAbilities.WRENCH_ROTATE) {
            return getMode(instance) == ConfiguratorMode.ROTATE;
        }
        return super.canPerformAction(instance, action);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level world = context.getLevel();
        if (!world.isClientSide() && player != null) {
            BlockPos pos = context.getClickedPos();
            Direction side = context.getClickedFace();
            BlockEntity tile = WorldUtils.getTileEntity(world, pos);
            ConfiguratorMode mode = getMode(context.getItemInHand());
            if (mode.isConfigurating()) { //Configurate
                TransmissionType transmissionType = Objects.requireNonNull(mode.getTransmission(), "Configurating state requires transmission type");
                if (tile instanceof ISideConfiguration config && config.getConfig().supports(transmissionType)) {
                    ConfigInfo info = config.getConfig().getConfig(transmissionType);
                    if (info != null) {
                        RelativeSide relativeSide = RelativeSide.fromDirections(config.getDirection(), side);
                        DataType dataType = info.getDataType(relativeSide);
                        if (!player.isShiftKeyDown()) {
                            player.sendOverlayMessage(MekanismLang.CONFIGURATOR_VIEW_MODE.translateColored(EnumColor.GRAY, transmissionType, dataType.getColor(),
                                  dataType, dataType.getColor().getColoredName()));
                        } else if (!IBlockSecurityUtils.INSTANCE.canAccessOrDisplayError(player, world, pos, tile)) {
                            return InteractionResult.FAIL;
                        } else {
                            DataType old = dataType;
                            dataType = info.incrementDataType(relativeSide);
                            if (dataType != old) {
                                player.sendOverlayMessage(MekanismLang.CONFIGURATOR_TOGGLE_MODE.translateColored(EnumColor.GRAY, transmissionType, dataType.getColor(),
                                      dataType, dataType.getColor().getColoredName()));
                                config.getConfig().sideChanged(transmissionType, relativeSide);
                            }
                        }
                    }
                    return InteractionResult.SUCCESS;
                }
                if (!IBlockSecurityUtils.INSTANCE.canAccessOrDisplayError(player, world, pos, tile)) {
                    return InteractionResult.FAIL;
                }
                IConfigurable config = WorldUtils.getCapability(world, Capabilities.CONFIGURABLE, pos, null, tile, side);
                if (config != null) {
                    if (player.isShiftKeyDown()) {
                        return config.onSneakRightClick(world, player);
                    }
                    return config.onRightClick(world, player);
                }
            } else if (mode == ConfiguratorMode.EMPTY) { //Empty
                if (tile instanceof TileEntityMekanism inv && inv.hasInventory()) {
                    if (!IBlockSecurityUtils.INSTANCE.canAccessOrDisplayError(player, world, pos, tile)) {
                        return InteractionResult.FAIL;
                    }
                    boolean creative = player.isCreative();
                    if (tile instanceof TileEntityBin bin && bin.getTier().isCreative()) {
                        //If the tile is a creative bin only allow clearing it if the player is in creative
                        // and don't bother popping the stack out
                        if (creative) {
                            ContainerType.ITEM.clearContents(bin.getBinSlot(), null);
                            return InteractionResult.SUCCESS;
                        }
                        return InteractionResult.FAIL;
                    }
                    //TODO: Switch this to items being handled by TileEntityMekanism, energy handled here (via lambdas?)
                    for (IInventorySlot inventorySlot : inv.getInventorySlots()) {
                        if (!inventorySlot.isEmpty()) {
                            InventoryUtils.dropStack(world, pos, side, inventorySlot.resource(), inventorySlot.amountAsLong(), (lvl, p, face, item) -> {
                                if (face == null) {//Note: Theoretically this should never be null
                                    Block.popResource(lvl, p, item);
                                } else {
                                    Block.popResourceFromFace(lvl, p, face, item);
                                }
                            });
                            ContainerType.ITEM.clearContents(inventorySlot, null);
                        }
                    }
                    return InteractionResult.SUCCESS;
                }
            } else if (mode == ConfiguratorMode.ROTATE) { //Rotate
                if (tile instanceof TileEntityMekanism tileMekanism) {
                    if (!tileMekanism.isDirectional()) {
                        return InteractionResult.PASS;
                    } else if (!IBlockSecurityUtils.INSTANCE.canAccessOrDisplayError(player, world, pos, tile)) {
                        return InteractionResult.FAIL;
                    } else if (Attribute.matches(tileMekanism.getBlockHolder(), AttributeStateFacing.class, AttributeStateFacing::canRotate)) {
                        tileMekanism.setFacing(player.isShiftKeyDown() ? side.getOpposite() : side);
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
    public <ITEM extends TypedInstance<Item> & DataComponentGetter> void addHUDStrings(List<Component> list, Player player, ITEM instance, EquipmentSlot slotType) {
        list.add(MekanismLang.MODE.translateColored(EnumColor.PINK, getMode(instance)));
    }

    @Override
    public void changeMode(Player player, ItemAccess itemAccess, int shift, DisplayChange displayChange, TransactionContext transaction) {
        ConfiguratorMode mode = getMode(itemAccess);
        ConfiguratorMode newMode = mode.adjust(shift);
        if (mode != newMode && setMode(itemAccess, player, newMode, transaction)) {
            displayChange.sendMessage(player, newMode, MekanismLang.CONFIGURE_STATE::translate);
        }
    }

    @Override
    public <ITEM extends TypedInstance<Item> & DataComponentGetter> Component getScrollTextComponent(ITEM instance) {
        return getMode(instance).getTextComponent();
    }

    @Override
    public <ITEM extends TypedInstance<Item> & DataComponentGetter> RadialData<ConfiguratorMode> getRadialData(ITEM instance) {
        return LAZY_RADIAL_DATA.get();
    }

    @Override
    public DataComponentType<ConfiguratorMode> getModeDataType() {
        return MekanismDataComponents.CONFIGURATOR_MODE.get();
    }

    @Override
    public ConfiguratorMode getDefaultMode() {
        return ConfiguratorMode.CONFIGURATE_ITEMS;
    }

    public enum ConfiguratorMode implements IIncrementalEnum<ConfiguratorMode>, IHasEnumNameTextComponent, IRadialMode, StringRepresentable, TooltipProvider {
        CONFIGURATE_ITEMS(MekanismLang.CONFIGURATOR_CONFIGURATE, TransmissionType.ITEM, EnumColor.BRIGHT_GREEN, true, null),
        CONFIGURATE_FLUIDS(MekanismLang.CONFIGURATOR_CONFIGURATE, TransmissionType.FLUID, EnumColor.BRIGHT_GREEN, true, null),
        CONFIGURATE_CHEMICALS(MekanismLang.CONFIGURATOR_CONFIGURATE, TransmissionType.CHEMICAL, EnumColor.BRIGHT_GREEN, true, null),
        CONFIGURATE_ENERGY(MekanismLang.CONFIGURATOR_CONFIGURATE, TransmissionType.ENERGY, EnumColor.BRIGHT_GREEN, true, null),
        CONFIGURATE_HEAT(MekanismLang.CONFIGURATOR_CONFIGURATE, TransmissionType.HEAT, EnumColor.BRIGHT_GREEN, true, null),
        EMPTY(MekanismLang.CONFIGURATOR_EMPTY, null, EnumColor.DARK_RED, false, Mekanism.rl("radial/empty")),
        ROTATE(MekanismLang.CONFIGURATOR_ROTATE, null, EnumColor.YELLOW, false, Mekanism.rl("radial/rotate")),
        WRENCH(MekanismLang.CONFIGURATOR_WRENCH, null, EnumColor.PINK, false, Mekanism.rl("radial/wrench"));

        public static final Codec<ConfiguratorMode> CODEC = StringRepresentable.fromEnum(ConfiguratorMode::values);
        public static final IntFunction<ConfiguratorMode> BY_ID = ByIdMap.continuous(ConfiguratorMode::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, ConfiguratorMode> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, ConfiguratorMode::ordinal);

        private final String serializedName;
        private final ILangEntry langEntry;
        @Nullable
        private final TransmissionType transmissionType;
        private final EnumColor color;
        private final boolean configurating;
        private final Identifier icon;

        ConfiguratorMode(ILangEntry langEntry, @Nullable TransmissionType transmissionType, EnumColor color, boolean configurating, @Nullable Identifier icon) {
            this.serializedName = name().toLowerCase(Locale.ROOT);
            this.langEntry = langEntry;
            this.transmissionType = transmissionType;
            this.color = color;
            this.configurating = configurating;
            if (transmissionType == null) {
                this.icon = Objects.requireNonNull(icon, "Icon should only be null if there is a transmission type present.");
            } else {
                this.icon = transmissionType.guiTexture();
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

        @Override
        public ConfiguratorMode byIndex(int index) {
            return BY_ID.apply(index);
        }

        @Override
        public Component sliceName() {
            return configurating && transmissionType != null ? transmissionType.getLangEntry().translateColored(color) : getTextComponent();
        }

        @Override
        public Identifier icon() {
            return icon;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }

        @Override
        public void addToTooltip(TooltipContext context, Consumer<Component> builder, TooltipFlag flag, DataComponentGetter components) {
            builder.accept(MekanismLang.STATE.translateColored(EnumColor.PINK, this));
        }
    }
}