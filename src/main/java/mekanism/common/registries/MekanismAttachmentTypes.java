package mekanism.common.registries;

import com.mojang.serialization.Codec;
import mekanism.common.Mekanism;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.registration.impl.AttachmentTypeDeferredRegister;
import mekanism.common.registration.impl.AttachmentTypeRegistryObject;
import net.neoforged.neoforge.attachment.AttachmentType;

public class MekanismAttachmentTypes {

    private MekanismAttachmentTypes() {
    }

    public static final AttachmentTypeDeferredRegister ATTACHMENT_TYPES = new AttachmentTypeDeferredRegister(Mekanism.MODID);

    //TODO - 1.20.2: Expose to radiation manager So people don't have to make their own registry object? Or maybe we don't care
    //Note: We do not specify copy on death as we want radiation to reset to baseline on death
    //TODO - 1.20.2: Validate that works properly
    public static final AttachmentTypeRegistryObject<Double> RADIATION = ATTACHMENT_TYPES.register("radiation",
          () -> AttachmentType.builder(() -> RadiationManager.BASELINE)
                .serialize(Codec.doubleRange(RadiationManager.BASELINE, Double.MAX_VALUE))
                .build()
    );
}