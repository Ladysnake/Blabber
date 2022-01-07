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

import com.google.common.collect.ImmutableList;
import io.github.ladysnake.blabber.impl.client.BlabberClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class DialogueScreenHandler extends ScreenHandler {
    private final DialogueStateMachine dialogue;

    public DialogueScreenHandler(int syncId, DialogueStateMachine dialogue) {
        this(BlabberRegistrar.DIALOGUE_SCREEN_HANDLER, syncId, dialogue);
    }

    public DialogueScreenHandler(@Nullable ScreenHandlerType<?> type, int syncId, DialogueStateMachine dialogue) {
        super(type, syncId);
        this.dialogue = dialogue;
    }

    public boolean isUnskippable() {
        return this.dialogue.isUnskippable();
    }

    public Text getCurrentText() {
        return this.dialogue.getCurrentText();
    }

    public ImmutableList<Text> getCurrentChoices() {
        return this.dialogue.getCurrentChoices();
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    public ChoiceResult makeChoice(int choice) {
        BlabberClient.sendDialogueActionMessage(choice);
        return this.dialogue.choose(choice, action -> {});
    }

    public void makeChoice(ServerPlayerEntity player, int choice) {
        ChoiceResult result = this.dialogue.choose(choice, action -> action.handle(player));
        if (result == ChoiceResult.END_DIALOGUE) {
            PlayerDialogueTracker.get(player).endDialogue();
        }
    }
}
