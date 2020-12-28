package mekanism.common.integration.computercraft.peripherals;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.matrix.MatrixMultiblockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InductionMatrixPeripheral implements IPeripheral {
    private MatrixMultiblockData matrix;

    public InductionMatrixPeripheral(MatrixMultiblockData matrix) {
        this.matrix = matrix;

    }

    public MatrixMultiblockData getMatrix() {
        return matrix;
    }

    @LuaFunction
    public final Long getStoredEnergy(){
        return matrix.getEnergy().multiply(MekanismConfig.general.TO_FORGE.get()).longValue();
    }

    @LuaFunction
    public final Long getMaxEnergy(){
        return matrix.getEnergyContainer().getMaxEnergy().multiply(MekanismConfig.general.TO_FORGE.get()).longValue();
    }

    @LuaFunction
    public final Long getInputRate(){
        return matrix.getLastInput().multiply(MekanismConfig.general.TO_FORGE.get()).longValue();
    }

    @LuaFunction
    public final Long getOutputRate(){
        return matrix.getLastOutput().multiply(MekanismConfig.general.TO_FORGE.get()).longValue();
    }

    @LuaFunction
    public final Long getTransferCap(){
        return matrix.getTransferCap().multiply(MekanismConfig.general.TO_FORGE.get()).longValue();
    }

    @LuaFunction
    public final Long getStorageCap(){
        return matrix.getStorageCap().multiply(MekanismConfig.general.TO_FORGE.get()).longValue();
    }

    @NotNull
    @Override
    public String getType() {
        return "induction_matrix";
    }

    @Override
    public boolean equals(@Nullable IPeripheral iPeripheral) {
        if(iPeripheral instanceof InductionMatrixPeripheral){
            InductionMatrixPeripheral inductionMatrixPeripheral = (InductionMatrixPeripheral) iPeripheral;
            return inductionMatrixPeripheral.getMatrix().equals(matrix);
        } else return false;
    }
}
