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

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@FieldsAreNonnullByDefault
public abstract class SawmillRecipe extends MekanismRecipe implements Predicate<@NonNull ItemStack> {

    protected static final Random RANDOM = new Random();

    private final ItemStackIngredient input;
    private final ItemStack mainOutputDefinition;
    private final ItemStack secondaryOutputDefinition;
    private final double secondaryChance;

    public SawmillRecipe(ResourceLocation id, ItemStackIngredient input, ItemStack mainOutputDefinition, ItemStack secondaryOutputDefinition, double secondaryChance) {
        super(id);
        this.input = input;
        this.mainOutputDefinition = mainOutputDefinition;
        this.secondaryOutputDefinition = secondaryOutputDefinition;
        this.secondaryChance = secondaryChance;
    }

    @Override
    public boolean test(@NonNull ItemStack stack) {
        return this.input.test(stack);
    }

    public ChanceOutput getOutput(ItemStack input) {
        return new ChanceOutput(RANDOM.nextDouble());
    }

    public List<ItemStack> getMainOutputDefinition() {
        return mainOutputDefinition.isEmpty() ? Collections.emptyList() : Collections.singletonList(mainOutputDefinition);
    }

    public List<ItemStack> getSecondaryOutputDefinition() {
        return secondaryOutputDefinition.isEmpty() ? Collections.emptyList() :  Collections.singletonList(secondaryOutputDefinition);
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
        buffer.writeItemStack(mainOutputDefinition);
        buffer.writeItemStack(secondaryOutputDefinition);
        buffer.writeDouble(secondaryChance);
    }

    //TODO: nextChanceOutput() method so that we can have a more accurate calculation for OutputHelper
    public class ChanceOutput {

        protected final double rand;

        public ChanceOutput(double rand) {
            this.rand = rand;
        }

        public ItemStack getMainOutput() {
            return mainOutputDefinition.copy();
        }

        /**
         * Used for checking the maximum amount we can get as a secondary for purposes of seeing if we have space to process
         */
        public ItemStack getMaxSecondaryOutput() {
            return secondaryChance > 0 ? secondaryOutputDefinition.copy() : ItemStack.EMPTY;
        }

        public ItemStack getSecondaryOutput() {
            if (rand <= secondaryChance) {
                return secondaryOutputDefinition.copy();
            }
            return ItemStack.EMPTY;
        }

        //TODO: JavaDoc
        public ItemStack nextSecondaryOutput() {
            if (secondaryChance > 0) {
                double rand = RANDOM.nextDouble();
                if (rand <= secondaryChance) {
                    return secondaryOutputDefinition.copy();
                }
            }
            return ItemStack.EMPTY;
        }
    }
}
