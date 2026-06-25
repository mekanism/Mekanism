package mekanism.api.recipes.ingredients.chemical;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.util.Objects;
import java.util.stream.Stream;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalIds;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.recipes.ingredients.chemical.display.ChemicalTagSlotDisplay;
import mekanism.api.recipes.ingredients.creator.IChemicalIngredientCreator;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jspecify.annotations.Nullable;

/// Chemical ingredient that matches the chemicals specified by the given [HolderSet]. Most commonly, this will either be a list of chemicals or a chemical tag.
///
/// Unlike with ingredients, this is technically an explicit "type" of chemical ingredient, though in JSON, it is still written **without** a type field, see
/// [IChemicalIngredientCreator#codec()]
///
/// @since 10.8.0
public non-sealed class SimpleChemicalIngredient extends ChemicalIngredient {

    public static final MapCodec<SimpleChemicalIngredient> CODEC = new MapCodec<>() {
        @Override
        public <T> Stream<T> keys(DynamicOps<T> dynamicOps) {
            return Stream.empty();
        }

        @Override
        public <T> DataResult<SimpleChemicalIngredient> decode(DynamicOps<T> ops, MapLike<T> mapLike) {
            return DataResult.error(() -> "Simple chemical ingredients cannot be decoded using map syntax!");
        }

        @Override
        public <T> RecordBuilder<T> encode(SimpleChemicalIngredient ingredient, DynamicOps<T> ops, RecordBuilder<T> builder) {
            return builder.withErrorsFrom(DataResult.error(() -> "Simple chemical ingredients cannot be encoded using map syntax! Please use vanilla syntax (namespaced:chemical or #tag) instead!"));
        }
    };

    private final HolderSet<Chemical> values;

    /// @param values HolderSet to create a chemical ingredient from
    ///
    /// @apiNote Prefer calling via [IChemicalIngredientCreator#of(net.minecraft.core.HolderSet)]
    public SimpleChemicalIngredient(HolderSet<Chemical> values) {
        Objects.requireNonNull(values, "Chemical holder set cannot be null");
        if (values.isImmediatelyResolvable()) {
            values.unwrap().ifRight(list -> {
                if (list.isEmpty()) {
                    throw new UnsupportedOperationException("Chemical ingredients can't be empty!");
                } else if (list.stream().anyMatch(chemical -> chemical.is(ChemicalIds.EMPTY))) {
                    throw new UnsupportedOperationException("Chemical ingredients can't contain the empty chemical");
                }
            });
        }
        this.values = values;
    }

    @Override
    public final boolean test(ChemicalResource chemical) {
        return chemicalSet().contains(chemical.typeHolder());
    }

    @Override
    public final Stream<Holder<Chemical>> generateChemicals() {
        return chemicalSet().stream();
    }

    @Override
    public SlotDisplay display() {
        return chemicalSet().unwrapKey()
              .<SlotDisplay>map(ChemicalTagSlotDisplay::new)
              .orElseGet(super::display);
    }

    /// {@return holder set for the chemical to match}
    public HolderSet<Chemical> chemicalSet() {
        return values;
    }

    @Override
    public void logMissingTags() {
        HolderSet<Chemical> chemicalSet = chemicalSet();
        if (!chemicalSet.isBound()) {
            MekanismAPI.logger.error("Unbound chemical set: {}", chemicalSet);
        } else if (chemicalSet.size() == 0) {
            MekanismAPI.logger.error("Empty holder set: {}", chemicalSet);
        }
    }

    @Override
    public MapCodec<SimpleChemicalIngredient> codec() {
        return CODEC;
    }

    @Override
    public int hashCode() {
        return chemicalSet().hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        return obj instanceof SimpleChemicalIngredient other && other.chemicalSet().equals(chemicalSet());
    }
}
