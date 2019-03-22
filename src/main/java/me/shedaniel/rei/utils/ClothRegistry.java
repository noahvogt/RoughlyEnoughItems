package me.shedaniel.rei.utils;

import javafx.util.Pair;
import me.shedaniel.cloth.api.ClientUtils;
import me.shedaniel.cloth.api.ConfigScreenBuilder;
import me.shedaniel.cloth.gui.ClothConfigScreen;
import me.shedaniel.cloth.gui.entries.BooleanListEntry;
import me.shedaniel.cloth.gui.entries.IntegerListEntry;
import me.shedaniel.cloth.gui.entries.StringListEntry;
import me.shedaniel.cloth.hooks.ClothClientHooks;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.TabGetter;
import me.shedaniel.rei.client.RecipeHelperImpl;
import me.shedaniel.rei.client.ScreenHelper;
import me.shedaniel.rei.gui.ContainerScreenOverlay;
import me.shedaniel.rei.gui.config.ItemListOrderingEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ContainerScreen;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.ingame.CreativePlayerInventoryScreen;
import net.minecraft.client.gui.widget.RecipeBookButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.ActionResult;

import java.io.IOException;

public class ClothRegistry {
    
    public static void register() {
        ClothClientHooks.SYNC_RECIPES.register((minecraftClient, recipeManager, synchronizeRecipesS2CPacket) -> {
            ((RecipeHelperImpl) RoughlyEnoughItemsCore.getRecipeHelper()).recipesLoaded(recipeManager);
        });
        ClothClientHooks.SCREEN_ADD_BUTTON.register((minecraftClient, screen, abstractButtonWidget) -> {
            if (RoughlyEnoughItemsCore.getConfigManager().getConfig().disableRecipeBook && screen instanceof ContainerScreen && abstractButtonWidget instanceof RecipeBookButtonWidget)
                return ActionResult.FAIL;
            return ActionResult.PASS;
        });
        ClothClientHooks.SCREEN_INIT_POST.register((minecraftClient, screen, screenHooks) -> {
            if (screen instanceof ContainerScreen) {
                if (screen instanceof CreativePlayerInventoryScreen) {
                    TabGetter tabGetter = (TabGetter) screen;
                    if (tabGetter.rei_getSelectedTab() != ItemGroup.INVENTORY.getIndex())
                        return;
                }
                ScreenHelper.setLastContainerScreen((ContainerScreen) screen);
                screenHooks.cloth_getInputListeners().add(ScreenHelper.getLastOverlay(true, false));
            }
        });
        ClothClientHooks.SCREEN_RENDER_POST.register((minecraftClient, screen, i, i1, v) -> {
            if (screen instanceof ContainerScreen) {
                if (screen instanceof CreativePlayerInventoryScreen) {
                    TabGetter tabGetter = (TabGetter) screen;
                    if (tabGetter.rei_getSelectedTab() != ItemGroup.INVENTORY.getIndex())
                        return;
                }
                ScreenHelper.getLastOverlay().drawOverlay(i, i1, v);
            }
        });
        ClothClientHooks.SCREEN_MOUSE_SCROLLED.register((minecraftClient, screen, v, v1, v2) -> {
            if (screen instanceof ContainerScreen && !(screen instanceof CreativePlayerInventoryScreen)) {
                ContainerScreenOverlay overlay = ScreenHelper.getLastOverlay();
                if (ScreenHelper.isOverlayVisible() && ContainerScreenOverlay.getItemListOverlay().getListArea().contains(ClientUtils.getMouseLocation()))
                    if (overlay.mouseScrolled(v, v1, v2))
                        return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        });
    }
    
    public static void openConfigScreen(Screen parent) {
        ClothConfigScreen.Builder builder = new ClothConfigScreen.Builder(parent, I18n.translate("text.rei.config.title"), null);
        builder.addCategory("text.rei.config.general").addOption(new BooleanListEntry("text.rei.config.cheating", RoughlyEnoughItemsCore.getConfigManager().getConfig().cheating, "text.cloth.reset_value", () -> false, bool -> RoughlyEnoughItemsCore.getConfigManager().getConfig().cheating = bool));
        ConfigScreenBuilder.CategoryBuilder appearance = builder.addCategory("text.rei.config.appearance");
        appearance.addOption(new BooleanListEntry("text.rei.config.side_search_box", RoughlyEnoughItemsCore.getConfigManager().getConfig().sideSearchField, "text.cloth.reset_value", () -> false, bool -> RoughlyEnoughItemsCore.getConfigManager().getConfig().sideSearchField = bool));
        appearance.addOption(new ItemListOrderingEntry("text.rei.config.list_ordering", new Pair<>(RoughlyEnoughItemsCore.getConfigManager().getConfig().itemListOrdering, RoughlyEnoughItemsCore.getConfigManager().getConfig().isAscending)));
        appearance.addOption(new BooleanListEntry("text.rei.config.mirror_rei", RoughlyEnoughItemsCore.getConfigManager().getConfig().mirrorItemPanel, "text.cloth.reset_value", () -> false, bool -> RoughlyEnoughItemsCore.getConfigManager().getConfig().mirrorItemPanel = bool));
        appearance.addOption(new IntegerListEntry("text.rei.config.max_recipes_per_page", RoughlyEnoughItemsCore.getConfigManager().getConfig().maxRecipePerPage, "text.cloth.reset_value", () -> 3, i -> RoughlyEnoughItemsCore.getConfigManager().getConfig().maxRecipePerPage = i).setMinimum(2).setMaximum(99));
        ConfigScreenBuilder.CategoryBuilder modules = builder.addCategory("text.rei.config.modules");
        modules.addOption(new BooleanListEntry("text.rei.config.enable_craftable_only", RoughlyEnoughItemsCore.getConfigManager().getConfig().enableCraftableOnlyButton, "text.cloth.reset_value", () -> true, bool -> RoughlyEnoughItemsCore.getConfigManager().getConfig().enableCraftableOnlyButton = bool));
        modules.addOption(new BooleanListEntry("text.rei.config.enable_util_buttons", RoughlyEnoughItemsCore.getConfigManager().getConfig().showUtilsButtons, "text.cloth.reset_value", () -> false, bool -> RoughlyEnoughItemsCore.getConfigManager().getConfig().showUtilsButtons = bool));
        modules.addOption(new BooleanListEntry("text.rei.config.disable_recipe_book", RoughlyEnoughItemsCore.getConfigManager().getConfig().disableRecipeBook, "text.cloth.reset_value", () -> false, bool -> RoughlyEnoughItemsCore.getConfigManager().getConfig().disableRecipeBook = bool));
        ConfigScreenBuilder.CategoryBuilder advanced = builder.addCategory("text.rei.config.advanced");
        advanced.addOption(new StringListEntry("text.rei.give_command", RoughlyEnoughItemsCore.getConfigManager().getConfig().giveCommand, "text.cloth.reset_value", () -> "/give {player_name} {item_identifier}{nbt} {count}", s -> RoughlyEnoughItemsCore.getConfigManager().getConfig().giveCommand = s));
        advanced.addOption(new StringListEntry("text.rei.gamemode_command", RoughlyEnoughItemsCore.getConfigManager().getConfig().gamemodeCommand, "text.cloth.reset_value", () -> "/gamemode {gamemode}", s -> RoughlyEnoughItemsCore.getConfigManager().getConfig().gamemodeCommand = s));
        advanced.addOption(new StringListEntry("text.rei.weather_command", RoughlyEnoughItemsCore.getConfigManager().getConfig().weatherCommand, "text.cloth.reset_value", () -> "/weather {weather}", s -> RoughlyEnoughItemsCore.getConfigManager().getConfig().weatherCommand = s));
        advanced.addOption(new BooleanListEntry("text.rei.config.prefer_visible_recipes", RoughlyEnoughItemsCore.getConfigManager().getConfig().preferVisibleRecipes, "text.cloth.reset_value", () -> false, bool -> RoughlyEnoughItemsCore.getConfigManager().getConfig().preferVisibleRecipes = bool));
        builder.setOnSave(savedConfig -> {
            try {
                RoughlyEnoughItemsCore.getConfigManager().saveConfig();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        MinecraftClient.getInstance().openScreen(builder.build());
    }
    
}
