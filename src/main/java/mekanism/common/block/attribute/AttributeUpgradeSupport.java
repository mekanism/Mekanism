package mekanism.common.block.attribute;

import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.api.Upgrade;

public class AttributeUpgradeSupport implements Attribute {

    private final Set<Upgrade> supportedUpgrades;

    public AttributeUpgradeSupport(Set<Upgrade> supportedUpgrades) {
        this.supportedUpgrades = supportedUpgrades;
    }

    @Nonnull
    public Set<Upgrade> getSupportedUpgrades() {
        return supportedUpgrades;
    }
}
