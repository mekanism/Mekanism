package mekanism.common.inventory.container.item;

import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.registries.MekanismItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class DictionaryContainer extends MekanismItemContainer {

    public DictionaryContainer(int id, Inventory inv, InteractionHand hand, ItemAccess itemAccess) {
        super(MekanismContainerTypes.DICTIONARY, id, inv, hand, itemAccess);
    }

    @Override
    protected int getInventoryYOffset() {
        return super.getInventoryYOffset() + 5;
    }

    @Override
    protected boolean isValidType(ItemResource itemType) {
        return super.isValidType(itemType) && MekanismItems.DICTIONARY.is(itemType);
    }
}