package mekanism.api.recipes.ingredients.creator;

import java.util.Map;
import java.util.Optional;
import mekanism.api.IMekanismAccess;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidStackTemplate;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

/**
 * Provides access to helpers for creating various types of ingredients.
 */
public class IngredientCreatorAccess {

    private IngredientCreatorAccess() {
    }

    /**
     * Gets the item stack ingredient creator.
     */
    public static IItemStackIngredientCreator item() {
        return IMekanismAccess.INSTANCE.itemStackIngredientCreator();
    }

    /**
     * Gets the fluid stack ingredient creator.
     */
    public static IFluidStackIngredientCreator fluid() {
        return IMekanismAccess.INSTANCE.fluidStackIngredientCreator();
    }

    /**
     * Gets the chemical stack ingredient creator.
     *
     * @see #chemical()
     * @since 10.7.0
     */
    public static IChemicalStackIngredientCreator chemicalStack() {
        return IMekanismAccess.INSTANCE.chemicalStackIngredientCreator();
    }

    /**
     * Gets the chemical ingredient creator.
     *
     * @see #chemicalStack()
     * @since 10.7.0
     */
    public static IChemicalIngredientCreator chemical() {
        return IMekanismAccess.INSTANCE.chemicalIngredientCreator();
    }

    /**
     * Private helper to create a predicate out of a component patch.
     *
     * @since 10.6.0
     */
    @Nullable
    static DataComponentExactPredicate getComponentPatchPredicate(DataComponentPatch patch) {
        if (!patch.isEmpty()) {
            DataComponentExactPredicate.Builder builder = DataComponentExactPredicate.builder();
            for (Map.Entry<DataComponentType<?>, Optional<?>> entry : patch.entrySet()) {
                Optional<?> value = entry.getValue();
                //Note: We only add if the value is added, we don't check ones that have been removed from default, as that isn't easily feasible
                //noinspection OptionalIsPresent - Capturing lambda
                if (value.isPresent()) {
                    //noinspection rawtypes,unchecked
                    builder.expect((DataComponentType) entry.getKey(), value.get());
                }
            }
            return builder.build();
        }
        return null;
    }

    //TODO - 26.1: Docs, and maybe move the impl out of the API
    public static ItemStack createItemStack(TypedInstance<Item> instance) {
        return switch (instance) {
            case ItemStack stackIn -> stackIn;
            case ItemStackTemplate template -> template.create();
            case ItemResource resource -> resource.toStack();
            //TODO: Is there a decent way to grab any potential components patch?
            default -> new ItemStack(instance.typeHolder());
        };
    }

    //TODO - 26.1: Docs, and maybe move the impl out of the API
    public static FluidStack createFluidStack(TypedInstance<Fluid> instance) {
        return switch (instance) {
            case FluidStack stackIn -> stackIn;
            case FluidStackTemplate template -> template.create();
            case FluidResource resource -> resource.toStack(FluidType.BUCKET_VOLUME);
            //TODO: Is there a decent way to grab any potential components patch?
            default -> new FluidStack(instance.typeHolder(), FluidType.BUCKET_VOLUME);
        };
    }
}