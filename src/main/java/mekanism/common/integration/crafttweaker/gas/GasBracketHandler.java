package mekanism.common.integration.crafttweaker.gas;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.BracketHandler;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IngredientAny;
import crafttweaker.zenscript.IBracketHandler;
import java.util.List;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import stanhebben.zenscript.compiler.IEnvironmentGlobal;
import stanhebben.zenscript.expression.ExpressionCallStatic;
import stanhebben.zenscript.expression.ExpressionString;
import stanhebben.zenscript.expression.partial.IPartialExpression;
import stanhebben.zenscript.parser.Token;
import stanhebben.zenscript.symbols.IZenSymbol;
import stanhebben.zenscript.type.natives.IJavaMethod;
import stanhebben.zenscript.util.ZenPosition;

@BracketHandler(priority = 100)
@ZenRegister
public class GasBracketHandler implements IBracketHandler {

    private final IZenSymbol symbolAny;
    private final IJavaMethod method;

    public GasBracketHandler() {
        this.symbolAny = CraftTweakerAPI.getJavaStaticFieldSymbol(IngredientAny.class, "INSTANCE");
        this.method = CraftTweakerAPI.getJavaMethod(GasBracketHandler.class, "getGas", String.class);
    }

    public static IGasStack getGas(String name) {
        Gas gas = GasRegistry.getGas(name);
        return gas == null ? null : new CraftTweakerGasStack(new GasStack(gas, 1));
    }

    @Override
    public IZenSymbol resolve(IEnvironmentGlobal environment, List<Token> tokens) {
        if (tokens.size() == 1 && tokens.get(0).getValue().equals("*")) {
            return symbolAny;
        }
        if (tokens.size() > 2 && tokens.get(0).getValue().equals("gas") && tokens.get(1).getValue().equals(":")) {
            return find(environment, tokens, 2, tokens.size());
        }
        return null;
    }

    private IZenSymbol find(IEnvironmentGlobal environment, List<Token> tokens, int startIndex, int endIndex) {
        StringBuilder valueBuilder = new StringBuilder();
        for (int i = startIndex; i < endIndex; i++) {
            Token token = tokens.get(i);
            valueBuilder.append(token.getValue());
        }

        Gas gas = GasRegistry.getGas(valueBuilder.toString());
        return gas == null ? null : new GasReferenceSymbol(environment, valueBuilder.toString());
    }

    private class GasReferenceSymbol implements IZenSymbol {

        private final IEnvironmentGlobal environment;
        private final String name;

        public GasReferenceSymbol(IEnvironmentGlobal environment, String name) {
            this.environment = environment;
            this.name = name;
        }

        @Override
        public IPartialExpression instance(ZenPosition zenPosition) {
            return new ExpressionCallStatic(zenPosition, environment, method, new ExpressionString(zenPosition, name));
        }
    }
}