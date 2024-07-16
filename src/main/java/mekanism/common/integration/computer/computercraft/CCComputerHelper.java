package mekanism.common.integration.computer.computercraft;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.MethodResult;
import java.util.Map;
import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import org.jetbrains.annotations.NotNull;

public class CCComputerHelper extends BaseComputerHelper {

    private final IArguments arguments;

    public CCComputerHelper(IArguments arguments) {
        this.arguments = arguments;
    }

    @Override
    @NotNull
    public <T extends Enum<T>> T getEnum(int param, Class<T> enumClazz) throws ComputerException {
        try {
            return arguments.getEnum(param, enumClazz);
        } catch (LuaException e) {
            throw new ComputerException(e);
        }
    }

    @Override
    public boolean getBoolean(int param) throws ComputerException {
        try {
            return arguments.getBoolean(param);
        } catch (LuaException e) {
            throw new ComputerException(e);
        }
    }

    @Override
    public byte getByte(int param) throws ComputerException {
        try {
            return (byte) arguments.getInt(param);
        } catch (LuaException e) {
            throw new ComputerException(e);
        }
    }

    @Override
    public short getShort(int param) throws ComputerException {
        try {
            return (short) arguments.getInt(param);
        } catch (LuaException e) {
            throw new ComputerException(e);
        }
    }

    @Override
    public int getInt(int param) throws ComputerException {
        try {
            return arguments.getInt(param);
        } catch (LuaException e) {
            throw new ComputerException(e);
        }
    }

    @Override
    public long getLong(int param) throws ComputerException {
        try {
            return arguments.getLong(param);
        } catch (LuaException e) {
            throw new ComputerException(e);
        }
    }

    @Override
    public char getChar(int param) throws ComputerException {
        try {
            return arguments.getString(param).charAt(0);
        } catch (LuaException e) {
            throw new ComputerException(e);
        }
    }

    @Override
    public float getFloat(int param) throws ComputerException {
        try {
            return (float) arguments.getDouble(param);
        } catch (LuaException e) {
            throw new ComputerException(e);
        }
    }

    @Override
    public double getDouble(int param) throws ComputerException {
        try {
            return arguments.getDouble(param);
        } catch (LuaException e) {
            throw new ComputerException(e);
        }
    }

    @Override
    @NotNull
    public Map<?, ?> getMap(int param) throws ComputerException {
        try {
            return arguments.getTable(param);
        } catch (LuaException e) {
            throw new ComputerException(e);
        }
    }

    @Override
    @NotNull
    public String getString(int param) throws ComputerException {
        try {
            return arguments.getString(param);
        } catch (LuaException e) {
            throw new ComputerException(e);
        }
    }

    @Override
    public Object voidResult() {
        return MethodResult.of();
    }

    /*
    @Override
    public <FILTER extends IFilter<FILTER>> @Nullable FILTER getFilter(int param, Class<FILTER> expectedType) throws ComputerException {
        Object value;
        try {
            value = arguments.get(param);
        } catch (LuaException e) {
            throw new ComputerException(e);
        }
        if (value instanceof CCFilterWrapper<?> filterWrapper) {
            return filterWrapper.getAs(expectedType);
        }
        return super.getFilter(param, expectedType);
    }

    @Override
    public Object convert(@Nullable MinerFilter<?> minerFilter) {
        return minerFilter != null ? new CCFilterWrapper<>(minerFilter) : null;
    }

    @Override
    public Object convert(@Nullable SorterFilter<?> sorterFilter) {
        return sorterFilter != null ? new CCFilterWrapper<>(sorterFilter) : null;
    }

    @Override
    public Object convert(@Nullable QIOFilter<?> qioFilter) {
        return qioFilter != null ? new CCFilterWrapper<>(qioFilter) : null;
    }

    @Override
    public Object convert(@Nullable OredictionificatorFilter<?, ?, ?> filter) {
        return filter != null ? new CCFilterWrapper<>(filter) : null;
    }*/
}
