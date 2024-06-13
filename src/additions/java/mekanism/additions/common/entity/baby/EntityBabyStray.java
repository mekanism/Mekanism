package mekanism.additions.common.entity.baby;

import mekanism.additions.common.registries.AdditionsEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Stray;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;

public class EntityBabyStray extends Stray {

    //Copy of stray spawn restrictions
    public static boolean spawnRestrictions(EntityType<EntityBabyStray> type, ServerLevelAccessor world, MobSpawnType reason, BlockPos pos, RandomSource random) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());
        do {
            mutable.move(Direction.UP);
        } while (world.getBlockState(mutable).is(Blocks.POWDER_SNOW));
        if (checkMonsterSpawnRules(type, world, reason, pos, random)) {
            if (reason == MobSpawnType.SPAWNER) {
                return true;
            }
            mutable.move(Direction.DOWN);
            return world.canSeeSky(mutable);
        }
        return false;
    }

    public EntityBabyStray(EntityType<EntityBabyStray> type, Level world) {
        super(type, world);
        this.xpReward = (int) (this.xpReward * 2.5);
        AdditionsEntityTypes.setupBabyModifiers(this);
    }

    @Override
    public boolean isBaby() {
        return true;
    }
}