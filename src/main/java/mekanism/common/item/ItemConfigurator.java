package mekanism.common.item;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.IntFunction;
import mekanism.api.IIncrementalEnum;
import mekanism.api.MekanismItemAbilities;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.radial.IRadialDataHelper;
import mekanism.api.radial.RadialData;
import mekanism.api.radial.mode.IRadialMode;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent.IHasEnumNameTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.item.ItemConfigurator.ConfiguratorMode;
import mekanism.common.item.interfaces.IItemHUDProvider;
import mekanism.common.lib.radial.IRadialModeItem;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.WrenchUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.LevelReader;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemConfigurator extends Item implements IRadialModeItem<ConfiguratorMode>, IItemHUDProvider {

    private static final Lazy<RadialData<ConfiguratorMode>> LAZY_RADIAL_DATA = Lazy.of(() ->
          IRadialDataHelper.INSTANCE.dataForEnum(Mekanism.rl("configurator_mode"), ConfiguratorMode.class));

    public ItemConfigurator(Properties properties) {
        super(properties.rarity(Rarity.UNCOMMON).stacksTo(1)
              .component(MekanismDataComponents.CONFIGURATOR_MODE, ConfiguratorMode.CONFIGURATE_ITEMS)
        );
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(MekanismLang.STATE.translateColored(EnumColor.PINK, getMode(stack)));
    }

    @NotNull
    @Override
    public Component getName(@NotNull ItemStack stack) {
        return TextComponentUtil.build(EnumColor.AQUA, super.getName(stack));
    }

    @Override
    public boolean canPerformAction(@NotNull ItemStack stack, @NotNull ItemAbility action) {
        if (action == MekanismItemAbilities.WRENCH_CONFIGURE) {
            return getMode(stack).configurating;
        } else if (action == MekanismItemAbilities.WRENCH_CONFIGURE_CHEMICALS) {
            return getMode(stack) == ConfiguratorMode.CONFIGURATE_CHEMICALS;
        } else if (action == MekanismItemAbilities.WRENCH_CONFIGURE_ENERGY) {
            return getMode(stack) == ConfiguratorMode.CONFIGURATE_ENERGY;
        } else if (action == MekanismItemAbilities.WRENCH_CONFIGURE_FLUIDS) {
            return getMode(stack) == ConfiguratorMode.CONFIGURATE_FLUIDS;
        } else if (action == MekanismItemAbilities.WRENCH_CONFIGURE_HEAT) {
            return getMode(stack) == ConfiguratorMode.CONFIGURATE_HEAT;
        } else if (action == MekanismItemAbilities.WRENCH_CONFIGURE_ITEMS) {
            return getMode(stack) == ConfiguratorMode.CONFIGURATE_ITEMS;
        } else if (action == MekanismItemAbilities.WRENCH_DISMANTLE) {
            return getMode(stack) == ConfiguratorMode.WRENCH;
        } else if (action == MekanismItemAbilities.WRENCH_EMPTY) {
            return getMode(stack) == ConfiguratorMode.EMPTY;
        } else if (action == MekanismItemAbilities.WRENCH_ROTATE) {
            return getMode(stack) == ConfiguratorMode.ROTATE;
        }
        return super.canPerformAction(stack, action);
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        return WrenchUtils.useConfigurator(context, this).getInteractionResult(context.getLevel().isClientSide);
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
    public void changeMode(@NotNull Player player, @NotNull ItemStack stack, int shift, DisplayChange displayChange) {
        ConfiguratorMode mode = getMode(stack);
        ConfiguratorMode newMode = mode.adjust(shift);
        if (mode != newMode) {
            setMode(stack, player, newMode);
            displayChange.sendMessage(player, newMode, MekanismLang.CONFIGURE_STATE::translate);
        }
    }

    @NotNull
    @Override
    public Component getScrollTextComponent(@NotNull ItemStack stack) {
        return getMode(stack).getTextComponent();
    }

    @NotNull
    @Override
    public RadialData<ConfiguratorMode> getRadialData(ItemStack stack) {
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

    @NothingNullByDefault
    public enum ConfiguratorMode implements IIncrementalEnum<ConfiguratorMode>, IHasEnumNameTextComponent, IRadialMode, StringRepresentable {
        CONFIGURATE_ITEMS(MekanismLang.CONFIGURATOR_CONFIGURATE, TransmissionType.ITEM, EnumColor.BRIGHT_GREEN, null),
        CONFIGURATE_FLUIDS(MekanismLang.CONFIGURATOR_CONFIGURATE, TransmissionType.FLUID, EnumColor.BRIGHT_GREEN, null),
        CONFIGURATE_CHEMICALS(MekanismLang.CONFIGURATOR_CONFIGURATE, TransmissionType.CHEMICAL, EnumColor.BRIGHT_GREEN, null),
        CONFIGURATE_ENERGY(MekanismLang.CONFIGURATOR_CONFIGURATE, TransmissionType.ENERGY, EnumColor.BRIGHT_GREEN, null),
        CONFIGURATE_HEAT(MekanismLang.CONFIGURATOR_CONFIGURATE, TransmissionType.HEAT, EnumColor.BRIGHT_GREEN, null),
        EMPTY(MekanismLang.CONFIGURATOR_EMPTY, null, EnumColor.DARK_RED, MekanismUtils.getResource(ResourceType.GUI_RADIAL, "empty.png")),
        ROTATE(MekanismLang.CONFIGURATOR_ROTATE, null, EnumColor.YELLOW, MekanismUtils.getResource(ResourceType.GUI_RADIAL, "rotate.png")),
        WRENCH(MekanismLang.CONFIGURATOR_WRENCH, null, EnumColor.PINK, MekanismUtils.getResource(ResourceType.GUI_RADIAL, "wrench.png"));

        public static final Codec<ConfiguratorMode> CODEC = StringRepresentable.fromEnum(ConfiguratorMode::values);
        public static final IntFunction<ConfiguratorMode> BY_ID = ByIdMap.continuous(ConfiguratorMode::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, ConfiguratorMode> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, ConfiguratorMode::ordinal);

        private final String serializedName;
        private final ILangEntry langEntry;
        @Nullable
        private final TransmissionType transmissionType;
        private final EnumColor color;
        public final boolean configurating;
        private final ResourceLocation icon;

        ConfiguratorMode(ILangEntry langEntry, @Nullable TransmissionType transmissionType, EnumColor color, @Nullable ResourceLocation icon) {
            this.serializedName = name().toLowerCase(Locale.ROOT);
            this.langEntry = langEntry;
            this.transmissionType = transmissionType;
            this.color = color;
            this.configurating = transmissionType != null;
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

        @Nullable
        public TransmissionType getTransmission() {
            return transmissionType;
        }

        @NotNull
        @Override
        public ConfiguratorMode byIndex(int index) {
            return BY_ID.apply(index);
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

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }
}