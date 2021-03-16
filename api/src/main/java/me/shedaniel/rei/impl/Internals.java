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

package me.shedaniel.rei.impl;

import com.google.gson.JsonObject;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.ClientHelper;
import me.shedaniel.rei.api.favorites.FavoriteEntry;
import me.shedaniel.rei.api.gui.DrawableConsumer;
import me.shedaniel.rei.api.gui.Renderer;
import me.shedaniel.rei.api.gui.widgets.*;
import me.shedaniel.rei.api.ingredient.EntryIngredient;
import me.shedaniel.rei.api.ingredient.EntryStack;
import me.shedaniel.rei.api.ingredient.entry.EntryDefinition;
import me.shedaniel.rei.api.ingredient.entry.EntryType;
import me.shedaniel.rei.api.plugins.BuiltinPlugin;
import me.shedaniel.rei.api.plugins.PluginManager;
import me.shedaniel.rei.api.registry.screen.ClickArea;
import me.shedaniel.rei.api.view.ViewSearchBuilder;
import me.shedaniel.rei.api.view.Views;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

@ApiStatus.Internal
public final class Internals {
    private static Supplier<ClientHelper> clientHelper = Internals::throwNotSetup;
    private static Supplier<EntryStackProvider> entryStackProvider = Internals::throwNotSetup;
    private static Supplier<EntryIngredientProvider> entryIngredientProvider = Internals::throwNotSetup;
    private static Supplier<WidgetsProvider> widgetsProvider = Internals::throwNotSetup;
    private static Supplier<ViewSearchBuilder> viewSearchBuilder = Internals::throwNotSetup;
    private static Function<ResourceLocation, EntryType<?>> entryTypeDeferred = (object) -> throwNotSetup();
    private static Supplier<Views> views = Internals::throwNotSetup;
    private static Supplier<PluginManager> pluginManager = Internals::throwNotSetup;
    private static BiFunction<Supplier<FavoriteEntry>, Supplier<JsonObject>, FavoriteEntry> delegateFavoriteEntry = (supplier, toJson) -> throwNotSetup();
    private static Function<JsonObject, FavoriteEntry> favoriteEntryFromJson = (object) -> throwNotSetup();
    private static Function<@NotNull Boolean, ClickArea.Result> clickAreaHandlerResult = (result) -> throwNotSetup();
    private static BiFunction<@Nullable Point, Collection<Component>, Tooltip> tooltipProvider = (point, texts) -> throwNotSetup();
    private static Supplier<BuiltinPlugin> builtinPlugin = Internals::throwNotSetup;
    
    private static <T> T throwNotSetup() {
        throw new AssertionError("REI Internals have not been initialized!");
    }
    
    public static ClientHelper getClientHelper() {
        return clientHelper.get();
    }
    
    public static EntryStackProvider getEntryStackProvider() {
        return entryStackProvider.get();
    }
    
    public static EntryIngredientProvider getEntryIngredientProvider() {
        return entryIngredientProvider.get();
    }
    
    public static WidgetsProvider getWidgetsProvider() {
        return widgetsProvider.get();
    }
    
    public static ViewSearchBuilder createViewSearchBuilder() {
        return viewSearchBuilder.get();
    }
    
    public static ClickArea.Result createClickAreaHandlerResult(boolean applicable) {
        return clickAreaHandlerResult.apply(applicable);
    }
    
    public static Tooltip createTooltip(@Nullable Point point, Collection<Component> texts) {
        return tooltipProvider.apply(point, texts);
    }
    
    public static BuiltinPlugin getBuiltinPlugin() {
        return builtinPlugin.get();
    }
    
    @ApiStatus.Internal
    public static <T> void attachInstance(T instance, Class<T> clazz) {
        attachInstance((Supplier<T>) () -> instance, clazz.getSimpleName());
    }
    
    @ApiStatus.Internal
    public static <T> void attachInstance(T instance, String name) {
        try {
            for (Field field : Internals.class.getDeclaredFields()) {
                if (field.getName().equalsIgnoreCase(name)) {
                    field.setAccessible(true);
                    field.set(null, instance);
                    return;
                }
            }
            throw new RuntimeException("Failed to attach " + instance + " with field name: " + name);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static FavoriteEntry delegateFavoriteEntry(Supplier<FavoriteEntry> supplier, Supplier<JsonObject> toJoin) {
        return delegateFavoriteEntry.apply(supplier, toJoin);
    }
    
    public static FavoriteEntry favoriteEntryFromJson(JsonObject object) {
        return favoriteEntryFromJson.apply(object);
    }
    
    public static EntryType<?> deferEntryType(ResourceLocation id) {
        return entryTypeDeferred.apply(id);
    }
    
    public static Views getViews() {
        return views.get();
    }
    
    public static PluginManager getPluginManager() {
        return pluginManager.get();
    }
    
    public interface EntryStackProvider {
        EntryStack<Unit> empty();
        
        <T> EntryStack<T> of(EntryDefinition<T> definition, T value);
        
        EntryType<Unit> emptyType(ResourceLocation id);
        
        EntryType<Renderer> renderingType(ResourceLocation id);
    }
    
    public interface EntryIngredientProvider {
        EntryIngredient empty();
        
        EntryIngredient of(EntryStack<?> stack);
        
        EntryIngredient of(EntryStack<?>... stacks);
        
        EntryIngredient of(Iterable<EntryStack<?>> stacks);
    }
    
    @Environment(EnvType.CLIENT)
    public interface WidgetsProvider {
        boolean isRenderingPanel(Panel panel);
        
        Widget createDrawableWidget(DrawableConsumer drawable);
        
        Slot createSlot(Point point);
        
        Button createButton(Rectangle bounds, Component text);
        
        Panel createPanelWidget(Rectangle bounds);
        
        Label createLabel(Point point, FormattedText text);
        
        Arrow createArrow(Rectangle rectangle);
        
        BurningFire createBurningFire(Rectangle rectangle);
        
        DrawableConsumer createTexturedConsumer(ResourceLocation texture, int x, int y, int width, int height, float u, float v, int uWidth, int vHeight, int textureWidth, int textureHeight);
        
        DrawableConsumer createFillRectangleConsumer(Rectangle rectangle, int color);
    }
}