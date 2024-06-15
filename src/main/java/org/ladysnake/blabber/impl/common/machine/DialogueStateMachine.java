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
package org.ladysnake.blabber.impl.common.machine;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import it.unimi.dsi.fastutil.ints.Int2BooleanMap;
import it.unimi.dsi.fastutil.ints.Int2BooleanMaps;
import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.blabber.Blabber;
import org.ladysnake.blabber.api.illustration.DialogueIllustration;
import org.ladysnake.blabber.api.layout.DialogueLayout;
import org.ladysnake.blabber.impl.common.InstancedDialogueAction;
import org.ladysnake.blabber.impl.common.model.DialogueChoice;
import org.ladysnake.blabber.impl.common.model.DialogueChoiceCondition;
import org.ladysnake.blabber.impl.common.model.DialogueState;
import org.ladysnake.blabber.impl.common.model.DialogueTemplate;
import org.ladysnake.blabber.impl.common.model.UnavailableAction;
import org.ladysnake.blabber.impl.common.model.UnavailableDisplay;
import org.ladysnake.blabber.impl.common.packets.ChoiceAvailabilityPayload;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

public final class DialogueStateMachine {
    private static final DynamicCommandExceptionType INVALID_PREDICATE_EXCEPTION = new DynamicCommandExceptionType(id -> Text.translatable("blabber:commands.dialogue.start.predicate.invalid", String.valueOf(id)));

    private final Identifier id;
    private final DialogueTemplate template;
    private final Map<String, Int2BooleanMap> conditionalChoices;
    private @Nullable String currentStateKey;
    private ImmutableList<AvailableChoice> availableChoices = ImmutableList.of();

    public DialogueStateMachine(Identifier id, DialogueTemplate template, @Nullable String start) {
        this.template = template;
        this.id = id;
        this.conditionalChoices = gatherConditionalChoices(template);
        this.selectState(start == null ? template.start() : start);
    }

    private static Map<String, Int2BooleanMap> gatherConditionalChoices(DialogueTemplate template) {
        Map<String, Int2BooleanMap> conditionalChoices = new HashMap<>();
        for (Map.Entry<String, DialogueState> entry : template.states().entrySet()) {
            List<DialogueChoice> choices = entry.getValue().choices();
            Int2BooleanMap m = new Int2BooleanOpenHashMap();
            for (int i = 0; i < choices.size(); i++) {
                DialogueChoice choice = choices.get(i);
                if (choice.condition().isPresent()) {
                    m.put(i, false);
                }
            }
            if (!m.isEmpty()) {
                conditionalChoices.put(entry.getKey(), m);
            }
        }
        return conditionalChoices;
    }

    public static final PacketCodec<RegistryByteBuf, DialogueStateMachine> PACKET_CODEC = PacketCodec.tuple(
            Identifier.PACKET_CODEC, DialogueStateMachine::getId,
            DialogueTemplate.PACKET_CODEC, m -> m.template,
            PacketCodecs.STRING, DialogueStateMachine::getCurrentStateKey,
            DialogueStateMachine::new
    );

    private Map<String, DialogueState> getStates() {
        return this.template.states();
    }

    private DialogueState getCurrentState() {
        return getStates().get(this.getCurrentStateKey());
    }

    public Identifier getId() {
        return this.id;
    }

    public DialogueLayout<?> getLayout() {
        return this.template.layout();
    }

    public Text getCurrentText() {
        return this.getCurrentState().text();
    }

    public List<String> getCurrentIllustrations() {
        return this.getCurrentState().illustrations();
    }

    public Map<String, DialogueIllustration> getIllustrations() {
        return this.template.illustrations();
    }

    public ImmutableList<AvailableChoice> getAvailableChoices() {
        return this.availableChoices;
    }

    public boolean hasConditions() {
        return !this.conditionalChoices.isEmpty();
    }

    public @Nullable ChoiceAvailabilityPayload updateConditions(LootContext context) throws CommandSyntaxException {
        ChoiceAvailabilityPayload ret = null;
        for (Map.Entry<String, Int2BooleanMap> conditionalState : this.conditionalChoices.entrySet()) {
            List<DialogueChoice> availableChoices = getStates().get(conditionalState.getKey()).choices();
            for (Int2BooleanMap.Entry conditionalChoice : conditionalState.getValue().int2BooleanEntrySet()) {
                RegistryKey<LootCondition> predicateId = availableChoices.get(conditionalChoice.getIntKey()).condition().orElseThrow().predicate();
                Optional<LootCondition> condition = context.getWorld().getServer()
                        .getReloadableRegistries()
                        .createRegistryLookup()
                        .getOptionalEntry(RegistryKeys.PREDICATE, predicateId)
                        .map(RegistryEntry::value);
                if (condition.isEmpty()) throw INVALID_PREDICATE_EXCEPTION.create(predicateId);
                boolean testResult = runTest(condition.get(), context);
                if (testResult != conditionalChoice.setValue(testResult)) {
                    if (ret == null) ret = new ChoiceAvailabilityPayload();
                    ret.markUpdated(conditionalState.getKey(), conditionalChoice.getIntKey(), testResult);
                }
            }
        }
        return ret;
    }

    public ChoiceAvailabilityPayload createFullAvailabilityUpdatePacket() {
        return new ChoiceAvailabilityPayload(this.conditionalChoices);
    }

    private static boolean runTest(LootCondition condition, LootContext context) {
        LootContext.Entry<LootCondition> lootEntry = LootContext.predicate(condition);
        context.markActive(lootEntry);
        boolean testResult = condition.test(context);
        context.markInactive(lootEntry);
        return testResult;
    }

    public void applyAvailabilityUpdate(ChoiceAvailabilityPayload payload) {
        payload.updatedChoices().forEach((stateKey, choiceIndices) -> {
            Int2BooleanMap conditionalState = this.conditionalChoices.get(stateKey);
            for (Int2BooleanMap.Entry updatedChoice : choiceIndices.int2BooleanEntrySet()) {
                conditionalState.put(updatedChoice.getIntKey(), updatedChoice.getBooleanValue());
            }
            this.availableChoices = this.rebuildAvailableChoices();
        });
    }

    public boolean isAvailable(int choice) {
        return this.conditionalChoices.getOrDefault(this.currentStateKey, Int2BooleanMaps.EMPTY_MAP).getOrDefault(choice, true);
    }

    public Optional<InstancedDialogueAction<?>> getStartAction() {
        return this.getStates().get(this.template.start()).action();
    }

    /**
     * @throws IllegalStateException if making an invalid choice
     */
    public ChoiceResult choose(int choice) {
        if (choice == AvailableChoice.ESCAPE_HATCH.originalChoiceIndex() && IntStream.range(0, this.getCurrentState().choices().size()).noneMatch(this::isAvailable)) {
            Blabber.LOGGER.warn("(Blabber) Escape hatch used on {}#{}", this.getId(), this.currentStateKey);
            return ChoiceResult.DEFAULT_END;
        }

        this.validateChoice(choice);
        return this.selectState(this.getCurrentState().getNextState(choice));
    }

    private void validateChoice(int choice) {
        if (choice < 0 || choice >= this.getCurrentState().choices().size()) {
            throw new IllegalStateException("only choices 0 to %d available".formatted(this.getCurrentState().choices().size() - 1));
        } else if (!this.isAvailable(choice)) {
            throw new IllegalStateException("condition %s is not fulfilled".formatted(this.getCurrentState().choices().get(choice).condition()));
        }
    }

    public DialogueState selectState(String state) {
        if (!this.getStates().containsKey(state)) {
            throw new IllegalArgumentException(state + " is not an available dialogue state");
        }
        this.currentStateKey = state;
        DialogueState currentState = this.getStates().get(state);
        this.availableChoices = rebuildAvailableChoices();
        return currentState;
    }

    private ImmutableList<AvailableChoice> rebuildAvailableChoices() {
        ImmutableList.Builder<AvailableChoice> newChoices = ImmutableList.builder();
        List<DialogueChoice> availableChoices = this.getCurrentState().choices();
        boolean allUnavailable = true;
        for (int i = 0; i < availableChoices.size(); i++) {
            DialogueChoice c = availableChoices.get(i);
            boolean available = conditionalChoices.getOrDefault(this.currentStateKey, Int2BooleanMaps.EMPTY_MAP).getOrDefault(i, true);
            Optional<UnavailableAction> whenUnavailable = c.condition().map(DialogueChoiceCondition::whenUnavailable);
            allUnavailable &= !available;
            if (available || (whenUnavailable.filter(t -> t.display() == UnavailableDisplay.GRAYED_OUT).isPresent())) {
                newChoices.add(new AvailableChoice(
                        i,
                        c.text(),
                        c.illustrations(),
                        whenUnavailable.filter(t -> !available).flatMap(a -> a.message().or(DialogueStateMachine::defaultLockedMessage))
                ));
            }
        }
        if (allUnavailable) {
            Blabber.LOGGER.warn("[Blabber] No choice available in state '{}' of {} ({} were all unavailable)", this.currentStateKey, this.id, availableChoices);
            newChoices.add(AvailableChoice.ESCAPE_HATCH);
        }
        return newChoices.build();
    }

    private static Optional<Text> defaultLockedMessage() {
        return Optional.of(Text.translatable("blabber:dialogue.locked_choice"));
    }

    public String getCurrentStateKey() {
        return Objects.requireNonNull(this.currentStateKey, () -> this + " has not been initialized !");
    }

    public boolean isUnskippable() {
        return this.template.unskippable();
    }

    @Override
    public String toString() {
        return "DialogueStateMachine" + this.getStates();
    }
}
