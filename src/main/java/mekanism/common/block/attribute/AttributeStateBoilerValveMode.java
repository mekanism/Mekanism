package mekanism.common.block.attribute;

import java.util.List;
import mekanism.api.IIncrementalEnum;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;

public class AttributeStateBoilerValveMode implements AttributeState {

    public static final EnumProperty<BoilerValveMode> modeProperty = EnumProperty.create("mode", BoilerValveMode.class);

    @Override
    public BlockState copyStateData(BlockState oldState, BlockState newState) {
        if (Attribute.has(newState, AttributeStateBoilerValveMode.class)) {
            newState = newState.setValue(modeProperty, oldState.getValue(modeProperty));
        }
        return newState;
    }

    @Override
    public BlockState getDefaultState(@NotNull BlockState state) {
        return state.setValue(modeProperty, BoilerValveMode.INPUT);
    }

    @Override
    public void fillBlockStateContainer(Block block, List<Property<?>> properties) {
        properties.add(modeProperty);
    }

    @NothingNullByDefault
    public enum BoilerValveMode implements StringRepresentable, IHasTextComponent, IIncrementalEnum<BoilerValveMode> {
        INPUT("input", MekanismLang.BOILER_VALVE_MODE_INPUT, EnumColor.BRIGHT_GREEN),
        OUTPUT_STEAM("output_steam", MekanismLang.BOILER_VALVE_MODE_OUTPUT_STEAM, EnumColor.GRAY),
        OUTPUT_COOLANT("output_coolant", MekanismLang.BOILER_VALVE_MODE_OUTPUT_COOLANT, EnumColor.DARK_AQUA);

        private static final BoilerValveMode[] MODES = values();

        private final String name;
        private final ILangEntry langEntry;
        private final EnumColor color;

        BoilerValveMode(String name, ILangEntry langEntry, EnumColor color) {
            this.name = name;
            this.langEntry = langEntry;
            this.color = color;
        }

        @Override
        public String getSerializedName() {
            return name;
        }

        @Override
        public Component getTextComponent() {
            return langEntry.translateColored(color);
        }

        public static BoilerValveMode byIndexStatic(int index) {
            return MathUtils.getByIndexMod(MODES, index);
        }

        @Override
        public BoilerValveMode byIndex(int index) {
            return byIndexStatic(index);
        }
    }
}
