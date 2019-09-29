package mekanism.common.recipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.inventory.IgnoredIInventory;
import mekanism.api.recipes.ChemicalInfuserRecipe;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.FluidGasToGasRecipe;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.api.recipes.GasToItemStackRecipe;
import mekanism.api.recipes.ItemStackGasToGasRecipe;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.api.recipes.ItemStackToGasRecipe;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.common.Mekanism;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistry;

//TODO: Should this be moved to API package?
//TODO: Create a way to add/remove recipes via the API
public class MekanismRecipeType<RECIPE_TYPE extends MekanismRecipe> implements IRecipeType<RECIPE_TYPE> {

    private static final List<MekanismRecipeType<? extends MekanismRecipe>> types = new ArrayList<>();

    public static final MekanismRecipeType<ItemStackToItemStackRecipe> CRUSHING = create("crushing");
    public static final MekanismRecipeType<ItemStackToItemStackRecipe> ENRICHING = create("enriching");
    public static final MekanismRecipeType<ItemStackToItemStackRecipe> SMELTING = create("smelting");

    public static final MekanismRecipeType<ChemicalInfuserRecipe> CHEMICAL_INFUSING = create("chemical_infusing");

    public static final MekanismRecipeType<CombinerRecipe> COMBINING = create("combining");

    public static final MekanismRecipeType<ElectrolysisRecipe> SEPARATING = create("separating");

    public static final MekanismRecipeType<FluidGasToGasRecipe> WASHING = create("washing");

    public static final MekanismRecipeType<FluidToFluidRecipe> EVAPORATING = create("evaporating");

    public static final MekanismRecipeType<GasToGasRecipe> ACTIVATING = create("activating");

    public static final MekanismRecipeType<GasToItemStackRecipe> CRYSTALLIZING = create("crystallizing");

    public static final MekanismRecipeType<ItemStackGasToGasRecipe> DISSOLUTION = create("dissolution");

    public static final MekanismRecipeType<ItemStackGasToItemStackRecipe> COMPRESSING = create("compressing");
    public static final MekanismRecipeType<ItemStackGasToItemStackRecipe> PURIFYING = create("purifying");
    public static final MekanismRecipeType<ItemStackGasToItemStackRecipe> INJECTING = create("injecting");

    public static final MekanismRecipeType<ItemStackToGasRecipe> OXIDIZING = create("oxidizing");

    public static final MekanismRecipeType<MetallurgicInfuserRecipe> METALLURGIC_INFUSING = create("metallurgic_infusing");

    public static final MekanismRecipeType<PressurizedReactionRecipe> REACTION = create("reaction");

    public static final MekanismRecipeType<SawmillRecipe> SAWING = create("sawing");

    private static <RECIPE_TYPE extends MekanismRecipe> MekanismRecipeType<RECIPE_TYPE> create(String name) {
        MekanismRecipeType<RECIPE_TYPE> type = new MekanismRecipeType<>(name);
        types.add(type);
        return type;
    }

    //TODO: Convert this to using the proper forge registry once we stop needing to directly use the vanilla registry as a work around
    public static void registerRecipeTypes(IForgeRegistry<IRecipeSerializer<?>> registry) {
        types.forEach(type -> Registry.register(Registry.RECIPE_TYPE, type.registryName, type));
        //TODO: Should the list be cleared afterwards as it isn't really needed anymore after registration
    }

    private final ResourceLocation registryName;
    private final String name;

    private MekanismRecipeType(String name) {
        this.name = name;
        this.registryName = new ResourceLocation(Mekanism.MODID, name);
    }

    @Override
    public String toString() {
        return name;
    }

    @Nonnull
    public List<RECIPE_TYPE> getRecipes(@Nullable World world) {
        if (world == null) {
            return Collections.emptyList();
        }
        //TODO: Cache this stuff by dimension. Update it when /reload is run or things
        return world.getRecipeManager().getRecipes(this, IgnoredIInventory.INSTANCE, world);
    }

    public Stream<RECIPE_TYPE> stream(@Nullable World world) {
        return getRecipes(world).stream();
    }

    @Nullable
    public RECIPE_TYPE findFirst(@Nullable World world, Predicate<RECIPE_TYPE> matchCriteria) {
        return stream(world).filter(matchCriteria).findFirst().orElse(null);
    }

    public boolean contains(@Nullable World world, Predicate<RECIPE_TYPE> matchCriteria) {
        return stream(world).anyMatch(matchCriteria);
    }
}