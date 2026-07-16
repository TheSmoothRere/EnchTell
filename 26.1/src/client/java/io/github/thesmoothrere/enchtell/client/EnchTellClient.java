package io.github.thesmoothrere.enchtell.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import io.github.thesmoothrere.enchtell.Constants;
import io.github.thesmoothrere.enchtell.client.regsitry.KeyMappingRegistry;
import io.github.thesmoothrere.enchtell.config.EnchTellConfig;
import io.github.thesmoothrere.enchtell.utils.ShowType;
import io.github.thesmoothrere.relib.config.ConfigManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.List;

@Environment(EnvType.CLIENT)
public class EnchTellClient implements ClientModInitializer {
    private static final EnchTellConfig CONFIG = ConfigManager.get(EnchTellConfig.class);
    private static final KeyMapping SHOW_KEY = KeyMappingRegistry.SHOW_KEY;
    private static boolean IS_TOGGLED = false;
    private static boolean WAS_KEY_DOWN = false;

    @Override
    public void onInitializeClient() {
        Constants.LOGGER.info(Constants.MOD_NAME + " client initialized!");
        KeyMappingRegistry.registerKeyMappings();
        ItemTooltipCallback.EVENT.register(EnchTellClient::onTooltip);
    }

    private static void onTooltip(ItemStack itemStack, Item.TooltipContext tooltipContext,
                                  TooltipFlag tooltipFlag, List<Component> components) {
        ShowType showType = CONFIG.showType().getValue();

        boolean shouldShow = false;

        if (showType == ShowType.ALWAYS) {
            shouldShow = true;
        } else {
            // Get the window handle and the bound key code
            Window window = Minecraft.getInstance().getWindow();
            int keyCode = KeyMappingHelper.getBoundKeyOf(SHOW_KEY).getValue();

            // Direct GLFW check to see if the key is currently physically held down
            boolean isKeyDown = InputConstants.isKeyDown(window, keyCode);

            if (showType == ShowType.HOLD) {
                shouldShow = isKeyDown;
            }
            else if (showType == ShowType.TOGGLE) {
                // Edge detection: Trigger toggle only on the frame the key goes from UP to DOWN
                if (isKeyDown && !WAS_KEY_DOWN) {
                    IS_TOGGLED = !IS_TOGGLED;
                }
                WAS_KEY_DOWN = isKeyDown; // Update state tracker
                shouldShow = IS_TOGGLED;
            }
        }

        if (!shouldShow) return;

        ItemEnchantments normalEnchantments = itemStack.get(DataComponents.ENCHANTMENTS);
        if (normalEnchantments != null && !normalEnchantments.isEmpty() && CONFIG.showOnItems().getValue()) {
            insertDescriptions(normalEnchantments, components);
        }

        // Target stored enchantments (Enchanted Books, or modded items storing spell templates)
        ItemEnchantments storedEnchantments = itemStack.get(DataComponents.STORED_ENCHANTMENTS);
        if (storedEnchantments != null && !storedEnchantments.isEmpty()) {
            insertDescriptions(storedEnchantments, components);
        }
    }

    // Helper method to find the enchantment name in the tooltip list and slide the description right under it
    private static void insertDescriptions(ItemEnchantments enchantments, List<Component> components) {
        for (Holder<Enchantment> holder : enchantments.keySet()) {
            int level = enchantments.getLevel(holder);

            // Get the exact visual name (e.g., "Sharpness V" or "Curse of Vanishing")
            String displayName = Enchantment.getFullname(holder, level).getString();

            unwrapKeyAndFindLineToInsertDescription(components, holder, displayName);
        }
    }

    private static void unwrapKeyAndFindLineToInsertDescription(List<Component> components, Holder<Enchantment> holder, String displayName) {
        holder.unwrapKey().ifPresent(key -> {
            String keyNamespace = key.identifier().getNamespace();
            String keyPath = key.identifier().getPath();

            String translatableKeyDesc = "enchantment." + keyNamespace + "." + keyPath + ".desc";

            if (!I18n.exists(translatableKeyDesc)) return;

            // Find where this text is inside the tooltip list
            int insertIndex = -1;
            for (int i = 0; i < components.size(); i++) {
                if (components.get(i).getString().contains(displayName)) {
                    insertIndex = i;
                    break;
                }
            }

            // If we found the line, insert our description right beneath it
            if (insertIndex != -1) {
                Component desc = Component.empty()
                        .append(CommonComponents.SPACE)
                        .append(Component.translatable(translatableKeyDesc))
                        .withStyle(ChatFormatting.DARK_GRAY);

                components.add(insertIndex + 1, desc);
            }
        });
    }
}
