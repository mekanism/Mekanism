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

public class AttributeStateCommonValveMode implements AttributeState {

    public static final EnumProperty<AttributeStateCommonValveMode.CommonValveMode> modeProperty = EnumProperty.create("mode", AttributeStateCommonValveMode.CommonValveMode.class);

    @Override
    public BlockState copyStateData(BlockState oldState, BlockState newState) {
        if (Attribute.has(newState, AttributeStateCommonValveMode.class)) {
            newState = newState.setValue(modeProperty, oldState.getValue(modeProperty));
        }
        return newState;
    }

    @Override
    public BlockState getDefaultState(@NotNull BlockState state) {
        return state.setValue(modeProperty, AttributeStateCommonValveMode.CommonValveMode.INPUT);
    }

    @Override
    public void fillBlockStateContainer(Block block, List<Property<?>> properties) {
        properties.add(modeProperty);
    }

    @NothingNullByDefault
    public enum CommonValveMode implements StringRepresentable, IHasEnumNameTextComponent, IIncrementalEnum<AttributeStateCommonValveMode.CommonValveMode> {
        INPUT("input", MekanismLang.COMMON_VALVE_MODE_INPUT, EnumColor.BRIGHT_GREEN),
        OUTPUT("output", MekanismLang.COMMON_VALVE_MODE_OUTPUT, EnumColor.RED);

        public static final IntFunction<AttributeStateCommonValveMode.CommonValveMode> BY_ID = ByIdMap.continuous(AttributeStateCommonValveMode.CommonValveMode::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, AttributeStateCommonValveMode.CommonValveMode> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, AttributeStateCommonValveMode.CommonValveMode::ordinal);

        private final String name;
        private final ILangEntry langEntry;
        private final EnumColor color;

        CommonValveMode(String name, ILangEntry langEntry, EnumColor color) {
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
        public AttributeStateCommonValveMode.CommonValveMode byIndex(int index) {
            return BY_ID.apply(index);
        }
    }
}
