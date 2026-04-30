package mekanism.api.recipes.ingredients;

import com.mojang.datafixers.util.Either;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.holdersets.OrHolderSet;
import org.jetbrains.annotations.ApiStatus;

/// Hack to be able to use this in Datagen. Don't use anywhere except for an input to Ingredient.
///
/// Overrides [#unwrap()] to pretend to be a tag holder, so that [net.minecraft.world.item.crafting.Ingredient] doesn't try to enumerate it
///
/// Remove when/if Neo fixes it
@ApiStatus.Internal
public class DataGenOnly_OrHolderSet extends OrHolderSet<Item> {
    public DataGenOnly_OrHolderSet(List<HolderSet<Item>> values) {
        super(values);
    }

    @SafeVarargs
    public DataGenOnly_OrHolderSet(HolderSet<Item>... values) {
        this(List.of(values));
    }

    @Override
    public Either<TagKey<Item>, List<Holder<Item>>> unwrap() {
        return Either.left(ItemTags.LOGS);
    }
}
