package mekanism.generators.common.block.attribute;

import java.util.List;
import mekanism.api.IIncrementalEnum;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeState;
import mekanism.generators.common.GeneratorsLang;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;

public class AttributeStateFissionPortMode implements AttributeState {

    public static final EnumProperty<FissionPortMode> modeProperty = EnumProperty.create("mode", FissionPortMode.class);

    @Override
    public BlockState copyStateData(BlockState oldState, BlockState newState) {
        if (Attribute.has(newState, AttributeStateFissionPortMode.class)) {
            newState = newState.setValue(modeProperty, oldState.getValue(modeProperty));
        }
        return newState;
    }

    @Override
    public BlockState getDefaultState(@NotNull BlockState state) {
        return state.setValue(modeProperty, FissionPortMode.INPUT);
    }

    @Override
    public void fillBlockStateContainer(Block block, List<Property<?>> properties) {
        properties.add(modeProperty);
    }

    @NothingNullByDefault
    public enum FissionPortMode implements StringRepresentable, IHasTextComponent, IIncrementalEnum<FissionPortMode> {
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

        @Override
        public String getSerializedName() {
            return name;
        }

        @Override
        public Component getTextComponent() {
            return langEntry.translateColored(color);
        }

        public static FissionPortMode byIndexStatic(int index) {
            return MathUtils.getByIndexMod(MODES, index);
        }

        @Override
        public FissionPortMode byIndex(int index) {
            return byIndexStatic(index);
        }
    }
}