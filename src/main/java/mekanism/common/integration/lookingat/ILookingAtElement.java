package mekanism.common.integration.lookingat;

import net.minecraft.resources.Identifier;

public sealed interface ILookingAtElement permits TextElement, LookingAtElement {

    Identifier getID();
}