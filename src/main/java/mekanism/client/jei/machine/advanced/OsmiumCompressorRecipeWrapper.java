package mekanism.client.jei.machine.advanced;

import java.util.List;
import mekanism.api.gas.Gas;
import mekanism.client.jei.machine.AdvancedMachineRecipeWrapper;
import mekanism.common.MekanismItems;
import mekanism.common.recipe.machines.AdvancedMachineRecipe;
import mekanism.common.util.ListUtils;
import net.minecraft.item.ItemStack;

public class OsmiumCompressorRecipeWrapper extends AdvancedMachineRecipeWrapper {

    public OsmiumCompressorRecipeWrapper(AdvancedMachineRecipe r) {
        super(r);
    }

    @Override
    public List<ItemStack> getFuelStacks(Gas gasType) {
        return ListUtils.asList(new ItemStack(MekanismItems.Ingot, 1, 1));
    }
}
