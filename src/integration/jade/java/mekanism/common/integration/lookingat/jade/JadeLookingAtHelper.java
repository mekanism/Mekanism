package mekanism.common.integration.lookingat.jade;

import mekanism.api.SerializationConstants;
import mekanism.common.integration.lookingat.LookingAtElementType;
import mekanism.common.integration.lookingat.SimpleLookingAtHelper;
import net.minecraft.nbt.CompoundTag;
import snownee.jade.api.Accessor;

public class JadeLookingAtHelper extends SimpleLookingAtHelper {

    public void finalizeData(CompoundTag data, Accessor<?> accessor) {
        if (!elements.isEmpty()) {
            data.put(SerializationConstants.MEK_DATA, accessor.encodeAsNbt(LookingAtElementType.ELEMENT_LIST_STREAM_CODEC, elements));
        }
    }
}