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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import io.github.ladysnake.blabber.Blabber;
import io.github.ladysnake.blabber.DialogueAction;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

public final class DialogueStateMachine {

    private final BiMap<String, DialogueState> states;
    private final Identifier id;
    private final boolean unskippable;
    private @Nullable DialogueState currentState;
    private ImmutableList<Text> currentChoices = ImmutableList.of();

    public DialogueStateMachine(DialogueTemplate template, Identifier id) {
        this(template, id, template.start());
    }

    private DialogueStateMachine(DialogueTemplate template, Identifier id, String start) {
        this.states = HashBiMap.create(template.states());
        this.id = id;
        this.unskippable = template.unskippable();
        this.selectState(start);
    }

    public static DialogueStateMachine fromPacket(World world, PacketByteBuf buf) {
        Identifier id = buf.readIdentifier();
        String currentState = buf.readString();

        DialogueStateMachine dialogue = DialogueRegistryImpl.INSTANCE.startDialogue(world, id);
        dialogue.selectState(currentState);
        return dialogue;
    }

    private DialogueState getCurrentState() {
        return Objects.requireNonNull(this.currentState, () -> this + " has not been initialized !");
    }

    public Identifier getId() {
        return this.id;
    }

    public Text getCurrentText() {
        return this.getCurrentState().text();
    }

    public ImmutableList<Text> getCurrentChoices() {
        return this.currentChoices;
    }

    public ChoiceResult choose(int choice, Consumer<DialogueAction> actionRunner) {
        DialogueState nextState = this.selectState(this.getCurrentState().getNextState(choice));
        nextState.action().map(Blabber::getAction).ifPresent(actionRunner);
        return nextState.type();
    }

    public DialogueState selectState(String state) {
        if (!this.states.containsKey(state)) {
            throw new IllegalArgumentException(state + " is not an available dialogue state");
        }
        this.currentState = this.states.get(state);
        this.currentChoices = this.currentState.getAvailableChoices();
        return this.currentState;
    }

    public String getCurrentStateKey() {
        return this.states.inverse().get(this.getCurrentState());
    }

    public boolean isUnskippable() {
        return this.unskippable;
    }

    @Override
    public String toString() {
        return "DialogueStateMachine" + this.states;
    }

}
