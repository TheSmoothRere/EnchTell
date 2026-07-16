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
    private static final ToggleTracker TOGGLE_TRACKER = new ToggleTracker();

    @Override
    public void onInitializeClient() {
        Constants.LOGGER.info(Constants.MOD_NAME + " client initialized!");

        KeyMappingRegistry.registerKeyMappings();
        ItemTooltipCallback.EVENT.register(EnchTellClient::onTooltip);
    }

    private static void onTooltip(ItemStack itemStack, Item.TooltipContext tooltipContext,
                                  TooltipFlag tooltipFlag, List<Component> components) {
        if (!shouldShowTooltip()) return;

        // Process normal enchantments if allowed by config
        if (CONFIG.showOnItems().getValue()) {
            ItemEnchantments normalEnchantments = itemStack.get(DataComponents.ENCHANTMENTS);
            if (normalEnchantments != null && !normalEnchantments.isEmpty()) {
                insertDescriptions(normalEnchantments, components);
            }
        }

        // Process stored enchantments (Enchanted Books, etc.)
        ItemEnchantments storedEnchantments = itemStack.get(DataComponents.STORED_ENCHANTMENTS);
        if (storedEnchantments != null && !storedEnchantments.isEmpty()) {
            insertDescriptions(storedEnchantments, components);
        }
    }

    // Evaluates input logic cleanly using a switch expression
    private static boolean shouldShowTooltip() {
        ShowType showType = CONFIG.showType().getValue();

        if (showType == ShowType.ALWAYS) return true;

        // Fetch physical key state bypasses UI screen blocking
        Window window = Minecraft.getInstance().getWindow();
        int keyCode = KeyMappingHelper.getBoundKeyOf(SHOW_KEY).getValue();
        boolean isKeyDown = InputConstants.isKeyDown(window, keyCode);

        return switch (showType) {
            case HOLD -> isKeyDown;
            case TOGGLE -> TOGGLE_TRACKER.updateAndGet(isKeyDown);
            default -> false;
        };
    }

    private static void insertDescriptions(ItemEnchantments enchantments, List<Component> components) {
        for (Holder<Enchantment> holder : enchantments.keySet()) {
            int level = enchantments.getLevel(holder);
            String displayName = Enchantment.getFullname(holder, level).getString();

            unwrapKeyAndFindLineToInsertDescription(components, holder, displayName);
        }
    }

    private static void unwrapKeyAndFindLineToInsertDescription(List<Component> components, Holder<Enchantment> holder, String displayName) {
        holder.unwrapKey().ifPresent(key -> {
            String translatableKeyDesc = "enchantment." + key.identifier().getNamespace() + "." + key.identifier().getPath() + ".desc";

            if (!I18n.exists(translatableKeyDesc)) return;

            // Find where this text is inside the tooltip list
            int insertIndex = -1;
            for (int i = 0; i < components.size(); i++) {
                if (components.get(i).getString().contains(displayName)) {
                    insertIndex = i;
                    break;
                }
            }

            // Insert our description right beneath it
            if (insertIndex != -1) {
                Component desc = Component.empty()
                        .append(CommonComponents.SPACE)
                        .append(Component.translatable(translatableKeyDesc))
                        .withStyle(ChatFormatting.DARK_GRAY);

                components.add(insertIndex + 1, desc);
            }
        });
    }

    private static class ToggleTracker {
        private boolean isToggled = false;
        private boolean wasKeyDown = false;

        public boolean updateAndGet(boolean isKeyDown) {
            if (isKeyDown && !wasKeyDown) {
                isToggled = !isToggled;
            }
            wasKeyDown = isKeyDown;
            return isToggled;
        }
    }
}
