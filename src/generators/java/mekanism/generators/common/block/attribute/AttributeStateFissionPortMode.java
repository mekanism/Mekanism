package mekanism.generators.common.block.attribute;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.IIncrementalEnum;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeState;
import mekanism.generators.common.GeneratorsLang;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.Property;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.text.ITextComponent;

public class AttributeStateFissionPortMode implements AttributeState {

    public static final EnumProperty<FissionPortMode> modeProperty = EnumProperty.create("mode", FissionPortMode.class);

    @Override
    public BlockState copyStateData(BlockState oldState, BlockState newState) {
        if (Attribute.has(newState.getBlock(), AttributeStateFissionPortMode.class)) {
            newState = newState.with(modeProperty, oldState.get(modeProperty));
        }
        return newState;
    }

    @Override
    public BlockState getDefaultState(@Nonnull BlockState state) {
        return state.with(modeProperty, FissionPortMode.INPUT);
    }

    @Override
    public void fillBlockStateContainer(Block block, List<Property<?>> properties) {
        properties.add(modeProperty);
    }

    public enum FissionPortMode implements IStringSerializable, IHasTextComponent, IIncrementalEnum<FissionPortMode> {
        INPUT("input", GeneratorsLang.FISSION_PORT_MODE_INPUT, EnumColor.BRIGHT_GREEN),
        OUTPUT_WASTE("output_waste", GeneratorsLang.FISSION_PORT_MODE_OUTPUT_WASTE, EnumColor.BROWN),
        OUTPUT_COOLANT("output_coolant", GeneratorsLang.FISSION_PORT_MODE_OUTPUT_COOLANT, EnumColor.DARK_AQUA);

        private static final FissionPortMode[] MODES = values();

        private final String name;
        private final ILangEntry langEntry;
        private final EnumColor color;

        FissionPortMode(String name, ILangEntry langEntry, EnumColor color) {
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

        public static FissionPortMode byIndexStatic(int index) {
            return MathUtils.getByIndexMod(MODES, index);
        }

        @Nonnull
        @Override
        public FissionPortMode byIndex(int index) {
            return byIndexStatic(index);
        }
    }
}