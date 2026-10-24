package mekanism.common.recipe;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalIds;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.datagen.recipe.RecipeCriterion;
import mekanism.api.datagen.recipe.builder.ItemStackToChemicalRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ItemStackToItemStackRecipeBuilder;
import mekanism.api.datagen.recipe.builder.SawmillRecipeBuilder;
import mekanism.api.recipes.book.CrushingBookCategory;
import mekanism.api.recipes.book.EnrichingBookCategory;
import mekanism.api.recipes.book.SawingBookCategory;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.recipe.builder.ExtendedCookingRecipeBuilder;
import mekanism.common.registries.MekanismItems;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.predicates.MinMaxBounds;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
import net.minecraft.advancements.triggers.InventoryChangeTrigger.TriggerInstance;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.references.BlockItemId;
import net.minecraft.references.BlockItemIds;
import net.minecraft.references.ItemIds;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.fluids.FluidStackTemplate;
import org.jspecify.annotations.Nullable;

/// Class for helpers that are also used by some of our recipe compat providers for convenience
public class RecipeProviderUtil {

    private RecipeProviderUtil() {
    }

    public static void addSmeltingBlastingRecipes(RecipeOutput consumer, Ingredient smeltingInput, Holder<Item> output, float experience, int smeltingTime,
          @Nullable Identifier group, Identifier baseLocation, RecipeCriterion... criteria) {
        ExtendedCookingRecipeBuilder.blasting(CookingBookCategory.MISC, output, smeltingInput, smeltingTime / 2)
              .experience(experience)
              .category(RecipeCategory.MISC)
              .group(group)
              .unlockedBy(Arrays.stream(criteria))
              .save(consumer, baseLocation.withSuffix("_blasting"));
        ExtendedCookingRecipeBuilder.smelting(CookingBookCategory.MISC, output, smeltingInput, smeltingTime)
              .experience(experience)
              .category(RecipeCategory.MISC)
              .group(group)
              .unlockedBy(Arrays.stream(criteria))
              .save(consumer, baseLocation.withSuffix("_smelting"));
    }

    /// Copied from the recipe unlock condition for vanilla chests, with values of `10` for `occupied`
    ///
    /// @see #lotsOfItems(int, int)
    public static RecipeCriterion lotsOfItems(int occupied) {
        return lotsOfItems(occupied, 0);
    }

    /// Copied from the recipe unlock condition for vanilla chests, with values of `10` for `occupied`, and `0` for `fullSlots`
    public static RecipeCriterion lotsOfItems(int occupied, int fullSlots) {
        MinMaxBounds.Ints occupiedSlotsBounds = occupied == 0 ? MinMaxBounds.Ints.ANY : MinMaxBounds.Ints.atLeast(occupied);
        MinMaxBounds.Ints fullSlotsBounds = fullSlots == 0 ? MinMaxBounds.Ints.ANY : MinMaxBounds.Ints.atLeast(fullSlots);
        return new RecipeCriterion("has_lots_of_items", CriteriaTriggers.INVENTORY_CHANGED.createCriterion(new InventoryChangeTrigger.TriggerInstance(
              Optional.empty(),
              new InventoryChangeTrigger.TriggerInstance.Slots(occupiedSlotsBounds, fullSlotsBounds, MinMaxBounds.Ints.ANY),
              Collections.emptyList()
        )));
    }

    public static void addPrecisionSawmillWoodTypeRecipes(RecipeOutput consumer, HolderGetter<Item> items, String basePath, BlockItemId planks,
          @Nullable ResourceKey<Item> boat, @Nullable ResourceKey<Item> chestBoat, BlockItemId door, BlockItemId fenceGate, @Nullable TagKey<Item> log,
          BlockItemId pressurePlate, BlockItemId trapdoor, @Nullable BlockItemId hangingSign, BlockItemId strippedLog, BlockItemId shelf, WoodType woodType) {
        addPrecisionSawmillWoodTypeRecipes(consumer, items, basePath,
              items.getOrThrow(planks.item()),
              boat == null ? null : items.getOrThrow(boat),
              chestBoat == null ? null : items.getOrThrow(chestBoat),
              items.getOrThrow(door.item()),
              items.getOrThrow(fenceGate.item()),
              log,
              items.getOrThrow(pressurePlate.item()),
              items.getOrThrow(trapdoor.item()),
              hangingSign == null ? null : items.getOrThrow(hangingSign.item()),
              items.getOrThrow(strippedLog.item()),
              items.getOrThrow(shelf.item()),
              woodType.name(), null);
    }

    @SuppressWarnings("deprecation")
    public static void addPrecisionSawmillWoodTypeRecipes(RecipeOutput consumer, HolderGetter<Item> items, String basePath, Item planks, @Nullable Item boat,
          @Nullable Item chestBoat, Item door, Item fenceGate, @Nullable TagKey<Item> log, Item pressurePlate, Item trapdoor,
          @Nullable Item hangingSign, Item strippedLog, Item shelf, String name, @Nullable ICondition condition) {
        addPrecisionSawmillWoodTypeRecipes(consumer, items, basePath,
              planks.builtInRegistryHolder(),
              boat == null ? null : boat.builtInRegistryHolder(),
              chestBoat == null ? null : chestBoat.builtInRegistryHolder(),
              door.builtInRegistryHolder(),
              fenceGate.builtInRegistryHolder(),
              log,
              pressurePlate.builtInRegistryHolder(),
              trapdoor.builtInRegistryHolder(),
              hangingSign == null ? null : hangingSign.builtInRegistryHolder(),
              strippedLog.builtInRegistryHolder(),
              shelf.builtInRegistryHolder(),
              name, condition);
    }

    public static void addPrecisionSawmillWoodTypeRecipes(RecipeOutput consumer, HolderGetter<Item> items, String basePath, Holder<Item> planks,
          @Nullable Holder<Item> boat, @Nullable Holder<Item> chestBoat, Holder<Item> door, Holder<Item> fenceGate, @Nullable TagKey<Item> log,
          Holder<Item> pressurePlate, Holder<Item> trapdoor, @Nullable Holder<Item> hangingSign, Holder<Item> strippedLog, Holder<Item> shelf,
          String name, @Nullable ICondition condition) {
        if (boat != null) {
            //Boat
            save(consumer, SawmillRecipeBuilder.sawing(
                  IngredientCreatorAccess.item().from(BaseRecipeProvider.ingredient(boat)),
                  new ItemStackTemplate(planks, 5)
            ).category(SawingBookCategory.WOOD_TYPE)
                  .group(Mekanism.rl("boat"))
                  .unlockedBy(hasCriterion(items, boat)), basePath + "boat/" + name, condition);
            if (chestBoat != null) {
                //Chest Boat
                save(consumer, SawmillRecipeBuilder.sawing(
                      IngredientCreatorAccess.item().from(BaseRecipeProvider.ingredient(chestBoat)),
                      new ItemStackTemplate(boat),
                      template(items, BlockItemIds.CHEST),
                      1
                ).category(SawingBookCategory.WOOD_TYPE)
                      .group(Mekanism.rl("chest_boat"))
                      .unlockedBy(hasCriterion(items, chestBoat)), basePath + "chest_boat/" + name, condition);
            }
        }
        //Door
        save(consumer, SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(BaseRecipeProvider.ingredient(door)),
              new ItemStackTemplate(planks, 2)
        ).category(SawingBookCategory.WOOD_TYPE)
              .group(Mekanism.rl("door"))
              .unlockedBy(hasCriterion(items, door)), basePath + "door/" + name, condition);
        //Fence Gate
        save(consumer, SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(BaseRecipeProvider.ingredient(fenceGate)),
              new ItemStackTemplate(planks, 2),
              template(items, ItemIds.STICK, 4),
              1
        ).category(SawingBookCategory.WOOD_TYPE)
              .group(Mekanism.rl("fence_gate"))
              .unlockedBy(hasCriterion(items, fenceGate)), basePath + "fence_gate/" + name, condition);
        if (log != null) {
            //Log
            save(consumer, SawmillRecipeBuilder.sawing(
                  IngredientCreatorAccess.item().from(items, log),
                  new ItemStackTemplate(planks, 6),
                  MekanismItems.SAWDUST.asTemplate(),
                  0.25
            ).category(SawingBookCategory.WOOD_TYPE)
                  .group(Mekanism.rl("log"))
                  .unlockedBy("has_log", has(items, log)), basePath + "log/" + name, condition);
        }
        if (hangingSign != null) {
            //Hanging sign
            save(consumer, SawmillRecipeBuilder.sawing(
                  IngredientCreatorAccess.item().from(BaseRecipeProvider.ingredient(hangingSign)),
                  new ItemStackTemplate(planks, 2),
                  MekanismItems.SAWDUST.asTemplate(),
                  0.5
            ).category(SawingBookCategory.WOOD_TYPE)
                  .group(Mekanism.rl("hanging_sign"))
                  .unlockedBy(hasCriterion(items, hangingSign)), basePath + "hanging_sign/" + name, condition);
        }
        //Pressure plate
        save(consumer, SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(BaseRecipeProvider.ingredient(pressurePlate)),
              new ItemStackTemplate(planks),
              MekanismItems.SAWDUST.asTemplate(2),
              0.25
        ).category(SawingBookCategory.WOOD_TYPE)
              .group(Mekanism.rl("pressure_plate"))
              .unlockedBy(hasCriterion(items, pressurePlate)), basePath + "pressure_plate/" + name, condition);
        //Trapdoor
        save(consumer, SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(BaseRecipeProvider.ingredient(trapdoor)),
              new ItemStackTemplate(planks, 3)
        ).category(SawingBookCategory.WOOD_TYPE)
              .group(Mekanism.rl("trapdoor"))
              .unlockedBy(hasCriterion(items, trapdoor)), basePath + "trapdoor/" + name, condition);
        //Shelf
        save(consumer, SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(BaseRecipeProvider.ingredient(shelf)),
              new ItemStackTemplate(strippedLog)
        ).category(SawingBookCategory.WOOD_TYPE)
              .group(Mekanism.rl("shelf"))
              .unlockedBy(hasCriterion(items, shelf)), basePath + "shelf/" + name, condition);
    }

    public static void addSandStoneToSandRecipe(RecipeOutput consumer, HolderGetter<Item> items, String path, @Nullable ICondition condition, BlockItemId sand,
          TagKey<Item> sandstoneTag) {
        save(consumer, ItemStackToItemStackRecipeBuilder.crushing(
                          IngredientCreatorAccess.item().from(items, sandstoneTag),
                          template(items, sand, 2)
                    ).category(CrushingBookCategory.BUILDING)
                    .unlockedBy("has_sandstone", has(items, sandstoneTag)),
              path, condition);
    }

    @SuppressWarnings("unchecked")
    public static void addEnrichingDyeRecipe(RecipeOutput consumer, HolderGetter<Item> items, String basePath, ResourceKey<Item> dye, int outputAmount, String name, BlockItemId... inputs) {
        addEnrichingDyeRecipe(consumer, items, basePath, dye, outputAmount, name, Arrays.stream(inputs).map(BlockItemId::item).toArray(ResourceKey[]::new));
    }

    @SafeVarargs
    public static void addEnrichingDyeRecipe(RecipeOutput consumer, HolderGetter<Item> items, String basePath, ResourceKey<Item> dye, int outputAmount, String name, ResourceKey<Item>... inputs) {
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(items, inputs),
                    template(items, dye, outputAmount)
              ).category(EnrichingBookCategory.DECORATIVE)
              .group(dye)
              .unlockedBy(Arrays.stream(inputs).map(input -> hasCriterion(items, input)))
              .save(consumer, Mekanism.rl(basePath + name));
    }

    public static void addEnrichingDyeRecipe(RecipeOutput consumer, HolderGetter<Item> items, String basePath, ResourceKey<Item> dye, int outputAmount, String name, TagKey<Item> input) {
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(items, input),
                    template(items, dye, outputAmount)
              ).category(EnrichingBookCategory.DECORATIVE)
              .group(dye)
              .unlockedBy("has_flower", has(items, input))
              .save(consumer, Mekanism.rl(basePath + name));
    }

    @SuppressWarnings("unchecked")
    public static void addPigmentExtractingDyeRecipe(RecipeOutput consumer, HolderGetter<Item> items, HolderGetter<Chemical> chemicals, String basePath, EnumColor color,
          int outputAmount, String name, BlockItemId... inputs) {
        addPigmentExtractingDyeRecipe(consumer, items, chemicals, basePath, color, outputAmount, name, Arrays.stream(inputs).map(BlockItemId::item).toArray(ResourceKey[]::new));
    }

    @SafeVarargs
    public static void addPigmentExtractingDyeRecipe(RecipeOutput consumer, HolderGetter<Item> items, HolderGetter<Chemical> chemicals, String basePath, EnumColor color,
          int outputAmount, String name, ResourceKey<Item>... inputs) {
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
              IngredientCreatorAccess.item().from(items, inputs),
              chemicalTemplate(chemicals, ChemicalIds.SIMPLE_PIGMENTS.pick(color), outputAmount)
        ).group(ItemIds.DYE.pick(Objects.requireNonNull(color.getDyeColor())))
              .unlockedBy(Arrays.stream(inputs).map(input -> hasCriterion(items, input)))
              .save(consumer, Mekanism.rl(basePath + name));
    }

    public static void addPigmentExtractingDyeRecipe(RecipeOutput consumer, HolderGetter<Item> items, HolderGetter<Chemical> chemicals, String basePath, EnumColor color,
          int outputAmount, String name, TagKey<Item> input) {
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
                    IngredientCreatorAccess.item().from(items, input),
                    chemicalTemplate(chemicals, ChemicalIds.SIMPLE_PIGMENTS.pick(color), outputAmount)
              ).group(ItemIds.DYE.pick(Objects.requireNonNull(color.getDyeColor())))
              .unlockedBy("has_flower", has(items, input))
              .save(consumer, Mekanism.rl(basePath + name));
    }

    public static void save(RecipeOutput consumer, MekanismRecipeBuilder<?, ?, ?> builder, String path, @Nullable ICondition condition) {
        if (condition != null) {
            //If there is a condition, add it to the recipe builder
            builder.addCondition(condition);
        }
        builder.save(consumer, Mekanism.rl(path));
    }

    public static ItemStackTemplate template(HolderGetter<Item> items, ResourceKey<Item> item) {
        return template(items, item, 1);
    }

    public static ItemStackTemplate template(HolderGetter<Item> items, ResourceKey<Item> item, int amount) {
        return new ItemStackTemplate(items.getOrThrow(item), amount);
    }

    public static ItemStackTemplate template(HolderGetter<Item> items, BlockItemId item) {
        return template(items, item, 1);
    }

    public static ItemStackTemplate template(HolderGetter<Item> items, BlockItemId item, int amount) {
        return template(items, item.item(), amount);
    }

    public static ChemicalStackTemplate chemicalTemplate(HolderGetter<Chemical> chemicals, ResourceKey<Chemical> chemical, int amount) {
        return new ChemicalStackTemplate(chemicals.getOrThrow(chemical), amount);
    }

    public static FluidStackTemplate fluidTemplate(HolderGetter<Fluid> fluids, ResourceKey<Fluid> fluid, int amount) {
        return new FluidStackTemplate(fluids.getOrThrow(fluid), amount);
    }

    public static RecipeCriterion hasCriterion(HolderGetter<Item> items, ResourceKey<Item> name) {
        Item item = items.getOrThrow(name).value();
        return new RecipeCriterion(RecipeProvider.getHasName(item), has(items, item));
    }

    public static RecipeCriterion hasCriterion(HolderGetter<Item> items, Holder<Item> holder) {
        Item item = holder.value();
        return new RecipeCriterion(RecipeProvider.getHasName(item), has(items, item));
    }


    public static Criterion<TriggerInstance> has(HolderGetter<Item> items, Holder<Item> item) {
        return has(items, item.value());
    }

    public static Criterion<TriggerInstance> has(HolderGetter<Item> items, Item item) {
        return RecipeProvider.inventoryTrigger(ItemPredicate.Builder.item().of(items, item));
    }

    public static Criterion<TriggerInstance> has(HolderGetter<Item> items, TagKey<Item> tag) {
        return RecipeProvider.inventoryTrigger(ItemPredicate.Builder.item().of(items, tag));
    }
}