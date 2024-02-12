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
package org.ladysnake.blabber.impl.common;

import com.demonwav.mcdev.annotations.CheckEnv;
import com.demonwav.mcdev.annotations.Env;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.blabber.Blabber;
import org.ladysnake.blabber.api.DialogueIllustration;
import org.ladysnake.blabber.impl.client.BlabberClient;
import org.ladysnake.blabber.impl.common.machine.AvailableChoice;
import org.ladysnake.blabber.impl.common.machine.DialogueStateMachine;
import org.ladysnake.blabber.impl.common.model.ChoiceResult;
import org.ladysnake.blabber.impl.common.model.DialogueLayout;
import org.ladysnake.blabber.impl.common.packets.ChoiceAvailabilityPacket;

import java.util.List;
import java.util.Map;

public class DialogueScreenHandler extends ScreenHandler {
    private final DialogueStateMachine dialogue;
    private final @Nullable Entity interlocutor;

    public DialogueScreenHandler(int syncId, DialogueStateMachine dialogue, @Nullable Entity interlocutor) {
        this(BlabberRegistrar.DIALOGUE_SCREEN_HANDLER, syncId, dialogue, interlocutor);
    }

    public DialogueScreenHandler(@Nullable ScreenHandlerType<?> type, int syncId, DialogueStateMachine dialogue, @Nullable Entity interlocutor) {
        super(type, syncId);
        this.dialogue = dialogue;
        this.interlocutor = interlocutor;
    }

    @SuppressWarnings("unused") // may be useful for custom layouts one day
    public @Nullable Entity getInterlocutor() {
        return interlocutor;
    }

    public DialogueLayout getLayout() {
        return this.dialogue.getLayout();
    }

    public boolean isUnskippable() {
        return this.dialogue.isUnskippable();
    }

    public Text getCurrentText() {
        return this.dialogue.getCurrentText();
    }

    public List<String> getCurrentIllustrations() {
        return this.dialogue.getCurrentIllustrations();
    }

    @Nullable
    public DialogueIllustration getIllustration(String name) {
        return this.dialogue.getIllustration(name);
    }

    public ImmutableList<AvailableChoice> getAvailableChoices() {
        return this.dialogue.getAvailableChoices();
    }

    public String getCurrentStateKey() {
        return this.dialogue.getCurrentStateKey();
    }

    public void setCurrentState(String key) {
        this.dialogue.selectState(key);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    public void handleAvailabilityUpdate(ChoiceAvailabilityPacket packet) {
        this.dialogue.applyAvailabilityUpdate(packet);
    }

    @CheckEnv(Env.CLIENT)
    public ChoiceResult makeChoice(int choice) {
        int originalChoiceIndex = this.getAvailableChoices().get(choice).originalChoiceIndex();
        ChoiceResult result = this.dialogue.choose(originalChoiceIndex, action -> {});
        BlabberClient.sendDialogueActionMessage(originalChoiceIndex);
        return result;
    }

    public boolean makeChoice(ServerPlayerEntity player, int choice) {
        try {  // Can't throw here, could cause trouble with a bad packet
            ChoiceResult result = this.dialogue.choose(choice, action -> action.handle(player, this.interlocutor));
            if (result == ChoiceResult.END_DIALOGUE) {
                PlayerDialogueTracker.get(player).endDialogue();
            }

            return true;
        } catch (IllegalStateException e) {
            Blabber.LOGGER.error("{} made invalid choice {} in {}#{}: {}", player.getNameForScoreboard(), choice, this.dialogue.getId(), this.getCurrentStateKey(), e.getMessage());
            return false;
        }
    }
}
