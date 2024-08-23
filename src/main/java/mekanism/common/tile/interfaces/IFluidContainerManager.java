package mekanism.common.tile.interfaces;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Locale;
import java.util.function.IntFunction;
import mekanism.api.IIncrementalEnum;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.text.IHasTextComponent.IHasEnumNameTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

public interface IFluidContainerManager extends IHasMode {

    ContainerEditMode getContainerEditMode();

    @NothingNullByDefault
    enum ContainerEditMode implements IIncrementalEnum<ContainerEditMode>, IHasEnumNameTextComponent, StringRepresentable {
        BOTH(MekanismLang.FLUID_CONTAINER_BOTH),
        FILL(MekanismLang.FLUID_CONTAINER_FILL),
        EMPTY(MekanismLang.FLUID_CONTAINER_EMPTY);

        public static final Codec<ContainerEditMode> CODEC = StringRepresentable.fromEnum(ContainerEditMode::values);
        public static final IntFunction<ContainerEditMode> BY_ID = ByIdMap.continuous(ContainerEditMode::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, ContainerEditMode> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, ContainerEditMode::ordinal);

        private final String serializedName;
        private final ILangEntry langEntry;

        ContainerEditMode(ILangEntry langEntry) {
            this.serializedName = name().toLowerCase(Locale.ROOT);
            this.langEntry = langEntry;
        }

        @Override
        public Component getTextComponent() {
            return langEntry.translate();
        }

        @Override
        public ContainerEditMode byIndex(int index) {
            return BY_ID.apply(index);
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }
}