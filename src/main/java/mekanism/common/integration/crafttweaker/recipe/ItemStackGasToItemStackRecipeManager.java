package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.item.crafting.IRecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_ITEM_STACK_GAS_TO_ITEM_STACK)
public abstract class ItemStackGasToItemStackRecipeManager implements IRecipeManager {

    protected ItemStackGasToItemStackRecipeManager() {
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_COMPRESSING)
    public static class OsmiumCompressorRecipeManager extends ItemStackGasToItemStackRecipeManager {

        public static final OsmiumCompressorRecipeManager INSTANCE = new OsmiumCompressorRecipeManager();

        private OsmiumCompressorRecipeManager() {
        }

        @Override
        public IRecipeType<ItemStackGasToItemStackRecipe> getRecipeType() {
            return MekanismRecipeType.COMPRESSING;
        }
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_PURIFYING)
    public static class PurificationRecipeManager extends ItemStackGasToItemStackRecipeManager {

        public static final PurificationRecipeManager INSTANCE = new PurificationRecipeManager();

        private PurificationRecipeManager() {
        }

        @Override
        public IRecipeType<ItemStackGasToItemStackRecipe> getRecipeType() {
            return MekanismRecipeType.PURIFYING;
        }
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_INJECTING)
    public static class ChemicalInjectionRecipeManager extends ItemStackGasToItemStackRecipeManager {

        public static final ChemicalInjectionRecipeManager INSTANCE = new ChemicalInjectionRecipeManager();

        private ChemicalInjectionRecipeManager() {
        }

        @Override
        public IRecipeType<ItemStackGasToItemStackRecipe> getRecipeType() {
            return MekanismRecipeType.INJECTING;
        }
    }
}