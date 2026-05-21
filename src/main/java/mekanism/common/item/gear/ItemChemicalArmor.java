package mekanism.common.item.gear;

import java.util.function.Consumer;
import mekanism.api.chemical.Chemical;
import mekanism.common.item.interfaces.IChemicalItem;
import mekanism.common.registration.impl.CreativeTabDeferredRegister.ICustomCreativeTabContents;
import mekanism.common.util.ChemicalUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import org.jetbrains.annotations.NotNull;

public abstract class ItemChemicalArmor extends ItemSpecialArmor implements IChemicalItem, ICustomCreativeTabContents {

    protected ItemChemicalArmor(ArmorMaterial material, ArmorType armorType, Item.Properties properties) {
        super(material, armorType, properties.rarity(Rarity.RARE).setNoCombineRepair().stacksTo(1));
    }

    protected abstract Holder<Chemical> getChemicalType();

    @Override
    public boolean hasChemical(ItemStack stack) {
        return ChemicalUtils.hasChemicalOfType(stack, getChemicalType());
    }

    @Override
    @Deprecated
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        StorageUtils.addStoredChemical(ItemAccess.forStack(stack), tooltipAdder);
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {
        return StorageUtils.getBarWidth(stack);
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        return ChemicalUtils.getRGBDurabilityForDisplay(ItemAccess.forStack(stack));
    }

    @Override
    public void addItems(Holder<Item> item, Consumer<ItemStack> tabOutput) {
        tabOutput.accept(ChemicalUtils.getFilledVariant(item, getChemicalType()));
    }
}