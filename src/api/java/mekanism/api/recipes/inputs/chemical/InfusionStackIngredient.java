package mekanism.api.recipes.inputs.chemical;

import com.google.gson.JsonElement;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.providers.IInfuseTypeProvider;
import mekanism.api.recipes.inputs.chemical.ChemicalStackIngredient.MultiIngredient;
import mekanism.api.recipes.inputs.chemical.ChemicalStackIngredient.SingleIngredient;
import mekanism.api.recipes.inputs.chemical.ChemicalStackIngredient.TaggedIngredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.ITag;

public interface InfusionStackIngredient extends IChemicalStackIngredient<InfuseType, InfusionStack> {

    static InfusionStackIngredient from(@Nonnull InfusionStack instance) {
        return from(instance.getType(), instance.getAmount());
    }

    static InfusionStackIngredient from(@Nonnull IInfuseTypeProvider infuseType, long amount) {
        return new Single(infuseType.getStack(amount));
    }

    static InfusionStackIngredient from(@Nonnull ITag<InfuseType> tag, long amount) {
        return new Tagged(tag, amount);
    }

    static InfusionStackIngredient read(PacketBuffer buffer) {
        return ChemicalIngredientDeserializer.INFUSION.read(buffer);
    }

    static InfusionStackIngredient deserialize(@Nullable JsonElement json) {
        return ChemicalIngredientDeserializer.INFUSION.deserialize(json);
    }

    static InfusionStackIngredient createMulti(InfusionStackIngredient... ingredients) {
        return ChemicalIngredientDeserializer.INFUSION.createMulti(ingredients);
    }

    @Override
    default ChemicalIngredientInfo<InfuseType, InfusionStack> getIngredientInfo() {
        return ChemicalIngredientInfo.INFUSION;
    }

    class Single extends SingleIngredient<InfuseType, InfusionStack> implements InfusionStackIngredient {

        protected Single(@Nonnull InfusionStack stack) {
            super(stack);
        }
    }

    class Tagged extends TaggedIngredient<InfuseType, InfusionStack> implements InfusionStackIngredient {

        protected Tagged(@Nonnull ITag<InfuseType> tag, long amount) {
            super(tag, amount);
        }
    }

    class Multi extends MultiIngredient<InfuseType, InfusionStack, InfusionStackIngredient> implements InfusionStackIngredient {

        protected Multi(@Nonnull InfusionStackIngredient... ingredients) {
            super(ingredients);
        }
    }
}