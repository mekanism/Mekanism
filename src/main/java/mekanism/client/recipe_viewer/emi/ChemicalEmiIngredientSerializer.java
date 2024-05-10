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
public class ChemicalEmiIngredientSerializer<CHEMICAL extends Chemical<CHEMICAL>, EMI_STACK extends ChemicalEmiStack<CHEMICAL>> implements EmiStackSerializer<EMI_STACK> {

    private final EmiStackCreator<CHEMICAL, EMI_STACK> stackCreator;
    private final Registry<CHEMICAL> registry;
    private final String type;

    ChemicalEmiIngredientSerializer(Registry<CHEMICAL> registry, EmiStackCreator<CHEMICAL, EMI_STACK> stackCreator) {
        this.registry = registry;
        this.stackCreator = stackCreator;
        this.type = registry.key().location().toString().replace(':', '_');
    }

    @Override
    public EmiStack create(ResourceLocation id, DataComponentPatch ignored, long amount) {
        Optional<CHEMICAL> chemical = registry.getOptional(id).filter(c -> !c.isEmptyType());
        if (chemical.isPresent()) {
            return stackCreator.create(chemical.get(), amount);
        }
        return EmiStack.EMPTY;
    }

    @Override
    public String getType() {
        return type;
    }

    void addEmiStacks(EmiRegistry emiRegistry) {
        for (CHEMICAL chemical : registry) {
            if (!chemical.isHidden()) {
                emiRegistry.addEmiStack(stackCreator.create(chemical, 1));
            }
        }
    }

    @FunctionalInterface
    public interface EmiStackCreator<CHEMICAL extends Chemical<CHEMICAL>, EMI_STACK extends ChemicalEmiStack<CHEMICAL>> {

        EMI_STACK create(CHEMICAL chemical, long amount);
    }
}