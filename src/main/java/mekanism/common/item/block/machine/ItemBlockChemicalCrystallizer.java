package mekanism.common.item.block.machine;

import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.registries.MekanismAttachmentTypes;
import net.neoforged.bus.api.IEventBus;

public class ItemBlockChemicalCrystallizer extends ItemBlockMachine {

    public ItemBlockChemicalCrystallizer(BlockTile<?, ?> block) {
        super(block);
    }

    @Override
    public void attachAttachments(IEventBus eventBus) {
        super.attachAttachments(eventBus);
        //Note: We pass null for the event bus to not expose this attachment as a capability
        ContainerType.addMergedDefaultContainer(null, this, MekanismAttachmentTypes.CRYSTALLIZER_CONTENTS_HANDLER);
    }
}