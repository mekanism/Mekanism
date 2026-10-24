package mekanism.api.recipes.book;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.function.IntFunction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter.Provider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.references.BlockItemIds;
import net.minecraft.references.ItemIds;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import org.jspecify.annotations.Nullable;

/// Recipe book categories for smelting recipes.
///
/// @see net.minecraft.world.item.crafting.CookingBookCategory
/// @since 10.8.0
public enum SmeltingBookCategory implements MekanismBookCategory {
    FOOD(CookingBookCategory.FOOD, RecipeBookCategories.FURNACE_FOOD, ItemIds.PORKCHOP, null),
    //TODO: If we end up adding support for blasting at some point, vanilla's recipe book uses different icons for blasting blocks and misc
    BLOCKS(CookingBookCategory.BLOCKS, RecipeBookCategories.FURNACE_BLOCKS, BlockItemIds.STONE.item(), null),
    MISC(CookingBookCategory.MISC, RecipeBookCategories.FURNACE_MISC, ItemIds.LAVA_BUCKET, ItemIds.EMERALD);

    public static final Codec<SmeltingBookCategory> CODEC = StringRepresentable.fromEnum(SmeltingBookCategory::values);
    public static final IntFunction<SmeltingBookCategory> BY_ID = ByIdMap.continuous(SmeltingBookCategory::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, SmeltingBookCategory> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, SmeltingBookCategory::ordinal);

    private final RecipeBookCategory category;
    private final Holder<Item> primaryIcon;
    @Nullable
    private final Holder<Item> secondaryIcon;
    private final String name;

    SmeltingBookCategory(CookingBookCategory cookingBookCategory, RecipeBookCategory recipeBookCategory, ResourceKey<Item> primaryIcon, @Nullable ResourceKey<Item> secondaryIcon) {
        this.name = cookingBookCategory.getSerializedName();
        this.primaryIcon = MekanismBookCategory.createItem(primaryIcon);
        this.secondaryIcon = MekanismBookCategory.createItem(secondaryIcon);
        this.category = recipeBookCategory;
    }

    /// Converts a vanilla cooking book category into a mekanism smelting book category.
    public static SmeltingBookCategory fromVanilla(CookingBookCategory cookingBookCategory) {
        return switch (cookingBookCategory) {
            case FOOD -> FOOD;
            case BLOCKS -> BLOCKS;
            case MISC -> MISC;
        };
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    @Override
    public RecipeBookCategory toRecipeBookCategory() {
        return category;
    }

    @Override
    public Object getPrimaryIcon(Provider holderLookup) {
        return new ItemStack(primaryIcon);
    }

    @Override
    public Optional<Object> getSecondaryIcon(Provider holderLookup) {
        return secondaryIcon == null ? Optional.empty() : Optional.of(new ItemStack(secondaryIcon));
    }

    /// Recipe book info for crushing recipes.
    ///
    /// @param category Recipe book category.
    /// @param group    Recipe group.
    ///
    /// @see net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo
    /// @see net.minecraft.world.item.crafting.AbstractCookingRecipe.CookingBookInfo
    public record SmeltingBookInfo(SmeltingBookCategory category, String group) implements Recipe.BookInfo<SmeltingBookCategory> {

        public static final MapCodec<SmeltingBookInfo> MAP_CODEC = Recipe.BookInfo.mapCodec(CODEC, MISC, SmeltingBookInfo::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, SmeltingBookInfo> STREAM_CODEC = Recipe.BookInfo.streamCodec(SmeltingBookCategory.STREAM_CODEC, SmeltingBookInfo::new);
    }
}