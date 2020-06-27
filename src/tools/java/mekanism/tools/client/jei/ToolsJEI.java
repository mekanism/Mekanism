package mekanism.tools.client.jei;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Collections;
import javax.annotation.Nonnull;
import mekanism.api.providers.IItemProvider;
import mekanism.tools.common.IHasRepairType;
import mekanism.tools.common.MekanismTools;
import mekanism.tools.common.registries.ToolsItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

@JeiPlugin
public class ToolsJEI implements IModPlugin {

    @Nonnull
    @Override
    public ResourceLocation getPluginUid() {
        return MekanismTools.rl("jei_plugin");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registry) {
        IVanillaRecipeFactory factory = registry.getVanillaRecipeFactory();
        //Add the Anvil repair recipes to JEI for all the different tools and armors in Mekanism Tools
        for (IItemProvider toolsItem : ToolsItems.ITEMS.getAllItems()) {
            //Based off of how JEI adds for Vanilla items
            ItemStack damaged2 = toolsItem.getItemStack();
            damaged2.setDamage(damaged2.getMaxDamage() * 3 / 4);
            ItemStack damaged3 = toolsItem.getItemStack();
            damaged3.setDamage(damaged3.getMaxDamage() * 2 / 4);

            //Two damaged items combine to undamaged
            registry.addRecipes(ImmutableList.of(factory.createAnvilRecipe(damaged2, Collections.singletonList(damaged2), Collections.singletonList(damaged3))), VanillaRecipeCategoryUid.ANVIL);

            Item item = toolsItem.getItem();
            if (item instanceof IHasRepairType) {
                ItemStack[] repairStacks = ((IHasRepairType) item).getRepairMaterial().getMatchingStacks();
                //Damaged item + the repair material
                if (repairStacks.length > 0) {
                    //While this is damaged1 it is down here as we don't need to bother creating the reference if we don't have a repair material
                    ItemStack damaged1 = toolsItem.getItemStack();
                    damaged1.setDamage(damaged1.getMaxDamage());
                    registry.addRecipes(ImmutableList.of(factory.createAnvilRecipe(damaged1, Arrays.asList(repairStacks), Collections.singletonList(damaged2))), VanillaRecipeCategoryUid.ANVIL);
                }
            }
        }
    }
}