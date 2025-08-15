package mekanism.common.integration.lookingat.jade;

import com.mojang.serialization.DataResult;
import mekanism.api.SerializationConstants;
import mekanism.common.Mekanism;
import mekanism.common.integration.lookingat.ILookingAtElement;
import mekanism.common.integration.lookingat.SimpleLookingAtHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;

public class JadeLookingAtHelper extends SimpleLookingAtHelper {

    private final HolderLookup.Provider provider;

    public JadeLookingAtHelper(HolderLookup.Provider provider) {
        this.provider = provider;
    }

    public void finalizeData(CompoundTag data) {
        if (!elements.isEmpty()) {
            RegistryOps<Tag> registryOps = provider.createSerializationContext(NbtOps.INSTANCE);
            ListTag list = new ListTag(elements.size());
            for (ILookingAtElement element : elements) {
                DataResult<Tag> encoded = JadeTooltipRenderer.ELEMENT_CODEC.encodeStart(registryOps, element);
                encoded.ifSuccess(list::add);
                encoded.ifError(error -> Mekanism.logger.warn("Failed to serialize jade looking at data: {}", error.message()));
            }
            data.put(SerializationConstants.MEK_DATA, list);
        }
    }
}