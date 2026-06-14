package com.singal.maddog.input;

import com.singal.maddog.math.Vector2i;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Robust action-map based input manager handling keyboard and mouse input states.
 */
public class InputManager implements KeyListener, MouseListener, MouseMotionListener {
    // Key states
    private final boolean[] rawKeys = new boolean[65536];
    private final boolean[] keysPressed = new boolean[65536];
    private final boolean[] keysReleased = new boolean[65536];
    private final boolean[] keysHeld = new boolean[65536];

    // Mouse states
    private final boolean[] rawMouseButtons = new boolean[8];
    private final boolean[] mouseButtonsPressed = new boolean[8];
    private final boolean[] mouseButtonsReleased = new boolean[8];
    private final boolean[] mouseButtonsHeld = new boolean[8];
    private final Vector2i mousePosition = new Vector2i();

    // Action bindings
    private final Map<String, List<Integer>> actionKeyBindings = new HashMap<>();
    private final Map<String, List<Integer>> actionMouseButtonBindings = new HashMap<>();

    public InputManager() {
        // Register default bindings
        bindAction("move_up", KeyEvent.VK_W, KeyEvent.VK_UP);
        bindAction("move_down", KeyEvent.VK_S, KeyEvent.VK_DOWN);
        bindAction("move_left", KeyEvent.VK_A, KeyEvent.VK_LEFT);
        bindAction("move_right", KeyEvent.VK_D, KeyEvent.VK_RIGHT);
        bindAction("shoot", MouseEvent.BUTTON1); // Left mouse click
    }

    /**
     * Call at the start of each game loop tick to update key transition states.
     */
    public synchronized void update() {
        for (int i = 0; i < rawKeys.length; i++) {
            keysPressed[i] = rawKeys[i] && !keysHeld[i];
            keysReleased[i] = !rawKeys[i] && keysHeld[i];
            keysHeld[i] = rawKeys[i];
        }

        for (int i = 0; i < rawMouseButtons.length; i++) {
            mouseButtonsPressed[i] = rawMouseButtons[i] && !mouseButtonsHeld[i];
            mouseButtonsReleased[i] = !rawMouseButtons[i] && mouseButtonsHeld[i];
            mouseButtonsHeld[i] = rawMouseButtons[i];
        }
    }

    public void bindAction(String action, int... keyCodesOrMouseButtons) {
        for (int code : keyCodesOrMouseButtons) {
            if (code >= 1 && code <= 3) {
                // Treat as mouse button
                actionMouseButtonBindings.computeIfAbsent(action, k -> new ArrayList<>()).add(code);
            } else {
                // Treat as key code
                actionKeyBindings.computeIfAbsent(action, k -> new ArrayList<>()).add(code);
            }
        }
    }

    public boolean isActionHeld(String action) {
        List<Integer> keys = actionKeyBindings.get(action);
        if (keys != null) {
            for (int key : keys) {
                if (keysHeld[key]) return true;
            }
        }
        List<Integer> mouseButtons = actionMouseButtonBindings.get(action);
        if (mouseButtons != null) {
            for (int btn : mouseButtons) {
                if (mouseButtonsHeld[btn]) return true;
            }
        }
        return false;
    }

    public boolean isActionPressed(String action) {
        List<Integer> keys = actionKeyBindings.get(action);
        if (keys != null) {
            for (int key : keys) {
                if (keysPressed[key]) return true;
            }
        }
        List<Integer> mouseButtons = actionMouseButtonBindings.get(action);
        if (mouseButtons != null) {
            for (int btn : mouseButtons) {
                if (mouseButtonsPressed[btn]) return true;
            }
        }
        return false;
    }

    public boolean isActionReleased(String action) {
        List<Integer> keys = actionKeyBindings.get(action);
        if (keys != null) {
            for (int key : keys) {
                if (keysReleased[key]) return true;
            }
        }
        List<Integer> mouseButtons = actionMouseButtonBindings.get(action);
        if (mouseButtons != null) {
            for (int btn : mouseButtons) {
                if (mouseButtonsReleased[btn]) return true;
            }
        }
        return false;
    }

    public Vector2i getMousePosition() {
        return mousePosition;
    }

    // KeyListener Methods
    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public synchronized void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code >= 0 && code < rawKeys.length) {
            rawKeys[code] = true;
        }
    }

    @Override
    public synchronized void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code >= 0 && code < rawKeys.length) {
            rawKeys[code] = false;
        }
    }

    // MouseListener Methods
    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public synchronized void mousePressed(MouseEvent e) {
        int btn = e.getButton();
        if (btn >= 0 && btn < rawMouseButtons.length) {
            rawMouseButtons[btn] = true;
        }
    }

    @Override
    public synchronized void mouseReleased(MouseEvent e) {
        int btn = e.getButton();
        if (btn >= 0 && btn < rawMouseButtons.length) {
            rawMouseButtons[btn] = false;
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    // MouseMotionListener Methods
    @Override
    public void mouseDragged(MouseEvent e) {
        mousePosition.set(e.getX(), e.getY());
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mousePosition.set(e.getX(), e.getY());
    }
}
