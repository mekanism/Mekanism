package mekanism.common.item.block.machine;

import mekanism.api.energy.IEnergyContainer;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeEnergy;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.capabilities.energy.item.ResistiveHeaterItemEnergyContainer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ItemBlockResistiveHeater extends ItemBlockMachine {

    public ItemBlockResistiveHeater(BlockTile<?, ?> block) {
        super(block);
    }

    @Nullable
    @Override
    protected IEnergyContainer getDefaultEnergyContainer(ItemStack stack) {
        return ResistiveHeaterItemEnergyContainer.create(Attribute.get(getBlock(), AttributeEnergy.class));
    }
}