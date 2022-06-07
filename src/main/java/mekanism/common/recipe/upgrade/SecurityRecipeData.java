package mekanism.common.recipe.upgrade;

import java.util.UUID;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.security.SecurityMode;
import net.minecraft.world.item.ItemStack;

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
            return MekanismAPI.getSecurityUtils().moreRestrictive(mode, other.mode) ? other : this;
        }
        //If the owners don't match fail
        return null;
    }

    @Override
    public boolean applyToStack(ItemStack stack) {
        return true;
    }
}