package mekanism.common.recipe.upgrade;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import mekanism.common.lib.transaction.TransactionHelper;
import mekanism.common.recipe.WrappedShapedRecipe;
import mekanism.common.registries.MekanismRecipeSerializersInternal;
import mekanism.common.util.ItemAccessUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class MekanismShapedRecipe extends WrappedShapedRecipe {

    public MekanismShapedRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo, ShapedRecipePattern pattern, ItemStackTemplate result) {
        super(commonInfo, bookInfo, pattern, result);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public RecipeSerializer<ShapedRecipe> getSerializer() {
        return (RecipeSerializer) MekanismRecipeSerializersInternal.MEK_DATA.get();
    }

    @Override
    public ItemStack assemble(CraftingInput inv) {
        ItemStack toReturn = super.assemble(inv);
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
        ItemAccess toReturnAccess = ItemAccess.forStack(toReturn);
        Set<RecipeUpgradeType> supportedTypes = RecipeUpgradeData.getSupportedTypes(toReturnAccess);
        if (supportedTypes.isEmpty()) {
            //If we have no supported types "fail" gracefully by just not transferring any data
            return toReturn;
        }
        //Protect against any mods that might be doing transactional logic, such as if an auto crafter validates it has enough energy before calling this method
        try (Transaction transaction = TransactionHelper.openTransactionSafe()) {
            Map<RecipeUpgradeType, List<RecipeUpgradeData<?>>> upgradeInfo = new EnumMap<>(RecipeUpgradeType.class);
            //Only bother checking input items that have NBT as ones that do not, don't have any data they may need to transfer
            for (ItemStack stack : componentInputs) {
                ItemAccess itemAccess = ItemAccessUtils.sideEffectFreeAccess(stack);
                Set<RecipeUpgradeType> stackSupportedTypes = RecipeUpgradeData.getSupportedTypes(itemAccess);
                for (RecipeUpgradeType supportedType : stackSupportedTypes) {
                    if (supportedTypes.contains(supportedType)) {
                        RecipeUpgradeData<?> data = RecipeUpgradeData.getUpgradeData(supportedType, itemAccess, transaction);
                        if (data != null) {
                            //If something went wrong, and we didn't actually get any data don't add it
                            upgradeInfo.computeIfAbsent(supportedType, _ -> new ArrayList<>()).add(data);
                        }
                    }
                }
            }
            for (Entry<RecipeUpgradeType, List<RecipeUpgradeData<?>>> entry : upgradeInfo.entrySet()) {
                List<RecipeUpgradeData<?>> upgradeData = entry.getValue();
                if (!upgradeData.isEmpty()) {
                    //Skip any empty data, even though we should never have any
                    RecipeUpgradeData<?> data = RecipeUpgradeData.mergeUpgradeData(upgradeData);
                    if (data == null || !data.applyToStack(toReturnAccess, transaction)) {
                        //Fail, incompatible data
                        return ItemStack.EMPTY;
                    }
                }
            }
            transaction.commit();
            return toReturn;
        }
    }
}