package mekanism.common.block.attribute;

import mekanism.common.content.blocktype.FactoryType;
import org.jetbrains.annotations.NotNull;

public class AttributeFactoryType implements Attribute {

    private final FactoryType type;

    public AttributeFactoryType(FactoryType type) {
        this.type = type;
    }

    @NotNull
    public FactoryType getFactoryType() {
        return type;
    }
}