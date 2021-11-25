package mekanism.common.content.oredictionificator;

import mekanism.common.config.MekanismConfig;
import mekanism.common.config.value.CachedOredictionificatorConfigValue;
import mekanism.common.content.filter.FilterType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.ITagCollection;
import net.minecraft.tags.ItemTags;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

//TODO - V11: Rewrite/refactor usages of this to better handle tags for fluids and chemicals by allowing them to make use of the super OredictionificatorFilter class
public class OredictionificatorItemFilter extends OredictionificatorFilter<Item, ItemStack, OredictionificatorItemFilter> {

    public OredictionificatorItemFilter() {
    }

    public OredictionificatorItemFilter(OredictionificatorItemFilter filter) {
        super(filter);
    }

    @Override
    public Item getResultElement() {
        return getResult().getItem();
    }

    @Override
    protected IForgeRegistry<Item> getRegistry() {
        return ForgeRegistries.ITEMS;
    }

    @Override
    protected ITagCollection<Item> getTagCollection() {
        return ItemTags.getAllTags();
    }

    @Override
    protected Item getFallbackElement() {
        return Items.AIR;
    }

    @Override
    protected ItemStack getEmptyStack() {
        return ItemStack.EMPTY;
    }

    @Override
    protected ItemStack createResultStack(Item item) {
        return new ItemStack(item);
    }

    @Override
    protected CachedOredictionificatorConfigValue getValidValuesConfig() {
        return MekanismConfig.general.validOredictionificatorFilters;
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.OREDICTIONIFICATOR_ITEM_FILTER;
    }

    @Override
    public OredictionificatorItemFilter clone() {
        return new OredictionificatorItemFilter(this);
    }
}