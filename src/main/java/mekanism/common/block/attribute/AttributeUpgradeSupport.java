package mekanism.common.block.attribute;

import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.api.Upgrade;

public record AttributeUpgradeSupport(@Nonnull Set<Upgrade> supportedUpgrades) implements Attribute {
}
