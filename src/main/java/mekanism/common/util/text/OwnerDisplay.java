package mekanism.common.util.text;

import java.util.UUID;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.client.MekanismClient;
import mekanism.common.MekanismLang;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;

public class OwnerDisplay implements IHasTextComponent {

    private final PlayerEntity player;
    private final UUID ownerUUID;
    private final String ownerName;

    private OwnerDisplay(PlayerEntity player, UUID ownerUUID, String ownerName) {
        this.player = player;
        this.ownerUUID = ownerUUID;
        this.ownerName = ownerName;
    }

    public static OwnerDisplay of(UUID ownerUUID) {
        return of(null, ownerUUID);
    }

    public static OwnerDisplay of(PlayerEntity player, UUID ownerUUID) {
        return of(player, ownerUUID, null);
    }

    public static OwnerDisplay of(UUID ownerUUID, String ownerName) {
        return of(null, ownerUUID, ownerName);
    }

    public static OwnerDisplay of(PlayerEntity player, UUID ownerUUID, String ownerName) {
        return new OwnerDisplay(player, ownerUUID, ownerName);
    }

    @Override
    public ITextComponent getTextComponent() {
        if (ownerUUID == null) {
            return MekanismLang.NO_OWNER.translateColored(EnumColor.RED);
        }
        //TODO: If the name is supposed to be gotten differently server side, then do so
        //Allows for the name to be overridden by a passed value
        String name = ownerName == null ? MekanismClient.clientUUIDMap.get(ownerUUID) : ownerName;
        if (player == null) {
            return MekanismLang.OWNER.translateColored(EnumColor.DARK_GRAY, name);
        }
        return MekanismLang.OWNER.translateColored(EnumColor.DARK_GRAY, player.getUniqueID().equals(ownerUUID) ? EnumColor.BRIGHT_GREEN : EnumColor.RED, name);
    }
}