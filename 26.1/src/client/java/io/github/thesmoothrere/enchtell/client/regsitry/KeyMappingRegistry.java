package io.github.thesmoothrere.enchtell.client.regsitry;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.thesmoothrere.enchtell.Constants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public final class KeyMappingRegistry {
    private static final KeyMapping.Category ENCH_TELL_CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "main")
    );

    public static final KeyMapping SHOW_KEY = new KeyMapping(
            "key." + Constants.MOD_ID + ".show_key",
            InputConstants.KEY_LSHIFT,
            ENCH_TELL_CATEGORY
    );

    public static void registerKeyMappings() {
        KeyMappingHelper.registerKeyMapping(SHOW_KEY);

        Constants.LOGGER.info(Constants.MOD_NAME + " key mappings registered!");
    }
}
