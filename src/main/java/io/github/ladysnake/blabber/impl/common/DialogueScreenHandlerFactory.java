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
package io.github.ladysnake.blabber.impl.common;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class DialogueScreenHandlerFactory implements ExtendedScreenHandlerFactory {
    private final DialogueStateMachine dialogue;
    private final Text displayName;

    public DialogueScreenHandlerFactory(DialogueStateMachine dialogue, Text displayName) {
        this.dialogue = dialogue;
        this.displayName = displayName;
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeIdentifier(this.dialogue.getId());
        buf.writeString(this.dialogue.getCurrentStateKey());
    }

    @Override
    public Text getDisplayName() {
        return this.displayName;
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new DialogueScreenHandler(BlabberRegistrar.DIALOGUE_SCREEN_HANDLER, syncId, this.dialogue);
    }
}
