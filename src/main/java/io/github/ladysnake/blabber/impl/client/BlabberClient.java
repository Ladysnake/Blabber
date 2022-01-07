/*
 * Blabber
 * Copyright (C) 2022 Ladysnake
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
package io.github.ladysnake.blabber.impl.client;

import io.github.ladysnake.blabber.impl.common.BlabberRegistrar;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.network.PacketByteBuf;

import static io.netty.buffer.Unpooled.buffer;

public final class BlabberClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ScreenRegistry.register(BlabberRegistrar.DIALOGUE_SCREEN_HANDLER, CutsceneDialogueScreen::new);
    }

    public static void sendDialogueActionMessage(int choice) {
        PacketByteBuf buf = new PacketByteBuf(buffer());
        buf.writeByte(choice);
        ClientPlayNetworking.send(BlabberRegistrar.DIALOGUE_ACTION, buf);
    }
}
