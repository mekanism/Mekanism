package mekanism.common.integration.crafttweaker.gas;

import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemCondition;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.item.IItemTransformer;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.player.IPlayer;
import mekanism.api.gas.GasStack;

import java.util.List;

public class CraftTweakerGasStack implements IGasStack
{
    private final GasStack stack;

    public CraftTweakerGasStack(GasStack stack)
    {
        this.stack = stack;
    }


    @Override
    public IGasDefinition getDefinition()
    {
        return new CraftTweakerGasDefinition(stack.getGas());
    }

    @Override
    public String getName()
    {
        return stack.getGas().getName();
    }

    @Override
    public String getDisplayName()
    {
        return stack.getGas().getLocalizedName();
    }

    @Override
    public String getMark()
    {
        return null;
    }

    @Override
    public int getAmount()
    {
        return stack.amount;
    }

    @Override
    public List<IItemStack> getItems()
    {
        return null;
    }

    @Override
    public IItemStack[] getItemArray()
    {
        return new IItemStack[0];
    }

    @Override
    public List<ILiquidStack> getLiquids()
    {
        return null;
    }

    @Override
    public IIngredient amount(int amount)
    {
        return withAmount(amount);
    }

    @Override
    public IIngredient or(IIngredient iIngredient)
    {
        return null;
    }

    @Override
    public IIngredient transform(IItemTransformer iItemTransformer)
    {
        return null;
    }

    @Override
    public IIngredient only(IItemCondition iItemCondition)
    {
        return null;
    }

    @Override
    public IIngredient marked(String s)
    {
        return null;
    }

    @Override
    public boolean matches(IItemStack iItemStack)
    {
        return false;
    }

    @Override
    public boolean matchesExact(IItemStack iItemStack)
    {
        return false;
    }

    @Override
    public boolean matches(ILiquidStack iLiquidStack)
    {
        return false;
    }

    @Override
    public boolean contains(IIngredient iIngredient)
    {
        return false;
    }

    @Override
    public IItemStack applyTransform(IItemStack iItemStack, IPlayer iPlayer)
    {
        return null;
    }

    @Override
    public boolean hasTransformers()
    {
        return false;
    }

    @Override
    public IGasStack withAmount(int amount)
    {
        return new CraftTweakerGasStack(new GasStack(stack.getGas(), amount));
    }

    @Override
    public Object getInternal()
    {
        return null;
    }

    @Override
    public String toString()
    {
        return String.format("<gas:%sx%dmb>", stack.getGas().getName(), stack.amount);
    }
}
