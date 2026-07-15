package io.github.thesmoothrere.enchtell.client;

import io.github.thesmoothrere.enchtell.Constants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
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

    @Override
    public void onInitializeClient() {
        Constants.LOGGER.info(Constants.MOD_NAME + " client initialized!");
        ItemTooltipCallback.EVENT.register(EnchTellClient::onTooltip);
    }

    private static void onTooltip(ItemStack itemStack, Item.TooltipContext tooltipContext,
                                  TooltipFlag tooltipFlag, List<Component> components) {

        ItemEnchantments normalEnchantments = itemStack.get(DataComponents.ENCHANTMENTS);
        if (normalEnchantments != null && !normalEnchantments.isEmpty()) {
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
}
