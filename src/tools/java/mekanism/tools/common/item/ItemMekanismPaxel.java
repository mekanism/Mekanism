package mekanism.tools.common.item;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
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
import net.minecraft.item.ItemTier;
import net.minecraft.item.ToolItem;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;

public class ItemMekanismPaxel extends ToolItem implements IHasRepairType {

    public ItemMekanismPaxel(IMekanismMaterial material) {
        super(material.getPaxelDamage(), material.getPaxelAtkSpeed(), material, new HashSet<>(), new Item.Properties().group(Mekanism.tabMekanism)
              .addToolType(ToolType.AXE, material.getPaxelHarvestLevel()).addToolType(ToolType.PICKAXE, material.getPaxelHarvestLevel())
              .addToolType(ToolType.SHOVEL, material.getPaxelHarvestLevel()));
        setRegistryName(new ResourceLocation(MekanismTools.MODID, material.getRegistryPrefix() + "_paxel"));
    }

    public ItemMekanismPaxel(ItemTier material) {
        super(4, -2.4F, material, new HashSet<>(), new Item.Properties().group(Mekanism.tabMekanism)
              .addToolType(ToolType.AXE, material.getHarvestLevel()).addToolType(ToolType.PICKAXE, material.getHarvestLevel())
              .addToolType(ToolType.SHOVEL, material.getHarvestLevel()));
        setRegistryName(new ResourceLocation(MekanismTools.MODID, material.name().toLowerCase(Locale.ROOT) + "_paxel"));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(TextComponentUtil.build(Translation.of("mekanism.tooltip.hp"), ": " + (stack.getMaxDamage() - stack.getDamage())));
    }

    @Nonnull
    @Override
    public Ingredient getRepairMaterial() {
        return getTier().getRepairMaterial();
    }
}