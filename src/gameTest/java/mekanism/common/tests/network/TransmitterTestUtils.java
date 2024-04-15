package mekanism.common.tests.network;

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
        Player player = helper.makeMockPlayer();
        player.setShiftKeyDown(true);
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
}