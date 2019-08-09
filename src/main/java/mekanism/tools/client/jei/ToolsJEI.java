package mekanism.tools.client.jei;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import javax.annotation.Nonnull;
import mekanism.tools.common.MekanismTools;
import mekanism.tools.common.ToolsItem;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

@JeiPlugin
public class ToolsJEI implements IModPlugin {

    @Nonnull
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(MekanismTools.MODID, "jei_plugin");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registry) {
        IVanillaRecipeFactory factory = registry.getVanillaRecipeFactory();
        //Add the Anvil repair recipes to JEI for all the different tools and armors in Mekanism Tools
        for (ToolsItem toolsItem : ToolsItem.values()) {
            //Based off of how JEI adds for Vanilla items: https://github.com/mezz/JustEnoughItems/blob/1.12/src/main/java/mezz/jei/plugins/vanilla/anvil/AnvilRecipeMaker.java#L180
            ItemStack damaged2 = toolsItem.getItemStack();
            damaged2.setDamage(damaged2.getMaxDamage() * 3 / 4);
            ItemStack damaged3 = toolsItem.getItemStack();
            damaged3.setDamage(damaged3.getMaxDamage() * 2 / 4);

            //Two damaged items combine to undamaged
            registry.addRecipes(ImmutableList.of(factory.createAnvilRecipe(damaged2, Collections.singletonList(damaged2), Collections.singletonList(damaged3))), VanillaRecipeCategoryUid.ANVIL);

            ItemStack repairMaterial = toolsItem.getRepairStack();
            //Damaged item + the repair material
            if (!repairMaterial.isEmpty()) {
                //While this is damaged1 it is down here as we don't need to bother creating the reference if we don't have a repair material
                ItemStack damaged1 = toolsItem.getItemStack();
                damaged1.setDamage(damaged1.getMaxDamage());
                registry.addRecipes(ImmutableList.of(factory.createAnvilRecipe(damaged1, Collections.singletonList(repairMaterial), Collections.singletonList(damaged2))), VanillaRecipeCategoryUid.ANVIL);
            }
        }
    }
}