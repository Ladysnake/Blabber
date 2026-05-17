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

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotComparisonAlgorithm;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotComparisonOptions;
import org.ladysnake.blabber.impl.client.BlabberRpgDialogueScreen;

public final class BlabberClientTestSuite implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (var singleplayer = context.worldBuilder().create()) {
            singleplayer.getClientLevel().waitForChunksDownload();

            singleplayer.getServer().runCommand("blabber dialogue start babblings:rpg_layout_gametest @p");
            context.waitForScreen(BlabberRpgDialogueScreen.class);
            // Full screen (may miss details due to fuzzy matching)
            context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("rpg_dialogue_illustration_slots").save());
            // Details
            TestScreenshotComparisonAlgorithm detailCmpAlgo = TestScreenshotComparisonAlgorithm.meanSquaredDifference(0.002f);
            context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("rpg_dialogue_illustration_slots_left").withAlgorithm(detailCmpAlgo).withRegion(0, 100, 260, 290));
            context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("rpg_dialogue_illustration_slots_right").withAlgorithm(detailCmpAlgo).withRegion(600, 160, 140, 160));
        }
    }
}
