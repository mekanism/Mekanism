package mekanism.api;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Expose this as a capability on your TileEntity to allow your block to be modified by a Configurator.
 *
 * @author aidancbrady
 */
@FunctionalInterface
public interface IConfigurable {

    /**
     * Called when a player attempts to configure a thing with a Configurator.
     *
     * @param context the context corresponding to the configuration attempt
     *
     * @return action that was performed
     *
     * @apiNote Only called from the server
     */
    WrenchResult onConfigure(ConfigureContext context);

    enum ConfigureAction {
        SENSE("toggle redstone sensitivity"),
        PROBE("probe stuff and diagnose multiblocks"),
        CONNECT("connect/disconnect plumbing"),
        COLOR("color logistical transporters"),
        ACTIVATE("activate appliances"),
        RESET("reset block configuration"),
        CYCLE("cycle device mode (set to next)"),
        PLUMB("push/pull junctions and connect/disconnect plumbing");

        public final String displayName;

        ConfigureAction(String displayName) {
            this.displayName = displayName;
        }
    }

    enum ConfigureActions {
        PROBE_OR_COLOR(ConfigureAction.PROBE, ConfigureAction.COLOR),
        PLUMB_OR_COLOR(ConfigureAction.PLUMB, ConfigureAction.COLOR),
        PLUMB_ONLY(ConfigureAction.PLUMB, null);

        public final @Nullable ConfigureAction regularAction, sneakAction;

        ConfigureActions(@Nullable ConfigureAction regularAction, @Nullable ConfigureAction sneakAction) {
            this.regularAction = regularAction;
            this.sneakAction = sneakAction;
        }
    }

    record ConfigureContext(ConfigureAction action, Player player, Direction side, ItemStack toolStack) {

        public ConfigureContext(ConfigureActions actions, Player player, Direction side, ItemStack toolStack) {
            this(player.isShiftKeyDown() ? actions.sneakAction : actions.regularAction, player, side, toolStack);
        }

        public boolean is(ConfigureAction action) {
            return this.action == action;
        }

        public boolean is(ConfigureAction actionA, ConfigureAction actionB) {
            return is(actionA) || is(actionB);
        }

        public Item toolItem() {
            return toolStack.getItem();
        }

        public WrenchResult chain(IConfigurable A, IConfigurable B) {
            final WrenchResult result = A.onConfigure(this);
            return result != WrenchResult.PASS ? result : B.onConfigure(this);
        }

        public WrenchResult chain(IConfigurable A, IConfigurable B, IConfigurable C) {
            final WrenchResult result = chain(A, B);
            return result != WrenchResult.PASS ? result : C.onConfigure(this);
        }
    }
}