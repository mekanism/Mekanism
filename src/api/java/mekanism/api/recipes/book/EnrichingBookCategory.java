package mekanism.api.recipes.book;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.function.IntFunction;
import mekanism.api.recipes.MekanismRecipeTypes;
import net.minecraft.core.Holder;
import net.minecraft.data.recipes.RecipeCategory;
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

/// Recipe book categories for enriching recipes.
///
/// @since 10.8.0
public enum EnrichingBookCategory implements MekanismBookCategory {
    BUILDING("building", "enrichment_chamber", null),
    EFFICIENCY("efficiency", "enrichment_chamber", null),
    DECORATIVE("decorative", "enrichment_chamber", null),
    MISC("misc", "enrichment_chamber", null);

    public static final Codec<EnrichingBookCategory> CODEC = StringRepresentable.fromEnum(EnrichingBookCategory::values);
    public static final IntFunction<EnrichingBookCategory> BY_ID = ByIdMap.continuous(EnrichingBookCategory::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, EnrichingBookCategory> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, EnrichingBookCategory::ordinal);

    private final Holder<RecipeBookCategory> categoryHolder;
    private final Holder<Item> primaryIcon;
    @Nullable
    private final Holder<Item> secondaryIcon;
    private final String name;

    EnrichingBookCategory(String name, String primaryMekIcon, @Nullable String secondaryMekIcon) {
        this(name, MekanismBookCategory.createMekanismItem(primaryMekIcon), MekanismBookCategory.createMekanismItem(secondaryMekIcon));
    }

    EnrichingBookCategory(String name, Holder<Item> primaryIcon, @Nullable Holder<Item> secondaryIcon) {
        this.name = name;
        this.primaryIcon = primaryIcon;
        this.secondaryIcon = secondaryIcon;
        this.categoryHolder = MekanismBookCategory.createHolder(MekanismRecipeTypes.NAME_ENRICHING, this.name);
    }

    @Nullable
    @Override
    public String getAdvancementFolder() {
        return switch (this) {
            case BUILDING -> RecipeCategory.BUILDING_BLOCKS.getFolderName();
            case MISC -> null;
            default -> name;
        };
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

    /// Recipe book info for enriching recipes.
    ///
    /// @param category Recipe book category.
    /// @param group    Recipe group.
    ///
    /// @see net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo
    public record EnrichingBookInfo(EnrichingBookCategory category, String group) implements Recipe.BookInfo<EnrichingBookCategory> {

        public static final MapCodec<EnrichingBookInfo> MAP_CODEC = Recipe.BookInfo.mapCodec(CODEC, MISC, EnrichingBookInfo::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, EnrichingBookInfo> STREAM_CODEC = Recipe.BookInfo.streamCodec(EnrichingBookCategory.STREAM_CODEC, EnrichingBookInfo::new);
    }
}