package mekanism.common.block.attribute;

import java.util.Set;
import mekanism.api.Upgrade;
import org.jetbrains.annotations.NotNull;

public record AttributeUpgradeSupport(@NotNull Set<Upgrade> supportedUpgrades) implements Attribute {
}
