package mekanism.common.content.oredictionificator;

import mekanism.common.config.MekanismConfig;
import mekanism.common.config.value.CachedOredictionificatorConfigValue;
import mekanism.common.content.filter.FilterType;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.tags.TagUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.tags.ITagManager;
import org.jetbrains.annotations.NotNull;

//TODO - V11: Rewrite/refactor usages of this to better handle tags for fluids and chemicals by allowing them to make use of the super OredictionificatorFilter class
public class OredictionificatorItemFilter extends OredictionificatorFilter<Item, ItemStack, OredictionificatorItemFilter> {

    public OredictionificatorItemFilter() {
    }

    public OredictionificatorItemFilter(OredictionificatorItemFilter filter) {
        super(filter);
    }

    @Override
    @ComputerMethod(nameOverride = "getSelectedOutput")
    public Item getResultElement() {
        return getResult().getItem();
    }

    @Override
    protected IForgeRegistry<Item> getRegistry() {
        return ForgeRegistries.ITEMS;
    }

    @Override
    protected ITagManager<Item> getTagManager() {
        return TagUtils.manager(ForgeRegistries.ITEMS);
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

    @ComputerMethod(nameOverride = "setSelectedOutput", threadSafe = true)
    void computer_setSelectedOutput(@NotNull Item item) {
        setSelectedOutput(item);
    }
}