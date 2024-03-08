package mekanism.common.block.attribute;

import mekanism.api.tier.ITier;

public record AttributeTier<TIER extends ITier>(TIER tier) implements Attribute {
}