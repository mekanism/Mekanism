package mekanism.api.recipes.outputs;


import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.common.util.FieldsAreNonnullByDefault;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreIngredient;
import org.jetbrains.annotations.Contract;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@FieldsAreNonnullByDefault
public class OreDictSupplier implements Supplier<ItemStack> {

    private final String oredictName;

    private final OreIngredient oreIngredient;

    public OreDictSupplier(String oredictName) {
        this.oredictName = oredictName;
        this.oreIngredient = new OreIngredient(oredictName);
    }

    @Override
    @Contract("-> new")
    public ItemStack get() {
        ItemStack[] stacks = oreIngredient.getMatchingStacks();
        return stacks.length > 0 ? stacks[0].copy() : ItemStack.EMPTY;
    }

    public List<ItemStack> getPossibleOutputs() {
        return Arrays.asList(oreIngredient.getMatchingStacks());
    }

    public String getOredictName() {
        return oredictName;
    }
}
