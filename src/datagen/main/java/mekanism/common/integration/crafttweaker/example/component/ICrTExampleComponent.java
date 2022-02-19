package mekanism.common.integration.crafttweaker.example.component;

import javax.annotation.Nonnull;

public interface ICrTExampleComponent {

    /**
     * Converts this example component into the string representation it will have inside the example script.
     */
    @Nonnull
    String asString();
}