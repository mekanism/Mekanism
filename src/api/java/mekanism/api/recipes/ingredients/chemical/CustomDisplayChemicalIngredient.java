package mekanism.api.recipes.ingredients.chemical;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.stream.Stream;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.recipes.ingredients.creator.IChemicalIngredientCreator;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import net.minecraft.core.Holder;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jspecify.annotations.Nullable;

/// ChemicalIngredient that wraps another chemical ingredient to override its [SlotDisplay].
///
/// @see net.neoforged.neoforge.common.crafting.CustomDisplayIngredient CustomDisplayIngredient, its item equivalent
/// @see net.neoforged.neoforge.fluids.crafting.CustomDisplayFluidIngredient CustomDisplayFluidIngredient, its fluid equivalent
/// @since 10.8.0
public final class CustomDisplayChemicalIngredient extends ChemicalIngredient {

    public static final MapCodec<CustomDisplayChemicalIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
          IngredientCreatorAccess.chemical().codec().fieldOf(SerializationConstants.BASE).forGetter(CustomDisplayChemicalIngredient::base),
          SlotDisplay.CODEC.fieldOf(SerializationConstants.DISPLAY).forGetter(CustomDisplayChemicalIngredient::display)
    ).apply(instance, CustomDisplayChemicalIngredient::new));

    private final ChemicalIngredient base;
    private final SlotDisplay display;

    /// @param base    Ingredient the chemical must match
    /// @param display Display to use in place of the `base`'s display
    ///
    /// @apiNote Prefer calling via [IChemicalIngredientCreator#customDisplay(ChemicalIngredient, SlotDisplay)]
    public CustomDisplayChemicalIngredient(ChemicalIngredient base, SlotDisplay display) {
        this.base = Objects.requireNonNull(base, "Base ingredient may not be null");
        this.display = Objects.requireNonNull(display, "Custom display may not be null");
    }

    @Override
    public boolean test(ChemicalResource chemical) {
        return base.test(chemical);
    }

    @Override
    public Stream<Holder<Chemical>> generateChemicals() {
        return base.generateChemicals();
    }

    /// {@return ingredient the chemical must match}
    public ChemicalIngredient base() {
        return base;
    }

    @Override
    public SlotDisplay display() {
        return display;
    }

    @Override
    public void logMissingTags() {
        base().logMissingTags();
    }

    @Override
    public MapCodec<CustomDisplayChemicalIngredient> codec() {
        return CODEC;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) {
            return true;
        }
        return obj instanceof CustomDisplayChemicalIngredient other && Objects.equals(this.base, other.base) && Objects.equals(this.display, other.display);
    }

    @Override
    public int hashCode() {
        return Objects.hash(base, display);
    }

    @Override
    public String toString() {
        return "CustomDisplayChemicalIngredient[base=" + base + ", display=" + display + ']';
    }
}
