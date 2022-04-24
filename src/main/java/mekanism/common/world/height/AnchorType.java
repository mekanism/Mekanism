package mekanism.common.world.height;

import net.minecraft.world.level.levelgen.WorldGenerationContext;

public enum AnchorType {
    ABSOLUTE((context, value) -> value),
    ABOVE_BOTTOM((context, value) -> context.getMinGenY() + value),
    BELOW_TOP((context, value) -> context.getGenDepth() - 1 + context.getMinGenY() - value);

    private final YResolver yResolver;

    AnchorType(YResolver yResolver) {
        this.yResolver = yResolver;
    }

    public int resolveY(WorldGenerationContext context, int value) {
        return yResolver.resolve(context, value);
    }

    @FunctionalInterface
    private interface YResolver {

        int resolve(WorldGenerationContext context, int value);
    }
}