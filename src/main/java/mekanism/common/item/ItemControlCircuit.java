package mekanism.common.item;

import mekanism.common.config_old.MekanismConfigOld;
import mekanism.common.tier.BaseTier;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class ItemControlCircuit extends ItemMekanismTiered {

    public ItemControlCircuit(BaseTier tier) {
        super(tier, "control_circuit");
    }

    @Override
    public void registerOreDict() {
        if (MekanismConfigOld.current().general.controlCircuitOreDict.get()) {
            OreDictionary.registerOre("circuit" + getTier().getSimpleName(), new ItemStack(this));
        }
    }
}