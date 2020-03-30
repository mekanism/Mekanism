package mekanism.common.capabilities.radiation.item;

import java.util.function.Function;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.ItemCapabilityWrapper.ItemCapability;
import mekanism.common.content.radiation.IRadiationShielding;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;

public class RadiationShieldingHandler extends ItemCapability implements IRadiationShielding {

    private Function<ItemStack, Double> shieldingFunction;

    public static RadiationShieldingHandler create(Function<ItemStack, Double> shieldingFunction) {
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
        return shieldingFunction.apply(getStack());
    }
}
