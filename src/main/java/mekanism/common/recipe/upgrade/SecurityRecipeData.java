package mekanism.common.recipe.upgrade;

import java.util.UUID;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.common.lib.security.ISecurityItem;
import mekanism.common.lib.security.ISecurityTile.SecurityMode;
import net.minecraft.item.ItemStack;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class SecurityRecipeData implements RecipeUpgradeData<SecurityRecipeData> {

    private final UUID owner;
    private final SecurityMode mode;

    SecurityRecipeData(UUID owner, SecurityMode mode) {
        this.owner = owner;
        this.mode = mode;
    }

    @Nullable
    @Override
    public SecurityRecipeData merge(SecurityRecipeData other) {
        if (owner.equals(other.owner)) {
            //Pick the most restrictive security mode
            //TODO: Do this a better way at some point than just using ordinals
            return new SecurityRecipeData(owner, SecurityMode.byIndexStatic(Math.max(mode.ordinal(), other.mode.ordinal())));
        }
        //If the owners don't match fail
        return null;

    }

    @Override
    public boolean applyToStack(ItemStack stack) {
        ISecurityItem securityItem = (ISecurityItem) stack.getItem();
        securityItem.setOwnerUUID(stack, owner);
        securityItem.setSecurity(stack, mode);
        return true;
    }
}