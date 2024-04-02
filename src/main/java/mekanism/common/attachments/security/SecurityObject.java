package mekanism.common.attachments.security;

import java.util.UUID;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.security.ISecurityObject;
import mekanism.api.security.SecurityMode;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class SecurityObject extends OwnerObject implements ISecurityObject, INBTSerializable<CompoundTag> {

    private SecurityMode securityMode = SecurityMode.PUBLIC;

    public SecurityObject(IAttachmentHolder attachmentHolder) {
        super(attachmentHolder);
    }

    private SecurityObject(IAttachmentHolder attachmentHolder, @Nullable UUID ownerUUID, SecurityMode securityMode) {
        super(attachmentHolder, ownerUUID);
        this.securityMode = securityMode;
    }

    @Override
    public SecurityMode getSecurityMode() {
        return securityMode;
    }

    @Override
    public void setSecurityMode(SecurityMode mode) {
        if (securityMode != mode) {
            SecurityMode old = securityMode;
            securityMode = mode;
            onSecurityChanged(old, mode);
        }
    }

    @Override
    public void onSecurityChanged(@NotNull SecurityMode old, @NotNull SecurityMode mode) {
        //Note: For now we don't bother booting players out of item containers if the security mode on the item itself changed
        // as that requires the player that can change the security mode to be holding the item, so they are the only one who
        // could have it open. When override settings change we properly recheck if players should be kicked out
    }

    @Override
    public boolean isCompatible(OwnerObject other) {
        return super.isCompatible(other) && securityMode == ((SecurityObject) other).securityMode;
    }

    @Nullable
    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        if (ownerUUID != null) {
            nbt.putUUID(NBTConstants.OWNER_UUID, ownerUUID);
        }
        if (securityMode != SecurityMode.PUBLIC) {
            nbt.putInt(NBTConstants.SECURITY_MODE, securityMode.ordinal());
        }
        return nbt.isEmpty() ? null : nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt.hasUUID(NBTConstants.OWNER_UUID)) {
            ownerUUID = nbt.getUUID(NBTConstants.OWNER_UUID);
        }
        if (nbt.contains(NBTConstants.SECURITY_MODE, Tag.TAG_INT)) {
            securityMode = SecurityMode.byIndexStatic(nbt.getInt(NBTConstants.SECURITY_MODE));
        }
    }

    @Nullable
    public SecurityObject copy(IAttachmentHolder holder) {
        if (ownerUUID == null && securityMode == SecurityMode.PUBLIC) {
            return null;
        }
        return new SecurityObject(holder, ownerUUID, securityMode);
    }
}