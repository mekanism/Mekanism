package universalelectricity.implement;

import net.minecraft.src.ItemStack;

public interface IItemElectric extends IElectricityStorage, IVoltage
{
    public double onReceiveElectricity(double wattHourReceive, ItemStack itemStack);
    
    public double onUseElectricity(double wattHourRequest, ItemStack itemStack);
    
    public boolean canReceiveElectricity();
    
    public boolean canProduceElectricity();
    
    public double getTransferRate();   
}
