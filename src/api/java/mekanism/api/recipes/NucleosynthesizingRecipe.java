package mekanism.api.recipes;

import java.util.List;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ingredients.GasStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Extension of {@link ItemStackGasToItemStackRecipe} with a defined amount of ticks needed to process. Input: ItemStack
 * <br>
 * Input: Gas (Base value, will be multiplied by a per tick amount)
 * <br>
 * Output: ItemStack
 *
 * @apiNote Nucleosynthesizers can process this recipe type.
 */
@NothingNullByDefault
public abstract class NucleosynthesizingRecipe extends ItemStackGasToItemStackRecipe {

    private static final Holder<Item> ANTIPROTONIC_NUCLEOSYNTHESIZER = DeferredHolder.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "antiprotonic_nucleosynthesizer"));

    public NucleosynthesizingRecipe() {
        super(MekanismRecipeTypes.TYPE_NUCLEOSYNTHESIZING.value());
    }

    /**
     * Gets the duration in ticks this recipe takes to complete.
     */
    public abstract int getDuration();

    @Override
    public abstract ItemStackIngredient getItemInput();

    @Override
    public abstract GasStackIngredient getChemicalInput();

    @Override
    @Contract(value = "_, _ -> new", pure = true)
    public abstract ItemStack getOutput(ItemStack inputItem, GasStack inputChemical);

    @NotNull
    @Override
    public abstract ItemStack getResultItem(@NotNull HolderLookup.Provider provider);

    @Override
    public abstract boolean test(ItemStack itemStack, GasStack gasStack);

    @Override
    public abstract List<@NotNull ItemStack> getOutputDefinition();

    @Override
    public String getGroup() {
        return "antiprotonic_nucleosynthesizer";
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(ANTIPROTONIC_NUCLEOSYNTHESIZER);
    }
}
