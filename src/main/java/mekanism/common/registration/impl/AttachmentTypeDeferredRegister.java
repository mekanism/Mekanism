package mekanism.common.registration.impl;

import java.util.function.Supplier;
import mekanism.common.registration.WrappedDeferredRegister;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.ForgeRegistries;

public class AttachmentTypeDeferredRegister extends WrappedDeferredRegister<AttachmentType<?>> {

    public AttachmentTypeDeferredRegister(String modid) {
        super(modid, ForgeRegistries.Keys.ATTACHMENT_TYPES);
    }

    public <TYPE> AttachmentTypeRegistryObject<TYPE> register(String name, Supplier<AttachmentType<TYPE>> supplier) {
        return register(name, supplier, AttachmentTypeRegistryObject::new);
    }
}