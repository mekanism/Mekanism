package mekanism.common.capabilities.radiation.item;

import java.util.Objects;
import java.util.function.DoubleSupplier;
import mekanism.api.radiation.capability.IRadiationShielding;

public class RadiationShieldingHandler implements IRadiationShielding {

    public static RadiationShieldingHandler create(double radiationShielding) {
        //TODO - 1.20.2: Validate radiation shielding value?
        return create(() -> radiationShielding);
    }

    public static RadiationShieldingHandler create(DoubleSupplier shieldingFunction) {
        Objects.requireNonNull(shieldingFunction, "Shielding function cannot be null");
        return new RadiationShieldingHandler(shieldingFunction);
    }

    private final DoubleSupplier shieldingFunction;

    private RadiationShieldingHandler(DoubleSupplier shieldingFunction) {
        this.shieldingFunction = shieldingFunction;
    }

    @Override
    public double getRadiationShielding() {
        return shieldingFunction.getAsDouble();
    }
}