/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  org.lwjgl.glfw.GLFW
 *  org.lwjgl.glfw.GLFWWindowCloseCallback
 *  org.lwjgl.glfw.GLFWWindowCloseCallbackI
 */
package name.modid.client;

import name.modid.client.InputLock;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWWindowCloseCallback;
import org.lwjgl.glfw.GLFWWindowCloseCallbackI;

public final class WindowCloseGuard {
    private static boolean installed = false;
    private static GLFWWindowCloseCallbackI vanilla;

    private WindowCloseGuard() {
    }

    public static void install() {
        if (installed) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getWindow() == null) {
            return;
        }
        long handle = mc.getWindow().handle();
        if (handle == 0L) {
            return;
        }
        GLFWWindowCloseCallback previous = GLFW.glfwSetWindowCloseCallback((long)handle, WindowCloseGuard::onClose);
        vanilla = previous;
        installed = true;
    }

    private static void onClose(long window) {
        if (InputLock.engaged()) {
            GLFW.glfwSetWindowShouldClose((long)window, (boolean)false);
            return;
        }
        GLFW.glfwSetWindowShouldClose((long)window, (boolean)true);
        if (vanilla != null) {
            vanilla.invoke(window);
        }
    }
}

