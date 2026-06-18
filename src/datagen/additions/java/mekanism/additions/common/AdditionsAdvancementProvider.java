package mekanism.additions.common;

import java.util.Objects;
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
import net.minecraft.advancements.predicates.DamagePredicate;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.EntityHurtPlayerTrigger;
import net.minecraft.advancements.triggers.KilledTrigger;
import net.minecraft.advancements.triggers.KilledTrigger.TriggerInstance;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.references.BlockItemIds;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

public class AdditionsAdvancementProvider extends BaseAdvancementProvider {

    @Override
    public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> consumer) {
        HolderGetter<Item> items = registries.lookupOrThrow(Registries.ITEM);
        HolderGetter<EntityType<?>> entityTypeLookup = registries.lookupOrThrow(Registries.ENTITY_TYPE);
        advancement(AdditionsAdvancements.BALLOON)
              .display(AdditionsItems.BALLOONS.get(EnumColor.AQUA), AdvancementType.TASK, false)
              .addCriterion("balloon", hasItems(items, AdditionsTags.Items.BALLOONS))
              .save(consumer);
        advancement(AdditionsAdvancements.POP_POP)
              .display(AdditionsItems.BALLOONS.get(EnumColor.RED), null, AdvancementType.GOAL, true, false, true)
              .addCriterion("pop", kill(entityTypeLookup, AdditionsEntityTypes.BALLOON))
              .save(consumer);
        advancement(AdditionsAdvancements.GLOW_IN_THE_DARK)
              .display(AdditionsBlocks.GLOW_PANELS.get(EnumColor.ORANGE).getItemHolder(), AdvancementType.TASK, false)
              .addCriterion("glow_panel", hasItems(items, AdditionsTags.BlockItems.GLOW_PANELS.item()))
              .save(consumer);
        advancement(AdditionsAdvancements.HURT_BY_BABIES)
              .display(items, BlockItemIds.CREEPER_HEAD.item(), null, AdvancementType.GOAL, true, true, true)
              .andCriteria(AdditionsEntityTypes.BABIES.values().stream()
                    .map(baby -> damagedCriterion(entityTypeLookup, baby))
                    .toArray(RecipeCriterion[]::new)
              ).save(consumer);
        advancement(AdditionsAdvancements.NOT_THE_BABIES)
              .display(items, BlockItemIds.WITHER_SKELETON_SKULL.item(), AdvancementType.GOAL, false)
              .orCriteria(AdditionsEntityTypes.BABIES.values().stream()
                    .map(baby -> killCriterion(entityTypeLookup, baby))
                    .toArray(RecipeCriterion[]::new)
              ).save(consumer);
    }

    private RecipeCriterion killCriterion(HolderGetter<EntityType<?>> entityTypeLookup, Holder<EntityType<?>> type) {
        return new RecipeCriterion(getName(type), kill(entityTypeLookup, type));
    }

    private Criterion<TriggerInstance> kill(HolderGetter<EntityType<?>> entityTypeLookup, Holder<EntityType<?>> type) {
        return KilledTrigger.TriggerInstance.playerKilledEntity(EntityPredicate.Builder.entity().of(entityTypeLookup, type.value()));
    }

    private RecipeCriterion damagedCriterion(HolderGetter<EntityType<?>> entityTypeLookup, Holder<EntityType<?>> type) {
        return new RecipeCriterion(getName(type), damaged(entityTypeLookup, type));
    }

    private String getName(Holder<?> holder) {
        return Objects.requireNonNull(holder.getKey()).identifier().getPath();
    }

    private Criterion<EntityHurtPlayerTrigger.TriggerInstance> damaged(HolderGetter<EntityType<?>> entityTypeLookup, Holder<EntityType<?>> type) {
        //Damaged by entity and not blocked
        return EntityHurtPlayerTrigger.TriggerInstance.entityHurtPlayer(DamagePredicate.Builder.damageInstance()
              .sourceEntity(EntityPredicate.Builder.entity().of(entityTypeLookup, type.value()).build())
              .blocked(false)
        );
    }
}