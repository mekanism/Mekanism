package mekanism.common.integration.crafttweaker.example.component;

import org.jetbrains.annotations.NotNull;

public interface ICrTExampleComponent {

    /**
     * Converts this example component into the string representation it will have inside the example script.
     */
    @NotNull
    String asString();
}