package mekanism.common.capabilities.proxy;

import mekanism.api.IConfigurable;
import mekanism.common.capabilities.holder.IHolder;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public class ProxyConfigurable extends ProxyHandler<@Nullable IHolder> implements IConfigurable {

    private final ISidedConfigurable configurable;

    public ProxyConfigurable(@Nullable Direction side, ISidedConfigurable configurable) {
        super(side, null);
        this.configurable = configurable;
    }

    @Override
    public InteractionResult onSneakRightClick(Level level, Player player) {
        return readOnly || side == null ? InteractionResult.PASS : configurable.onSneakRightClick(level, player, side);
    }

    @Override
    public InteractionResult onRightClick(Level level, Player player) {
        return readOnly || side == null ? InteractionResult.PASS : configurable.onRightClick(level, player, side);
    }

    public interface ISidedConfigurable extends IConfigurable {

        InteractionResult onSneakRightClick(Level level, Player player, Direction side);

        @Override
        default InteractionResult onSneakRightClick(Level level, Player player) {
            return InteractionResult.PASS;
        }

        InteractionResult onRightClick(Level level, Player player, Direction side);

        @Override
        default InteractionResult onRightClick(Level level, Player player) {
            return InteractionResult.PASS;
        }
    }
}