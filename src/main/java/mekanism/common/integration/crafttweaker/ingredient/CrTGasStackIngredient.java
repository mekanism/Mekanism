package mekanism.common.integration.crafttweaker.ingredient;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.data.IData;
import com.blamejared.crafttweaker.api.data.converter.JSONConverter;
import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import com.blamejared.crafttweaker.api.util.Many;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import java.util.List;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTGasStack;
import net.minecraft.tags.TagKey;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@NativeTypeRegistration(value = GasStackIngredient.class, zenCodeName = CrTConstants.CLASS_GAS_STACK_INGREDIENT)
public class CrTGasStackIngredient {

    private CrTGasStackIngredient() {
    }

    /**
     * Creates a {@link GasStackIngredient} that matches a given gas and amount.
     *
     * @param instance Gas to match
     * @param amount   Amount needed
     *
     * @return A {@link GasStackIngredient} that matches a given gas and amount.
     */
    @ZenCodeType.StaticExpansionMethod
    public static GasStackIngredient from(Gas instance, long amount) {
        CrTIngredientHelper.assertValid(instance, amount, "GasStackIngredients", "gas");
        return IngredientCreatorAccess.gas().from(instance, amount);
    }

    /**
     * Creates a {@link GasStackIngredient} that matches a given gas stack.
     *
     * @param instance Gas stack to match
     *
     * @return A {@link GasStackIngredient} that matches a given gas stack.
     */
    @ZenCodeType.StaticExpansionMethod
    public static GasStackIngredient from(ICrTGasStack instance) {
        CrTIngredientHelper.assertValid(instance, "GasStackIngredients");
        return IngredientCreatorAccess.gas().from(instance.getImmutableInternal());
    }

    /**
     * Creates a {@link GasStackIngredient} that matches a given gas tag with a given amount.
     *
     * @param gasTag Tag to match
     * @param amount Amount needed
     *
     * @return A {@link GasStackIngredient} that matches a given gas tag with a given amount.
     */
    @ZenCodeType.StaticExpansionMethod
    public static GasStackIngredient from(KnownTag<Gas> gasTag, long amount) {
        TagKey<Gas> tag = CrTIngredientHelper.assertValidAndGet(gasTag, amount, "GasStackIngredients");
        return IngredientCreatorAccess.gas().from(tag, amount);
    }

    /**
     * Creates a {@link GasStackIngredient} that matches a given gas tag with amount.
     *
     * @param gasTag Tag and amount to match
     *
     * @return A {@link GasStackIngredient} that matches a given gas tag with amount.
     */
    @ZenCodeType.StaticExpansionMethod
    public static GasStackIngredient from(Many<KnownTag<Gas>> gasTag) {
        return from(gasTag.getData(), gasTag.getAmount());
    }

    /**
     * Combines multiple {@link GasStackIngredient}s into a single {@link GasStackIngredient}.
     *
     * @param ingredients Ingredients to combine
     *
     * @return A single {@link GasStackIngredient} representing all the passed in ingredients.
     */
    @ZenCodeType.StaticExpansionMethod
    public static GasStackIngredient createMulti(GasStackIngredient... ingredients) {
        return CrTIngredientHelper.createMulti("GasStackIngredients", IngredientCreatorAccess.gas(), ingredients);
    }

    /**
     * Converts this {@link GasStackIngredient} into JSON ({@link IData}).
     *
     * @return {@link GasStackIngredient} as JSON.
     */
    @ZenCodeType.Method
    @ZenCodeType.Caster(implicit = true)
    public static IData asIData(GasStackIngredient _this) {
        return JSONConverter.convert(_this.serialize());
    }

    /**
     * Checks if a given {@link ICrTGasStack} has a type match for this {@link GasStackIngredient}. Type matches ignore stack size.
     *
     * @param type Type to check for a match
     *
     * @return {@code true} if the type is supported by this {@link GasStackIngredient}.
     */
    @ZenCodeType.Method
    public static boolean testType(GasStackIngredient _this, ICrTGasStack type) {
        return _this.testType(type.getInternal());
    }

    /**
     * Checks if a given {@link ICrTGasStack} matches this {@link GasStackIngredient}. (Checks size for >=)
     *
     * @param stack Stack to check for a match
     *
     * @return {@code true} if the stack fulfills the requirements for this {@link GasStackIngredient}.
     */
    @ZenCodeType.Method
    public static boolean test(GasStackIngredient _this, ICrTGasStack stack) {
        return _this.test(stack.getInternal());
    }

    /**
     * Gets a list of valid instances for this {@link GasStackIngredient}, may not include all or may be empty depending on how complex the ingredient is as the internal
     * version is mostly used for JEI display purposes.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("representations")
    public static List<ICrTGasStack> getRepresentations(GasStackIngredient _this) {
        return CrTUtils.convertGas(_this.getRepresentations());
    }

    /**
     * OR's this {@link GasStackIngredient} with another {@link GasStackIngredient} to create a multi {@link GasStackIngredient}
     *
     * @param other {@link GasStackIngredient} to combine with.
     *
     * @return Multi {@link GasStackIngredient} that matches both the source {@link GasStackIngredient} and the OR'd {@link GasStackIngredient}.
     */
    @ZenCodeType.Method
    @ZenCodeType.Operator(ZenCodeType.OperatorType.OR)
    public static GasStackIngredient or(GasStackIngredient _this, GasStackIngredient other) {
        return IngredientCreatorAccess.gas().createMulti(_this, other);
    }
}