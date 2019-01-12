package me.shedaniel.rei.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.client.ClientHelper;
import me.shedaniel.rei.client.GuiHelper;
import me.shedaniel.rei.gui.widget.*;
import me.shedaniel.rei.listeners.IMixinContainerGui;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ContainerGui;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiEventListener;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.Window;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ContainerGuiOverlay extends Gui {
    
    public static String searchTerm = "";
    private static int page = 0;
    private static ItemListOverlay itemListOverlay;
    private final List<IWidget> widgets;
    private final List<QueuedTooltip> queuedTooltips;
    private Rectangle rectangle;
    private IMixinContainerGui containerGui;
    private Window window;
    private ButtonWidget buttonLeft, buttonRight;
    private int lastLeft;
    
    public ContainerGuiOverlay(ContainerGui containerGui) {
        this.queuedTooltips = new ArrayList<>();
        this.containerGui = (IMixinContainerGui) containerGui;
        this.widgets = new ArrayList<>();
    }
    
    public void onInitialized() {
        //Update Variables
        this.widgets.clear();
        this.window = MinecraftClient.getInstance().window;
        if (MinecraftClient.getInstance().currentGui instanceof ContainerGui)
            this.containerGui = (IMixinContainerGui) MinecraftClient.getInstance().currentGui;
        this.rectangle = calculateBoundary();
        widgets.add(this.itemListOverlay = new ItemListOverlay(containerGui, page));
        this.lastLeft = getLeft();
        
        this.itemListOverlay.updateList(getItemListArea(), page, searchTerm);
        widgets.add(buttonLeft = new ButtonWidget(rectangle.x, rectangle.y + 5, 16, 16, "<") {
            @Override
            public void onPressed(int button, double mouseX, double mouseY) {
                page--;
                if (page < 0)
                    page = getTotalPage();
                itemListOverlay.updateList(getItemListArea(), page, searchTerm);
            }
        });
        widgets.add(buttonRight = new ButtonWidget(rectangle.x + rectangle.width - 18, rectangle.y + 5, 16, 16, ">") {
            @Override
            public void onPressed(int button, double mouseX, double mouseY) {
                page++;
                if (page > getTotalPage())
                    page = 0;
                itemListOverlay.updateList(getItemListArea(), page, searchTerm);
            }
        });
        page = MathHelper.clamp(page, 0, getTotalPage());
        widgets.add(new ButtonWidget(10, 10, 40, 20, "") {
            @Override
            public void draw(int int_1, int int_2, float float_1) {
                this.text = getCheatModeText();
                super.draw(int_1, int_2, float_1);
            }
            
            @Override
            public void onPressed(int button, double mouseX, double mouseY) {
                ClientHelper.setCheating(!ClientHelper.isCheating());
            }
        });
        this.widgets.add(new LabelWidget(rectangle.x + (rectangle.width / 2), rectangle.y + 10, "") {
            @Override
            public void draw(int mouseX, int mouseY, float partialTicks) {
                page = MathHelper.clamp(page, 0, getTotalPage());
                this.text = String.format("%s/%s", page + 1, getTotalPage() + 1);
                super.draw(mouseX, mouseY, partialTicks);
            }
        });
//        Rectangle textFieldArea = getTextFieldArea();
//        this.widgets.add(searchField = new TextFieldWidget(-1, MinecraftClient.getInstance().fontRenderer,
//                (int) textFieldArea.getX(), (int) textFieldArea.getY(), (int) textFieldArea.getWidth(), (int) textFieldArea.getHeight()) {
//            @Override
//            public void addText(String string_1) {
//                super.addText(string_1);
//                searchTerm = this.getText();
//                itemListOverlay.updateList(page, searchTerm);
//            }
//        });
        if (GuiHelper.searchField == null)
            GuiHelper.searchField = new TextFieldWidget(0, 0, 0, 0) {
                @Override
                public void addText(String string_1) {
                    super.addText(string_1);
                    searchTerm = this.getText();
                    itemListOverlay.updateList(page, searchTerm);
                }
            };
        GuiHelper.searchField.setBounds(getTextFieldArea());
        this.widgets.add(GuiHelper.searchField);
        GuiHelper.searchField.setText(searchTerm);
        
        this.listeners.addAll(widgets);
    }
    
    private Rectangle getTextFieldArea() {
        if (MinecraftClient.getInstance().currentGui instanceof RecipeViewingWidget) {
            RecipeViewingWidget widget = (RecipeViewingWidget) MinecraftClient.getInstance().currentGui;
            return new Rectangle(widget.getBounds().x, window.getScaledHeight() - 22, widget.getBounds().width, 18);
        }
        return new Rectangle(containerGui.getContainerLeft(), window.getScaledHeight() - 22, containerGui.getContainerWidth(), 18);
    }
    
    private String getCheatModeText() {
        return I18n.translate(String.format("%s%s", "text.rei.", ClientHelper.isCheating() ? "cheat" : "nocheat"));
    }
    
    private Rectangle getItemListArea() {
        return new Rectangle(rectangle.x + 2, rectangle.y + 24, rectangle.width - 4, rectangle.height - 27);
    }
    
    public Rectangle getRectangle() {
        return rectangle;
    }
    
    public void render(int mouseX, int mouseY, float partialTicks) {
        if (getLeft() != lastLeft)
            onInitialized();
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GuiLighting.disable();
        this.draw(mouseX, mouseY, partialTicks);
        GuiLighting.disable();
        queuedTooltips.forEach(queuedTooltip -> containerGui.getContainerGui().drawTooltip(queuedTooltip.text, queuedTooltip.mouse.x, queuedTooltip.mouse.y));
        queuedTooltips.clear();
        GuiLighting.disable();
    }
    
    public void setContainerGui(IMixinContainerGui containerGui) {
        this.containerGui = containerGui;
    }
    
    public void addTooltip(QueuedTooltip queuedTooltip) {
        queuedTooltips.add(queuedTooltip);
    }
    
    @Override
    public void draw(int int_1, int int_2, float float_1) {
        if (!GuiHelper.isOverlayVisible())
            return;
        widgets.forEach(widget -> {
            GuiLighting.disable();
            widget.draw(int_1, int_2, float_1);
        });
        GuiLighting.disable();
        itemListOverlay.draw(int_1, int_2, float_1);
        GuiLighting.disable();
        super.draw(int_1, int_2, float_1);
    }
    
    private Rectangle calculateBoundary() {
        int startX = containerGui.getContainerLeft() + containerGui.getContainerWidth() + 10;
        int width = window.getScaledWidth() - startX;
        if (MinecraftClient.getInstance().currentGui instanceof RecipeViewingWidget) {
            RecipeViewingWidget widget = (RecipeViewingWidget) MinecraftClient.getInstance().currentGui;
            startX = widget.getBounds().x + widget.getBounds().width + 10;
            width = window.getScaledWidth() - startX;
        }
        return new Rectangle(startX, 0, width, window.getScaledHeight());
    }
    
    private int getLeft() {
        if (MinecraftClient.getInstance().currentGui instanceof RecipeViewingWidget) {
            RecipeViewingWidget widget = (RecipeViewingWidget) MinecraftClient.getInstance().currentGui;
            return widget.getBounds().x;
        }
        return containerGui.getContainerLeft();
    }
    
    private int getTotalPage() {
        return MathHelper.ceil(itemListOverlay.getCurrentDisplayed().size() / itemListOverlay.getTotalSlotsPerPage());
    }
    
    @Override
    public boolean mouseScrolled(double amount) {
        if (rectangle.contains(ClientHelper.getMouseLocation())) {
            if (amount > 0 && buttonLeft.enabled)
                buttonLeft.onPressed(0, 0, 0);
            else if (amount < 0 && buttonRight.enabled)
                buttonRight.onPressed(0, 0, 0);
            else return false;
            return true;
        }
        for(IWidget widget : widgets)
            if (widget.mouseScrolled(amount))
                return true;
        return false;
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        for(GuiEventListener listener : listeners)
            if (listener.keyPressed(int_1, int_2, int_3))
                return true;
        Point point = ClientHelper.getMouseLocation();
        ItemStack itemStack = null;
        for(IWidget widget : itemListOverlay.getListeners())
            if (widget instanceof ItemSlotWidget && ((ItemSlotWidget) widget).isHighlighted(point.x, point.y)) {
                itemStack = ((ItemSlotWidget) widget).getCurrentStack();
                break;
            }
        if (itemStack == null && MinecraftClient.getInstance().currentGui instanceof RecipeViewingWidget) {
            RecipeViewingWidget recipeViewingWidget = (RecipeViewingWidget) MinecraftClient.getInstance().currentGui;
            for(GuiEventListener entry : recipeViewingWidget.getEntries())
                if (entry instanceof ItemSlotWidget && ((ItemSlotWidget) entry).isHighlighted(point.x, point.y)) {
                    itemStack = ((ItemSlotWidget) entry).getCurrentStack();
                    break;
                }
        }
        if (itemStack == null && MinecraftClient.getInstance().currentGui instanceof ContainerGui)
            if (containerGui.getHoveredSlot() != null)
                itemStack = containerGui.getHoveredSlot().getStack();
        if (itemStack != null && !itemStack.isEmpty()) {
            if (ClientHelper.RECIPE.matchesKey(int_1, int_2))
                return ClientHelper.executeRecipeKeyBind(this, itemStack, containerGui);
            else if (ClientHelper.USAGE.matchesKey(int_1, int_2))
                return ClientHelper.executeUsageKeyBind(this, itemStack, containerGui);
        }
        if (ClientHelper.HIDE.matchesKey(int_1, int_2)) {
            GuiHelper.toggleOverlayVisible();
            return true;
        }
        return false;
    }
    
    @Override
    public boolean charTyped(char char_1, int int_1) {
        for(GuiEventListener listener : listeners)
            if (listener.charTyped(char_1, int_1))
                return true;
        return super.charTyped(char_1, int_1);
    }
    
}
