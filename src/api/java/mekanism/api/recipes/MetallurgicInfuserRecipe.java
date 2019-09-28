package mekanism.api.recipes;

import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.infuse.InfusionStack;
import mekanism.api.recipes.inputs.InfusionIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

/**
 * Created by Thiakil on 14/07/2019.
 */
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MetallurgicInfuserRecipe extends MekanismRecipe implements BiPredicate<InfusionStack, ItemStack> {

    private final ItemStackIngredient itemInput;
    private final InfusionIngredient infusionInput;
    private final ItemStack outputDefinition;

    public MetallurgicInfuserRecipe(ResourceLocation id, ItemStackIngredient itemInput, InfusionIngredient infusionInput, ItemStack outputDefinition) {
        super(id);
        this.itemInput = itemInput;
        this.infusionInput = infusionInput;
        this.outputDefinition = outputDefinition.copy();
    }

    @Override
    public boolean test(InfusionStack infusionContainer, ItemStack itemStack) {
        return infusionInput.test(infusionContainer) && itemInput.test(itemStack);
    }

    public @NonNull List<@NonNull ItemStack> getOutputDefinition() {
        return outputDefinition.isEmpty() ? Collections.emptyList() : Collections.singletonList(outputDefinition);
    }

    public ItemStack getOutput(InfusionStack inputInfuse, ItemStack inputItem) {
        return this.outputDefinition.copy();
    }

    public InfusionIngredient getInfusionInput() {
        return this.infusionInput;
    }

    public ItemStackIngredient getItemInput() {
        return this.itemInput;
    }

    @Override
    public void write(PacketBuffer buffer) {
        itemInput.write(buffer);
        infusionInput.write(buffer);
        buffer.writeItemStack(outputDefinition);
    }
}
