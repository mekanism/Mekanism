package mekanism.client.recipe_viewer.emi;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.serializer.EmiStackSerializer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

@NothingNullByDefault
public class ChemicalEmiIngredientSerializer<CHEMICAL extends Chemical<CHEMICAL>, EMI_STACK extends ChemicalEmiStack<CHEMICAL>> implements EmiStackSerializer<EMI_STACK> {

    private final EmiStackCreator<CHEMICAL, EMI_STACK> stackCreator;
    final Registry<CHEMICAL> registry;
    private final String type;

    public ChemicalEmiIngredientSerializer(String type, Registry<CHEMICAL> registry, EmiStackCreator<CHEMICAL, EMI_STACK> stackCreator) {
        this.type = type;
        this.registry = registry;
        this.stackCreator = stackCreator;
    }

    public EmiStack create(CHEMICAL chemical) {
        return stackCreator.create(chemical, 1);
    }

    @Override
    public EmiStack create(ResourceLocation id, CompoundTag nbt, long amount) {
        return registry.getOptional(id)
              .filter(chemical -> !chemical.isEmptyType())
              .<EmiStack>map(chemical -> stackCreator.create(chemical, amount))
              .orElse(EmiStack.EMPTY);
    }

    @Override
    public String getType() {
        return type;
    }

    @FunctionalInterface
    public interface EmiStackCreator<CHEMICAL extends Chemical<CHEMICAL>, EMI_STACK extends ChemicalEmiStack<CHEMICAL>> {

        EMI_STACK create(CHEMICAL chemical, long amount);
    }
}