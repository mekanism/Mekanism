package mekanism.common.integration.projecte.mappers;

import java.util.List;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ItemStackChemicalToItemStackRecipe;
import mekanism.common.integration.projecte.IngredientHelper;
import mekanism.common.integration.projecte.NSSChemical;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

@RecipeTypeMapper
public class ItemStackChemicalToItemStackRecipeMapper extends TypedMekanismRecipeMapper<ItemStackChemicalToItemStackRecipe> {

    public ItemStackChemicalToItemStackRecipeMapper() {
        super(ItemStackChemicalToItemStackRecipe.class, MekanismRecipeType.COMPRESSING, MekanismRecipeType.PURIFYING, MekanismRecipeType.INJECTING,
              MekanismRecipeType.PAINTING, MekanismRecipeType.METALLURGIC_INFUSING);
    }

    @Override
    public String getName() {
        return "MekItemStackChemicalToItemStack";
    }

    @Override
    public String getDescription() {
        return "Maps Mekanism Machine recipes that go from item, chemical to item. (Compressing, Purifying, Injecting, Metallurgic Infusing, Painting)";
    }

    @Override
    protected boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, ItemStackChemicalToItemStackRecipe recipe) {
        boolean handled = false;
        List<@NotNull ItemStack> itemRepresentations = recipe.getItemInput().getRepresentations();
        List<@NotNull ChemicalStack> chemicalRepresentations = recipe.getChemicalInput().getRepresentations();
        for (ChemicalStack chemicalRepresentation : chemicalRepresentations) {
            NSSChemical nssChemical = NSSChemical.createChemical(chemicalRepresentation);
            long chemicalAmount = chemicalRepresentation.getAmount();
            if (recipe.perTickUsage()) {
                chemicalAmount *= TileEntityAdvancedElectricMachine.BASE_TICKS_REQUIRED;
            }
            for (ItemStack itemRepresentation : itemRepresentations) {
                ItemStack output = recipe.getOutput(itemRepresentation, chemicalRepresentation);
                if (!output.isEmpty()) {
                    IngredientHelper ingredientHelper = new IngredientHelper(mapper);
                    ingredientHelper.put(itemRepresentation);
                    ingredientHelper.put(nssChemical, chemicalAmount);
                    if (ingredientHelper.addAsConversion(output)) {
                        handled = true;
                    }
                }
            }
        }
        return handled;
    }
}