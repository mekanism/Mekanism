package mekanism.common.content.gear.mekatool;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import mekanism.api.IIncrementalEnum;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.radial.IRadialDataHelper;
import mekanism.api.radial.RadialData;
import mekanism.api.radial.mode.IRadialMode;
import mekanism.api.radial.mode.NestedRadialMode;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.util.MekanismUtils;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.TranslatableEnum;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ParametersAreNotNullByDefault
public record ModuleExcavationEscalationUnit(ExcavationMode excavationMode) implements ICustomModule<ModuleExcavationEscalationUnit> {

    public static final ResourceLocation EXCAVATION_MODE = Mekanism.rl("efficiency");

    private static final ResourceLocation RADIAL_ID = Mekanism.rl("excavation_mode");
    private static final Int2ObjectMap<Lazy<NestedRadialMode>> RADIAL_DATAS = Util.make(() -> {
        int types = ExcavationMode.values().length - 2;
        Int2ObjectMap<Lazy<NestedRadialMode>> map = new Int2ObjectArrayMap<>(types);
        for (int type = 1; type <= types; type++) {
            int accessibleValues = type + 2;
            map.put(type, Lazy.of(() -> new NestedRadialMode(IRadialDataHelper.INSTANCE.dataForTruncated(RADIAL_ID, accessibleValues, ExcavationMode.NORMAL),
                  MekanismLang.RADIAL_EXCAVATION_SPEED, ExcavationMode.NORMAL.icon(), EnumColor.YELLOW)));
        }
        return map;
    });

    public ModuleExcavationEscalationUnit(IModule<ModuleExcavationEscalationUnit> module) {
        this(module.<ExcavationMode>getConfigOrThrow(EXCAVATION_MODE).get());
    }

    private NestedRadialMode getNestedData(IModule<ModuleExcavationEscalationUnit> module) {
        return RADIAL_DATAS.get(module.getInstalledCount()).get();
    }

    private RadialData<?> getRadialData(IModule<ModuleExcavationEscalationUnit> module) {
        return getNestedData(module).nestedData();
    }

    @Override
    public void addRadialModes(IModule<ModuleExcavationEscalationUnit> module, @NotNull ItemStack stack, Consumer<NestedRadialMode> adder) {
        adder.accept(getNestedData(module));
    }

    @Nullable
    @Override
    public <MODE extends IRadialMode> MODE getMode(IModule<ModuleExcavationEscalationUnit> module, ItemStack stack, RadialData<MODE> radialData) {
        if (radialData == getRadialData(module)) {
            return (MODE) excavationMode;
        }
        return null;
    }

    @Override
    public <MODE extends IRadialMode> boolean setMode(IModule<ModuleExcavationEscalationUnit> module, Player player, IModuleContainer moduleContainer, ItemStack stack, RadialData<MODE> radialData, MODE mode) {
        if (radialData == getRadialData(module)) {
            ExcavationMode newMode = (ExcavationMode) mode;
            if (excavationMode != newMode) {
                moduleContainer.replaceModuleConfig(player.registryAccess(), stack, module.getDataHolder(), module.<ExcavationMode>getConfigOrThrow(EXCAVATION_MODE).with(newMode));
            }
        }
        return false;
    }

    @Override
    public Component getModeScrollComponent(IModule<ModuleExcavationEscalationUnit> module, ItemStack stack) {
        return MekanismLang.GENERIC_WITH_PARENTHESIS.translateColored(EnumColor.INDIGO, excavationMode.sliceName(), EnumColor.AQUA, excavationMode.getEfficiency());
    }

    @Override
    public void changeMode(IModule<ModuleExcavationEscalationUnit> module, Player player, IModuleContainer moduleContainer, ItemStack stack, int shift, boolean displayChangeMessage) {
        ExcavationMode newMode = excavationMode.adjust(shift, v -> v.ordinal() < module.getInstalledCount() + 2);
        if (excavationMode != newMode) {
            if (displayChangeMessage) {
                module.displayModeChange(player, MekanismLang.MODULE_EFFICIENCY.translate(), newMode);
            }
            moduleContainer.replaceModuleConfig(player.registryAccess(), stack, module.getDataHolder(), module.<ExcavationMode>getConfigOrThrow(EXCAVATION_MODE).with(newMode));
        }
    }

    @Override
    public void addHUDStrings(IModule<ModuleExcavationEscalationUnit> module, IModuleContainer moduleContainer, ItemStack stack, Player player, Consumer<Component> hudStringAdder) {
        if (module.isEnabled()) {
            hudStringAdder.accept(MekanismLang.DISASSEMBLER_EFFICIENCY.translateColored(EnumColor.DARK_GRAY, EnumColor.INDIGO, getEfficiency()));
        }
    }

    public float getEfficiency() {
        return excavationMode.getEfficiency();
    }

    @NothingNullByDefault
    public enum ExcavationMode implements IIncrementalEnum<ExcavationMode>, IHasTextComponent, TranslatableEnum, IRadialMode, StringRepresentable {
        OFF(MekanismLang.RADIAL_EXCAVATION_SPEED_OFF, 0, EnumColor.WHITE, "speed_off"),
        SLOW(MekanismLang.RADIAL_EXCAVATION_SPEED_SLOW, 4, EnumColor.PINK, "speed_slow"),
        NORMAL(MekanismLang.RADIAL_EXCAVATION_SPEED_NORMAL, 16, EnumColor.BRIGHT_GREEN, "speed_normal"),
        FAST(MekanismLang.RADIAL_EXCAVATION_SPEED_FAST, 32, EnumColor.YELLOW, "speed_fast"),
        SUPER_FAST(MekanismLang.RADIAL_EXCAVATION_SPEED_SUPER, 64, EnumColor.ORANGE, "speed_super"),
        EXTREME(MekanismLang.RADIAL_EXCAVATION_SPEED_EXTREME, 128, EnumColor.RED, "speed_extreme");

        public static final Codec<ExcavationMode> CODEC = StringRepresentable.fromEnum(ExcavationMode::values);
        public static final IntFunction<ExcavationMode> BY_ID = ByIdMap.continuous(ExcavationMode::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, ExcavationMode> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, ExcavationMode::ordinal);

        private final String serializedName;
        private final ResourceLocation icon;
        private final ILangEntry langEntry;
        private final Component label;
        private final EnumColor color;
        private final int efficiency;

        ExcavationMode(ILangEntry langEntry, int efficiency, EnumColor color, String texture) {
            this.serializedName = name().toLowerCase(Locale.ROOT);
            this.langEntry = langEntry;
            this.efficiency = efficiency;
            this.color = color;
            this.icon = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_RADIAL, texture + ".png");
            this.label = TextComponentUtil.getString(Integer.toString(efficiency));
        }

        @Override
        public ExcavationMode byIndex(int index) {
            return BY_ID.apply(index);
        }

        @Override
        public Component getTextComponent() {
            return label;
        }

        @Override
        public Component getTranslatedName() {
            return sliceName();
        }

        public int getEfficiency() {
            return efficiency;
        }

        @NotNull
        @Override
        public Component sliceName() {
            return langEntry.translateColored(color);
        }

        @NotNull
        @Override
        public ResourceLocation icon() {
            return icon;
        }

        @Override
        public EnumColor color() {
            return color;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }
}
