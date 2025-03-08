package mekanism.common.integration.crafttweaker.ingredient;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.data.IData;
import com.blamejared.crafttweaker.api.data.op.IDataOps;
import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import com.blamejared.crafttweaker.api.util.Many;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
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

    /**
     * Creates a {@link ChemicalStackIngredient} that matches a given chemical type and amount.
     *
     * @param chemical Chemical type to match
     * @param amount   Amount needed
     *
     * @return A {@link ChemicalStackIngredient} that matches a given chemical and amount.
     */
    @SuppressWarnings("removal")
    @ZenCodeType.StaticExpansionMethod
    @Deprecated(forRemoval = true, since = "10.7.11")//TODO - 1.22: Replace with method that accepts holders once jared adds holder support in CrT
    public static ChemicalStackIngredient from(Chemical chemical, long amount) {
        assertValidAmount(amount);
        if (chemical.isEmptyType()) {
            throw new IllegalArgumentException("ChemicalStackIngredients cannot be created from an empty chemical.");
        }
        return IngredientCreatorAccess.chemicalStack().from(chemical, amount);
    }

    /**
     * Creates a {@link ChemicalStackIngredient} that matches a given chemical stack.
     *
     * @param instance Chemical stack to match
     *
     * @return A {@link ChemicalStackIngredient} that matches a given chemical stack.
     */
    @ZenCodeType.StaticExpansionMethod
    public static ChemicalStackIngredient from(ICrTChemicalStack instance) {
        if (instance.isEmpty()) {
            throw new IllegalArgumentException("ChemicalStackIngredients cannot be created from an empty stack.");
        }
        return IngredientCreatorAccess.chemicalStack().from(instance.getImmutableInternal());
    }

    /**
     * Creates a {@link ChemicalStackIngredient} that matches the given chemicals and amount.
     *
     * @param amount    Amount needed
     * @param chemicals Chemicals to match
     *
     * @return A {@link ChemicalStackIngredient} that matches the given chemicals and amount.
     */
    @SuppressWarnings("removal")
    @ZenCodeType.StaticExpansionMethod
    @Deprecated(forRemoval = true, since = "10.7.11")//TODO - 1.22: Replace with method that accepts holders once jared adds holder support in CrT
    public static ChemicalStackIngredient from(long amount, Chemical... chemicals) {
        return from(amount, Arrays.stream(chemicals).map(Chemical::getAsHolder));
    }

    /**
     * Creates a {@link ChemicalStackIngredient} that matches the given chemicals and amount.
     *
     * @param amount    Amount needed
     * @param chemicals Chemicals to match
     *
     * @return A {@link ChemicalStackIngredient} that matches the given chemicals and amount.
     */
    @ZenCodeType.StaticExpansionMethod
    public static ChemicalStackIngredient from(long amount, ICrTChemicalStack... chemicals) {
        return from(amount, Arrays.stream(chemicals).map(ICrTChemicalStack::getChemicalHolder));
    }

    private static ChemicalStackIngredient from(long amount, Stream<Holder<Chemical>> holders) {
        assertValidAmount(amount);
        Holder<Chemical>[] chemicals = holders.toArray(Holder[]::new);
        if (chemicals.length == 0) {
            throw new IllegalArgumentException("ChemicalStackIngredients cannot be created from zero chemicals.");
        }
        for (Holder<Chemical> instance : chemicals) {
            if (instance.is(MekanismAPI.EMPTY_CHEMICAL_KEY)) {
                throw new IllegalArgumentException("ChemicalStackIngredients cannot be created from an empty chemical.");
            }
        }
        return IngredientCreatorAccess.chemicalStack().fromHolders(amount, chemicals);
    }

    /**
     * Creates a {@link ChemicalStackIngredient} that matches the given chemical stacks. The first stack's size will be used for this ingredient.
     *
     * @param chemicals Chemical stacks to match
     *
     * @return A {@link ChemicalStackIngredient} that matches a given chemical stack.
     */
    @ZenCodeType.StaticExpansionMethod
    public static ChemicalStackIngredient from(ICrTChemicalStack... chemicals) {
        if (chemicals == null || chemicals.length == 0) {
            throw new IllegalArgumentException("ChemicalStackIngredients cannot be created from zero chemicals.");
        }
        List<ChemicalIngredient> ingredients = new ArrayList<>(chemicals.length);
        long amount = 0;
        for (ICrTChemicalStack instance : chemicals) {
            if (instance.isEmpty()) {
                throw new IllegalArgumentException("ChemicalStackIngredients cannot be created from an empty chemical.");
            } else if (amount == 0) {
                amount = instance.getAmount();
            }
            ingredients.add(IngredientCreatorAccess.chemical().of(instance.getChemicalHolder()));
        }
        assertValidAmount(amount);
        return IngredientCreatorAccess.chemicalStack().from(IngredientCreatorAccess.chemical().ofIngredients(ingredients), amount);
    }

    /**
     * Creates a {@link ChemicalStackIngredient} that matches a given chemical tag with a given amount.
     *
     * @param chemicalTag Tag to match
     * @param amount      Amount needed
     *
     * @return A {@link ChemicalStackIngredient} that matches a given chemical tag with a given amount.
     */
    @ZenCodeType.StaticExpansionMethod
    public static ChemicalStackIngredient from(KnownTag<Chemical> chemicalTag, long amount) {
        assertValidAmount(amount);
        TagKey<Chemical> tag = CrTUtils.validateTagAndGet(chemicalTag);
        return IngredientCreatorAccess.chemicalStack().from(tag, amount);
    }

    /**
     * Creates a {@link ChemicalStackIngredient} that matches a given chemical tag with amount.
     *
     * @param chemicalTag Tag and amount to match
     *
     * @return A {@link ChemicalStackIngredient} that matches a given chemical tag with amount.
     */
    @ZenCodeType.StaticExpansionMethod
    public static ChemicalStackIngredient from(Many<KnownTag<Chemical>> chemicalTag) {
        return from(chemicalTag.getData(), chemicalTag.getAmount());
    }

    /**
     * Converts this {@link ChemicalStackIngredient} into JSON ({@link IData}).
     *
     * @return {@link ChemicalStackIngredient} as JSON.
     */
    @ZenCodeType.Method
    @ZenCodeType.Caster(implicit = true)
    public static IData asIData(ChemicalStackIngredient _this) {
        return IngredientCreatorAccess.chemicalStack().codec().encodeStart(IDataOps.INSTANCE.withRegistryAccess(), _this).getOrThrow();
    }

    /**
     * Checks if a given {@link ICrTChemicalStack} has a type match for this {@link ChemicalStackIngredient}. Type matches ignore stack size.
     *
     * @param type Type to check for a match
     *
     * @return {@code true} if the type is supported by this {@link ChemicalStackIngredient}.
     */
    @ZenCodeType.Method
    public static boolean testType(ChemicalStackIngredient _this, ICrTChemicalStack type) {
        return _this.testType(type.getInternal());
    }

    /**
     * Checks if a given chemical has a type match for this {@link ChemicalStackIngredient}. Type matches ignore stack size.
     *
     * @param chemical Type to check for a match
     *
     * @return {@code true} if the type is supported by this {@link ChemicalStackIngredient}.
     */
    @ZenCodeType.Method
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true, since = "10.7.11")//TODO - 1.22: Replace with method that accepts holders once jared adds holder support in CrT
    public static boolean testType(ChemicalStackIngredient _this, Chemical chemical) {
        return _this.testType(chemical);
    }

    /**
     * Checks if a given {@link ICrTChemicalStack} matches this {@link ChemicalStackIngredient}. (Checks size for >=)
     *
     * @param stack Stack to check for a match
     *
     * @return {@code true} if the stack fulfills the requirements for this {@link ChemicalStackIngredient}.
     */
    @ZenCodeType.Method
    public static boolean test(ChemicalStackIngredient _this, ICrTChemicalStack stack) {
        return _this.test(stack.getInternal());
    }

    /**
     * Gets a list of valid instances for this {@link ChemicalStackIngredient}, may not include all or may be empty depending on how complex the ingredient is as the
     * internal version is mostly used for JEI display purposes.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("representations")
    public static List<ICrTChemicalStack> getRepresentations(ChemicalStackIngredient _this) {
        return CrTUtils.convertChemical(_this.getRepresentations());
    }

    /**
     * OR's this {@link ChemicalStackIngredient} with another {@link ChemicalStackIngredient} to create a multi {@link ChemicalStackIngredient}
     *
     * @param other {@link ChemicalStackIngredient} to combine with.
     *
     * @return Multi {@link ChemicalStackIngredient} that matches both the source {@link ChemicalStackIngredient} and the OR'd {@link ChemicalStackIngredient}.
     */
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

    /**
     * Validates that the amount is greater than zero. If it is not it throws an error.
     */
    private static void assertValidAmount(long amount) {
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