package mekanism.common.item;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.client.MekanismKeyHandler;
import mekanism.common.Upgrade;
import mekanism.common.base.IUpgradeItem;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

public class ItemUpgrade extends ItemMekanism implements IUpgradeItem {

    private Upgrade upgrade;

    public ItemUpgrade(Upgrade type) {
        //Upgrade names are upgrade_type for purposes of tab complete
        super("upgrade_" + type.getRawName(), new Item.Properties().maxStackSize(type.getMax()));
        upgrade = type;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack itemstack, World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        if (!InputMappings.isKeyDown(Minecraft.getInstance().mainWindow.getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
            tooltip.add(TextComponentUtil.build(Translation.of("mekanism.tooltip.hold"), " ", EnumColor.INDIGO, MekanismKeyHandler.sneakKey.getKey(),
                  EnumColor.GREY, " ", Translation.of("mekanism.tooltip.for_details"), "."));
        } else {
            tooltip.add(TextComponentUtil.build(Translation.of(getUpgradeType(itemstack).getDescription())));
        }
    }

    @Override
    public Upgrade getUpgradeType(ItemStack stack) {
        return upgrade;
    }

    @Nonnull
    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        if (player != null && player.isSneaking()) {
            World world = context.getWorld();
            TileEntity tile = world.getTileEntity(context.getPos());
            ItemStack stack = player.getHeldItem(context.getHand());
            Upgrade type = getUpgradeType(stack);
            if (tile instanceof IUpgradeTile) {
                TileComponentUpgrade component = ((IUpgradeTile) tile).getComponent();
                if (component.supports(type)) {
                    if (!world.isRemote && component.getUpgrades(type) < type.getMax()) {
                        component.addUpgrade(type);
                        stack.shrink(1);
                    }
                }
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.PASS;
    }
}