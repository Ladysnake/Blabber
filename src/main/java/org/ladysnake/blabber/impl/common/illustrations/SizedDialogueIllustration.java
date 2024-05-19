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
package org.ladysnake.blabber.impl.common.illustrations;

import org.ladysnake.blabber.api.DialogueIllustration;
import org.ladysnake.blabber.impl.common.model.IllustrationAnchor;

public interface SizedDialogueIllustration extends DialogueIllustration {
    IllustrationAnchor anchor();
    int x();
    int y();
    int width();
    int height();

    default int minX(PositionTransform positionTransform) {
        return positionTransform.transformX(anchor(), x());
    }

    default int minY(PositionTransform positionTransform) {
        return positionTransform.transformY(anchor(), y());
    }

    default int maxX(PositionTransform positionTransform) {
        return minX(positionTransform) + width();
    }

    default int maxY(PositionTransform positionTransform) {
        return minY(positionTransform) + height();
    }
}
