package mekanism.common.registries;

import com.mojang.serialization.Codec;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.radiation.IRadiationManager;
import mekanism.common.Mekanism;
import mekanism.common.item.gear.ItemFlamethrower.FlamethrowerMode;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.MekanismDeferredRegister;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@NothingNullByDefault
public class MekanismAttachmentTypes {

    private MekanismAttachmentTypes() {
    }

    public static final MekanismDeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = new MekanismDeferredRegister<>(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Mekanism.MODID);

    //Note: We do not specify copy on death as we want radiation to reset to baseline on death
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<Double>> RADIATION = ATTACHMENT_TYPES.register("radiation",
          () -> AttachmentType.builder(IRadiationManager.INSTANCE::baselineRadiation)
                .serialize(Codec.doubleRange(IRadiationManager.INSTANCE.baselineRadiation(), Double.MAX_VALUE), radiation -> radiation > IRadiationManager.INSTANCE.baselineRadiation())
                .copyHandler((radiation, holder, provider) -> radiation > IRadiationManager.INSTANCE.baselineRadiation() ? radiation : null)
                .build()
    );

    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<FlamethrowerMode>> FLAMETHROWER_MODE = ATTACHMENT_TYPES.register("flamethrower_mode", () ->
          AttachmentType.builder(() -> FlamethrowerMode.COMBAT)
                .serialize(FlamethrowerMode.CODEC, mode -> mode != FlamethrowerMode.COMBAT)
                .build());
}