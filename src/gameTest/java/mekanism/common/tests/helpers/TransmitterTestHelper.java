package mekanism.common.tests.helpers;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.tier.AlloyTier;
import mekanism.common.registries.MekanismItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.world.entity.player.Player;

@NothingNullByDefault
public class TransmitterTestHelper extends MekGameTestHelper {

    public TransmitterTestHelper(GameTestInfo info) {
        super(info);
    }

    public void useConfigurator(int x, int y, int z, Direction side) {
        useConfigurator(x, y, z, side, 1);
    }

    public void useConfigurator(int x, int y, int z, Direction side, int times) {
        useConfigurator(x, y, z, side, times, true);
    }

    public void useConfigurator(int x, int y, int z, Direction side, boolean shift) {
        useConfigurator(x, y, z, side, 1, shift);
    }

    public void useConfigurator(int x, int y, int z, Direction side, int times, boolean shift) {
        Player player = makeMockPlayerLookingAt(x, y, z, side.getOpposite());
        player.setShiftKeyDown(shift);
        useOn(new BlockPos(x, y, z), MekanismItems.CONFIGURATOR.asStack(), player, side, times);
    }

    public void applyAlloyUpgrade(BlockPos relativePos, AlloyTier tier) {
        //Note: We don't need to bother making the fake player look at the block as it isn't relevant for alloy upgrading
        useOn(relativePos, (switch (tier) {
            case INFUSED -> MekanismItems.INFUSED_ALLOY;
            case REINFORCED -> MekanismItems.REINFORCED_ALLOY;
            case ATOMIC -> MekanismItems.ATOMIC_ALLOY;
        }).asStack(), makeMockPlayer(), Direction.UP);
    }
}