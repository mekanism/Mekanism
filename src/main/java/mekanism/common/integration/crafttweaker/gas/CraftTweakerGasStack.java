package mekanism.common.integration.crafttweaker.gas;

import com.blamejared.crafttweaker.api.item.IItemStack;
import mekanism.api.gas.GasStack;
import net.minecraft.item.crafting.Ingredient;

public class CraftTweakerGasStack implements IGasStack {

    private final GasStack stack;

    public CraftTweakerGasStack(GasStack stack) {
        this.stack = stack;
    }

    @Override
    public IGasDefinition getDefinition() {
        return new CraftTweakerGasDefinition(stack.getGas());
    }

    @Override
    public String getName() {
        return stack.getGas().getName();
    }

    @Override
    public String getDisplayName() {
        //TODO
        return stack.getGas().getTranslationKey();
    }

    @Override
    public int getAmount() {
        return stack.amount;
    }

    @Override
    public IGasStack withAmount(int amount) {
        return new CraftTweakerGasStack(new GasStack(stack.getGas(), amount));
    }

    @Override
    public GasStack getInternal() {
        return stack;
    }

    @Override
    public String getCommandString() {
        return stack.amount > 1 ? String.format("<gas:%s> * %s", stack.getGas().getName(), stack.amount) : String.format("<gas:%s>", stack.getGas().getName());
    }

    @Override
    public String toString() {
        return getCommandString();
    }

    @Override
    public boolean matches(IItemStack stack) {
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