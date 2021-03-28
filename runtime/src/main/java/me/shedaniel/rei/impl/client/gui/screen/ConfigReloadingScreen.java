/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020 shedaniel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.shedaniel.rei.impl.client.gui.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import net.minecraft.Util;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class ConfigReloadingScreen extends Screen {
    private Runnable parent;
    
    public ConfigReloadingScreen(Runnable parent) {
        super(NarratorChatListener.NO_TITLE);
        this.parent = parent;
    }
    
    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
    
    @Override
    public void render(PoseStack matrices, int int_1, int int_2, float float_1) {
        this.renderDirtBackground(0);
        if (!PluginManager.areAnyPluginsReloading()) {
            parent.run();
        }
        drawCenteredString(matrices, this.font, I18n.get("text.rei.config.is.reloading"), this.width / 2, this.height / 2 - 50, 16777215);
        String string_3;
        switch ((int) (Util.getMillis() / 300L % 4L)) {
            case 0:
            default:
                string_3 = "O o o";
                break;
            case 1:
            case 3:
                string_3 = "o O o";
                break;
            case 2:
                string_3 = "o o O";
        }
        drawCenteredString(matrices, this.font, string_3, this.width / 2, this.height / 2 - 41, 8421504);
        super.render(matrices, int_1, int_2, float_1);
    }
}
