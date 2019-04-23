package mekanism.common.security;

import io.netty.buffer.ByteBuf;
import java.util.UUID;
import mekanism.common.HashList;
import mekanism.common.PacketHandler;
import mekanism.api.TileNetworkList;
import mekanism.common.frequency.Frequency;
import mekanism.common.security.ISecurityTile.SecurityMode;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.Constants.NBT;

public class SecurityFrequency extends Frequency {

    public static final String SECURITY = "Security";

    public boolean override;

    public HashList<String> trusted;

    public SecurityMode securityMode;

    public SecurityFrequency(UUID uuid) {
        super("Security", uuid);

        trusted = new HashList<>();
        securityMode = SecurityMode.PUBLIC;
    }

    public SecurityFrequency(NBTTagCompound nbtTags) {
        super(nbtTags);
    }

    public SecurityFrequency(ByteBuf dataStream) {
        super(dataStream);
    }

    @Override
    public void write(NBTTagCompound nbtTags) {
        super.write(nbtTags);

        nbtTags.setBoolean("override", override);
        nbtTags.setInteger("securityMode", securityMode.ordinal());

        if (!trusted.isEmpty()) {
            NBTTagList trustedList = new NBTTagList();

            for (String s : trusted) {
                trustedList.appendTag(new NBTTagString(s));
            }

            nbtTags.setTag("trusted", trustedList);
        }
    }

    @Override
    protected void read(NBTTagCompound nbtTags) {
        super.read(nbtTags);

        trusted = new HashList<>();
        securityMode = SecurityMode.PUBLIC;

        override = nbtTags.getBoolean("override");
        securityMode = SecurityMode.values()[nbtTags.getInteger("securityMode")];

        if (nbtTags.hasKey("trusted")) {
            NBTTagList trustedList = nbtTags.getTagList("trusted", NBT.TAG_STRING);

            for (int i = 0; i < trustedList.tagCount(); i++) {
                trusted.add(trustedList.getStringTagAt(i));
            }
        }
    }

    @Override
    public void write(TileNetworkList data) {
        super.write(data);

        data.add(override);
        data.add(securityMode.ordinal());

        data.add(trusted.size());

        for (String s : trusted) {
            data.add(s);
        }
    }

    @Override
    protected void read(ByteBuf dataStream) {
        super.read(dataStream);

        trusted = new HashList<>();
        securityMode = SecurityMode.PUBLIC;

        override = dataStream.readBoolean();
        securityMode = SecurityMode.values()[dataStream.readInt()];

        int size = dataStream.readInt();

        for (int i = 0; i < size; i++) {
            trusted.add(PacketHandler.readString(dataStream));
        }
    }
}
