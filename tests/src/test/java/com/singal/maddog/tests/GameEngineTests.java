package com.singal.maddog.tests;

import com.singal.maddog.ai.AStarPathfinder;
import com.singal.maddog.ai.NavigationGrid;
import com.singal.maddog.math.MathUtils;
import com.singal.maddog.math.Vector2f;
import com.singal.maddog.math.Vector2i;
import com.singal.maddog.physics.AABB;
import com.singal.maddog.save.SaveSystem;
import java.io.File;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GameEngineTests {

    @Test
    public void testVector2iMath() {
        Vector2i v1 = new Vector2i(10, 20);
        Vector2i v2 = new Vector2i(5, 5);

        v1.add(v2);
        assertEquals(15, v1.x);
        assertEquals(25, v1.y);

        v1.subtract(5, 10);
        assertEquals(10, v1.x);
        assertEquals(15, v1.y);

        double dist = v1.distance(new Vector2i(10, 10));
        assertEquals(5.0, dist, 0.0001);
    }

    @Test
    public void testVector2fMath() {
        Vector2f v1 = new Vector2f(3.0f, 4.0f);
        assertEquals(5.0f, v1.length(), 0.0001f);

        v1.normalize();
        assertEquals(0.6f, v1.x, 0.0001f);
        assertEquals(0.8f, v1.y, 0.0001f);
    }

    @Test
    public void testMathUtils() {
        assertEquals(10, MathUtils.clamp(15, 0, 10));
        assertEquals(0, MathUtils.clamp(-5, 0, 10));
        assertEquals(5, MathUtils.clamp(5, 0, 10));

        assertEquals(15.0f, MathUtils.lerp(10.0f, 20.0f, 0.5f), 0.0001f);
    }

    @Test
    public void testAABBIntersections() {
        AABB box1 = new AABB(0, 0, 10, 10);
        AABB box2 = new AABB(5, 5, 10, 10);
        AABB box3 = new AABB(20, 20, 10, 10);

        assertTrue(box1.intersects(box2));
        assertFalse(box1.intersects(box3));
        assertTrue(box1.contains(5, 5));
        assertFalse(box1.contains(15, 5));
    }

    @Test
    public void testAStarPathfinding() {
        // Create a 5x5 simple navigation grid where (2, 2) is a solid wall
        NavigationGrid grid = (x, y) -> x == 2 && y == 2;

        Vector2i start = new Vector2i(1, 1);
        Vector2i goal = new Vector2i(3, 3);

        List<Vector2i> path = AStarPathfinder.findPath(start, goal, grid);
        assertNotNull(path);
        assertTrue(path.size() > 0);

        // Path should lead to goal, i.e. path.get(0) is goal
        assertEquals(goal, path.get(0));

        // The path should not contain the wall coordinate (2, 2)
        for (Vector2i step : path) {
            assertFalse(step.x == 2 && step.y == 2);
        }
    }

    @Test
    public void testSaveSystemJsonSerialization() {
        String testSavePath = "build/tmp/test_settings.json";
        SaveSystem.GameSettings settings = new SaveSystem.GameSettings();
        settings.title = "Test Remastered Title";
        settings.scale = 4;

        boolean saved = SaveSystem.saveSettings(settings, testSavePath);
        assertTrue(saved);

        SaveSystem.GameSettings loaded = SaveSystem.loadSettings(testSavePath);
        assertNotNull(loaded);
        assertEquals("Test Remastered Title", loaded.title);
        assertEquals(4, loaded.scale);
    }

    @AfterAll
    public static void cleanup() {
        File file = new File("build/tmp/test_settings.json");
        if (file.exists()) {
            file.delete();
        }
    }
}
