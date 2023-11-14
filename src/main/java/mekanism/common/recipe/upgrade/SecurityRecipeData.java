package mekanism.common.recipe.upgrade;

import java.util.UUID;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.security.IOwnerObject;
import mekanism.api.security.ISecurityObject;
import mekanism.api.security.ISecurityUtils;
import mekanism.api.security.SecurityMode;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
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
            return ISecurityUtils.INSTANCE.moreRestrictive(mode, other.mode) ? other : this;
        }
        //If the owners don't match fail
        return null;
    }

    @Override
    public boolean applyToStack(ItemStack stack) {
        IOwnerObject ownerObject = Capabilities.OWNER_OBJECT.getCapability(stack);
        if (ownerObject != null) {
            ownerObject.setOwnerUUID(owner);
            ISecurityObject security = Capabilities.SECURITY_OBJECT.getCapability(stack);
            if (security != null) {
                security.setSecurityMode(mode);
            }
        }
        return true;
    }
}