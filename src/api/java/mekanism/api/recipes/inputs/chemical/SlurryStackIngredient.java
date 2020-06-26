package mekanism.api.recipes.inputs.chemical;

import com.google.gson.JsonElement;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.providers.ISlurryProvider;
import mekanism.api.recipes.inputs.chemical.ChemicalStackIngredient.MultiIngredient;
import mekanism.api.recipes.inputs.chemical.ChemicalStackIngredient.SingleIngredient;
import mekanism.api.recipes.inputs.chemical.ChemicalStackIngredient.TaggedIngredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.ITag;

public interface SlurryStackIngredient extends IChemicalStackIngredient<Slurry, SlurryStack> {

    static SlurryStackIngredient from(@Nonnull SlurryStack instance) {
        return from(instance.getType(), instance.getAmount());
    }

    static SlurryStackIngredient from(@Nonnull ISlurryProvider slurry, long amount) {
        return new Single(slurry.getStack(amount));
    }

    static SlurryStackIngredient from(@Nonnull ITag<Slurry> tag, long amount) {
        return new Tagged(tag, amount);
    }

    static SlurryStackIngredient read(PacketBuffer buffer) {
        return ChemicalIngredientDeserializer.SLURRY.read(buffer);
    }

    static SlurryStackIngredient deserialize(@Nullable JsonElement json) {
        return ChemicalIngredientDeserializer.SLURRY.deserialize(json);
    }

    static SlurryStackIngredient createMulti(SlurryStackIngredient... ingredients) {
        return ChemicalIngredientDeserializer.SLURRY.createMulti(ingredients);
    }

    @Override
    default ChemicalIngredientInfo<Slurry, SlurryStack> getIngredientInfo() {
        return ChemicalIngredientInfo.SLURRY;
    }

    class Single extends SingleIngredient<Slurry, SlurryStack> implements SlurryStackIngredient {

        protected Single(@Nonnull SlurryStack stack) {
            super(stack);
        }
    }

    class Tagged extends TaggedIngredient<Slurry, SlurryStack> implements SlurryStackIngredient {

        protected Tagged(@Nonnull ITag<Slurry> tag, long amount) {
            super(tag, amount);
        }
    }

    class Multi extends MultiIngredient<Slurry, SlurryStack, SlurryStackIngredient> implements SlurryStackIngredient {

        protected Multi(@Nonnull SlurryStackIngredient... ingredients) {
            super(ingredients);
        }
    }
}