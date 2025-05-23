package mekanism.api;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;

public enum WrenchResult {
    PROBED, //Swing only on client
    CONFIGURED,
    DISMANTLED,
    ROTATED,
    EMPTIED,
    PASS,
    NOT_ALLOWED,
    RADIOACTIVE;

    public InteractionResult getInteractionResult(boolean isClientSide) {
        return switch (this) {
            case PROBED -> InteractionResult.sidedSuccess(isClientSide);
            case CONFIGURED, DISMANTLED, ROTATED, EMPTIED -> InteractionResult.SUCCESS;
            case PASS -> InteractionResult.PASS;
            case NOT_ALLOWED, RADIOACTIVE -> InteractionResult.FAIL;
        };
    }

    public ItemInteractionResult getItemInteractionResult() {
        return switch (this) {
            case PASS -> ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            case NOT_ALLOWED, RADIOACTIVE -> ItemInteractionResult.FAIL;
            default -> ItemInteractionResult.SUCCESS;
        };
    }
}