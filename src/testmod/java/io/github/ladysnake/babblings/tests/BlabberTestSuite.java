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
package io.github.ladysnake.babblings.tests;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import io.github.ladysnake.blabber.Blabber;
import io.github.ladysnake.blabber.impl.common.BlabberRegistrar;
import io.github.ladysnake.blabber.impl.common.DialogueScreenHandler;
import io.github.ladysnake.blabber.impl.common.DialogueTemplate;
import io.github.ladysnake.elmendorf.GameTestUtil;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.RegistryEntry;

import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;

public final class BlabberTestSuite implements FabricGameTest {
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void nominal(TestContext ctx) {
        ServerPlayerEntity player = ctx.spawnServerPlayer(2, 2, 2);
        Blabber.startDialogue(player, new Identifier("babblings:remnant_choice_builtin"));
        GameTestUtil.assertTrue("startDialogue did not work", player.currentScreenHandler instanceof DialogueScreenHandler handler && handler.isUnskippable() && handler.getCurrentChoices().size() == 3);
        ((DialogueScreenHandler) player.currentScreenHandler).makeChoice(player, 0);
        GameTestUtil.assertTrue("choice 0 did not work", player.currentScreenHandler instanceof DialogueScreenHandler handler && handler.getCurrentChoices().size() == 1);
        ((DialogueScreenHandler) player.currentScreenHandler).makeChoice(player, 420);
        GameTestUtil.assertTrue("choice 420 was not ignored", player.currentScreenHandler instanceof DialogueScreenHandler handler && handler.getCurrentChoices().size() == 1);
        ((DialogueScreenHandler) player.currentScreenHandler).makeChoice(player, 0);
        ((DialogueScreenHandler) player.currentScreenHandler).makeChoice(player, 1);
        GameTestUtil.assertTrue("choice 1 did not work", player.currentScreenHandler instanceof DialogueScreenHandler handler && handler.getCurrentChoices().size() == 2);
        ((DialogueScreenHandler) player.currentScreenHandler).makeChoice(player, 1);
        ((DialogueScreenHandler) player.currentScreenHandler).makeChoice(player, 1);
        GameTestUtil.assertTrue("dialogue did not end", player.currentScreenHandler == player.playerScreenHandler);
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void registryGetsPopulated(TestContext ctx) {
        GameTestUtil.assertTrue("dialogue registry does not match expected state",
                ctx.getWorld().getRegistryManager().get(BlabberRegistrar.DIALOGUE_REGISTRY_KEY).streamEntries().map(RegistryEntry.Reference::getKey).map(k -> k.orElseThrow().getValue()).sorted().toList().equals(List.of(
                        new Identifier("babblings:mountain_king"),
                        new Identifier("babblings:remnant_choice"))
                ));
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void validationFailsOnIncompleteDialogue(TestContext ctx) {
        DataResult<Pair<DialogueTemplate, JsonElement>> result = DialogueTemplate.CODEC.decode(JsonOps.INSTANCE, new Gson().fromJson(new InputStreamReader(Objects.requireNonNull(BlabberTestSuite.class.getResourceAsStream("/incomplete_dialogue.json"))), JsonElement.class));
        GameTestUtil.assertTrue("Dialogue validation does not detect incomplete dialogues", result.error().filter(it -> it.message().equals("(Blabber) a has no available choices but is not an end state")).isPresent());
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void validationFailsOnLoopingDialogue(TestContext ctx) {
        DataResult<Pair<DialogueTemplate, JsonElement>> result = DialogueTemplate.CODEC.decode(JsonOps.INSTANCE, new Gson().fromJson(new InputStreamReader(Objects.requireNonNull(BlabberTestSuite.class.getResourceAsStream("/looping_dialogue.json"))), JsonElement.class));
        GameTestUtil.assertTrue("Dialogue validation does not detect looping dialogues", result.error().filter(it -> it.message().equals("(Blabber) a does not have any path to the end of the dialogue")).isPresent());
        ctx.complete();
    }
}
