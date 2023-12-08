package mekanism.common.registries;

import com.mojang.serialization.Codec;
import java.util.Objects;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.chemical.item.ChemicalTankContentsHandler;
import mekanism.common.capabilities.merged.GaugeDropperContentsHandler;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.MekanismDeferredRegister;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class MekanismAttachmentTypes {

    private MekanismAttachmentTypes() {
    }

    public static final MekanismDeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = new MekanismDeferredRegister<>(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Mekanism.MODID);

    //Note: We do not specify copy on death as we want radiation to reset to baseline on death
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<Double>> RADIATION = ATTACHMENT_TYPES.register("radiation",
          () -> AttachmentType.builder(() -> RadiationManager.BASELINE)
                .serialize(Codec.doubleRange(RadiationManager.BASELINE, Double.MAX_VALUE))
                //Note: Technically this comparator is not needed as by default neo only checks for attachment compatability for item stacks,
                // but we set it regardless just so that if anyone is checking it for entities then they can bypass the serialization for it
                .comparator(Objects::equals)
                .build()
    );

    //Non-serializable attachments for use in persisting a backing object between mutliple capabilities
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<ChemicalTankContentsHandler>> CHEMICAL_TANK_CONTENTS_HANDLER = ATTACHMENT_TYPES.register("chemical_tank_contents_handler",
          () -> AttachmentType.builder(ChemicalTankContentsHandler::createDummy).build());
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<GaugeDropperContentsHandler>> GAUGE_DROPPER_CONTENTS_HANDLER = ATTACHMENT_TYPES.register("gauge_dropper_contents_handler",
          () -> AttachmentType.builder(GaugeDropperContentsHandler::createDummy).build());
}