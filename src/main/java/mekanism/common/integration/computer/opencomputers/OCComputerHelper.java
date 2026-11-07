package mekanism.common.integration.computer.opencomputers;

import java.util.Map;
import li.cil.oc.api.machine.Arguments;
import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import org.jetbrains.annotations.NotNull;

final class OCComputerHelper extends BaseComputerHelper {

    private final Arguments arguments;

    OCComputerHelper(Arguments arguments) {
        this.arguments = arguments;
    }

    @Override
    public boolean getBoolean(int param) throws ComputerException {
        return arguments.checkBoolean(param);
    }

    @Override
    public byte getByte(int param) throws ComputerException {
        return (byte) arguments.checkInteger(param);
    }

    @Override
    public short getShort(int param) throws ComputerException {
        return (short) arguments.checkInteger(param);
    }

    @Override
    public int getInt(int param) throws ComputerException {
        return arguments.checkInteger(param);
    }

    @Override
    public long getLong(int param) throws ComputerException {
        return arguments.checkLong(param);
    }

    @Override
    public char getChar(int param) throws ComputerException {
        return arguments.checkString(param).charAt(0);
    }

    @Override
    public float getFloat(int param) throws ComputerException {
        return (float) arguments.checkDouble(param);
    }

    @Override
    public double getDouble(int param) throws ComputerException {
        return arguments.checkDouble(param);
    }

    @Override
    @NotNull
    public String getString(int param) throws ComputerException {
        return arguments.checkString(param);
    }

    @Override
    @NotNull
    public Map<?, ?> getMap(int param) throws ComputerException {
        return arguments.checkTable(param);
    }
}
