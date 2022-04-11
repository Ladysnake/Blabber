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

import io.github.ladysnake.blabber.Blabber;
import io.github.ladysnake.blabber.impl.common.DialogueScreenHandler;
import io.github.ladysnake.elmendorf.GameTestUtil;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;

public final class BlabberTestSuite implements FabricGameTest {
    @GameTest(structureName = EMPTY_STRUCTURE)
    public void complete(TestContext ctx) {
        ServerPlayerEntity player = ctx.spawnServerPlayer(2, 2, 2);
        Blabber.startDialogue(player, new Identifier("babblings:remnant_choice"));
        GameTestUtil.assertTrue("startDialogue did not work", player.currentScreenHandler instanceof DialogueScreenHandler handler && handler.isUnskippable() && handler.getCurrentChoices().size() == 3);
        ctx.complete();
    }
}
