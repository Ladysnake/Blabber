/*
 * Blabber
 * Copyright (C) 2022-2025 Ladysnake
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
package org.ladysnake.blabber.api.client.illustration;

import org.ladysnake.blabber.api.illustration.DialogueIllustration;
import org.ladysnake.blabber.impl.client.BlabberClient;

import java.util.HashMap;
import java.util.Map;

public class IllustrationContainer {
    private final Map<String, DialogueIllustrationRenderer<?>> illustrations = new HashMap<>();

    public void setIllustrations(Map<String, DialogueIllustration> illustrations) {
        this.illustrations.clear();
        illustrations.forEach((key, illustration) -> this.illustrations.put(key, BlabberClient.createRenderer(illustration)));
    }

    public DialogueIllustrationRenderer<?> getRenderer(String illustrationName) {
        DialogueIllustrationRenderer<?> renderer = this.illustrations.get(illustrationName);
        if (renderer == null) throw new IllegalArgumentException("Unknown illustration " + illustrationName);
        return renderer;
    }
}
