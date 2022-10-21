package mekanism.additions.common;

import java.util.function.Consumer;
import mekanism.additions.common.advancements.AdditionsAdvancements;
import mekanism.additions.common.registries.AdditionsBlocks;
import mekanism.additions.common.registries.AdditionsEntityTypes;
import mekanism.additions.common.registries.AdditionsItems;
import mekanism.api.datagen.recipe.RecipeCriterion;
import mekanism.api.providers.IEntityTypeProvider;
import mekanism.api.text.EnumColor;
import mekanism.common.advancements.BaseAdvancementProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.DamagePredicate;
import net.minecraft.advancements.critereon.EntityHurtPlayerTrigger;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.KilledTrigger;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

public class AdditionsAdvancementProvider extends BaseAdvancementProvider {

    public AdditionsAdvancementProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, existingFileHelper, MekanismAdditions.MODID);
    }

    @Override
    protected void registerAdvancements(@NotNull Consumer<Advancement> consumer) {
        advancement(AdditionsAdvancements.BALLOON)
              .display(AdditionsItems.BALLOONS.get(EnumColor.AQUA), FrameType.TASK, false)
              .addCriterion("balloon", hasItems(AdditionsTags.Items.BALLOONS))
              .save(consumer);
        advancement(AdditionsAdvancements.POP_POP)
              .display(AdditionsItems.BALLOONS.get(EnumColor.RED), null, FrameType.GOAL, true, false, true)
              .addCriterion("pop", kill(AdditionsEntityTypes.BALLOON))
              .save(consumer);
        advancement(AdditionsAdvancements.GLOW_IN_THE_DARK)
              .display(AdditionsBlocks.GLOW_PANELS.get(EnumColor.ORANGE), FrameType.TASK, false)
              .addCriterion("glow_panel", hasItems(AdditionsTags.Items.GLOW_PANELS))
              .save(consumer);
        advancement(AdditionsAdvancements.HURT_BY_BABIES)
              .display(Items.CREEPER_HEAD, null, FrameType.GOAL, true, true, true)
              .andCriteria(damagedCriterion(AdditionsEntityTypes.BABY_CREEPER),
                    damagedCriterion(AdditionsEntityTypes.BABY_ENDERMAN),
                    damagedCriterion(AdditionsEntityTypes.BABY_SKELETON),
                    damagedCriterion(AdditionsEntityTypes.BABY_STRAY),
                    damagedCriterion(AdditionsEntityTypes.BABY_WITHER_SKELETON)
              ).save(consumer);
        advancement(AdditionsAdvancements.NOT_THE_BABIES)
              .display(Items.WITHER_SKELETON_SKULL, FrameType.GOAL, false)
              .orCriteria(killCriterion(AdditionsEntityTypes.BABY_CREEPER),
                    killCriterion(AdditionsEntityTypes.BABY_ENDERMAN),
                    killCriterion(AdditionsEntityTypes.BABY_SKELETON),
                    killCriterion(AdditionsEntityTypes.BABY_STRAY),
                    killCriterion(AdditionsEntityTypes.BABY_WITHER_SKELETON)
              ).save(consumer);
    }

    private RecipeCriterion killCriterion(IEntityTypeProvider entityTypeProvider) {
        return new RecipeCriterion(entityTypeProvider.getName(), kill(entityTypeProvider));
    }

    private CriterionTriggerInstance kill(IEntityTypeProvider entityTypeProvider) {
        return KilledTrigger.TriggerInstance.playerKilledEntity(EntityPredicate.Builder.entity().of(entityTypeProvider.getEntityType()));
    }

    private RecipeCriterion damagedCriterion(IEntityTypeProvider entityTypeProvider) {
        return new RecipeCriterion(entityTypeProvider.getName(), damaged(entityTypeProvider));
    }

    private CriterionTriggerInstance damaged(IEntityTypeProvider entityTypeProvider) {
        //Damaged by entity and not blocked
        return EntityHurtPlayerTrigger.TriggerInstance.entityHurtPlayer(DamagePredicate.Builder.damageInstance()
              .sourceEntity(EntityPredicate.Builder.entity().of(entityTypeProvider.getEntityType()).build())
              .blocked(false)
        );
    }
}