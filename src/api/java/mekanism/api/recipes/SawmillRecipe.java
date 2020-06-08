package mekanism.api.recipes;


import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.Contract;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class SawmillRecipe extends MekanismRecipe implements Predicate<@NonNull ItemStack> {

    protected static final Random RANDOM = new Random();

    private final ItemStackIngredient input;
    private final ItemStack mainOutput;
    private final ItemStack secondaryOutput;
    private final double secondaryChance;

    public SawmillRecipe(ResourceLocation id, ItemStackIngredient input, ItemStack mainOutput, ItemStack secondaryOutput, double secondaryChance) {
        super(id);
        this.input = input;
        this.mainOutput = mainOutput;
        this.secondaryOutput = secondaryOutput;
        this.secondaryChance = secondaryChance;
    }

    @Override
    public boolean test(ItemStack stack) {
        return this.input.test(stack);
    }

    @Contract(value = "_ -> new")
    public ChanceOutput getOutput(ItemStack input) {
        return new ChanceOutput(RANDOM.nextDouble());
    }

    public List<ItemStack> getMainOutputDefinition() {
        return mainOutput.isEmpty() ? Collections.emptyList() : Collections.singletonList(mainOutput);
    }

    public List<ItemStack> getSecondaryOutputDefinition() {
        return secondaryOutput.isEmpty() ? Collections.emptyList() : Collections.singletonList(secondaryOutput);
    }

    public double getSecondaryChance() {
        return secondaryChance;
    }

    public ItemStackIngredient getInput() {
        return input;
    }

    @Override
    public void write(PacketBuffer buffer) {
        input.write(buffer);
        buffer.writeItemStack(mainOutput);
        buffer.writeItemStack(secondaryOutput);
        buffer.writeDouble(secondaryChance);
    }

    //TODO: nextChanceOutput() method so that we can have a more accurate calculation for OutputHelper
    public class ChanceOutput {

        protected final double rand;

        public ChanceOutput(double rand) {
            this.rand = rand;
        }

        public ItemStack getMainOutput() {
            return mainOutput.copy();
        }

        /**
         * Used for checking the maximum amount we can get as a secondary for purposes of seeing if we have space to process
         */
        public ItemStack getMaxSecondaryOutput() {
            return secondaryChance > 0 ? secondaryOutput.copy() : ItemStack.EMPTY;
        }

        public ItemStack getSecondaryOutput() {
            if (rand <= secondaryChance) {
                return secondaryOutput.copy();
            }
            return ItemStack.EMPTY;
        }

        //TODO: JavaDoc
        public ItemStack nextSecondaryOutput() {
            if (secondaryChance > 0) {
                double rand = RANDOM.nextDouble();
                if (rand <= secondaryChance) {
                    return secondaryOutput.copy();
                }
            }
            return ItemStack.EMPTY;
        }
    }
}