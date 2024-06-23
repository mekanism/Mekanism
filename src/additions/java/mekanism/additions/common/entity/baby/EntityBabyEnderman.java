package mekanism.additions.common.entity.baby;

import mekanism.additions.common.registries.AdditionsEntityTypes;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class EntityBabyEnderman extends EnderMan {

    public EntityBabyEnderman(EntityType<EntityBabyEnderman> type, Level world) {
        super(type, world);
        this.xpReward = (int) (this.xpReward * 2.5);
        AdditionsEntityTypes.setupBabyModifiers(this);
    }

    @Override
    public boolean isBaby() {
        return true;
    }

    @NotNull
    @Override
    public EntityDimensions getDefaultDimensions(@NotNull Pose pose) {
        //Note: We already have the age scale factored into the dimensions
        return getType().getDimensions();
    }
}