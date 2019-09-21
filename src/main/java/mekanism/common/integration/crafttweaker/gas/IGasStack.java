package mekanism.common.integration.crafttweaker.gas;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IIngredient;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.gas.GasStack;
import org.openzen.zencode.java.ZenCodeType;

//TODO: Move this to our API package?
// Also create a bracket handler for metallurgic infusion type
@ZenRegister
@ZenCodeType.Name("mekanism.gas.IGasStack")
public interface IGasStack extends IIngredient {
    //TODO: Should this even be extending IIngredient

    @ZenCodeType.Getter("definition")
    IGasDefinition getDefinition();

    //TODO: Remove?
    @ZenCodeType.Getter("name")
    String getName();

    @ZenCodeType.Getter("displayName")
    String getDisplayName();

    @ZenCodeType.Operator(ZenCodeType.OperatorType.MUL)
    @ZenCodeType.Method
    IGasStack withAmount(int amount);

    @ZenCodeType.Getter("amount")
    int getAmount();

    @Nonnull
    GasStack getInternal();

    @ZenCodeType.Getter("gases")
    List<IGasStack> getGases();

    @ZenCodeType.Method
    boolean matches(IGasStack gasStack);
}