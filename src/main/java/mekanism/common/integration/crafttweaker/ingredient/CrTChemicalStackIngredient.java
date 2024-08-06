package mekanism.common.integration.crafttweaker.ingredient;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.data.IData;
import com.blamejared.crafttweaker.api.data.op.IDataOps;
import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import com.blamejared.crafttweaker.api.util.Many;
import com.blamejared.crafttweaker_annotations.annotations.NativeMethod;
import com.blamejared.crafttweaker_annotations.annotations.NativeMethod.MethodParameter;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.chemical.Chemical;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.chemical.IChemicalIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import net.minecraft.tags.TagKey;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@NativeMethod(name = "testType", parameters = @MethodParameter(type = Chemical.class, name = "chemical"))
@NativeTypeRegistration(value = ChemicalStackIngredient.class, zenCodeName = CrTConstants.CLASS_CHEMICAL_STACK_INGREDIENT)
public class CrTChemicalStackIngredient {

    private CrTChemicalStackIngredient() {
    }

    /**
     * Creates a {@link ChemicalStackIngredient} that matches a given chemical type and amount.
     *
     * @param instance Chemical type to match
     * @param amount   Amount needed
     *
     * @return A {@link ChemicalStackIngredient} that matches a given chemical and amount.
     */
    @ZenCodeType.StaticExpansionMethod
    public static ChemicalStackIngredient from(Chemical instance, long amount) {
        CrTIngredientHelper.assertValid(instance, amount, "ChemicalStackIngredients", "chemical");
        return IngredientCreatorAccess.chemicalStack().from(instance, amount);
    }

    /**
     * Creates a {@link ChemicalStackIngredient} that matches a given infusion stack.
     *
     * @param instance Chemical stack to match
     *
     * @return A {@link ChemicalStackIngredient} that matches a given infusion stack.
     */
    @ZenCodeType.StaticExpansionMethod
    public static ChemicalStackIngredient from(ICrTChemicalStack instance) {
        CrTIngredientHelper.assertValid(instance, "ChemicalStackIngredients");
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
    @ZenCodeType.StaticExpansionMethod
    public static ChemicalStackIngredient from(long amount, Chemical... chemicals) {
        CrTIngredientHelper.assertMultiple(amount, "ChemicalStackIngredients", "chemical", chemicals);
        return IngredientCreatorAccess.chemicalStack().from(amount, chemicals);
    }

    /**
     * Creates a {@link ChemicalStackIngredient} that matches the given chemicals and amount.
     *
     * @param amount      Amount needed
     * @param infuseTypes Chemicals to match
     *
     * @return A {@link ChemicalStackIngredient} that matches the given chemicals and amount.
     */
    @ZenCodeType.StaticExpansionMethod
    public static ChemicalStackIngredient from(long amount, ICrTChemicalStack... infuseTypes) {
        CrTIngredientHelper.assertMultiple(amount, "ChemicalStackIngredients", "chemical", infuseTypes);
        return IngredientCreatorAccess.chemicalStack().from(amount, infuseTypes);
    }

    /**
     * Creates a {@link ChemicalStackIngredient} that matches the given infusion stacks. The first stack's size will be used for this ingredient.
     *
     * @param infuseTypes Infusion stacks to match
     *
     * @return A {@link ChemicalStackIngredient} that matches a given infusion stack.
     */
    @ZenCodeType.StaticExpansionMethod
    public static ChemicalStackIngredient from(ICrTChemicalStack... infuseTypes) {
        long amount = CrTIngredientHelper.assertMultiple("ChemicalStackIngredients", "chemical", infuseTypes);
        return IngredientCreatorAccess.chemicalStack().from(amount, infuseTypes);
    }

    /**
     * Creates a {@link ChemicalStackIngredient} that matches a given chemical tag with a given amount.
     *
     * @param infuseTypeTag Tag to match
     * @param amount        Amount needed
     *
     * @return A {@link ChemicalStackIngredient} that matches a given chemical tag with a given amount.
     */
    @ZenCodeType.StaticExpansionMethod
    public static ChemicalStackIngredient from(KnownTag<Chemical> infuseTypeTag, long amount) {
        TagKey<Chemical> tag = CrTIngredientHelper.assertValidAndGet(infuseTypeTag, amount, "ChemicalStackIngredients");
        return IngredientCreatorAccess.chemicalStack().from(tag, amount);
    }

    /**
     * Creates a {@link ChemicalStackIngredient} that matches a given chemical tag with amount.
     *
     * @param infuseTypeTag Tag and amount to match
     *
     * @return A {@link ChemicalStackIngredient} that matches a given chemical tag with amount.
     */
    @ZenCodeType.StaticExpansionMethod
    public static ChemicalStackIngredient from(Many<KnownTag<Chemical>> infuseTypeTag) {
        return from(infuseTypeTag.getData(), infuseTypeTag.getAmount());
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
            throw new IllegalArgumentException("InfusionStack ingredients can only be or'd if they have the same counts");
        }
        List<IChemicalIngredient> ingredients = new ArrayList<>();
        CrTIngredientHelper.addIngredient(ingredients, _this.ingredient());
        CrTIngredientHelper.addIngredient(ingredients, other.ingredient());
        return IngredientCreatorAccess.chemicalStack().from(IngredientCreatorAccess.chemical().ofIngredients(ingredients), _this.amount());
    }
}