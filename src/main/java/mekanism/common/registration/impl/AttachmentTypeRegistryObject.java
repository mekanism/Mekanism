package mekanism.common.registration.impl;

import mekanism.common.registration.WrappedRegistryObject;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.RegistryObject;

public class AttachmentTypeRegistryObject<TYPE> extends WrappedRegistryObject<AttachmentType<TYPE>> {

    public AttachmentTypeRegistryObject(RegistryObject<AttachmentType<TYPE>> registryObject) {
        super(registryObject);
    }
}