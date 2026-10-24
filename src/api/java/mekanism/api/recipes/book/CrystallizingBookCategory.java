package mekanism.api.recipes.book;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.function.IntFunction;
import mekanism.api.recipes.MekanismRecipeTypes;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import org.jspecify.annotations.Nullable;

/// Recipe book categories for chemical crystallizing recipes.
///
/// @since 10.8.0
public enum CrystallizingBookCategory implements MekanismBookCategory {
    EFFICIENCY("efficiency", "chemical_crystallizer", null),
    MISC("misc", "chemical_crystallizer", null);

    public static final Codec<CrystallizingBookCategory> CODEC = StringRepresentable.fromEnum(CrystallizingBookCategory::values);
    public static final IntFunction<CrystallizingBookCategory> BY_ID = ByIdMap.continuous(CrystallizingBookCategory::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, CrystallizingBookCategory> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, CrystallizingBookCategory::ordinal);

    private final Holder<RecipeBookCategory> categoryHolder;
    private final Holder<Item> primaryIcon;
    @Nullable
    private final Holder<Item> secondaryIcon;
    private final String name;

    CrystallizingBookCategory(String name, String primaryMekIcon, @Nullable String secondaryMekIcon) {
        this(name, MekanismBookCategory.createMekanismItem(primaryMekIcon), MekanismBookCategory.createMekanismItem(secondaryMekIcon));
    }

    CrystallizingBookCategory(String name, Holder<Item> primaryIcon, @Nullable Holder<Item> secondaryIcon) {
        this.name = name;
        this.primaryIcon = primaryIcon;
        this.secondaryIcon = secondaryIcon;
        this.categoryHolder = MekanismBookCategory.createHolder(MekanismRecipeTypes.NAME_CRYSTALLIZING, this.name);
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    @Override
    public RecipeBookCategory toRecipeBookCategory() {
        return categoryHolder.value();
    }

    @Override
    public ItemStack getPrimaryIcon() {
        return new ItemStack(primaryIcon);
    }

    @Override
    public Optional<ItemStack> getSecondaryIcon() {
        return secondaryIcon == null ? Optional.empty() : Optional.of(new ItemStack(secondaryIcon));
    }

    /// Recipe book info for chemical crystallizing recipes.
    ///
    /// @param category Recipe book category.
    /// @param group    Recipe group.
    ///
    /// @see net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo
    public record CrystallizingBookInfo(CrystallizingBookCategory category, String group) implements Recipe.BookInfo<CrystallizingBookCategory> {

        public static final MapCodec<CrystallizingBookInfo> MAP_CODEC = Recipe.BookInfo.mapCodec(CODEC, MISC, CrystallizingBookInfo::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, CrystallizingBookInfo> STREAM_CODEC = Recipe.BookInfo.streamCodec(CrystallizingBookCategory.STREAM_CODEC, CrystallizingBookInfo::new);
    }
}