package mekanism.common.recipe.upgrade;

import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.security.IOwnerObject;
import mekanism.api.security.SecurityMode;
import mekanism.common.capabilities.Capabilities;
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
        Optional<IOwnerObject> ownerCapability = stack.getCapability(Capabilities.OWNER_OBJECT).resolve();
        if (ownerCapability.isPresent()) {
            IOwnerObject ownerObject = ownerCapability.get();
            ownerObject.setOwnerUUID(owner);
            stack.getCapability(Capabilities.SECURITY_OBJECT).ifPresent(securityObject -> securityObject.setSecurityMode(mode));
            return true;
        }
        return false;
    }
}