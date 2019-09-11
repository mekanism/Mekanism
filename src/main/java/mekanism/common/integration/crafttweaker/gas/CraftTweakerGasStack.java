package mekanism.common.integration.crafttweaker.gas;

import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemCondition;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.item.IItemTransformer;
import crafttweaker.api.item.IItemTransformerNew;
import crafttweaker.api.item.IngredientOr;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.player.IPlayer;
import crafttweaker.mc1120.item.MCItemStack;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasItem;
import mekanism.common.MekanismBlocks;
import mekanism.common.item.ItemBlockGasTank;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;

public class CraftTweakerGasStack implements IGasStack {

    private final IItemTransformerNew transformerNew;
    private final GasStack stack;

    public CraftTweakerGasStack(GasStack stack) {
        this(stack, null);
    }

    public CraftTweakerGasStack(GasStack stack, IItemTransformerNew transformerNew) {
        this.stack = stack;
        this.transformerNew = transformerNew;
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
        return stack.getGas().getLocalizedName();
    }

    @Override
    public String getMark() {
        return null;
    }

    @Override
    public int getAmount() {
        return stack.amount;
    }

    @Override
    public List<IItemStack> getItems() {
        IItemStack stack = getGasTankExample();
        return stack == null ? Collections.emptyList() : Collections.singletonList(stack);
    }

    @Override
    public IItemStack[] getItemArray() {
        IItemStack stack = getGasTankExample();
        return stack == null ? new IItemStack[0] : new IItemStack[]{stack};
    }

    @Nullable
    private IItemStack getGasTankExample() {
        ItemStack gasTank = new ItemStack(MekanismBlocks.GasTank);
        //TODO: Should we also make it be a specific tier
        ((ItemBlockGasTank) gasTank.getItem()).setGas(gasTank, this.stack);
        IItemStack stack = CraftTweakerMC.getIItemStack(gasTank);
        return stack == null ? null : stack.withDisplayName(String.format("Any container with %s * %d", getDisplayName(), this.getAmount()));
    }

    @Override
    public List<ILiquidStack> getLiquids() {
        return Collections.emptyList();
    }

    @Override
    public IIngredient amount(int amount) {
        return withAmount(amount);
    }

    @Override
    public IIngredient or(IIngredient ingredient) {
        return new IngredientOr(this, ingredient);
    }

    @Override
    public IIngredient transformNew(IItemTransformerNew transformer) {
        return new CraftTweakerGasStack(this.stack, transformer);
    }

    @Override
    public IIngredient transform(IItemTransformer transformer) {
        throw new UnsupportedOperationException("Gas stacks can't have transformers");
    }

    @Override
    public IIngredient only(IItemCondition condition) {
        throw new UnsupportedOperationException("Gas stacks can't have conditions");
    }

    @Override
    public IIngredient marked(String s) {
        throw new UnsupportedOperationException("Gas stacks can't be marked");
    }

    @Override
    public boolean matches(IItemStack stack) {
        ItemStack itemStack = CraftTweakerMC.getItemStack(stack);
        if (itemStack.getItem() instanceof IGasItem) {
            IGasItem item = (IGasItem) itemStack.getItem();
            GasStack gasStack = item.getGas(itemStack);
            return gasStack != null && matches(new CraftTweakerGasStack(gasStack));
        }
        return false;
    }

    @Override
    public boolean matchesExact(IItemStack stack) {
        return this.matches(stack);
    }

    @Override
    public boolean matches(ILiquidStack liquid) {
        return false;
    }

    @Override
    public boolean contains(IIngredient ingredient) {
        if (ingredient == null) {
            return false;
        }
        List<IGasStack> gases = IngredientExpansion.getGases(ingredient);
        return gases.stream().allMatch(gasStack -> IngredientExpansion.matches(ingredient, gasStack)) && !gases.isEmpty();
    }

    @Override
    public IItemStack applyTransform(IItemStack item, IPlayer player) {
        return item;
    }

    @Override
    public IItemStack applyNewTransform(IItemStack item) {
        if (transformerNew != null) {
            return transformerNew.transform(item);
        }
        ItemStack itemStack = CraftTweakerMC.getItemStack(item);
        if (itemStack.getItem() instanceof IGasItem) {
            IGasItem gasItem = (IGasItem) itemStack.getItem();
            if (gasItem.canProvideGas(itemStack, stack.getGas())) {
                //TODO: Do we need to check to ensure it has enough? Or will that be caught by the matches check
                gasItem.removeGas(itemStack, stack.amount);
            }
            return MCItemStack.createNonCopy(itemStack);
        }
        return CraftTweakerMC.getIItemStack(ForgeHooks.getContainerItem(itemStack));
    }

    @Override
    public boolean hasNewTransformers() {
        //Always return true so that the draining can be done
        return true;
    }

    @Override
    public boolean hasTransformers() {
        return false;
    }

    @Override
    public IGasStack withAmount(int amount) {
        return new CraftTweakerGasStack(new GasStack(stack.getGas(), amount));
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
    public Object getInternal() {
        return stack;
    }

    @Override
    public String toString() {
        //TODO: MCLiquidStack does not include a multiplication value. Should we mirror that
        return stack.amount > 1 ? String.format("<gas:%s> * %s", stack.getGas().getName(), stack.amount) : String.format("<gas:%s>", stack.getGas().getName());
    }

    @Override
    public String toCommandString() {
        return toString();
    }
}