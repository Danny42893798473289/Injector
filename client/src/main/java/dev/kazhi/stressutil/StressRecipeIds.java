package dev.kazhi.stressutil;

import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class StressRecipeIds {
    private static Method entryIdMethod;

    private StressRecipeIds() {}

    public static List<RecipeDisplayId> collect(LocalPlayer player) {
        List<RecipeDisplayId> ids = new ArrayList<>();
        ClientRecipeBook book = player.getRecipeBook();

        for (RecipeCollection collection : book.getCollections()) {
            for (RecipeDisplayEntry entry : collection.getRecipes()) {
                RecipeDisplayId id = recipeId(entry);
                if (id != null) {
                    ids.add(id);
                }
            }
        }

        if (ids.isEmpty()) {
            ids.addAll(recipeMapKeys(book));
        }

        return ids;
    }

    private static RecipeDisplayId recipeId(RecipeDisplayEntry entry) {
        try {
            if (entryIdMethod == null) {
                entryIdMethod = RecipeDisplayEntry.class.getMethod("id");
            }
            return (RecipeDisplayId) entryIdMethod.invoke(entry);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static List<RecipeDisplayId> recipeMapKeys(ClientRecipeBook book) {
        for (String fieldName : new String[]{"known", "recipes"}) {
            try {
                Field recipes = ClientRecipeBook.class.getDeclaredField(fieldName);
                recipes.setAccessible(true);
                Map<RecipeDisplayId, ?> map = (Map<RecipeDisplayId, ?>) recipes.get(book);
                return new ArrayList<>(map.keySet());
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return List.of();
    }
}
