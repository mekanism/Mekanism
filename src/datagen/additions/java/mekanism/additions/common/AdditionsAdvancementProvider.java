package mekanism.additions.common;

import java.util.function.Consumer;
import javax.annotation.Nonnull;
import mekanism.additions.common.advancements.AdditionsAdvancements;
import mekanism.additions.common.registries.AdditionsBlocks;
import mekanism.additions.common.registries.AdditionsEntityTypes;
import mekanism.additions.common.registries.AdditionsItems;
import mekanism.api.datagen.recipe.RecipeCriterion;
import mekanism.api.providers.IEntityTypeProvider;
import mekanism.api.text.EnumColor;
import mekanism.common.advancements.BaseAdvancementProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.KilledTrigger;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.data.ExistingFileHelper;

public class AdditionsAdvancementProvider extends BaseAdvancementProvider {

    public AdditionsAdvancementProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, existingFileHelper, MekanismAdditions.MODID);
    }

    @Override
    protected void registerAdvancements(@Nonnull Consumer<Advancement> consumer) {
        advancement(AdditionsAdvancements.BALLOON)
              .display(AdditionsItems.BALLOONS.get(EnumColor.AQUA), FrameType.TASK)
              .addCriterion("balloon", hasItems(AdditionsTags.Items.BALLOONS))
              .save(consumer);
        advancement(AdditionsAdvancements.POP_POP)
              .display(AdditionsItems.BALLOONS.get(EnumColor.RED), null, FrameType.GOAL, true, true, true)
              .addCriterion("pop", kill(AdditionsEntityTypes.BALLOON))
              .save(consumer);
        advancement(AdditionsAdvancements.GLOW_IN_THE_DARK)
              .display(AdditionsBlocks.GLOW_PANELS.get(EnumColor.ORANGE), FrameType.TASK)
              .addCriterion("glow_panel", hasItems(AdditionsTags.Items.GLOW_PANELS))
              .save(consumer);
        advancement(AdditionsAdvancements.NOT_THE_BABIES)
              .display(Items.WITHER_SKELETON_SKULL, FrameType.GOAL)
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

    private KilledTrigger.TriggerInstance kill(IEntityTypeProvider entityTypeProvider) {
        return KilledTrigger.TriggerInstance.playerKilledEntity(EntityPredicate.Builder.entity().of(entityTypeProvider.getEntityType()));
    }
}