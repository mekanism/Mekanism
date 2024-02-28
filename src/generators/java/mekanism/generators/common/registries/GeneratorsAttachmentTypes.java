package mekanism.generators.common.registries;

import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.impl.AttachmentTypeDeferredRegister;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorLogicAdapter.FissionReactorLogic;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorLogicAdapter.FusionReactorLogic;
import net.neoforged.neoforge.attachment.AttachmentType;

public class GeneratorsAttachmentTypes {

    private GeneratorsAttachmentTypes() {
    }

    public static final AttachmentTypeDeferredRegister ATTACHMENT_TYPES = new AttachmentTypeDeferredRegister(MekanismGenerators.MODID);

    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<FissionReactorLogic>> FISSION_LOGIC_TYPE = ATTACHMENT_TYPES.register("fission_logic", FissionReactorLogic.class);
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<FusionReactorLogic>> FUSION_LOGIC_TYPE = ATTACHMENT_TYPES.register("fusion_logic", FusionReactorLogic.class);

    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> ACTIVE_COOLED = ATTACHMENT_TYPES.registerBoolean("active_cooled", false);
}