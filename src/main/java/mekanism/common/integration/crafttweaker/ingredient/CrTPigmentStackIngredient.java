package mekanism.common.integration.crafttweaker.ingredient;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.data.base.IData;
import com.blamejared.crafttweaker.api.data.base.converter.JSONConverter;
import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import com.blamejared.crafttweaker.api.util.Many;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import java.util.List;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.PigmentStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTPigmentStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTPigmentStack;
import net.minecraft.tags.TagKey;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@NativeTypeRegistration(value = PigmentStackIngredient.class, zenCodeName = CrTConstants.CLASS_PIGMENT_STACK_INGREDIENT)
public class CrTPigmentStackIngredient {

    private CrTPigmentStackIngredient() {
    }

    /**
     * Creates a {@link PigmentStackIngredient} that matches a given pigment and amount.
     *
     * @param instance Pigment to match
     * @param amount   Amount needed
     *
     * @return A {@link PigmentStackIngredient} that matches a given pigment and amount.
     */
    @ZenCodeType.StaticExpansionMethod
    public static PigmentStackIngredient from(Pigment instance, long amount) {
        CrTIngredientHelper.assertValid(instance, amount, "PigmentStackIngredients", "pigment");
        return IngredientCreatorAccess.pigment().from(instance, amount);
    }

    /**
     * Creates a {@link PigmentStackIngredient} that matches a given pigment stack.
     *
     * @param instance Pigment stack to match
     *
     * @return A {@link PigmentStackIngredient} that matches a given pigment stack.
     */
    @ZenCodeType.StaticExpansionMethod
    public static PigmentStackIngredient from(ICrTPigmentStack instance) {
        CrTIngredientHelper.assertValid(instance, "PigmentStackIngredients");
        return IngredientCreatorAccess.pigment().from(instance.getImmutableInternal());
    }

    /**
     * Creates a {@link PigmentStackIngredient} that matches a given pigment tag with a given amount.
     *
     * @param pigmentTag Tag to match
     * @param amount     Amount needed
     *
     * @return A {@link PigmentStackIngredient} that matches a given pigment tag with a given amount.
     */
    @ZenCodeType.StaticExpansionMethod
    public static PigmentStackIngredient from(KnownTag<Pigment> pigmentTag, long amount) {
        TagKey<Pigment> tag = CrTIngredientHelper.assertValidAndGet(pigmentTag, amount, "PigmentStackIngredients");
        return IngredientCreatorAccess.pigment().from(tag, amount);
    }

    /**
     * Creates a {@link PigmentStackIngredient} that matches a given pigment tag with amount.
     *
     * @param pigmentTag Tag and amount to match
     *
     * @return A {@link PigmentStackIngredient} that matches a given pigment tag with amount.
     */
    @ZenCodeType.StaticExpansionMethod
    public static PigmentStackIngredient from(Many<KnownTag<Pigment>> pigmentTag) {
        return from(pigmentTag.getData(), pigmentTag.getAmount());
    }

    /**
     * Combines multiple {@link PigmentStackIngredient}s into a single {@link PigmentStackIngredient}.
     *
     * @param ingredients Ingredients to combine
     *
     * @return A single {@link PigmentStackIngredient} representing all the passed in ingredients.
     */
    @ZenCodeType.StaticExpansionMethod
    public static PigmentStackIngredient createMulti(PigmentStackIngredient... ingredients) {
        return CrTIngredientHelper.createMulti("PigmentStackIngredients", IngredientCreatorAccess.pigment(), ingredients);
    }

    /**
     * Converts this {@link PigmentStackIngredient} into JSON ({@link IData}).
     *
     * @return {@link PigmentStackIngredient} as JSON.
     */
    @ZenCodeType.Method
    @ZenCodeType.Caster(implicit = true)
    public static IData asIData(PigmentStackIngredient _this) {
        return JSONConverter.convert(_this.serialize());
    }

    /**
     * Checks if a given {@link ICrTPigmentStack} has a type match for this {@link PigmentStackIngredient}. Type matches ignore stack size.
     *
     * @param type Type to check for a match
     *
     * @return {@code true} if the type is supported by this {@link PigmentStackIngredient}.
     */
    @ZenCodeType.Method
    public static boolean testType(PigmentStackIngredient _this, ICrTPigmentStack type) {
        return _this.testType(type.getInternal());
    }

    /**
     * Checks if a given {@link ICrTPigmentStack} matches this {@link PigmentStackIngredient}. (Checks size for >=)
     *
     * @param stack Stack to check for a match
     *
     * @return {@code true} if the stack fulfills the requirements for this {@link PigmentStackIngredient}.
     */
    @ZenCodeType.Method
    public static boolean test(PigmentStackIngredient _this, ICrTPigmentStack stack) {
        return _this.test(stack.getInternal());
    }

    /**
     * Gets a list of valid instances for this {@link PigmentStackIngredient}, may not include all or may be empty depending on how complex the ingredient is as the
     * internal version is mostly used for JEI display purposes.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("representations")
    public static List<ICrTPigmentStack> getRepresentations(PigmentStackIngredient _this) {
        return CrTUtils.convert(_this.getRepresentations(), CrTPigmentStack::new);
    }

    /**
     * OR's this {@link PigmentStackIngredient} with another {@link PigmentStackIngredient} to create a multi {@link PigmentStackIngredient}
     *
     * @param other {@link PigmentStackIngredient} to combine with.
     *
     * @return Multi {@link PigmentStackIngredient} that matches both the source {@link PigmentStackIngredient} and the OR'd {@link PigmentStackIngredient}.
     */
    @ZenCodeType.Method
    @ZenCodeType.Operator(ZenCodeType.OperatorType.OR)
    public static PigmentStackIngredient or(PigmentStackIngredient _this, PigmentStackIngredient other) {
        return IngredientCreatorAccess.pigment().createMulti(_this, other);
    }
}