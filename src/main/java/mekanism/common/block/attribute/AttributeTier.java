package mekanism.common.block.attribute;

import java.util.HashMap;
import java.util.Map;
import mekanism.api.tier.ITier;
import mekanism.common.MekanismLang;
import mekanism.common.content.blocktype.BlockType;
import mekanism.common.content.blocktype.BlockType.BlockTypeBuilder;

public record AttributeTier<TIER extends ITier>(TIER tier) implements Attribute {

    // TODO remove this, eventually we'll natively use BlockType in transmitters
    private static final Map<ITier, BlockType> typeCache = new HashMap<>();

    public static <T extends ITier> BlockType getPassthroughType(T tier) {
        return typeCache.computeIfAbsent(tier, t -> BlockTypeBuilder.createBlock(MekanismLang.EMPTY).with(new AttributeTier<>(t)).build());
    }
}