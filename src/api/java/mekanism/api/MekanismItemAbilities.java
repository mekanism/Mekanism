package mekanism.api;

import net.neoforged.neoforge.common.ItemAbility;

/**
 * Common placeholder of all the Item Abilities we create in Mekanism. It is still possible to just recreate the item abilities yourself to avoid referencing this class
 *
 * @since 10.7.0
 */
public final class MekanismItemAbilities {

    /**
     * Represents the action of a paxel digging.
     *
     * @apiNote This is only used by Mekanism: Tools.
     */
    public static final ItemAbility PAXEL_DIG = ItemAbility.get("paxel_dig");

    /**
     * Exposed by wrenches that can currently configure a block.
     *
     * @apiNote Configuring in this case means something different then {@link #WRENCH_DISMANTLE}, {@link #WRENCH_EMPTY}, or {@link #WRENCH_ROTATE}
     */
    public static final ItemAbility WRENCH_CONFIGURE = ItemAbility.get("wrench_configure");
    /**
     * Exposed by wrenches that can currently configure chemical properties for a block.
     */
    public static final ItemAbility WRENCH_CONFIGURE_CHEMICALS = ItemAbility.get("wrench_configure_chemicals");
    /**
     * Exposed by wrenches that can currently configure energy properties for a block.
     */
    public static final ItemAbility WRENCH_CONFIGURE_ENERGY = ItemAbility.get("wrench_configure_energy");
    /**
     * Exposed by wrenches that can currently configure fluid properties for a block.
     */
    public static final ItemAbility WRENCH_CONFIGURE_FLUIDS = ItemAbility.get("wrench_configure_fluids");
    /**
     * Exposed by wrenches that can currently configure heat properties for a block.
     */
    public static final ItemAbility WRENCH_CONFIGURE_HEAT = ItemAbility.get("wrench_configure_heat");
    /**
     * Exposed by wrenches that can currently configure item properties for a block.
     */
    public static final ItemAbility WRENCH_CONFIGURE_ITEMS = ItemAbility.get("wrench_configure_items");

    /**
     * Exposed by wrenches that can currently dismantle blocks.
     */
    public static final ItemAbility WRENCH_DISMANTLE = ItemAbility.get("wrench_dismantle");
    /**
     * Exposed by wrenches that can currently empty the contents of blocks.
     */
    public static final ItemAbility WRENCH_EMPTY = ItemAbility.get("wrench_empty");
    /**
     * Exposed by wrenches that can currently rotate blocks.
     */
    public static final ItemAbility WRENCH_ROTATE = ItemAbility.get("wrench_rotate");
}