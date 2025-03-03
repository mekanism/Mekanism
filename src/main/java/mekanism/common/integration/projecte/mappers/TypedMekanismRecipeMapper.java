package mekanism.common.integration.projecte.mappers;

import com.mojang.datafixers.util.Function3;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SequencedCollection;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.InputIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.config.IConfigTranslation;
import mekanism.common.integration.projecte.NSSChemical;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.INSSFakeGroupManager;
import moze_intel.projecte.api.mapper.recipe.INSSFakeGroupManager.FakeGroupData;
import moze_intel.projecte.api.mapper.recipe.IRecipeTypeMapper;
import moze_intel.projecte.api.nss.NSSFluid;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.common.util.TriPredicate;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Nullable;

public abstract class TypedMekanismRecipeMapper<RECIPE extends Recipe<?>> implements IRecipeTypeMapper {

    protected static final boolean OPTIMIZE_BASIC = true;
    private static final IntBinaryOperator MERGE = Integer::sum;

    private final Holder<RecipeType<?>>[] supportedTypes;
    private final IConfigTranslation configTranslation;
    private final Class<RECIPE> recipeClass;

    @SafeVarargs
    protected TypedMekanismRecipeMapper(IConfigTranslation configTranslation, Class<RECIPE> recipeClass,
          DeferredHolder<RecipeType<?>, ? extends RecipeType<? extends RECIPE>>... supportedTypes) {
        this.configTranslation = configTranslation;
        this.supportedTypes = supportedTypes;
        this.recipeClass = recipeClass;
    }

    @Override
    public final String getName() {
        return configTranslation.title();
    }

    @Override
    public final String getTranslationKey() {
        return configTranslation.getTranslationKey();
    }

    @Override
    public final String getDescription() {
        return configTranslation.tooltip();
    }

    @Override
    public final boolean canHandle(RecipeType<?> recipeType) {
        for (Holder<RecipeType<?>> supportedType : supportedTypes) {
            if (supportedType.value() == recipeType) {
                return true;
            }
        }
        return false;
    }

    protected abstract boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, RECIPE recipe, MekFakeGroupHelper fakeGroupHelper);

    @Override
    public final boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, RecipeHolder<?> recipeHolder, RegistryAccess registryAccess,
          INSSFakeGroupManager fakeGroupManager) {
        Recipe<?> recipe = recipeHolder.value();
        if (recipeClass.isInstance(recipe) && !recipe.isIncomplete()) {
            //Double check that we have a type of recipe we know how to handle
            return handleRecipe(mapper, recipeClass.cast(recipe), new MekFakeGroupHelper(fakeGroupManager));
        }
        return false;
    }

    protected static boolean addConversion(IMappingCollector<NormalizedSimpleStack, Long> mapper, ChemicalStack output, Object2IntMap<NormalizedSimpleStack> recipeInput) {
        if (!output.isEmpty() && !recipeInput.isEmpty() && output.getAmount() <= Integer.MAX_VALUE) {
            mapper.addConversion((int) output.getAmount(), NSSChemical.createChemical(output), recipeInput);
            return true;
        }
        return false;
    }

    protected static boolean addConversion(IMappingCollector<NormalizedSimpleStack, Long> mapper, FluidStack output, Object2IntMap<NormalizedSimpleStack> recipeInput) {
        if (!output.isEmpty() && !recipeInput.isEmpty()) {
            mapper.addConversion(output.getAmount(), NSSFluid.createFluid(output), recipeInput);
            return true;
        }
        return false;
    }

    protected static boolean addConversion(IMappingCollector<NormalizedSimpleStack, Long> mapper, ItemStack output, Object2IntMap<NormalizedSimpleStack> recipeInput) {
        if (!output.isEmpty() && !recipeInput.isEmpty()) {
            mapper.addConversion(output.getCount(), NSSItem.createItem(output), recipeInput);
            return true;
        }
        return false;
    }

    protected static <INPUT, OUTPUT> boolean addConversions(IMappingCollector<NormalizedSimpleStack, Long> mapper, InputIngredient<INPUT> inputs,
          Function<INPUT, OUTPUT> recipe, Predicate<OUTPUT> emptyChecker, Function<SequencedCollection<INPUT>, Object2IntMap<NormalizedSimpleStack>> toIngredient,
          @Nullable Hash.Strategy<? super OUTPUT> hashStrategy,
          TriPredicate<IMappingCollector<NormalizedSimpleStack, Long>, OUTPUT, Object2IntMap<NormalizedSimpleStack>> conversionAdder) {
        Map<OUTPUT, List<INPUT>> reverseLookup = hashStrategy == null ? new HashMap<>() : new Object2ObjectOpenCustomHashMap<>(hashStrategy);
        for (INPUT representation : inputs.getRepresentations()) {
            OUTPUT output = recipe.apply(representation);
            if (!emptyChecker.test(output)) {
                reverseLookup.computeIfAbsent(output, k -> new ArrayList<>()).add(representation);
            }
        }
        boolean handled = false;
        for (Map.Entry<OUTPUT, List<INPUT>> entry : reverseLookup.entrySet()) {
            handled |= conversionAdder.test(mapper, entry.getKey(), toIngredient.apply(entry.getValue()));
        }
        return handled;
    }

    protected static <INPUT_A, INPUT_B, OUTPUT> boolean addConversions(IMappingCollector<NormalizedSimpleStack, Long> mapper, InputIngredient<INPUT_A> inputA,
          InputIngredient<INPUT_B> inputB, BiFunction<INPUT_A, INPUT_B, OUTPUT> recipe, Predicate<OUTPUT> emptyChecker,
          Function<SequencedCollection<INPUT_A>, Object2IntMap<NormalizedSimpleStack>> toIngredientA,
          Function<SequencedCollection<INPUT_B>, Object2IntMap<NormalizedSimpleStack>> toIngredientB,
          @Nullable Hash.Strategy<? super OUTPUT> hashStrategy,
          TriPredicate<IMappingCollector<NormalizedSimpleStack, Long>, OUTPUT, Object2IntMap<NormalizedSimpleStack>> conversionAdder) {
        return addConversions(mapper, inputA, inputB, recipe, emptyChecker, toIngredientA, toIngredientB, hashStrategy, conversionAdder, 1);
    }

    protected static <INPUT_A, INPUT_B, OUTPUT> boolean addConversions(IMappingCollector<NormalizedSimpleStack, Long> mapper, InputIngredient<INPUT_A> inputA,
          InputIngredient<INPUT_B> inputB, BiFunction<INPUT_A, INPUT_B, OUTPUT> recipe, Predicate<OUTPUT> emptyChecker,
          Function<SequencedCollection<INPUT_A>, Object2IntMap<NormalizedSimpleStack>> toIngredientA,
          Function<SequencedCollection<INPUT_B>, Object2IntMap<NormalizedSimpleStack>> toIngredientB,
          @Nullable Hash.Strategy<? super OUTPUT> hashStrategy,
          TriPredicate<IMappingCollector<NormalizedSimpleStack, Long>, OUTPUT, Object2IntMap<NormalizedSimpleStack>> conversionAdder, int secondaryInputScale) {
        record InputDetails<INPUT_A, INPUT_B>(SequencedCollection<INPUT_A> aInputs, SequencedCollection<INPUT_B> bInputs) {

            InputDetails() {//Note: We use reference sets to avoid adding the same exact instance multiple times
                this(new ReferenceLinkedOpenHashSet<>(), new ReferenceLinkedOpenHashSet<>());
            }
        }
        Map<OUTPUT, InputDetails<INPUT_A, INPUT_B>> reverseLookup = hashStrategy == null ? new HashMap<>() : new Object2ObjectOpenCustomHashMap<>(hashStrategy);
        List<INPUT_A> aRepresentations = inputA.getRepresentations();
        List<INPUT_B> bRepresentations = inputB.getRepresentations();
        for (INPUT_A aRepresentation : aRepresentations) {
            for (INPUT_B bRepresentation : bRepresentations) {
                OUTPUT output = recipe.apply(aRepresentation, bRepresentation);
                if (!emptyChecker.test(output)) {
                    InputDetails<INPUT_A, INPUT_B> details = reverseLookup.computeIfAbsent(output, k -> new InputDetails<>());
                    details.aInputs.add(aRepresentation);
                    details.bInputs.add(bRepresentation);
                }
            }
        }
        boolean handled = false;
        for (Map.Entry<OUTPUT, InputDetails<INPUT_A, INPUT_B>> entry : reverseLookup.entrySet()) {
            InputDetails<INPUT_A, INPUT_B> details = entry.getValue();
            handled |= conversionAdder.test(mapper, entry.getKey(), forIngredients(
                  toIngredientA.apply(details.aInputs()),
                  toIngredientB.apply(details.bInputs()),
                  secondaryInputScale
            ));
        }
        return handled;
    }

    protected static <INPUT_A, INPUT_B, INPUT_C, OUTPUT> boolean addConversions(IMappingCollector<NormalizedSimpleStack, Long> mapper, InputIngredient<INPUT_A> inputA,
          InputIngredient<INPUT_B> inputB, InputIngredient<INPUT_C> inputC, Function3<INPUT_A, INPUT_B, INPUT_C, OUTPUT> recipe, Predicate<OUTPUT> emptyChecker,
          Function<SequencedCollection<INPUT_A>, Object2IntMap<NormalizedSimpleStack>> toIngredientA,
          Function<SequencedCollection<INPUT_B>, Object2IntMap<NormalizedSimpleStack>> toIngredientB,
          Function<SequencedCollection<INPUT_C>, Object2IntMap<NormalizedSimpleStack>> toIngredientC, @Nullable Hash.Strategy<? super OUTPUT> hashStrategy,
          TriPredicate<IMappingCollector<NormalizedSimpleStack, Long>, OUTPUT, Object2IntMap<NormalizedSimpleStack>> conversionAdder) {
        record InputDetails<INPUT_A, INPUT_B, INPUT_C>(SequencedCollection<INPUT_A> aInputs, SequencedCollection<INPUT_B> bInputs, SequencedCollection<INPUT_C> cInputs) {

            InputDetails() {//Note: We use reference sets to avoid adding the same exact instance multiple times
                this(new ReferenceLinkedOpenHashSet<>(), new ReferenceLinkedOpenHashSet<>(), new ReferenceLinkedOpenHashSet<>());
            }
        }
        Map<OUTPUT, InputDetails<INPUT_A, INPUT_B, INPUT_C>> reverseLookup = hashStrategy == null ? new HashMap<>() : new Object2ObjectOpenCustomHashMap<>(hashStrategy);
        List<INPUT_A> aRepresentations = inputA.getRepresentations();
        List<INPUT_B> bRepresentations = inputB.getRepresentations();
        List<INPUT_C> cRepresentations = inputC.getRepresentations();
        for (INPUT_A aRepresentation : aRepresentations) {
            for (INPUT_B bRepresentation : bRepresentations) {
                for (INPUT_C cRepresentation : cRepresentations) {
                    OUTPUT output = recipe.apply(aRepresentation, bRepresentation, cRepresentation);
                    if (!emptyChecker.test(output)) {
                        InputDetails<INPUT_A, INPUT_B, INPUT_C> details = reverseLookup.computeIfAbsent(output, k -> new InputDetails<>());
                        details.aInputs.add(aRepresentation);
                        details.bInputs.add(bRepresentation);
                        details.cInputs.add(cRepresentation);
                    }
                }
            }
        }
        boolean handled = false;
        for (Map.Entry<OUTPUT, InputDetails<INPUT_A, INPUT_B, INPUT_C>> entry : reverseLookup.entrySet()) {
            InputDetails<INPUT_A, INPUT_B, INPUT_C> details = entry.getValue();
            handled |= conversionAdder.test(mapper, entry.getKey(), forIngredients(
                  toIngredientA.apply(details.aInputs()),
                  toIngredientB.apply(details.bInputs()),
                  toIngredientC.apply(details.cInputs())
            ));
        }
        return handled;
    }

    protected static Object2IntMap<NormalizedSimpleStack> forIngredients(Object2IntMap<NormalizedSimpleStack> a, NormalizedSimpleStack b, int bAmount) {
        if (a.isEmpty()) {
            return Object2IntMaps.emptyMap();
        }
        Object2IntMap<NormalizedSimpleStack> inputs = new Object2IntArrayMap<>(a);
        inputs.mergeInt(b, bAmount, MERGE);
        return inputs;
    }

    protected static Object2IntMap<NormalizedSimpleStack> forIngredients(Object2IntMap<NormalizedSimpleStack> a, Object2IntMap<NormalizedSimpleStack> b, int scaleB) {
        if (a.isEmpty() || b.isEmpty()) {
            return Object2IntMaps.emptyMap();
        }
        return insertScaled(new Object2IntArrayMap<>(a), b, scaleB);
    }

    protected static Object2IntMap<NormalizedSimpleStack> insertScaled(Object2IntMap<NormalizedSimpleStack> resultMap, Object2IntMap<NormalizedSimpleStack> inputs, int scale) {
        boolean hasValidInput = false;
        for (ObjectIterator<Entry<NormalizedSimpleStack>> iterator = Object2IntMaps.fastIterator(inputs); iterator.hasNext(); ) {
            Object2IntMap.Entry<NormalizedSimpleStack> entry = iterator.next();
            try {
                resultMap.mergeInt(entry.getKey(), Math.multiplyExact(entry.getIntValue(), scale), MERGE);
                hasValidInput = true;
            } catch (ArithmeticException ignored) {
            }
        }
        if (!hasValidInput) {
            return Object2IntMaps.emptyMap();
        }
        return resultMap;
    }

    @SafeVarargs
    protected static Object2IntMap<NormalizedSimpleStack> forIngredients(Object2IntMap<NormalizedSimpleStack>... ingredients) {
        Object2IntMap<NormalizedSimpleStack> inputs = new Object2IntArrayMap<>(ingredients.length);
        for (Object2IntMap<NormalizedSimpleStack> ingredient : ingredients) {
            if (ingredient.isEmpty()) {
                return Object2IntMaps.emptyMap();
            }
            insertScaled(inputs, ingredient, 1);
        }
        return inputs;
    }

    protected record MekFakeGroupHelper(INSSFakeGroupManager manager) {

        public Object2IntMap<NormalizedSimpleStack> forIngredients(InputIngredient<?>... ingredients) {
            Object2IntMap<NormalizedSimpleStack> inputs = new Object2IntArrayMap<>(ingredients.length);
            for (InputIngredient<?> ingredient : ingredients) {
                Object2IntMap<NormalizedSimpleStack> representations = switch (ingredient) {
                    case ItemStackIngredient itemIngredient -> forIngredient(itemIngredient);
                    case FluidStackIngredient fluidIngredient -> forIngredient(fluidIngredient);
                    case ChemicalStackIngredient chemicalIngredient -> forIngredient(chemicalIngredient);
                    case null, default -> Object2IntMaps.emptyMap();
                };
                if (representations.isEmpty()) {
                    return Object2IntMaps.emptyMap();
                }
                insertScaled(inputs, representations, 1);
            }
            return inputs;
        }

        public Object2IntMap<NormalizedSimpleStack> forIngredient(ItemStackIngredient ingredient) {
            return forItems(ingredient.getRepresentations());
        }

        public Object2IntMap<NormalizedSimpleStack> forItems(SequencedCollection<ItemStack> representations) {
            return forIngredient(representations, NSSItem::createItem, ItemStack::getCount);
        }

        public Object2IntMap<NormalizedSimpleStack> forIngredient(FluidStackIngredient ingredient) {
            return forFluids(ingredient.getRepresentations());
        }

        public Object2IntMap<NormalizedSimpleStack> forFluids(SequencedCollection<FluidStack> representations) {
            return forIngredient(representations, NSSFluid::createFluid, FluidStack::getAmount);
        }

        public Object2IntMap<NormalizedSimpleStack> forIngredient(ChemicalStackIngredient ingredient) {
            return forChemicals(ingredient.getRepresentations());
        }

        public Object2IntMap<NormalizedSimpleStack> forChemicals(SequencedCollection<ChemicalStack> representations) {
            for (ChemicalStack representation : representations) {
                if (representation.getAmount() > Integer.MAX_VALUE) {
                    return Object2IntMaps.emptyMap();
                }
            }
            return forIngredient(representations, NSSChemical::createChemical, stack -> (int) stack.getAmount());
        }

        private <STACK> Object2IntMap<NormalizedSimpleStack> forIngredient(SequencedCollection<STACK> representations, Function<STACK, NormalizedSimpleStack> nssCreator,
              ToIntFunction<STACK> stackSize) {
            int size = representations.size();
            if (size == 0) {
                return Object2IntMaps.emptyMap();
            } else if (size == 1) {
                STACK stack = representations.getFirst();
                return Object2IntMaps.singleton(
                      nssCreator.apply(stack),
                      stackSize.applyAsInt(stack)
                );
            }
            Object2IntMap<NormalizedSimpleStack> stacks = new Object2IntOpenHashMap<>(size);
            for (STACK representation : representations) {
                stacks.mergeInt(nssCreator.apply(representation), stackSize.applyAsInt(representation), MERGE);
            }
            FakeGroupData fakeGroup = manager.getOrCreateFakeGroupDirect(stacks, true);
            return Object2IntMaps.singleton(
                  fakeGroup.dummy(),
                  1
            );
        }
    }
}