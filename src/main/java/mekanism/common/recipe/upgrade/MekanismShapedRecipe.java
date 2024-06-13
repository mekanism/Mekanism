package mekanism.common.recipe.upgrade;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.recipe.WrappedShapedRecipe;
import mekanism.common.registries.MekanismRecipeSerializersInternal;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

@NothingNullByDefault
public class MekanismShapedRecipe extends WrappedShapedRecipe {

    public MekanismShapedRecipe(ShapedRecipe internal) {
        super(internal);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return MekanismRecipeSerializersInternal.MEK_DATA.get();
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider provider) {
        ItemStack resultItem = getResultItem(provider);
        if (resultItem.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack toReturn = resultItem.copy();
        List<ItemStack> componentInputs = new ArrayList<>();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && !stack.isComponentsPatchEmpty()) {
                componentInputs.add(stack);
            }
        }
        if (componentInputs.isEmpty()) {
            //If none of our items have NBT we can skip checking what data can be transferred
            return toReturn;
        }
        Set<RecipeUpgradeType> supportedTypes = RecipeUpgradeData.getSupportedTypes(toReturn);
        if (supportedTypes.isEmpty()) {
            //If we have no supported types "fail" gracefully by just not transferring any data
            return toReturn;
        }
        Map<RecipeUpgradeType, List<RecipeUpgradeData<?>>> upgradeInfo = new EnumMap<>(RecipeUpgradeType.class);
        //Only bother checking input items that have NBT as ones that do not, don't have any data they may need to transfer
        for (ItemStack stack : componentInputs) {
            Set<RecipeUpgradeType> stackSupportedTypes = RecipeUpgradeData.getSupportedTypes(stack);
            for (RecipeUpgradeType supportedType : stackSupportedTypes) {
                if (supportedTypes.contains(supportedType)) {
                    RecipeUpgradeData<?> data = RecipeUpgradeData.getUpgradeData(supportedType, stack);
                    if (data != null) {
                        //If something went wrong, and we didn't actually get any data don't add it
                        upgradeInfo.computeIfAbsent(supportedType, type -> new ArrayList<>()).add(data);
                    }
                }
            }
        }
        for (Entry<RecipeUpgradeType, List<RecipeUpgradeData<?>>> entry : upgradeInfo.entrySet()) {
            List<RecipeUpgradeData<?>> upgradeData = entry.getValue();
            if (!upgradeData.isEmpty()) {
                //Skip any empty data, even though we should never have any
                RecipeUpgradeData<?> data = RecipeUpgradeData.mergeUpgradeData(upgradeData);
                if (data == null || !data.applyToStack(provider, toReturn)) {
                    //Fail, incompatible data
                    return ItemStack.EMPTY;
                }
            }
        }
        return toReturn;
    }
}