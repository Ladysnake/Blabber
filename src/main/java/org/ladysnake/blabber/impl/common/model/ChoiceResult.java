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
package org.ladysnake.blabber.impl.common.model;

public enum ChoiceResult {
    DEFAULT(true, true), END_DIALOGUE(false, false), ASK_CONFIRMATION(false, true);

    private final boolean allowsIllustrations;
    private final boolean hasChoices;

    ChoiceResult(boolean allowsIllustrations, boolean hasChoices) {
        this.allowsIllustrations = allowsIllustrations;
        this.hasChoices = hasChoices;
    }

    public boolean allowsIllustrations() {
        return this.allowsIllustrations;
    }

    public boolean hasChoices() {
        return hasChoices;
    }
}
