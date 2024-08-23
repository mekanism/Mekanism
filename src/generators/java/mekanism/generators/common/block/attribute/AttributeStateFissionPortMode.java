package mekanism.generators.common.block.attribute;

import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.function.IntFunction;
import mekanism.api.IIncrementalEnum;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent.IHasEnumNameTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeState;
import mekanism.generators.common.GeneratorsLang;
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
    public enum FissionPortMode implements StringRepresentable, IHasEnumNameTextComponent, IIncrementalEnum<FissionPortMode> {
        INPUT("input", GeneratorsLang.FISSION_PORT_MODE_INPUT, EnumColor.BRIGHT_GREEN),
        OUTPUT_WASTE("output_waste", GeneratorsLang.FISSION_PORT_MODE_OUTPUT_WASTE, EnumColor.BROWN),
        OUTPUT_COOLANT("output_coolant", GeneratorsLang.FISSION_PORT_MODE_OUTPUT_COOLANT, EnumColor.DARK_AQUA);

        public static final IntFunction<FissionPortMode> BY_ID = ByIdMap.continuous(FissionPortMode::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, FissionPortMode> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, FissionPortMode::ordinal);

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

        @Override
        public FissionPortMode byIndex(int index) {
            return BY_ID.apply(index);
        }
    }
}