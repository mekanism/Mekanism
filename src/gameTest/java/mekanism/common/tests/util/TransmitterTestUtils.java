package mekanism.common.tests.util;

import mekanism.api.tier.AlloyTier;
import mekanism.common.registries.MekanismItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;

public class TransmitterTestUtils {

    private TransmitterTestUtils() {
    }

    public static void useConfigurator(ExtendedGameTestHelper helper, int x, int y, int z, Direction side) {
        useConfigurator(helper, x, y, z, side, 1);
    }

    public static void useConfigurator(ExtendedGameTestHelper helper, int x, int y, int z, Direction side, int times) {
        useConfigurator(helper, x, y, z, side, times, true);
    }

    public static void useConfigurator(ExtendedGameTestHelper helper, int x, int y, int z, Direction side, boolean shift) {
        useConfigurator(helper, x, y, z, side, 1, shift);
    }

    public static void useConfigurator(ExtendedGameTestHelper helper, int x, int y, int z, Direction side, int times, boolean shift) {
        Player player = helper.makeMockPlayer();
        player.setShiftKeyDown(shift);
        BlockPos pos = new BlockPos(x, y, z);
        //Set the player's look and position as we need accurate information for configurator usage to be applied properly to transmitters
        Direction direction = side.getOpposite();
        player.setXRot(direction == Direction.DOWN ? 90 : direction == Direction.UP ? -90 : 0);
        float yRot = direction.toYRot();
        player.setYRot(yRot);
        player.setYHeadRot(yRot);
        player.setPos(helper.absolutePos(pos).getCenter().subtract(0.45 * direction.getStepX(), 0.45 * direction.getStepY() + player.getEyeHeight(), 0.45 * direction.getStepZ()));
        for (int i = 0; i < times; i++) {
            helper.useOn(pos, MekanismItems.CONFIGURATOR.getItemStack(), player, side);
        }
    }

    public static void applyAlloyUpgrade(ExtendedGameTestHelper helper, BlockPos relativePos, AlloyTier tier) {
        helper.useOn(relativePos, (switch (tier) {
            case INFUSED -> MekanismItems.INFUSED_ALLOY;
            case REINFORCED -> MekanismItems.REINFORCED_ALLOY;
            case ATOMIC -> MekanismItems.ATOMIC_ALLOY;
        }).getItemStack(), helper.makeMockPlayer(), Direction.UP);
    }
}