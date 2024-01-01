package mekanism.common.integration.computer.opencomputers2;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import li.cil.oc2.api.bus.device.rpc.RPCInvocation;
import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class OC2ComputerHelper extends BaseComputerHelper {

    private final RPCInvocation invocation;

    public OC2ComputerHelper(RPCInvocation invocation) {
        this.invocation = invocation;
    }

    private JsonElement getParam(int param) throws ComputerException {
        JsonArray parameters = invocation.getParameters();
        if (parameters.size() <= param) {
            throw new ComputerException("Missing argument in position " + param);
        }
        return parameters.get(param);
    }

    @Override
    public boolean getBoolean(int param) throws ComputerException {
        return getParam(param).getAsBoolean();
    }

    @Override
    public byte getByte(int param) throws ComputerException {
        return getParam(param).getAsByte();
    }

    @Override
    public short getShort(int param) throws ComputerException {
        return getParam(param).getAsShort();
    }

    @Override
    public int getInt(int param) throws ComputerException {
        return getParam(param).getAsInt();
    }

    @Override
    public long getLong(int param) throws ComputerException {
        return getParam(param).getAsLong();
    }

    @Override
    public char getChar(int param) throws ComputerException {
        return getParam(param).getAsString().charAt(0);
    }

    @Override
    public float getFloat(int param) throws ComputerException {
        return getParam(param).getAsFloat();
    }

    @Override
    public double getDouble(int param) throws ComputerException {
        return getParam(param).getAsDouble();
    }

    @Override
    @NotNull
    public String getString(int param) throws ComputerException {
        return getParam(param).getAsString();
    }

    @Override
    @NotNull
    public Map<?, ?> getMap(int param) throws ComputerException {
        return getParam(param).getAsJsonObject().asMap();
    }
}
