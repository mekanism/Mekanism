package mekanism.common.integration.crafttweaker.ingredient;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.data.IData;
import com.blamejared.crafttweaker.api.data.op.IDataOps;
import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import com.blamejared.crafttweaker.api.util.Many;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import com.google.common.primitives.Ints;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import mekanism.api.MekanismRegistries;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalIds;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.chemical.ChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.CompoundChemicalIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@NativeTypeRegistration(value = ChemicalStackIngredient.class, zenCodeName = CrTConstants.CLASS_CHEMICAL_STACK_INGREDIENT)
public class CrTChemicalStackIngredient {

    private CrTChemicalStackIngredient() {
    }

    /// Creates a [ChemicalStackIngredient] that matches a given chemical stack.
    ///
    /// @param instance Chemical stack to match
    ///
    /// @return A [ChemicalStackIngredient] that matches a given chemical stack.
    @ZenCodeType.StaticExpansionMethod
    public static ChemicalStackIngredient from(ICrTChemicalStack instance) {
        if (instance.isEmpty()) {
            throw new IllegalArgumentException("ChemicalStackIngredients cannot be created from an empty stack.");
        }
        return IngredientCreatorAccess.chemicalStack().from(instance.getImmutableInternal());
    }

    /// Creates a [ChemicalStackIngredient] that matches the given chemicals and amount.
    ///
    /// @param amount    Amount needed
    /// @param chemicals Chemicals to match
    ///
    /// @return A [ChemicalStackIngredient] that matches the given chemicals and amount.
    @ZenCodeType.StaticExpansionMethod
    public static ChemicalStackIngredient from(int amount, ICrTChemicalStack... chemicals) {
        return from(amount, Arrays.stream(chemicals).map(ICrTChemicalStack::getChemicalHolder));
    }

    private static ChemicalStackIngredient from(int amount, Stream<Holder<Chemical>> holders) {
        assertValidAmount(amount);
        Holder<Chemical>[] chemicals = holders.toArray(Holder[]::new);
        if (chemicals.length == 0) {
            throw new IllegalArgumentException("ChemicalStackIngredients cannot be created from zero chemicals.");
        }
        for (Holder<Chemical> instance : chemicals) {
            if (instance.is(ChemicalIds.EMPTY)) {
                throw new IllegalArgumentException("ChemicalStackIngredients cannot be created from an empty chemical.");
            }
        }
        return IngredientCreatorAccess.chemicalStack().fromHolders(amount, chemicals);
    }

    /// Creates a [ChemicalStackIngredient] that matches the given chemical stacks. The first stack's size will be used for this ingredient.
    ///
    /// @param chemicals Chemical stacks to match
    ///
    /// @return A [ChemicalStackIngredient] that matches a given chemical stack.
    @ZenCodeType.StaticExpansionMethod
    public static ChemicalStackIngredient from(ICrTChemicalStack... chemicals) {
        if (chemicals == null || chemicals.length == 0) {
            throw new IllegalArgumentException("ChemicalStackIngredients cannot be created from zero chemicals.");
        }
        List<ChemicalIngredient> ingredients = new ArrayList<>(chemicals.length);
        int amount = 0;
        for (ICrTChemicalStack instance : chemicals) {
            if (instance.isEmpty()) {
                throw new IllegalArgumentException("ChemicalStackIngredients cannot be created from an empty chemical.");
            } else if (amount == 0) {
                amount = Ints.saturatedCast(instance.getAmount());
            }
            ingredients.add(IngredientCreatorAccess.chemical().of(instance.getChemicalHolder()));
        }
        assertValidAmount(amount);
        return IngredientCreatorAccess.chemicalStack().from(IngredientCreatorAccess.chemical().ofIngredients(ingredients), amount);
    }

    /// Creates a [ChemicalStackIngredient] that matches a given chemical tag with a given amount.
    ///
    /// @param chemicalTag Tag to match
    /// @param amount      Amount needed
    ///
    /// @return A [ChemicalStackIngredient] that matches a given chemical tag with a given amount.
    @ZenCodeType.StaticExpansionMethod
    public static ChemicalStackIngredient from(KnownTag<Chemical> chemicalTag, int amount) {
        assertValidAmount(amount);
        TagKey<Chemical> tag = CrTUtils.validateTagAndGet(chemicalTag);
        return IngredientCreatorAccess.chemicalStack().from(CraftTweakerAPI.getAccessibleElementsProvider()
              .registryAccess()
              .lookupOrThrow(MekanismRegistries.Keys.CHEMICAL), tag, amount);
    }

    /// Creates a [ChemicalStackIngredient] that matches a given chemical tag with amount.
    ///
    /// @param chemicalTag Tag and amount to match
    ///
    /// @return A [ChemicalStackIngredient] that matches a given chemical tag with amount.
    @ZenCodeType.StaticExpansionMethod
    public static ChemicalStackIngredient from(Many<KnownTag<Chemical>> chemicalTag) {
        return from(chemicalTag.getData(), chemicalTag.getAmount());
    }

    /// Converts this [ChemicalStackIngredient] into JSON ([IData]).
    ///
    /// @return [ChemicalStackIngredient] as JSON.
    @ZenCodeType.Method
    @ZenCodeType.Caster(implicit = true)
    public static IData asIData(ChemicalStackIngredient _this) {
        return IngredientCreatorAccess.chemicalStack().codec().encodeStart(IDataOps.INSTANCE.withRegistryAccess(), _this).getOrThrow();
    }

    /// Checks if a given [ICrTChemicalStack] has a type match for this [ChemicalStackIngredient]. Type matches ignore stack size.
    ///
    /// @param type Type to check for a match
    ///
    /// @return `true` if the type is supported by this [ChemicalStackIngredient].
    @ZenCodeType.Method
    public static boolean testType(ChemicalStackIngredient _this, ICrTChemicalStack type) {
        return _this.testType(type.getInternal());
    }

    /// Checks if a given [ICrTChemicalStack] matches this [ChemicalStackIngredient]. (Checks size for >=)
    ///
    /// @param stack Stack to check for a match
    ///
    /// @return `true` if the stack fulfills the requirements for this [ChemicalStackIngredient].
    @ZenCodeType.Method
    public static boolean test(ChemicalStackIngredient _this, ICrTChemicalStack stack) {
        return _this.test(stack.getInternal());
    }

    /// Gets a list of valid instances for this [ChemicalStackIngredient], may not include all or may be empty depending on how complex the ingredient is as the internal
    /// version is mostly used for JEI display purposes.
    @ZenCodeType.Method
    @ZenCodeType.Getter("representations")
    public static List<ICrTChemicalStack> getRepresentations(ChemicalStackIngredient _this) {
        return CrTUtils.convertChemical(_this.getRepresentations());
    }

    /// OR's this [ChemicalStackIngredient] with another [ChemicalStackIngredient] to create a multi [ChemicalStackIngredient]
    ///
    /// @param other [ChemicalStackIngredient] to combine with.
    ///
    /// @return Multi [ChemicalStackIngredient] that matches both the source [ChemicalStackIngredient] and the OR'd [ChemicalStackIngredient].
    @ZenCodeType.Method
    @ZenCodeType.Operator(ZenCodeType.OperatorType.OR)
    public static ChemicalStackIngredient or(ChemicalStackIngredient _this, ChemicalStackIngredient other) {
        if (_this.amount() != other.amount()) {
            throw new IllegalArgumentException("ChemicalStack ingredients can only be or'd if they have the same counts");
        }
        List<ChemicalIngredient> ingredients = new ArrayList<>();
        addIngredient(ingredients, _this.ingredient());
        addIngredient(ingredients, other.ingredient());
        return IngredientCreatorAccess.chemicalStack().from(IngredientCreatorAccess.chemical().ofIngredients(ingredients), _this.amount());
    }

    /// Validates that the amount is greater than zero. If it is not it throws an error.
    private static void assertValidAmount(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("ChemicalStackIngredients can only be created with a size of at least one. Received size was: " + amount);
        }
    }

    private static <INGREDIENT extends ChemicalIngredient> void addIngredient(List<INGREDIENT> ingredients, INGREDIENT ingredient) {
        if (ingredient instanceof CompoundChemicalIngredient compoundIngredient) {
            ingredients.addAll((List<INGREDIENT>) compoundIngredient.children());
        } else {
            ingredients.add(ingredient);
        }
    }
}