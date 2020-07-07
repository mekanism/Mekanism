package mekanism.common.loot.table;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.api.providers.IEntityTypeProvider;
import net.minecraft.data.loot.EntityLootTables;
import net.minecraft.entity.EntityType;
import net.minecraft.loot.LootTable;

public abstract class BaseEntityLootTables extends EntityLootTables {

    private final Set<EntityType<?>> knownEntityTypes = new ObjectOpenHashSet<>();

    @Override
    protected abstract void addTables();

    @Override
    protected void registerLootTable(@Nonnull EntityType<?> type, @Nonnull LootTable.Builder table) {
        //Overwrite the core register method to add to our list of known entity types
        //Note: This isn't the actual core method as that one takes a ResourceLocation, but all our things wil pass through this one
        super.registerLootTable(type, table);
        knownEntityTypes.add(type);
    }

    @Nonnull
    @Override
    protected Iterable<EntityType<?>> getKnownEntities() {
        return knownEntityTypes;
    }

    protected void registerLootTable(@Nonnull IEntityTypeProvider typeProvider, @Nonnull LootTable.Builder table) {
        registerLootTable(typeProvider.getEntityType(), table);
    }
}