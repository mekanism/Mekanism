package mekanism.additions.common;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import mekanism.additions.common.advancements.AdditionsAdvancements;
import mekanism.additions.common.registries.AdditionsBlocks;
import mekanism.additions.common.registries.AdditionsEntityTypes;
import mekanism.additions.common.registries.AdditionsItems;
import mekanism.api.datagen.recipe.RecipeCriterion;
import mekanism.api.text.EnumColor;
import mekanism.common.advancements.BaseAdvancementProvider;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.DamagePredicate;
import net.minecraft.advancements.critereon.EntityHurtPlayerTrigger;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.KilledTrigger;
import net.minecraft.advancements.critereon.KilledTrigger.TriggerInstance;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

public class AdditionsAdvancementProvider extends BaseAdvancementProvider {

    public AdditionsAdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, ExistingFileHelper existingFileHelper) {
        super(output, provider, existingFileHelper, MekanismAdditions.MODID);
    }

    @Override
    protected void registerAdvancements(@NotNull Consumer<AdvancementHolder> consumer) {
        advancement(AdditionsAdvancements.BALLOON)
              .display(AdditionsItems.BALLOONS.get(EnumColor.AQUA), AdvancementType.TASK, false)
              .addCriterion("balloon", hasItems(AdditionsTags.Items.BALLOONS))
              .save(consumer);
        advancement(AdditionsAdvancements.POP_POP)
              .display(AdditionsItems.BALLOONS.get(EnumColor.RED), null, AdvancementType.GOAL, true, false, true)
              .addCriterion("pop", kill(AdditionsEntityTypes.BALLOON))
              .save(consumer);
        advancement(AdditionsAdvancements.GLOW_IN_THE_DARK)
              .display(AdditionsBlocks.GLOW_PANELS.get(EnumColor.ORANGE), AdvancementType.TASK, false)
              .addCriterion("glow_panel", hasItems(AdditionsTags.Items.GLOW_PANELS))
              .save(consumer);
        advancement(AdditionsAdvancements.HURT_BY_BABIES)
              .display(Items.CREEPER_HEAD, null, AdvancementType.GOAL, true, true, true)
              .andCriteria(
                    damagedCriterion(AdditionsEntityTypes.BABY_BOGGED),
                    damagedCriterion(AdditionsEntityTypes.BABY_CREEPER),
                    damagedCriterion(AdditionsEntityTypes.BABY_ENDERMAN),
                    damagedCriterion(AdditionsEntityTypes.BABY_SKELETON),
                    damagedCriterion(AdditionsEntityTypes.BABY_STRAY),
                    damagedCriterion(AdditionsEntityTypes.BABY_WITHER_SKELETON)
              ).save(consumer);
        advancement(AdditionsAdvancements.NOT_THE_BABIES)
              .display(Items.WITHER_SKELETON_SKULL, AdvancementType.GOAL, false)
              .orCriteria(
                    killCriterion(AdditionsEntityTypes.BABY_BOGGED),
                    killCriterion(AdditionsEntityTypes.BABY_CREEPER),
                    killCriterion(AdditionsEntityTypes.BABY_ENDERMAN),
                    killCriterion(AdditionsEntityTypes.BABY_SKELETON),
                    killCriterion(AdditionsEntityTypes.BABY_STRAY),
                    killCriterion(AdditionsEntityTypes.BABY_WITHER_SKELETON)
              ).save(consumer);
    }

    private RecipeCriterion killCriterion(Holder<EntityType<?>> type) {
        return new RecipeCriterion(getName(type), kill(type));
    }

    private Criterion<TriggerInstance> kill(Holder<EntityType<?>> type) {
        return KilledTrigger.TriggerInstance.playerKilledEntity(EntityPredicate.Builder.entity().of(type.value()));
    }

    private RecipeCriterion damagedCriterion(Holder<EntityType<?>> type) {
        return new RecipeCriterion(getName(type), damaged(type));
    }

    private String getName(Holder<?> holder) {
        return Objects.requireNonNull(holder.getKey()).location().getPath();
    }

    private Criterion<EntityHurtPlayerTrigger.TriggerInstance> damaged(Holder<EntityType<?>> type) {
        //Damaged by entity and not blocked
        return EntityHurtPlayerTrigger.TriggerInstance.entityHurtPlayer(DamagePredicate.Builder.damageInstance()
              .sourceEntity(EntityPredicate.Builder.entity().of(type.value()).build())
              .blocked(false)
        );
    }
}