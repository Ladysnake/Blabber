/*
 * Blabber
 * Copyright (C) 2022-2026 Ladysnake
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
package org.ladysnake.blabber.impl.common;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.blabber.impl.common.machine.DialogueStateMachine;
import org.ladysnake.blabber.impl.common.packets.ChoiceAvailabilityPayload;

import java.util.Optional;

public class DialogueScreenHandlerFactory implements ExtendedScreenHandlerFactory<DialogueScreenHandlerFactory.DialogueOpeningData> {
    private static final Component DEFAULT_NAME = Component.translatable("blabber:container.dialogue");
    private final DialogueStateMachine dialogue;
    private final @Nullable Entity interlocutor;

    public DialogueScreenHandlerFactory(DialogueStateMachine dialogue, @Nullable Entity interlocutor) {
        this.dialogue = dialogue;
        this.interlocutor = interlocutor;
    }

    @Override
    public Component getDisplayName() {
        return this.dialogue.getName().orElseGet(() -> interlocutor == null ? DEFAULT_NAME : Component.translatable("blabber:container.dialogue_with_interlocutor", interlocutor.getName()));
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new DialogueScreenHandler(BlabberRegistrar.DIALOGUE_SCREEN_HANDLER, syncId, this.dialogue, this.interlocutor);
    }

    @Override
    public DialogueOpeningData getScreenOpeningData(ServerPlayer player) {
        return new DialogueOpeningData(
                this.dialogue,
                Optional.ofNullable(this.interlocutor).map(Entity::getId),
                this.dialogue.createFullAvailabilityUpdatePacket()
        );
    }

    public record DialogueOpeningData(DialogueStateMachine dialogue, Optional<Integer> interlocutorId,
                                      ChoiceAvailabilityPayload availableChoices) {
        public static final StreamCodec<RegistryFriendlyByteBuf, DialogueOpeningData> PACKET_CODEC = StreamCodec.composite(
                DialogueStateMachine.PACKET_CODEC, DialogueOpeningData::dialogue,
                ByteBufCodecs.VAR_INT.apply(ByteBufCodecs::optional), DialogueOpeningData::interlocutorId,
                ChoiceAvailabilityPayload.PACKET_CODEC, DialogueOpeningData::availableChoices,
                DialogueOpeningData::new
        );
    }
}
