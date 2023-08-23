package mod.acats.fromanotherworld.fabric;

import mod.acats.fromanotherworld.FromAnotherWorld;
import mod.acats.fromanotherworld.fabric.registry.*;
import net.fabricmc.api.ModInitializer;

public class FromAnotherWorldFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        new FromAnotherWorld().init();
        SoundRegistryFabric.register();
        StatusEffectRegistryFabric.register();
        EventRegistryFabric.register();
        DatapackRegistryFabric.register();
        RecipeRegistryFabric.register();
        SpawnEntryRegistryFabric.register();
    }
}
