package mekanism.common.tests.helpers;

import mekanism.common.entity.EntityFlame;
import mekanism.common.item.gear.ItemFlamethrower.FlamethrowerMode;
import mekanism.common.registries.MekanismAttachmentTypes;
import mekanism.common.registries.MekanismEntityTypes;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class FlamethrowerTestHelper extends MekGameTestHelper {

    public FlamethrowerTestHelper(GameTestInfo info) {
        super(info);
    }

    public ItemEntity fireAtItem(FlamethrowerMode mode, boolean aged, @Nullable Boolean destroyItems) {
        BlockPos pos = absolutePos(new BlockPos(2, 2, 2));
        //Similar to GameTestHelper#spawnItem, but slightly adjusted
        ItemEntity item = new ItemEntity(getLevel(), pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, new ItemStack(Items.COBBLESTONE, 3));
        item.setNoGravity(true);
        item.setDeltaMovement(Vec3.ZERO);
        if (aged) {
            item.tickCount = 5 * SharedConstants.TICKS_PER_SECOND + 1;
        }
        assertTrue(getLevel().addFreshEntity(item), "The dropped item should be spawned");

        EntityFlame flame = new EntityFlame(MekanismEntityTypes.FLAME.get(), getLevel());
        flame.setPos(item.position().add(-1, 0.1, 0));
        flame.setDeltaMovement(2, 0, 0);
        flame.setData(MekanismAttachmentTypes.FLAMETHROWER_MODE, mode);
        flame.OVERRIDE_FLAMETHROWER_DESTROY_ITEMS = destroyItems;
        assertTrue(getLevel().addFreshEntity(flame), "The flame should be spawned");
        flame.tick();
        assertTrue(flame.isRemoved(), "The flame should collide with the dropped item");
        return item;
    }
}