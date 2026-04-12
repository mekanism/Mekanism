package mekanism.common.item.loot;

import com.mojang.serialization.MapCodec;
import java.util.Set;
import mekanism.common.Mekanism;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MekanismLootFunctions {

    public static final DeferredRegister<MapCodec<? extends LootItemFunction>> REGISTER = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, Mekanism.MODID);
    public static final Set<ContextKey<?>> BLOCK_ENTITY_LOOT_CONTEXT = Set.of(LootContextParams.BLOCK_ENTITY);

    public static final DeferredHolder<MapCodec<? extends LootItemFunction>, MapCodec<PersonalStorageContentsLootFunction>> PERSONAL_STORAGE = REGISTER.register("personal_storage_contents", () -> PersonalStorageContentsLootFunction.MAP_CODEC);
}