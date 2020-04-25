package mekanism.common.block.attribute;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IProperty;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.text.ITextComponent;

public class AttributeStateBoilerValveMode extends AttributeState {

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
    public void fillBlockStateContainer(Block block, List<IProperty<?>>properties) {
        properties.add(modeProperty);
    }

    public enum BoilerValveMode implements IStringSerializable {
        INPUT("input", MekanismLang.BOILER_VALVE_MODE_INPUT, EnumColor.BRIGHT_GREEN),
        OUTPUT_STEAM("output_steam", MekanismLang.BOILER_VALVE_MODE_OUTPUT_STEAM, EnumColor.GRAY),
        OUTPUT_COOLANT("output_coolant", MekanismLang.BOILER_VALVE_MODE_OUTPUT_STEAM, EnumColor.DARK_AQUA);

        private String name;
        private ILangEntry langEntry;
        private EnumColor color;

        private BoilerValveMode(String name, ILangEntry langEntry, EnumColor color) {
            this.name = name;
            this.langEntry = langEntry;
            this.color = color;
        }

        @Override
        public String getName() {
            return name;
        }

        public ITextComponent translate() {
            return langEntry.translateColored(color);
        }
    }
}
