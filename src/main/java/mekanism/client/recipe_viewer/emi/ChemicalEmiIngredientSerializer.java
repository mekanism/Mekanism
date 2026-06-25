package mekanism.client.recipe_viewer.emi;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.serializer.EmiStackSerializer;
import java.util.Optional;
import mekanism.api.MekanismAPI;
import mekanism.api.MekanismRegistries;
import mekanism.api.chemical.Chemical;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.common.Mekanism;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.resources.Identifier;

public class ChemicalEmiIngredientSerializer implements EmiStackSerializer<ChemicalEmiStack> {

    @Override
    public EmiStack create(Identifier id, DataComponentPatch ignored, long amount) {
        Optional<Registry<Chemical>> optionalRegistry = RecipeViewerUtils.getRegistry(MekanismRegistries.Keys.CHEMICAL);
        if (optionalRegistry.isEmpty()) {
            //Something went horribly wrong, bail
            Mekanism.logger.warn("Failed to find chemical registry while deserializing EMI chemical ingredient");
        } else {
            Optional<Holder.Reference<Chemical>> chemical = optionalRegistry.get().get(id).filter(c -> !c.is(MekanismAPI.EMPTY_CHEMICAL_KEY));
            if (chemical.isPresent()) {
                return new ChemicalEmiStack(chemical.get(), amount);
            }
        }
        return EmiStack.EMPTY;
    }

    @Override
    public String getType() {
        return "mekanism_chemical";
    }

    void addEmiStacks(EmiRegistry emiRegistry) {
        Optional<Registry<Chemical>> optionalRegistry = RecipeViewerUtils.getRegistry(MekanismRegistries.Keys.CHEMICAL);
        if (optionalRegistry.isEmpty()) {
            //Something went horribly wrong, bail
            Mekanism.logger.warn("Failed to find chemical registry while registering EMI ingredients");
        } else {
            optionalRegistry.get().listElements().forEach(chemical -> {
                //Don't add the empty type. We will allow EMI to filter out any that are hidden from recipe viewers
                if (!chemical.is(MekanismAPI.EMPTY_CHEMICAL_KEY)) {
                    emiRegistry.addEmiStack(new ChemicalEmiStack(chemical, 1));
                }
            });
        }
    }
}