package mekanism.common.integration.computer.computercraft;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.MethodResult;
import mekanism.api.math.FloatingLong;
import mekanism.common.content.filter.IFilter;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.FancyComputerHelper;
import mekanism.common.integration.computer.SpecialConverters;
import org.jetbrains.annotations.Nullable;

public class CCComputerHelper implements FancyComputerHelper {

    private final IArguments arguments;

    public CCComputerHelper(IArguments arguments){
        this.arguments = arguments;
    }

    @Override
    public <T extends Enum<T>> T getEnum(int param, Class<T> enumClazz) throws ComputerException {
        try {
            return arguments.getEnum(param, enumClazz);
        } catch (LuaException e) {
            throw new ComputerException(e);
        }
    }

    @Override
    public boolean getBool(int param) throws ComputerException {
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
    public FloatingLong getFloatingLong(int param) throws ComputerException {
        try {
            Object opt = arguments.get(param);
            if (opt instanceof String s) {
                return FloatingLong.parseFloatingLong(s);
            }
            double finiteDouble = arguments.getFiniteDouble(param);
            if (finiteDouble < 0) {
                return FloatingLong.ZERO;
            }
            return FloatingLong.createConst(finiteDouble);
        } catch (LuaException e) {
            throw new ComputerException(e);
        }
    }

    @Override
    public <FILTER extends IFilter<FILTER>> FILTER getFilter(int param, Class<FILTER> expectedType) throws ComputerException {
        try {
            return SpecialConverters.convertMapToFilter(expectedType, arguments.getTable(param));
        } catch (LuaException e) {
            throw new ComputerException(e);
        }
    }

    @Override
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

    @Override
    public Object result(Object rawResult) {
        return null;
    }

    @Override
    public Object result(@Nullable FloatingLong result) {
        return FancyComputerHelper.super.result(result);
    }
}
