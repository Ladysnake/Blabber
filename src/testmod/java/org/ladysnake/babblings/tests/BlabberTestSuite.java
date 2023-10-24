/*
 * Blabber
 * Copyright (C) 2022-2023 Ladysnake
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
package org.ladysnake.babblings.tests;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import io.github.ladysnake.elmendorf.GameTestUtil;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import org.ladysnake.blabber.Blabber;
import org.ladysnake.blabber.impl.common.BlabberRegistrar;
import org.ladysnake.blabber.impl.common.DialogueScreenHandler;
import org.ladysnake.blabber.impl.common.model.DialogueTemplate;

import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;

public final class BlabberTestSuite implements FabricGameTest {
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void nominal(TestContext ctx) {
        ServerPlayerEntity player = ctx.spawnServerPlayer(2, 2, 2);
        Blabber.startDialogue(player, new Identifier("babblings:remnant_choice_builtin"));
        GameTestUtil.assertTrue("startDialogue should work", player.currentScreenHandler instanceof DialogueScreenHandler handler && handler.isUnskippable() && handler.getCurrentStateKey().equals("introduction") && handler.getAvailableChoices().size() == 3);
        ((DialogueScreenHandler) player.currentScreenHandler).makeChoice(player, 0);
        GameTestUtil.assertTrue("choice 0 should work", player.currentScreenHandler instanceof DialogueScreenHandler handler && handler.getCurrentStateKey().equals("explanation") && handler.getAvailableChoices().size() == 1);
        ((DialogueScreenHandler) player.currentScreenHandler).makeChoice(player, 420);
        GameTestUtil.assertTrue("choice 420 should be ignored", player.currentScreenHandler instanceof DialogueScreenHandler handler && handler.getCurrentStateKey().equals("explanation") && handler.getAvailableChoices().size() == 1);
        ((DialogueScreenHandler) player.currentScreenHandler).makeChoice(player, 0);
        ((DialogueScreenHandler) player.currentScreenHandler).makeChoice(player, 1);
        GameTestUtil.assertTrue("choice 1 should work", player.currentScreenHandler instanceof DialogueScreenHandler handler && handler.getCurrentStateKey().equals("interested") && handler.getAvailableChoices().size() == 2);
        ((DialogueScreenHandler) player.currentScreenHandler).makeChoice(player, 1);
        ((DialogueScreenHandler) player.currentScreenHandler).makeChoice(player, 1);
        GameTestUtil.assertTrue("dialogue should end", player.currentScreenHandler == player.playerScreenHandler);
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void registryGetsPopulated(TestContext ctx) {
        GameTestUtil.assertTrue("dialogue registry should match expected state",
                ctx.getWorld().getRegistryManager().get(BlabberRegistrar.DIALOGUE_REGISTRY_KEY).streamEntries().map(RegistryEntry.Reference::getKey).map(k -> k.orElseThrow().getValue()).sorted().toList().equals(List.of(
                        new Identifier("babblings:mountain_king"),
                        new Identifier("babblings:remnant_choice"),
                        new Identifier("babblings:remnant_choice_builtin")
                    )
                ));
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void validationFailsOnIncompleteDialogue(TestContext ctx) {
        DataResult<Pair<DialogueTemplate, JsonElement>> result = DialogueTemplate.CODEC.decode(JsonOps.INSTANCE, new Gson().fromJson(new InputStreamReader(Objects.requireNonNull(BlabberTestSuite.class.getResourceAsStream("/incomplete_dialogue.json"))), JsonElement.class));
        GameTestUtil.assertTrue("Dialogue validation should detect incomplete dialogues", result.error().filter(it -> it.message().equals("(Blabber) a has no available choices but is not an end state")).isPresent());
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void validationLogsConditionalDialogues(TestContext ctx) {
        DialogueTemplate.CODEC.decode(JsonOps.INSTANCE, new Gson().fromJson(new InputStreamReader(Objects.requireNonNull(BlabberTestSuite.class.getResourceAsStream("/conditional_dialogue.json"))), JsonElement.class));
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void validationFailsOnLoopingDialogue(TestContext ctx) {
        DataResult<Pair<DialogueTemplate, JsonElement>> result = DialogueTemplate.CODEC.decode(JsonOps.INSTANCE, new Gson().fromJson(new InputStreamReader(Objects.requireNonNull(BlabberTestSuite.class.getResourceAsStream("/looping_dialogue.json"))), JsonElement.class));
        GameTestUtil.assertTrue("Dialogue validation should detect looping dialogues", result.error().filter(it -> it.message().equals("(Blabber) a does not have any path to the end of the dialogue")).isPresent());
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void availableChoicesCanGetSelected(TestContext ctx) {
        ServerPlayerEntity player = ctx.spawnServerPlayer(2, 2, 2);
        Blabber.startDialogue(player, new Identifier("babblings:mountain_king"));
        ((DialogueScreenHandler) player.currentScreenHandler).makeChoice(player, 1);
        ((DialogueScreenHandler) player.currentScreenHandler).makeChoice(player, 0);
        GameTestUtil.assertTrue("dialogue should end", player.currentScreenHandler == player.playerScreenHandler);
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void unavailableChoicesCannotGetSelected(TestContext ctx) {
        ServerPlayerEntity player = ctx.spawnServerPlayer(2, 2, 2);
        player.setHealth(10f);
        Blabber.startDialogue(player, new Identifier("babblings:mountain_king"));
        ((DialogueScreenHandler) player.currentScreenHandler).makeChoice(player, 1);
        ((DialogueScreenHandler) player.currentScreenHandler).makeChoice(player, 0);
        GameTestUtil.assertTrue("unavailable choice 0 should be ignored", player.currentScreenHandler instanceof DialogueScreenHandler handler && handler.getCurrentStateKey().equals("bargain"));
        ((DialogueScreenHandler) player.currentScreenHandler).makeChoice(player, 2);
        GameTestUtil.assertTrue("dialogue should end", player.currentScreenHandler == player.playerScreenHandler);
        ctx.complete();
    }
}
