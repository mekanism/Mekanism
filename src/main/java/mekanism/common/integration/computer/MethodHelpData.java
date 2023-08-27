package mekanism.common.integration.computer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record MethodHelpData(String methodName, @Nullable List<Param> params, Returns returns, @Nullable String description, @Nullable MethodRestriction restriction) {

    public static MethodHelpData from(BoundMethodHolder.BoundMethodData<?> data) {
        return from(data.method());
    }

    public static MethodHelpData from(ComputerMethodFactory.MethodData<?> data) {
        return from(data.name(), data.argumentNames(), data.argClasses(), data.returnType(), data.methodDescription(), data.restriction());
    }

    @NotNull
    private static MethodHelpData from(String methodName, String[] argumentNames, Class<?>[] argumentClasses, Class<?> returnType, @Nullable String methodDescription, @Nullable MethodRestriction restriction) {
        List<Param> params = new ArrayList<>();
        for (int i = 0; i < argumentNames.length; i++) {
            Class<?> argClass = argumentClasses[i];
            params.add(new Param(argumentNames[i], getHumanType(argClass), argClass, getEnumConstantNames(argClass)));
        }

        Returns returns = returnType != void.class ? new Returns(getHumanType(returnType), returnType, getEnumConstantNames(returnType)) : Returns.NOTHING;

        return new MethodHelpData(methodName, params.isEmpty() ? null : params, returns, methodDescription, restriction);
    }

    @NotNull
    private static String getHumanType(Class<?> type) {
        Class<?> convertedType = BaseComputerHelper.convertType(type);
        return convertedType == Map.class ? "Table" : convertedType.getSimpleName();
    }

    @SuppressWarnings("unchecked")
    private static List<String> getEnumConstantNames(Class<?> argClass) {
        if (!Enum.class.isAssignableFrom(argClass)) {
            return null;
        }
        Enum<?>[] enumConstants = ((Class<? extends Enum<?>>) argClass).getEnumConstants();
        return Arrays.stream(enumConstants).map(Enum::name).toList();
    }

    public record Param(String name, String type, Class<?> javaType, @Nullable List<String> values){}

    public record Returns(String type, Class<?> javaType, @Nullable List<String> values){
        public static final Returns NOTHING = new Returns("Nothing", void.class, null);
    }
}
