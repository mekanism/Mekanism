package mekanism.tools.common.item;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.tools.common.IHasRepairType;
import mekanism.tools.common.material.BaseMekanismMaterial;
import mekanism.tools.common.util.ToolsUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemMekanismShield extends ShieldItem implements IHasRepairType {

    private final BaseMekanismMaterial material;

    public ItemMekanismShield(BaseMekanismMaterial material, Item.Properties properties) {
        super(properties.durability(material.getShieldDurability()));
        this.material = material;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);//Add the banner type description
        ToolsUtils.addDurability(tooltip, stack);
    }

    @Nonnull
    @Override
    public Ingredient getRepairMaterial() {
        return material.getRepairIngredient();
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return material.getShieldDurability();
    }

    @Override
    public boolean canBeDepleted() {
        return material.getShieldDurability() > 0;
    }

    @Override
    public boolean isValidRepairItem(@Nonnull ItemStack toRepair, @Nonnull ItemStack repair) {
        return getRepairMaterial().test(repair);
    }

    @Override
    public boolean isShield(ItemStack stack, @Nullable LivingEntity entity) {
        //Has to override this because default impl in IForgeItem checks for exact equality with the shield item instead of instanceof
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return material.getEnchantmentValue();
    }
}