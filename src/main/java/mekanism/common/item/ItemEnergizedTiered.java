package mekanism.common.item;

import mekanism.common.Mekanism;
import mekanism.common.Tier;
import net.minecraft.item.ItemStack;

public class ItemEnergizedTiered extends ItemEnergized
{
    /** The Tier used to define this equipment's energy stats. */
    public Tier.EquipmentTier EquipmentTier;

    public ItemEnergizedTiered(Tier.EquipmentTier tier)
    {
        super(0);
        EquipmentTier = tier;
        setMaxStackSize(1);
        setCreativeTab(Mekanism.tabMekanism);
    }


    @Override
    public double getMaxEnergy(ItemStack itemStack)
    {
        return EquipmentTier.energy;
    }

}
