package mekanism.tools.common.item;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.Mekanism;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import mekanism.tools.common.IHasRepairType;
import mekanism.tools.common.MekanismTools;
import mekanism.tools.common.material.IMekanismMaterial;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemMekanismPickaxe extends PickaxeItem implements IHasRepairType {

    public ItemMekanismPickaxe(IMekanismMaterial material) {
        super(material, material.getPickaxeDamage(), material.getPickaxeAtkSpeed(), new Item.Properties().group(Mekanism.tabMekanism));
        setRegistryName(new ResourceLocation(MekanismTools.MODID, material.getRegistryPrefix() + "_pickaxe"));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(TextComponentUtil.build(Translation.of("tooltip.mekanism.hp"), ": " + (stack.getMaxDamage() - stack.getDamage())));
    }

    @Nonnull
    @Override
    public Ingredient getRepairMaterial() {
        return getTier().getRepairMaterial();
    }
}