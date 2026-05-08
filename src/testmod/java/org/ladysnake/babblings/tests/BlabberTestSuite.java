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
package org.ladysnake.babblings.tests;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.ladysnake.blabber.Blabber;
import org.ladysnake.blabber.impl.common.DialogueRegistry;
import org.ladysnake.blabber.impl.common.DialogueScreenHandler;

import java.util.Set;

public final class BlabberTestSuite {
    @GameTest
    public void nominal(GameTestHelper ctx) {
        ServerPlayer player = ctx.spawnServerPlayer(2, 2, 2);
        Blabber.startDialogue(player, Identifier.parse("babblings:remnant_choice"));
        ctx.assertTrue("startDialogue should work", player.containerMenu instanceof DialogueScreenHandler handler && handler.isUnskippable() && handler.getCurrentStateKey().equals("introduction") && handler.getAvailableChoices().size() == 3);
        ((DialogueScreenHandler) player.containerMenu).makeChoice(player, 0);
        ctx.assertTrue("choice 0 should work", player.containerMenu instanceof DialogueScreenHandler handler && handler.getCurrentStateKey().equals("explanation") && handler.getAvailableChoices().size() == 1);
        ((DialogueScreenHandler) player.containerMenu).makeChoice(player, 420);
        ctx.assertTrue("choice 420 should be ignored", player.containerMenu instanceof DialogueScreenHandler handler && handler.getCurrentStateKey().equals("explanation") && handler.getAvailableChoices().size() == 1);
        ((DialogueScreenHandler) player.containerMenu).makeChoice(player, 0);
        ((DialogueScreenHandler) player.containerMenu).makeChoice(player, 1);
        ctx.assertTrue("choice 1 should work", player.containerMenu instanceof DialogueScreenHandler handler && handler.getCurrentStateKey().equals("interested") && handler.getAvailableChoices().size() == 2);
        ((DialogueScreenHandler) player.containerMenu).makeChoice(player, 1);
        ((DialogueScreenHandler) player.containerMenu).makeChoice(player, 1);
        ctx.assertTrue("dialogue should end", player.containerMenu == player.inventoryMenu);
        ctx.succeed();
    }

    @GameTest
    public void registryGetsPopulated(GameTestHelper ctx) {
        ctx.assertTrue("dialogue registry should match expected state (was " + DialogueRegistry.getIds() + ")",
                DialogueRegistry.getIds().equals(Set.of(
                        Identifier.parse("babblings:illustration_tests"),
                        Identifier.parse("babblings:mountain_king"),
                        Identifier.parse("babblings:perception_check"),
                        Identifier.parse("babblings:remnant_choice"),
                        Identifier.parse("babblings:rpg_layout_gametest")
                    )
                ));
        ctx.succeed();
    }

    @GameTest
    public void availableChoicesCanGetSelected(GameTestHelper ctx) {
        ServerPlayer player = ctx.spawnServerPlayer(2, 2, 2);
        Blabber.startDialogue(player, Identifier.parse("babblings:mountain_king"), player);
        ((DialogueScreenHandler) player.containerMenu).makeChoice(player, 1);
        ((DialogueScreenHandler) player.containerMenu).makeChoice(player, 0);
        ctx.assertTrue("dialogue should end", player.containerMenu == player.inventoryMenu);
        ctx.succeed();
    }

    @GameTest
    public void unavailableChoicesCannotGetSelected(GameTestHelper ctx) {
        ServerPlayer player = ctx.spawnServerPlayer(2, 2, 2);
        player.setHealth(10f);
        Blabber.startDialogue(player, Identifier.parse("babblings:mountain_king"), player);
        ((DialogueScreenHandler) player.containerMenu).makeChoice(player, 1);
        ctx.assertTrue("dialogue should be at state bargain", player.containerMenu instanceof DialogueScreenHandler handler && handler.getCurrentStateKey().equals("bargain"));
        ((DialogueScreenHandler) player.containerMenu).makeChoice(player, 0);
        ctx.assertTrue("unavailable choice 0 should be ignored", player.containerMenu instanceof DialogueScreenHandler handler && handler.getCurrentStateKey().equals("bargain"));
        ((DialogueScreenHandler) player.containerMenu).makeChoice(player, 2);
        ctx.assertTrue("dialogue should be at state friendship", player.containerMenu instanceof DialogueScreenHandler handler && handler.getCurrentStateKey().equals("friendship"));
        ctx.succeed();
    }
}
