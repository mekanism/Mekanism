package mekanism.additions.common.registries;

import java.util.Objects;
import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.item.ItemWalkieTalkie.WalkieData;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.impl.AttachmentTypeDeferredRegister;
import net.neoforged.neoforge.attachment.AttachmentType;

public class AdditionsAttachmentTypes {

    private AdditionsAttachmentTypes() {
    }

    public static final AttachmentTypeDeferredRegister ATTACHMENT_TYPES = new AttachmentTypeDeferredRegister(MekanismAdditions.MODID);

    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<WalkieData>> WALKIE_DATA = ATTACHMENT_TYPES.register("walkie_data",
          () -> AttachmentType.serializable(WalkieData::new)
                .comparator(Objects::equals)
                .build());
}