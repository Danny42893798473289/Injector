package dev.kazhi.module.impl.stress;

import dev.kazhi.stressutil.StressRecipeIds;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.protocol.game.ServerboundRecipeBookChangeSettingsPacket;
import net.minecraft.network.protocol.game.ServerboundRecipeBookSeenRecipePacket;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RecipeBookStressTest extends StressModule {
    public int packetsPerSecond = 120;
    public boolean recipeData = true;
    public boolean categoryOptions = true;
    public boolean craftPreview = true;
    public boolean craftAll = true;
    public boolean openInventory = false;
    public int randomIdRange = 4096;

    private List<RecipeDisplayId> recipeIds;
    private int recipeCursor;
    private int categoryCursor;
    private int actionCursor;
    private boolean categoryGuiOpen;

    public RecipeBookStressTest() {
        super("Recipe Book Stress", "Spams recipe book data, category options, and craft-preview packets.");
    }

    @Override
    protected void onEnable() {
        if (player() == null) {
            return;
        }
        if (!requireConnection()) {
            return;
        }

        refreshRecipeIds();
        recipeCursor = 0;
        categoryCursor = 0;
        actionCursor = 0;
        categoryGuiOpen = true;

        if (openInventory) {
            MC.setScreen(new InventoryScreen(player()));
        }

        info("Recipe book stress (~" + packetsPerSecond + " packets/s, " + recipeIds.size() + " known recipes).");
    }

    @Override
    protected void onDisable() {
        if (openInventory) {
            MC.setScreen(null);
        }
    }

    @Override
    public void onTick() {
        if (player() == null || MC.level == null || connection() == null) {
            return;
        }

        if (player().tickCount % 100 == 0) {
            refreshRecipeIds();
        }

        if (openInventory && !(MC.screen instanceof InventoryScreen)) {
            MC.setScreen(new InventoryScreen(player()));
        }

        for (int i = 0; i < packetsPerTick(packetsPerSecond); i++) {
            runAction();
        }
    }

    private void runAction() {
        int phase = actionCursor++ % 6;

        if (categoryOptions && phase == 0) {
            spamCategoryOptions();
            return;
        }

        if (recipeData && (phase == 1 || phase == 2)) {
            sendRecipeData(nextRecipeId());
            return;
        }

        if (craftPreview) {
            spamCraftPreview();
        } else if (recipeData) {
            sendRecipeData(nextRecipeId());
        }
    }

    private void spamCategoryOptions() {
        RecipeBookType[] types = RecipeBookType.values();
        RecipeBookType type = types[categoryCursor++ % types.length];

        connection().send(new ServerboundRecipeBookChangeSettingsPacket(
            type,
            categoryGuiOpen,
            !categoryGuiOpen
        ));

        if (categoryCursor % types.length == 0) {
            categoryGuiOpen = !categoryGuiOpen;
        }
    }

    private void sendRecipeData(RecipeDisplayId id) {
        connection().send(new ServerboundRecipeBookSeenRecipePacket(id));
    }

    private void spamCraftPreview() {
        RecipeDisplayId id = nextRecipeId();
        boolean all = craftAll && actionCursor % 2 == 0;
        int syncId = player().containerMenu.containerId;
        MC.gameMode.handlePlaceRecipe(syncId, id, all);
    }

    private RecipeDisplayId nextRecipeId() {
        if (!recipeIds.isEmpty()) {
            return recipeIds.get(recipeCursor++ % recipeIds.size());
        }

        int index = ThreadLocalRandom.current().nextInt(randomIdRange);
        return new RecipeDisplayId(index);
    }

    private void refreshRecipeIds() {
        if (player() == null) {
            recipeIds = List.of();
            return;
        }

        List<RecipeDisplayId> collected = StressRecipeIds.collect(player());
        recipeIds = collected.isEmpty() ? new ArrayList<>() : new ArrayList<>(collected);
    }
}
