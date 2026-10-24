package mekanism.api.recipes.book;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.function.IntFunction;
import mekanism.api.chemical.ChemicalIds;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.recipes.MekanismRecipeTypes;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter.Provider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import org.jspecify.annotations.Nullable;

/// Recipe book categories for centrifuging recipes.
///
/// @since 10.8.0
public enum CentrifugingBookCategory implements MekanismBookCategory {
    MISC("misc", "isotopic_centrifuge", null);

    public static final IntFunction<CentrifugingBookCategory> BY_ID = ByIdMap.continuous(CentrifugingBookCategory::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final Codec<CentrifugingBookCategory> CODEC = StringRepresentable.fromEnum(CentrifugingBookCategory::values);
    public static final StreamCodec<ByteBuf, CentrifugingBookCategory> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, CentrifugingBookCategory::ordinal);

    private final Holder<RecipeBookCategory> categoryHolder;
    private final Holder<Item> primaryIcon;
    @Nullable
    private final Holder<Item> secondaryIcon;
    private final String name;

    CentrifugingBookCategory(String name, String primaryMekIcon, @Nullable String secondaryMekIcon) {
        this(name, MekanismBookCategory.createMekanismKey(primaryMekIcon), MekanismBookCategory.createMekanismKey(secondaryMekIcon));
    }

    CentrifugingBookCategory(String name, ResourceKey<Item> primaryIcon, @Nullable ResourceKey<Item> secondaryIcon) {
        this.name = name;
        this.primaryIcon = MekanismBookCategory.createItem(primaryIcon);
        this.secondaryIcon = MekanismBookCategory.createItem(secondaryIcon);
        this.categoryHolder = MekanismBookCategory.createHolder(MekanismRecipeTypes.NAME_CENTRIFUGING, this.name);
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
    public Object getPrimaryIcon(Provider holderLookup) {
        if (this == MISC) {
            //Try to render misc as fissile fuel
            return holderLookup.get(ChemicalIds.FISSILE_FUEL).<Object>map(ChemicalResource::of).orElseGet(() -> new ItemStack(primaryIcon));
        }
        return new ItemStack(primaryIcon);
    }

    @Override
    public Optional<Object> getSecondaryIcon(Provider holderLookup) {
        return secondaryIcon == null ? Optional.empty() : Optional.of(new ItemStack(secondaryIcon));
    }

    /// Recipe book info for centrifuging recipes.
    ///
    /// @param category Recipe book category.
    /// @param group    Recipe group.
    ///
    /// @see net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo
    public record CentrifugingBookInfo(CentrifugingBookCategory category, String group) implements Recipe.BookInfo<CentrifugingBookCategory> {

        public static final MapCodec<CentrifugingBookInfo> MAP_CODEC = Recipe.BookInfo.mapCodec(CODEC, MISC, CentrifugingBookInfo::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, CentrifugingBookInfo> STREAM_CODEC = Recipe.BookInfo.streamCodec(CentrifugingBookCategory.STREAM_CODEC, CentrifugingBookInfo::new);
    }
}