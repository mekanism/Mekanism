package mekanism.common.lib.security;

import java.util.Set;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.security.IBlockSecurityUtils;
import mekanism.api.security.IOwnerObject;
import mekanism.api.security.ISecurityObject;
import mekanism.api.security.SecurityMode;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.Nullable;

/**
 * @apiNote Do not instantiate this class directly as it will be done via the service loader. Instead, access instances of this via {@link IBlockSecurityUtils#INSTANCE}
 */
@NothingNullByDefault
public class BlockSecurityUtils implements IBlockSecurityUtils {

    private static final BlockCapability<IOwnerObject, Void> OWNER_CAPABILITY = BlockCapability.createVoid(Capabilities.OWNER_OBJECT_NAME, IOwnerObject.class);
    private static final BlockCapability<ISecurityObject, Void> SECURITY_CAPABILITY = BlockCapability.createVoid(Capabilities.SECURITY_OBJECT_NAME, ISecurityObject.class);

    public static BlockSecurityUtils get() {
        return (BlockSecurityUtils) INSTANCE;
    }

    @Override
    public BlockCapability<IOwnerObject, Void> ownerCapability() {
        return OWNER_CAPABILITY;
    }

    @Override
    public BlockCapability<ISecurityObject, Void> securityCapability() {
        return SECURITY_CAPABILITY;
    }

    public void securityChanged(Set<Player> playersUsing, Level level, BlockPos pos, @Nullable BlockEntity target, SecurityMode old, SecurityMode mode) {
        SecurityUtils.get().securityChanged(playersUsing, player -> canAccess(player, level, pos, target), old, mode);
    }
}