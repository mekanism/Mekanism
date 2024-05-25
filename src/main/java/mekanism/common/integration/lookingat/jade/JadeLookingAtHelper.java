package mekanism.common.integration.lookingat.jade;

import mekanism.api.SerializationConstants;
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
            ListTag list = new ListTag();
            for (ILookingAtElement element : elements) {
                list.add(JadeTooltipRenderer.ELEMENT_CODEC.encodeStart(registryOps, element).getOrThrow());
            }
            data.put(SerializationConstants.MEK_DATA, list);
        }
    }
}