package mekanism.common.recipe.upgrade;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.IShapedRecipe;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MekanismShapedRecipe implements ICraftingRecipe, IShapedRecipe<CraftingInventory> {

    private final ShapedRecipe internal;

    public MekanismShapedRecipe(ShapedRecipe internal) {
        this.internal = internal;
    }

    public ShapedRecipe getInternal() {
        return internal;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return MekanismRecipeSerializers.MEK_DATA.getRecipeSerializer();
    }

    @Override
    public boolean matches(CraftingInventory inv, World world) {
        //Note: We do not override the matches method if it matches ignoring NBT,
        // to ensure that we return the proper value for if there is a match that gives a proper output
        return internal.matches(inv, world) && !getCraftingResult(inv).isEmpty();
    }

    @Override
    public ItemStack getCraftingResult(CraftingInventory inv) {
        if (getRecipeOutput().isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack toReturn = getRecipeOutput().copy();
        List<ItemStack> nbtInputs = new ArrayList<>();
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
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
                        //If something went wrong and we didn't actually get any data don't add it
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

    @Override
    public boolean canFit(int width, int height) {
        return internal.canFit(width, height);
    }

    @Override
    public ItemStack getRecipeOutput() {
        return internal.getRecipeOutput();
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv) {
        return internal.getRemainingItems(inv);
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return internal.getIngredients();
    }

    @Override
    public boolean isDynamic() {
        return internal.isDynamic();
    }

    @Override
    public String getGroup() {
        return internal.getGroup();
    }

    @Override
    public ItemStack getIcon() {
        return internal.getIcon();
    }

    @Override
    public ResourceLocation getId() {
        return internal.getId();
    }

    @Override
    public int getRecipeWidth() {
        return internal.getRecipeWidth();
    }

    @Override
    public int getRecipeHeight() {
        return internal.getRecipeHeight();
    }
}