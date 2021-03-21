package mekanism.api.recipes.chemical;

import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.inputs.chemical.IChemicalStackIngredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.Contract;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ChemicalChemicalToChemicalRecipe<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      INGREDIENT extends IChemicalStackIngredient<CHEMICAL, STACK>> extends MekanismRecipe implements BiPredicate<@NonNull STACK, @NonNull STACK> {

    private final INGREDIENT leftInput;
    private final INGREDIENT rightInput;
    protected final STACK output;

    public ChemicalChemicalToChemicalRecipe(ResourceLocation id, INGREDIENT leftInput, INGREDIENT rightInput, STACK output) {
        super(id);
        this.leftInput = leftInput;
        this.rightInput = rightInput;
        this.output = output;
    }

    @Override
    public boolean test(STACK input1, STACK input2) {
        return (leftInput.test(input1) && rightInput.test(input2)) || (rightInput.test(input1) && leftInput.test(input2));
    }

    @Contract(value = "_, _ -> new", pure = true)
    public abstract STACK getOutput(STACK input1, STACK input2);

    public INGREDIENT getLeftInput() {
        return leftInput;
    }

    public INGREDIENT getRightInput() {
        return rightInput;
    }

    public List<STACK> getOutputDefinition() {
        return Collections.singletonList(output);
    }

    @Override
    public void write(PacketBuffer buffer) {
        leftInput.write(buffer);
        rightInput.write(buffer);
        output.writeToPacket(buffer);
    }
}