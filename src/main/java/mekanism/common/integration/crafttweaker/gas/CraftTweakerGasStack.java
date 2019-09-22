package mekanism.common.integration.crafttweaker.gas;

import com.blamejared.crafttweaker.api.item.IItemStack;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasItem;
import mekanism.common.integration.crafttweaker.helpers.IngredientHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

//TODO: Port any other changes to CrT gas stack from recipe rewrite branch
public class CraftTweakerGasStack implements IGasStack {

    @Nonnull
    private final GasStack stack;

    public CraftTweakerGasStack(@Nonnull GasStack stack) {
        this.stack = stack;
    }

    @Override
    public IGasDefinition getDefinition() {
        return new CraftTweakerGasDefinition(stack.getType());
    }

    @Override
    public String getName() {
        return stack.getType().getName();
    }

    @Override
    public String getDisplayName() {
        //TODO
        return stack.getType().getTranslationKey();
    }

    @Override
    public int getAmount() {
        return stack.getAmount();
    }

    @Override
    public IGasStack withAmount(int amount) {
        return new CraftTweakerGasStack(new GasStack(stack, amount));
    }

    @Nonnull
    @Override
    public GasStack getInternal() {
        return stack;
    }

    @Override
    public List<IGasStack> getGases() {
        return Collections.singletonList(this);
    }

    @Override
    public boolean matches(IGasStack gasStack) {
        return gasStack != null && getDefinition().equals(gasStack.getDefinition()) && getAmount() <= gasStack.getAmount();
    }

    @Override
    public String toString() {
        //TODO: MCLiquidStack does not include a multiplication value. Should we mirror that
        return stack.getAmount() > 1 ? String.format("<gas:%s> * %s", stack.getType().getName(), stack.getAmount()) : String.format("<gas:%s>", stack.getType().getName());
    }

    @Override
    public String getCommandString() {
        return toString();
    }

    @Override
    public boolean matches(IItemStack stack) {
        ItemStack itemStack = IngredientHelper.getItemStack(stack);
        //TODO: Should this use GasConversionHandler
        if (itemStack.getItem() instanceof IGasItem) {
            IGasItem item = (IGasItem) itemStack.getItem();
            GasStack gasStack = item.getGas(itemStack);
            return !gasStack.isEmpty() && matches(new CraftTweakerGasStack(gasStack));
        }
        return false;
    }

    @Override
    public Ingredient asVanillaIngredient() {
        //TODO: Once Gas' are proper Ingredients implement this
        return null;
    }

    @Override
    public IItemStack[] getItems() {
        return new IItemStack[0];
    }
}