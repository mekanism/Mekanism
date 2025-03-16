package mekanism.client.recipe_viewer.emi;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.serializer.EmiStackSerializer;
import java.util.Optional;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.resources.ResourceLocation;

@NothingNullByDefault
public class ChemicalEmiIngredientSerializer implements EmiStackSerializer<ChemicalEmiStack> {

    @Override
    public EmiStack create(ResourceLocation id, DataComponentPatch ignored, long amount) {
        Optional<Holder.Reference<Chemical>> chemical = MekanismAPI.CHEMICAL_REGISTRY.getHolder(id).filter(c -> !c.is(MekanismAPI.EMPTY_CHEMICAL_KEY));
        if (chemical.isPresent()) {
            return new ChemicalEmiStack(chemical.get(), amount);
        }
        return EmiStack.EMPTY;
    }

    @Override
    public String getType() {
        return "mekanism_chemical";
    }

    void addEmiStacks(EmiRegistry emiRegistry) {
        MekanismAPI.CHEMICAL_REGISTRY.holders().forEach(chemical -> {
            //Don't add the empty type. We will allow EMI to filter out any that are hidden from recipe viewers
            if (!chemical.is(MekanismAPI.EMPTY_CHEMICAL_KEY)) {
                emiRegistry.addEmiStack(new ChemicalEmiStack(chemical, 1));
            }
        });
    }
}