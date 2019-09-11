package mekanism.api.infuse;

import javax.annotation.Nonnull;

/**
 * InfuseObject - an object associated with an ItemStack that can modify a Metallurgic Infuser's internal infuse.
 *
 * @author AidanBrady
 */
public class InfuseObject implements InfusionContainer {

    /**
     * The type of infuse this item stores
     */
    @Nonnull
    public final InfuseType type;

    /**
     * How much infuse this item stores
     */
    public final int stored;

    public InfuseObject(@Nonnull InfuseType infusion, int i) {
        type = infusion;
        stored = i;
    }

    //TODO: Why is this nullable when it should be Nonnull based on other spots (it has to do with InfusionContainer being Nullable)
    @Nonnull
    @Override
    public InfuseType getType() {
        return type;
    }

    @Override
    public int getAmount() {
        return stored;
    }
}