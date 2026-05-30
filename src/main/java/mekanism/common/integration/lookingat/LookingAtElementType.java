package mekanism.common.integration.lookingat;

import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.function.IntFunction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;

public enum LookingAtElementType {
    CHEMICAL(ChemicalElement.STREAM_CODEC),
    ENERGY(EnergyElement.STREAM_CODEC),
    FLUID(FluidElement.STREAM_CODEC),
    TEXT(TextElement.STREAM_CODEC);

    public static final IntFunction<LookingAtElementType> BY_ID = ByIdMap.continuous(LookingAtElementType::ordinal, values(), OutOfBoundsStrategy.WRAP);
    public static final StreamCodec<ByteBuf, LookingAtElementType> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, LookingAtElementType::ordinal);

    public static final StreamCodec<RegistryFriendlyByteBuf, ILookingAtElement> ELEMENT_STREAM_CODEC = STREAM_CODEC.<RegistryFriendlyByteBuf>cast().dispatch(
          LookingAtElementType::getType,
          type -> type.elementStreamCodec
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, List<ILookingAtElement>> ELEMENT_LIST_STREAM_CODEC = ELEMENT_STREAM_CODEC.apply(ByteBufCodecs.list());

    private final StreamCodec<? super RegistryFriendlyByteBuf, ? extends ILookingAtElement> elementStreamCodec;

    LookingAtElementType(StreamCodec<? super RegistryFriendlyByteBuf, ? extends ILookingAtElement> elementStreamCodec) {
        this.elementStreamCodec = elementStreamCodec;
    }

    private static LookingAtElementType getType(ILookingAtElement element) {
        return switch (element) {
            case ChemicalElement _ -> CHEMICAL;
            case EnergyElement _ -> ENERGY;
            case FluidElement _ -> FLUID;
            case TextElement _ -> TEXT;
        };
    }
}