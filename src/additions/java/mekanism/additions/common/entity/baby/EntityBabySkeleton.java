package mekanism.additions.common.entity.baby;

import mekanism.additions.common.config.MekanismAdditionsConfig;
import mekanism.additions.common.registries.AdditionsEntityTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public class EntityBabySkeleton extends Skeleton {

    public EntityBabySkeleton(EntityType<EntityBabySkeleton> type, Level world) {
        super(type, world);
        this.xpReward = (int) (this.xpReward * 2.5);
        //Make freezing a baby skeleton convert it to a baby stray instead
        freezingTracker.convertsTo = AdditionsEntityTypes.BABY_STRAY::get;
        AdditionsEntityTypes.setupBabyModifiers(this);
    }

    @Override
    public boolean isBaby() {
        return true;
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        //Note: We already have the age scale factored into the dimensions
        return getType().getDimensions();
    }

    @Override
    protected AbstractArrow getArrow(ItemStack arrow, float velocity, @Nullable ItemStack weapon) {
        AbstractArrow projectile = super.getArrow(arrow, velocity, weapon);
        projectile.setBaseDamage(projectile.baseDamage * MekanismAdditionsConfig.additions.babyArrowDamageMultiplier.get());
        return projectile;
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        super.populateDefaultEquipmentSlots(random, difficulty);
        //TODO - 26.3: Does this still exist for whatever part of vanilla we were mirroring?
        for (EquipmentSlot slot : EquipmentSlot.VALUES) {
            if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                setItemSlot(slot, ItemStack.EMPTY);
            }
        }
    }

    @Override
    public boolean is(EntityType<?> type) {
        return type == EntityTypes.SKELETON || super.is(type);
    }
}