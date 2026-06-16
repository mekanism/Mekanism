package mekanism.generators.common.item;

import java.util.function.Consumer;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.text.EnumColor;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.registration.impl.CreativeTabDeferredRegister.ICustomCreativeTabContents;
import mekanism.common.util.ItemAccessUtils;
import mekanism.common.util.StorageUtils;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.registries.GeneratorsChemicals;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;

public class ItemHohlraum extends Item implements ICustomCreativeTabContents {

    public ItemHohlraum(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    @Deprecated
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        StorageUtils.addStoredChemical(ItemAccessUtils.sideEffectFreeAccess(stack), tooltipAdder);
        ResourceHandler<ChemicalResource> handler = Capabilities.CHEMICAL.getQueryOnlyCapability(stack);
        if (handler != null && ResourceHandlerUtil.isFull(handler)) {
            tooltipAdder.accept(GeneratorsLang.READY_FOR_REACTION.translateColored(EnumColor.DARK_GREEN));
        } else {
            tooltipAdder.accept(GeneratorsLang.INSUFFICIENT_FUEL.translateColored(EnumColor.DARK_RED));
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return StorageUtils.isBarVisible(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return StorageUtils.getBarWidth(stack);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return ContainerType.CHEMICAL.getRGBDurabilityForDisplay(stack);
    }

    @Override
    public void addItems(Holder<Item> item, Consumer<ItemStack> tabOutput) {
        tabOutput.accept(ContainerType.CHEMICAL.getFilledVariant(item, GeneratorsChemicals.FUSION_FUEL, null));
    }
}