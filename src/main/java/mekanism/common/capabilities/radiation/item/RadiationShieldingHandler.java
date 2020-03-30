package mekanism.common.capabilities.radiation.item;

import java.util.function.ToDoubleFunction;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.ItemCapabilityWrapper.ItemCapability;
import mekanism.common.content.radiation.IRadiationShielding;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;

public class RadiationShieldingHandler extends ItemCapability implements IRadiationShielding {

    private ToDoubleFunction<ItemStack> shieldingFunction;

    public static RadiationShieldingHandler create(ToDoubleFunction<ItemStack> shieldingFunction) {
        RadiationShieldingHandler handler = new RadiationShieldingHandler();
        handler.shieldingFunction = shieldingFunction;
        return handler;
    }

    @Override
    public boolean canProcess(Capability<?> capability) {
        return capability == Capabilities.RADIATION_SHIELDING_CAPABILITY;
    }

    @Override
    public double getRadiationShielding() {
        return shieldingFunction.applyAsDouble(getStack());
    }
}
