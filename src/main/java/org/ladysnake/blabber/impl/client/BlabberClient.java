/*
 * Blabber
 * Copyright (C) 2022-2024 Ladysnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; If not, see <https://www.gnu.org/licenses>.
 */
package org.ladysnake.blabber.impl.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.ladysnake.blabber.Blabber;
import org.ladysnake.blabber.api.client.BlabberDialogueScreen;
import org.ladysnake.blabber.api.client.BlabberScreenRegistry;
import org.ladysnake.blabber.impl.common.BlabberRegistrar;
import org.ladysnake.blabber.impl.common.DialogueRegistry;
import org.ladysnake.blabber.impl.common.DialogueScreenHandler;
import org.ladysnake.blabber.impl.common.model.DialogueLayout;
import org.ladysnake.blabber.impl.common.packets.ChoiceAvailabilityPacket;
import org.ladysnake.blabber.impl.common.packets.DialogueListPacket;
import org.ladysnake.blabber.impl.common.packets.SelectedDialogueStatePacket;

import java.util.HashMap;
import java.util.Map;

import static io.netty.buffer.Unpooled.buffer;

public final class BlabberClient implements ClientModInitializer {
    private static final Map<Identifier, HandledScreens.Provider<DialogueScreenHandler, BlabberDialogueScreen>> screenRegistry = new HashMap<>();

    @Override
    public void onInitializeClient() {
        BlabberScreenRegistry.register(DialogueLayout.CLASSIC_LAYOUT_ID, BlabberDialogueScreen::new);
        BlabberScreenRegistry.register(DialogueLayout.RPG_LAYOUT_ID, BlabberRpgDialogueScreen::new);
        HandledScreens.register(BlabberRegistrar.DIALOGUE_SCREEN_HANDLER, BlabberClient::createDialogueScreen);
        ClientConfigurationNetworking.registerGlobalReceiver(DialogueListPacket.TYPE, (packet, responseSender) -> DialogueRegistry.setClientIds(packet.dialogueIds()));
        ClientPlayNetworking.registerGlobalReceiver(DialogueListPacket.TYPE, (packet, player, responseSender) -> DialogueRegistry.setClientIds(packet.dialogueIds()));
        ClientPlayNetworking.registerGlobalReceiver(ChoiceAvailabilityPacket.TYPE, (packet, player, responseSender) -> {
            if (player.currentScreenHandler instanceof DialogueScreenHandler dialogueScreenHandler) {
                dialogueScreenHandler.handleAvailabilityUpdate(packet);
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(SelectedDialogueStatePacket.TYPE, (packet, player, responseSender) -> {
            if (player.currentScreenHandler instanceof DialogueScreenHandler dialogueScreenHandler) {
                dialogueScreenHandler.setCurrentState(packet.stateKey());
            }
        });
    }

    public static void registerLayoutScreen(
            Identifier layoutId,
            HandledScreens.Provider<DialogueScreenHandler, BlabberDialogueScreen> screenProvider
    ) {
        screenRegistry.put(layoutId, screenProvider);
    }

    private static BlabberDialogueScreen createDialogueScreen(DialogueScreenHandler handler, PlayerInventory inventory, Text title) {
        Identifier layoutType = handler.getLayout().type();
        var provider = screenRegistry.get(layoutType);
        if (provider != null) {
            return provider.create(handler, inventory, title);
        }
        Blabber.LOGGER.error("(Blabber) No screen provider found for {}", layoutType);
        return new BlabberDialogueScreen(handler, inventory, title);
    }

    public static void sendDialogueActionMessage(int choice) {
        PacketByteBuf buf = new PacketByteBuf(buffer());
        buf.writeByte(choice);
        ClientPlayNetworking.send(BlabberRegistrar.DIALOGUE_ACTION, buf);
    }
}
