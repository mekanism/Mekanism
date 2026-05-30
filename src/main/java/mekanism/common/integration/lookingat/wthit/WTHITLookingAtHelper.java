package mekanism.common.integration.lookingat.wthit;

import java.util.ArrayList;
import java.util.List;
import mcp.mobius.waila.api.IData;
import mekanism.common.integration.lookingat.ILookingAtElement;
import mekanism.common.integration.lookingat.LookingAtElementType;
import mekanism.common.integration.lookingat.SimpleLookingAtHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class WTHITLookingAtHelper extends SimpleLookingAtHelper implements IData {

    public static final IData.Type<WTHITLookingAtHelper> TYPE = () -> MekanismWTHITPlugin.MEK_DATA;

    public static final StreamCodec<RegistryFriendlyByteBuf, WTHITLookingAtHelper> STREAM_CODEC = LookingAtElementType.ELEMENT_LIST_STREAM_CODEC.map(
          WTHITLookingAtHelper::new, helper -> helper.elements
    );

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
}