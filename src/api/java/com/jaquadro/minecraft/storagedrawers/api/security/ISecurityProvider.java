package com.jaquadro.minecraft.storagedrawers.api.security;

import com.jaquadro.minecraft.storagedrawers.api.storage.attribute.IProtectable;
import com.mojang.authlib.GameProfile;

public interface ISecurityProvider
{
    String getProviderID ();

    boolean hasOwnership (GameProfile profile, IProtectable target);

    boolean hasAccess (GameProfile profile, IProtectable target);
}
