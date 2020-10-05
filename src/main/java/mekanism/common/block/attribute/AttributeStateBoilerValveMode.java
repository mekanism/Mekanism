package mekanism.common.block.attribute;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.IIncrementalEnum;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.Property;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.text.ITextComponent;

public class AttributeStateBoilerValveMode implements AttributeState {

    public static final EnumProperty<BoilerValveMode> modeProperty = EnumProperty.create("mode", BoilerValveMode.class);

    @Override
    public BlockState copyStateData(BlockState oldState, BlockState newState) {
        if (Attribute.has(newState.getBlock(), AttributeStateBoilerValveMode.class)) {
            newState = newState.with(modeProperty, oldState.get(modeProperty));
        }
        return newState;
    }

    @Override
    public BlockState getDefaultState(@Nonnull BlockState state) {
        return state.with(modeProperty, BoilerValveMode.INPUT);
    }

    @Override
    public void fillBlockStateContainer(Block block, List<Property<?>> properties) {
        properties.add(modeProperty);
    }

    public enum BoilerValveMode implements IStringSerializable, IHasTextComponent, IIncrementalEnum<BoilerValveMode> {
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

        @Nonnull
        @Override
        public String getString() {
            return name;
        }

        @Override
        public ITextComponent getTextComponent() {
            return langEntry.translateColored(color);
        }

        @Nonnull
        public static BoilerValveMode byIndexStatic(int index) {
            return MathUtils.getByIndexMod(MODES, index);
        }

        @Nonnull
        @Override
        public BoilerValveMode byIndex(int index) {
            return byIndexStatic(index);
        }
    }
}
