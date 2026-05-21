package mekanism.common.inventory.container.item;

import mekanism.common.inventory.container.IEmptyContainer;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.registries.MekanismItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class SeismicReaderContainer extends MekanismItemContainer implements IEmptyContainer {

    public SeismicReaderContainer(int id, Inventory inv, InteractionHand hand, ItemAccess itemAccess) {
        super(MekanismContainerTypes.SEISMIC_READER, id, inv, hand, itemAccess);
    }

    @Override
    protected boolean isValidType(ItemResource itemType) {
        return super.isValidType(itemType) && MekanismItems.SEISMIC_READER.is(itemType);
    }
}