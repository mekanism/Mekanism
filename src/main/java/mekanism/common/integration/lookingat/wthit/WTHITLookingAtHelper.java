package mekanism.common.integration.lookingat.wthit;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.EncoderException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;
import mcp.mobius.waila.api.IData;
import mekanism.common.integration.lookingat.ChemicalElement;
import mekanism.common.integration.lookingat.EnergyElement;
import mekanism.common.integration.lookingat.FluidElement;
import mekanism.common.integration.lookingat.ILookingAtElement;
import mekanism.common.integration.lookingat.SimpleLookingAtHelper;
import mekanism.common.integration.lookingat.TextElement;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;

public class WTHITLookingAtHelper extends SimpleLookingAtHelper implements IData {

    public static final IData.Type<WTHITLookingAtHelper> TYPE = () -> MekanismWTHITPlugin.MEK_DATA;

    public static final StreamCodec<RegistryFriendlyByteBuf, WTHITLookingAtHelper> STREAM_CODEC = LookingAtTypes.STREAM_CODEC.<RegistryFriendlyByteBuf>cast()
          .dispatch(LookingAtTypes::getType,
                type -> switch (type) {
                    case ENERGY -> EnergyElement.STREAM_CODEC;
                    case FLUID -> FluidElement.STREAM_CODEC;
                    case CHEMICAL -> ChemicalElement.STREAM_CODEC;
                    case COMPONENT -> TextElement.STREAM_CODEC;
                }
          ).apply(ByteBufCodecs.list()).map(WTHITLookingAtHelper::new, helper -> helper.elements);

    public WTHITLookingAtHelper() {
        this(new ArrayList<>());
    }

    private WTHITLookingAtHelper(List<ILookingAtElement> elements) {
        super(elements);
    }

    @Override
    public Type<? extends IData> type() {
        return TYPE;
    }

    private enum LookingAtTypes {
        ENERGY,
        FLUID,
        CHEMICAL,
        COMPONENT;

        public static final IntFunction<LookingAtTypes> BY_ID = ByIdMap.continuous(LookingAtTypes::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, LookingAtTypes> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, LookingAtTypes::ordinal);

        public static LookingAtTypes getType(ILookingAtElement element) {
            return switch (element) {
                case TextElement textElement -> COMPONENT;
                case EnergyElement energyElement -> ENERGY;
                case FluidElement fluidElement -> FLUID;
                case ChemicalElement chemicalElement -> CHEMICAL;
                default -> throw new EncoderException("Unknown looking at type for " + element.getClass().getSimpleName());
            };
        }
    }
}