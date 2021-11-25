package mekanism.common.util.text;

import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.MekanismClient;
import mekanism.common.MekanismLang;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.thread.EffectiveSide;

public class OwnerDisplay implements IHasTextComponent {

    private final PlayerEntity player;
    private final UUID ownerUUID;
    private final String ownerName;
    private final boolean colorBase;

    private OwnerDisplay(PlayerEntity player, UUID ownerUUID, String ownerName, boolean colorBase) {
        this.player = player;
        this.ownerUUID = ownerUUID;
        this.ownerName = ownerName;
        this.colorBase = colorBase;
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
        return of(player, ownerUUID, ownerName, true);
    }

    public static OwnerDisplay of(PlayerEntity player, UUID ownerUUID, String ownerName, boolean colorBase) {
        return new OwnerDisplay(player, ownerUUID, ownerName, colorBase);
    }

    @Override
    public ITextComponent getTextComponent() {
        if (ownerUUID == null) {
            return MekanismLang.NO_OWNER.translateColored(EnumColor.RED);
        }
        String name = getOwnerName(player, ownerUUID, ownerName);
        ITextComponent component;
        if (player == null) {
            component = MekanismLang.OWNER.translate(name);
        } else {
            component = MekanismLang.OWNER.translate(player.getUUID().equals(ownerUUID) ? EnumColor.BRIGHT_GREEN : EnumColor.RED, name);
        }
        if (colorBase) {
            return TextComponentUtil.build(EnumColor.DARK_GRAY, component);
        }
        return component;
    }

    public static String getOwnerName(@Nullable PlayerEntity player, @Nonnull UUID ownerUUID, @Nullable String ownerName) {
        //Allows for the name to be overridden by a passed value
        if (ownerName != null) {
            return ownerName;
        } else if (player != null && !player.level.isClientSide || player == null && EffectiveSide.get().isServer()) {
            return MekanismUtils.getLastKnownUsername(ownerUUID);
        }
        String name = MekanismClient.clientUUIDMap.get(ownerUUID);
        if (name != null && player != null) {
            //If the name is still null, see if the uuid is the same as the client uuid
            if (player.getUUID().equals(ownerUUID)) {
                //If it is set the name to the name of the player
                name = player.getGameProfile().getName();
                //And cache the name
                MekanismClient.clientUUIDMap.put(ownerUUID, name);
            } else {
                //Otherwise, see if there is a player that the client knows about with the UUID
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
        return name;
    }
}