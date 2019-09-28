package mekanism.common.recipe;

import java.util.ArrayList;
import java.util.List;
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
import mekanism.common.recipe.impl.ChemicalCrystallizerIRecipe;
import mekanism.common.recipe.impl.ChemicalInfuserIRecipe;
import mekanism.common.recipe.impl.CombinerIRecipe;
import mekanism.common.recipe.impl.CompressingIRecipe;
import mekanism.common.recipe.impl.CrushingIRecipe;
import mekanism.common.recipe.impl.ElectrolysisIRecipe;
import mekanism.common.recipe.impl.EnrichingIRecipe;
import mekanism.common.recipe.impl.FluidGasToGasIRecipe;
import mekanism.common.recipe.impl.FluidToFluidIRecipe;
import mekanism.common.recipe.impl.GasToGasIRecipe;
import mekanism.common.recipe.impl.InjectingIRecipe;
import mekanism.common.recipe.impl.ItemStackGasToGasIRecipe;
import mekanism.common.recipe.impl.ItemStackToGasIRecipe;
import mekanism.common.recipe.impl.MetallurgicInfuserIRecipe;
import mekanism.common.recipe.impl.PressurizedReactionIRecipe;
import mekanism.common.recipe.impl.PurifyingIRecipe;
import mekanism.common.recipe.impl.SawmillIRecipe;
import mekanism.common.recipe.impl.SmeltingIRecipe;
import mekanism.common.recipe.serializer.GasToItemStackRecipeSerializer;
import mekanism.common.recipe.serializer.ChemicalInfuserRecipeSerializer;
import mekanism.common.recipe.serializer.CombinerRecipeSerializer;
import mekanism.common.recipe.serializer.ElectrolysisRecipeSerializer;
import mekanism.common.recipe.serializer.FluidGasToGasRecipeSerializer;
import mekanism.common.recipe.serializer.FluidToFluidRecipeSerializer;
import mekanism.common.recipe.serializer.GasToGasRecipeSerializer;
import mekanism.common.recipe.serializer.ItemStackGasToGasRecipeSerializer;
import mekanism.common.recipe.serializer.ItemStackGasToItemStackRecipeSerializer;
import mekanism.common.recipe.serializer.ItemStackToGasRecipeSerializer;
import mekanism.common.recipe.serializer.ItemStackToItemStackRecipeSerializer;
import mekanism.common.recipe.serializer.MetallurgicInfuserRecipeSerializer;
import mekanism.common.recipe.serializer.PressurizedReactionRecipeSerializer;
import mekanism.common.recipe.serializer.SawmillRecipeSerializer;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

//TODO: Allow for registry overrides?
//TODO: Make sure the serializers support conditions
//TODO: Should this and the serializer implementations be moved to API package? Probably at least the declarations should be moved
// Ideally we would not have to have a separate class for each one that is basically just a factory for setting the:
// serializer, recipe type, group, and icon.
// Is there some way we could declare those statically for each of the things
public class MekanismRecipeSerializers {

    private static final List<IRecipeSerializer<? extends MekanismRecipe>> types = new ArrayList<>();

    public static final IRecipeSerializer<ItemStackToItemStackRecipe> CRUSHING = create("crushing", new ItemStackToItemStackRecipeSerializer<>(CrushingIRecipe::new));
    public static final IRecipeSerializer<ItemStackToItemStackRecipe> ENRICHING = create("enriching", new ItemStackToItemStackRecipeSerializer<>(EnrichingIRecipe::new));
    public static final IRecipeSerializer<ItemStackToItemStackRecipe> SMELTING = create("smelting", new ItemStackToItemStackRecipeSerializer<>(SmeltingIRecipe::new));

    public static final IRecipeSerializer<ChemicalInfuserRecipe> CHEMICAL_INFUSING = create("chemical_infusing", new ChemicalInfuserRecipeSerializer<>(ChemicalInfuserIRecipe::new));

    public static final IRecipeSerializer<CombinerRecipe> COMBINING = create("combining", new CombinerRecipeSerializer<>(CombinerIRecipe::new));

    public static final IRecipeSerializer<ElectrolysisRecipe> SEPARATING = create("separating", new ElectrolysisRecipeSerializer<>(ElectrolysisIRecipe::new));

    public static final IRecipeSerializer<FluidGasToGasRecipe> WASHING = create("washing", new FluidGasToGasRecipeSerializer<>(FluidGasToGasIRecipe::new));

    public static final IRecipeSerializer<FluidToFluidRecipe> EVAPORATING = create("evaporating", new FluidToFluidRecipeSerializer<>(FluidToFluidIRecipe::new));

    public static final IRecipeSerializer<GasToGasRecipe> SOLAR_NEUTRON_ACTIVATOR = create("solar_neutron_activator", new GasToGasRecipeSerializer<>(GasToGasIRecipe::new));

    public static final IRecipeSerializer<GasToItemStackRecipe> CRYSTALLIZING = create("crystallizing", new GasToItemStackRecipeSerializer<>(ChemicalCrystallizerIRecipe::new));

    public static final IRecipeSerializer<ItemStackGasToGasRecipe> DISSOLUTION = create("dissolution", new ItemStackGasToGasRecipeSerializer<>(ItemStackGasToGasIRecipe::new));

    public static final IRecipeSerializer<ItemStackGasToItemStackRecipe> COMPRESSING = create("compressing", new ItemStackGasToItemStackRecipeSerializer<>(CompressingIRecipe::new));
    public static final IRecipeSerializer<ItemStackGasToItemStackRecipe> PURIFYING = create("purifying", new ItemStackGasToItemStackRecipeSerializer<>(PurifyingIRecipe::new));
    public static final IRecipeSerializer<ItemStackGasToItemStackRecipe> CHEMICAL_INJECTING = create("chemical_injecting", new ItemStackGasToItemStackRecipeSerializer<>(InjectingIRecipe::new));

    public static final IRecipeSerializer<ItemStackToGasRecipe> OXIDIZING = create("oxidizing", new ItemStackToGasRecipeSerializer<>(ItemStackToGasIRecipe::new));

    public static final IRecipeSerializer<MetallurgicInfuserRecipe> METALLURGIC_INFUSING = create("metallurgic_infusing", new MetallurgicInfuserRecipeSerializer<>(MetallurgicInfuserIRecipe::new));

    public static final IRecipeSerializer<PressurizedReactionRecipe> REACTION = create("reaction", new PressurizedReactionRecipeSerializer<>(PressurizedReactionIRecipe::new));

    public static final IRecipeSerializer<SawmillRecipe> SAWING = create("sawing", new SawmillRecipeSerializer<>(SawmillIRecipe::new));

    private static <T extends MekanismRecipe> IRecipeSerializer<T> create(String name, IRecipeSerializer<T> builder) {
        builder.setRegistryName(new ResourceLocation(Mekanism.MODID, name));
        types.add(builder);
        return builder;
    }

    public static void registerRecipeSerializers(IForgeRegistry<IRecipeSerializer<?>> registry) {
        types.forEach(registry::register);
        //TODO: Should the list be cleared afterwards as it isn't really needed anymore after registration
    }
}