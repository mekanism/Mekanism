package mekanism.common.capabilities.proxy;

import mekanism.api.IConfigurable;
import mekanism.api.WrenchResult;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ProxyConfigurable extends ProxyHandler implements IConfigurable {

    private final ISidedConfigurable configurable;

    public ProxyConfigurable(ISidedConfigurable configurable, @Nullable Direction side) {
        super(side, null);
        this.configurable = configurable;
    }

    @Override
    public WrenchResult onSneakRightClick(Player player) {
        return readOnly || side == null ? WrenchResult.PASS : configurable.onSneakRightClick(player, side);
    }

    @Override
    public WrenchResult onRightClick(Player player) {
        return readOnly || side == null ? WrenchResult.PASS : configurable.onRightClick(player, side);
    }

    public interface ISidedConfigurable extends IConfigurable {

        WrenchResult onSneakRightClick(Player player, Direction side);

        @Override
        default WrenchResult onSneakRightClick(Player player) {
            return WrenchResult.PASS;
        }

        WrenchResult onRightClick(Player player, Direction side);

        @Override
        default WrenchResult onRightClick(Player player) {
            return WrenchResult.PASS;
        }
    }
}