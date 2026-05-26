package mekanism.common.tile.base;

import net.minecraft.world.InteractionResult;

//TODO: Move this to a different package
public enum WrenchResult {
    DISMANTLED,
    SUCCESS,
    PASS,
    NO_SECURITY,
    RADIOACTIVE;

    public InteractionResult getInteractionResult() {
        return switch (this) {
            case PASS -> InteractionResult.PASS;
            case RADIOACTIVE -> InteractionResult.FAIL;
            default -> InteractionResult.SUCCESS;
        };
    }
}