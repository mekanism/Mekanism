package mekanism.client.recipe_viewer.emi;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.serializer.EmiStackSerializer;
import java.util.Optional;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.resources.ResourceLocation;

@NothingNullByDefault
public class ChemicalEmiIngredientSerializer implements EmiStackSerializer<ChemicalEmiStack> {

    private final Registry<Chemical> registry;
    private final String type;

    ChemicalEmiIngredientSerializer(Registry<Chemical> registry) {
        this.registry = registry;
        this.type = registry.key().location().toString().replace(':', '_');
    }

    @Override
    public EmiStack create(ResourceLocation id, DataComponentPatch ignored, long amount) {
        Optional<Chemical> chemical = registry.getOptional(id).filter(c -> !c.isEmptyType());
        if (chemical.isPresent()) {
            return new ChemicalEmiStack(chemical.get(), amount);
        }
        return EmiStack.EMPTY;
    }

    @Override
    public String getType() {
        return type;
    }

    void addEmiStacks(EmiRegistry emiRegistry) {
        for (Chemical chemical : registry) {
            if (!chemical.isEmptyType()) {//Don't add the empty type. We will allow EMI to filter out any that are hidden from recipe viewers
                emiRegistry.addEmiStack(new ChemicalEmiStack(chemical, 1));
            }
        }
    }
}