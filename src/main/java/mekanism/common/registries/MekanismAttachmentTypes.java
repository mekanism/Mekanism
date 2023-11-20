package mekanism.common.registries;

import com.mojang.serialization.Codec;
import mekanism.common.Mekanism;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.MekanismDeferredRegister;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class MekanismAttachmentTypes {

    private MekanismAttachmentTypes() {
    }

    public static final MekanismDeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = new MekanismDeferredRegister<>(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Mekanism.MODID);

    //TODO - 1.20.2: Expose to radiation manager So people don't have to make their own registry object? Or maybe we don't care
    //Note: We do not specify copy on death as we want radiation to reset to baseline on death
    //TODO - 1.20.2: Validate that works properly
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<Double>> RADIATION = ATTACHMENT_TYPES.register("radiation",
          () -> AttachmentType.builder(() -> RadiationManager.BASELINE)
                .serialize(Codec.doubleRange(RadiationManager.BASELINE, Double.MAX_VALUE))
                .build()
    );
}