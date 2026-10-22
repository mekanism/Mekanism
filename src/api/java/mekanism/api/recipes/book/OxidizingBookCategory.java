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

/// Recipe book categories for chemical oxidizing recipes.
///
/// @since 10.8.0
public enum OxidizingBookCategory implements MekanismBookCategory {
    MISC("misc", "chemical_oxidizer", null);

    public static final Codec<OxidizingBookCategory> CODEC = StringRepresentable.fromEnum(OxidizingBookCategory::values);
    public static final IntFunction<OxidizingBookCategory> BY_ID = ByIdMap.continuous(OxidizingBookCategory::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, OxidizingBookCategory> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, OxidizingBookCategory::ordinal);

    private final Holder<RecipeBookCategory> categoryHolder;
    private final Holder<Item> primaryIcon;
    @Nullable
    private final Holder<Item> secondaryIcon;
    private final String name;

    OxidizingBookCategory(String name, String primaryMekIcon, @Nullable String secondaryMekIcon) {
        this(name, MekanismBookCategory.createMekanismItem(primaryMekIcon), MekanismBookCategory.createMekanismItem(secondaryMekIcon));
    }

    OxidizingBookCategory(String name, Holder<Item> primaryIcon, @Nullable Holder<Item> secondaryIcon) {
        this.name = name;
        this.primaryIcon = primaryIcon;
        this.secondaryIcon = secondaryIcon;
        this.categoryHolder = MekanismBookCategory.createHolder(MekanismRecipeTypes.NAME_OXIDIZING, this.name);
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

    /// Recipe book info for chemical oxidizing recipes.
    ///
    /// @param category Recipe book category.
    /// @param group    Recipe group.
    ///
    /// @see net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo
    public record OxidizingBookInfo(OxidizingBookCategory category, String group) implements Recipe.BookInfo<OxidizingBookCategory> {

        public static final MapCodec<OxidizingBookInfo> MAP_CODEC = Recipe.BookInfo.mapCodec(CODEC, MISC, OxidizingBookInfo::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, OxidizingBookInfo> STREAM_CODEC = Recipe.BookInfo.streamCodec(OxidizingBookCategory.STREAM_CODEC, OxidizingBookInfo::new);
    }
}