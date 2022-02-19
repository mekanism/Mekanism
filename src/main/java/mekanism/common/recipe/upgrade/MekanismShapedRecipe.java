package mekanism.common.recipe.upgrade;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.common.recipe.WrappedShapedRecipe;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.ShapedRecipe;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MekanismShapedRecipe extends WrappedShapedRecipe {

    public MekanismShapedRecipe(ShapedRecipe internal) {
        super(internal);
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return MekanismRecipeSerializers.MEK_DATA.getRecipeSerializer();
    }

    @Override
    public ItemStack assemble(CraftingInventory inv) {
        if (getResultItem().isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack toReturn = getResultItem().copy();
        List<ItemStack> nbtInputs = new ArrayList<>();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && stack.hasTag()) {
                nbtInputs.add(stack);
            }
        }
        if (nbtInputs.isEmpty()) {
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
        for (ItemStack stack : nbtInputs) {
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
                if (data == null || !data.applyToStack(toReturn)) {
                    //Fail, incompatible data
                    return ItemStack.EMPTY;
                }
            }
        }
        return toReturn;
    }
}