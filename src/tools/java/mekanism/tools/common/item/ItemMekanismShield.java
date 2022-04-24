package mekanism.tools.common.item;

import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.tools.client.render.ToolsRenderPropertiesProvider;
import mekanism.tools.common.IHasRepairType;
import mekanism.tools.common.material.BaseMekanismMaterial;
import mekanism.tools.common.util.ToolsUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.IItemRenderProperties;

public class ItemMekanismShield extends ShieldItem implements IHasRepairType {

    private final BaseMekanismMaterial material;

    public ItemMekanismShield(BaseMekanismMaterial material, Item.Properties properties) {
        super(properties.durability(material.getShieldDurability()));
        this.material = material;
    }

    @Override
    public void initializeClient(@Nonnull Consumer<IItemRenderProperties> consumer) {
        consumer.accept(ToolsRenderPropertiesProvider.shield());
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level world, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
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
    public int getEnchantmentValue() {
        return material.getEnchantmentValue();
    }
}