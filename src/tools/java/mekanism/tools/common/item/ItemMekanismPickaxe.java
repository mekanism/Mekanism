package mekanism.tools.common.item;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.tools.common.IHasRepairType;
import mekanism.tools.common.ToolsLang;
import mekanism.tools.common.material.BaseMekanismMaterial;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemMekanismPickaxe extends PickaxeItem implements IHasRepairType {

    public ItemMekanismPickaxe(BaseMekanismMaterial material) {
        super(material, material.getPickaxeDamage(), material.getPickaxeAtkSpeed(), ItemDeferredRegister.getMekBaseProperties());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(ToolsLang.HP.translate(stack.getMaxDamage() - stack.getDamage()));
    }

    @Nonnull
    @Override
    public Ingredient getRepairMaterial() {
        return getTier().getRepairMaterial();
    }
}