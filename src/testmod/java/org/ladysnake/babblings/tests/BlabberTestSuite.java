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
package org.ladysnake.babblings.tests;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import org.ladysnake.blabber.Blabber;
import org.ladysnake.blabber.impl.common.DialogueRegistry;
import org.ladysnake.blabber.impl.common.DialogueScreenHandler;
import org.ladysnake.elmendorf.GameTestUtil;

import java.util.Set;

public final class BlabberTestSuite implements FabricGameTest {
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void nominal(TestContext ctx) {
        ServerPlayerEntity player = ctx.spawnServerPlayer(2, 2, 2);
        Blabber.startDialogue(player, new Identifier("babblings:remnant_choice"));
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
                DialogueRegistry.getIds().equals(Set.of(
                        new Identifier("babblings:mountain_king"),
                        new Identifier("babblings:perception_check"),
                        new Identifier("babblings:remnant_choice")
                    )
                ));
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
        GameTestUtil.assertTrue("dialogue should be at state bargain", player.currentScreenHandler instanceof DialogueScreenHandler handler && handler.getCurrentStateKey().equals("bargain"));
        ((DialogueScreenHandler) player.currentScreenHandler).makeChoice(player, 0);
        GameTestUtil.assertTrue("unavailable choice 0 should be ignored", player.currentScreenHandler instanceof DialogueScreenHandler handler && handler.getCurrentStateKey().equals("bargain"));
        ((DialogueScreenHandler) player.currentScreenHandler).makeChoice(player, 2);
        GameTestUtil.assertTrue("dialogue should be at state friendship", player.currentScreenHandler instanceof DialogueScreenHandler handler && handler.getCurrentStateKey().equals("friendship"));
        ctx.complete();
    }
}
