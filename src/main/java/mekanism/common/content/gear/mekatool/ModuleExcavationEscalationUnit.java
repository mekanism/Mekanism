package mekanism.common.content.gear.mekatool;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.function.Consumer;
import mekanism.api.IIncrementalEnum;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.config.IModuleConfigItem;
import mekanism.api.gear.config.ModuleConfigItemCreator;
import mekanism.api.gear.config.ModuleEnumData;
import mekanism.api.math.MathUtils;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ParametersAreNotNullByDefault
public class ModuleExcavationEscalationUnit implements ICustomModule<ModuleExcavationEscalationUnit> {

    private static final ResourceLocation RADIAL_ID = Mekanism.rl("excavation_mode");
    private static final Int2ObjectMap<Lazy<NestedRadialMode>> RADIAL_DATAS = Util.make(() -> {
        int types = ExcavationMode.MODES.length - 2;
        Int2ObjectMap<Lazy<NestedRadialMode>> map = new Int2ObjectArrayMap<>(types);
        for (int type = 1; type <= types; type++) {
            int accessibleValues = type + 2;
            map.put(type, Lazy.of(() -> new NestedRadialMode(MekanismAPI.getRadialDataHelper().dataForTruncated(RADIAL_ID, accessibleValues, ExcavationMode.NORMAL),
                  MekanismLang.RADIAL_EXCAVATION_SPEED, ExcavationMode.NORMAL.icon(), EnumColor.YELLOW)));
        }
        return map;
    });

    private IModuleConfigItem<ExcavationMode> excavationMode;

    @Override
    public void init(IModule<ModuleExcavationEscalationUnit> module, ModuleConfigItemCreator configItemCreator) {
        excavationMode = configItemCreator.createConfigItem("excavation_mode", MekanismLang.MODULE_EFFICIENCY,
              new ModuleEnumData<>(ExcavationMode.NORMAL, module.getInstalledCount() + 2));
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
            return (MODE) excavationMode.get();
        }
        return null;
    }

    @Override
    public <MODE extends IRadialMode> boolean setMode(IModule<ModuleExcavationEscalationUnit> module, Player player, ItemStack stack, RadialData<MODE> radialData, MODE mode) {
        if (radialData == getRadialData(module)) {
            ExcavationMode newMode = (ExcavationMode) mode;
            if (excavationMode.get() != newMode) {
                excavationMode.set(newMode);
            }
        }
        return false;
    }

    @Nullable
    @Override
    public Component getModeScrollComponent(IModule<ModuleExcavationEscalationUnit> module, ItemStack stack) {
        ExcavationMode mode = excavationMode.get();
        return MekanismLang.GENERIC_WITH_PARENTHESIS.translateColored(EnumColor.INDIGO, mode.sliceName(), EnumColor.AQUA, mode.getEfficiency());
    }

    @Override
    public void changeMode(IModule<ModuleExcavationEscalationUnit> module, Player player, ItemStack stack, int shift, boolean displayChangeMessage) {
        ExcavationMode currentMode = excavationMode.get();
        ExcavationMode newMode = currentMode.adjust(shift, v -> v.ordinal() < module.getInstalledCount() + 2);
        if (currentMode != newMode) {
            excavationMode.set(newMode);
            if (displayChangeMessage) {
                module.displayModeChange(player, MekanismLang.MODULE_EFFICIENCY.translate(), newMode);
            }
        }
    }

    @Override
    public void addHUDStrings(IModule<ModuleExcavationEscalationUnit> module, Player player, Consumer<Component> hudStringAdder) {
        if (module.isEnabled()) {
            hudStringAdder.accept(MekanismLang.DISASSEMBLER_EFFICIENCY.translateColored(EnumColor.DARK_GRAY, EnumColor.INDIGO, excavationMode.get().getEfficiency()));
        }
    }

    public float getEfficiency() {
        return excavationMode.get().getEfficiency();
    }

    @NothingNullByDefault
    public enum ExcavationMode implements IIncrementalEnum<ExcavationMode>, IHasTextComponent, IRadialMode {
        OFF(MekanismLang.RADIAL_EXCAVATION_SPEED_OFF, 0, EnumColor.WHITE, "speed_off"),
        SLOW(MekanismLang.RADIAL_EXCAVATION_SPEED_SLOW, 4, EnumColor.PINK, "speed_slow"),
        NORMAL(MekanismLang.RADIAL_EXCAVATION_SPEED_NORMAL, 16, EnumColor.BRIGHT_GREEN, "speed_normal"),
        FAST(MekanismLang.RADIAL_EXCAVATION_SPEED_FAST, 32, EnumColor.YELLOW, "speed_fast"),
        SUPER_FAST(MekanismLang.RADIAL_EXCAVATION_SPEED_SUPER, 64, EnumColor.ORANGE, "speed_super"),
        EXTREME(MekanismLang.RADIAL_EXCAVATION_SPEED_EXTREME, 128, EnumColor.RED, "speed_extreme");

        private static final ExcavationMode[] MODES = values();

        private final ResourceLocation icon;
        private final ILangEntry langEntry;
        private final Component label;
        private final EnumColor color;
        private final int efficiency;

        ExcavationMode(ILangEntry langEntry, int efficiency, EnumColor color, String texture) {
            this.langEntry = langEntry;
            this.efficiency = efficiency;
            this.color = color;
            this.icon = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_RADIAL, texture + ".png");
            this.label = TextComponentUtil.getString(Integer.toString(efficiency));
        }

        @Override
        public ExcavationMode byIndex(int index) {
            return MathUtils.getByIndexMod(MODES, index);
        }

        @Override
        public Component getTextComponent() {
            return label;
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
    }
}
