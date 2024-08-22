package mekanism.common.block.attribute;

import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.function.IntFunction;
import mekanism.api.IIncrementalEnum;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent.IHasEnumNameTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
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
    public enum BoilerValveMode implements StringRepresentable, IHasEnumNameTextComponent, IIncrementalEnum<BoilerValveMode> {
        INPUT("input", MekanismLang.BOILER_VALVE_MODE_INPUT, EnumColor.BRIGHT_GREEN),
        OUTPUT_STEAM("output_steam", MekanismLang.BOILER_VALVE_MODE_OUTPUT_STEAM, EnumColor.RED),
        OUTPUT_COOLANT("output_coolant", MekanismLang.BOILER_VALVE_MODE_OUTPUT_COOLANT, EnumColor.DARK_AQUA);

        public static final IntFunction<BoilerValveMode> BY_ID = ByIdMap.continuous(BoilerValveMode::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, BoilerValveMode> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, BoilerValveMode::ordinal);

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

        @Override
        public BoilerValveMode byIndex(int index) {
            return BY_ID.apply(index);
        }
    }
}
