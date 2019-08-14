package mekanism.common.recipe.ingredients;

import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.Tag;

public class TagMekIngredient implements IMekanismIngredient<ItemStack> {

    private final Tag<Item> tag;

    public TagMekIngredient(@Nonnull Tag<Item> tag) {
        this.tag = tag;
    }

    @Nonnull
    @Override
    public List<ItemStack> getMatching() {
        //TODO: Should this cache the list
        return tag.getAllElements().stream().map(ItemStack::new).collect(Collectors.toList());
    }

    @Override
    public boolean contains(@Nonnull ItemStack stack) {
        return tag.contains(stack.getItem());
    }

    @Override
    public int hashCode() {
        return tag.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TagMekIngredient && tag.equals(((TagMekIngredient) obj).tag);
    }
}