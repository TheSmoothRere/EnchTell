package io.github.thesmoothrere.enchtell;

import net.fabricmc.api.ModInitializer;

public class EnchTell implements ModInitializer {
    @Override
    public void onInitialize() {
        Constants.LOGGER.info(Constants.MOD_NAME + " initialized!");
    }
}
