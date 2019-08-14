package mekanism.common.integration.crafttweaker.gas;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mekanism.gas.IGasDefinition")
public interface IGasDefinition {

    @ZenCodeType.Operator(ZenCodeType.OperatorType.MUL)
    IGasStack asStack(int mb);

    @ZenCodeType.Getter("NAME")
    String getName();

    @ZenCodeType.Getter("displayName")
    String getDisplayName();
}