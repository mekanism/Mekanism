package mekanism.common.block.attribute;

import java.util.HashMap;
import java.util.Map;
import mekanism.api.tier.ITier;
import mekanism.common.MekanismLang;
import mekanism.common.content.blocktype.BlockType;
import mekanism.common.content.blocktype.BlockType.BlockTypeBuilder;

public class AttributeTier<TIER extends ITier> implements Attribute {

    private TIER tier;

    public AttributeTier(TIER tier) {
        this.tier = tier;
    }

    public TIER getTier() {
        return tier;
    }

    // TODO remove this, eventually we'll natively use BlockType in transmitters
    private static Map<ITier, BlockType> typeCache = new HashMap<>();
    public static <T extends ITier> BlockType getPassthroughType(T tier) {
        if (typeCache.containsKey(tier)) return typeCache.get(tier);
        BlockType type = BlockTypeBuilder.createBlock(MekanismLang.EMPTY).with(new AttributeTier<T>(tier)).build();
        typeCache.put(tier, type);
        return type;
    }
}
