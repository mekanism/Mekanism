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
        if (ownerName == null) {
            //If the name is still null, see if the uuid is the same as the client uuid
            if (player.getUUID().equals(ownerUUID)) {
                //If it is set the name to the name of the player
                name = player.getGameProfile().getName();
                //And cache the name
                MekanismClient.clientUUIDMap.put(ownerUUID, name);
            } else {
                //Otherwise see if there is a player that the client knows about with the UUID
                PlayerEntity owner = player.getCommandSenderWorld().getPlayerByUUID(ownerUUID);
                if (owner == null) {
                    //If there isn't just display the UUID
                    name = "<" + ownerUUID + ">";
                } else {
                    //If there is display the player's name
                    name = owner.getGameProfile().getName();
                    // and cache the name so that it continues to display if the player disconnects
                    MekanismClient.clientUUIDMap.put(ownerUUID, name);
                }
            }
        }
        if (player == null) {
            return MekanismLang.OWNER.translateColored(EnumColor.DARK_GRAY, name);
        }
        return MekanismLang.OWNER.translateColored(EnumColor.DARK_GRAY, player.getUUID().equals(ownerUUID) ? EnumColor.BRIGHT_GREEN : EnumColor.RED, name);
    }
}