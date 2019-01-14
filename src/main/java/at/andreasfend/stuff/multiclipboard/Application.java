package at.andreasfend.stuff.multiclipboard;

import lc.kra.system.keyboard.GlobalKeyboardHook;
import lc.kra.system.keyboard.event.GlobalKeyAdapter;
import lc.kra.system.keyboard.event.GlobalKeyEvent;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Application {

    private static boolean run = true;
    private static Clipboard clipboard;
    private static Robot robot;
    private final static List<String> store = new ArrayList<String>();


    public static void main(String[] args) {
        // might throw a UnsatisfiedLinkError if the native library fails to load or a RuntimeException if hooking fails
        GlobalKeyboardHook keyboardHook = new GlobalKeyboardHook(true); // use false here to switch to hook instead of raw input

        clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        try {
            robot = new Robot();
        } catch (AWTException e) {
            System.err.println("Can not create Robot! Shutting down ....");
            return;
        }


        System.out.println("Global keyboard hook successfully started, press [escape] key to shutdown. Connected keyboards:");
        for(Map.Entry<Long,String> keyboard:GlobalKeyboardHook.listKeyboards().entrySet())
            System.out.format("%d: %s\n", keyboard.getKey(), keyboard.getValue());

        keyboardHook.addKeyListener(new GlobalKeyAdapter() {
            @Override public void keyPressed(GlobalKeyEvent event) {
                //System.out.println(event.getVirtualKeyCode());

            }
            @Override public void keyReleased(GlobalKeyEvent event) {
                //System.out.println(event);
                if (event.isControlPressed() && event.getKeyChar() == 'c') {
                    try {
                        // TODO: Check if clipboard content is really a string
                        String data = (String) clipboard.getData(DataFlavor.stringFlavor);
                        put(data);
                    } catch (UnsupportedFlavorException e) {
                        System.err.println("Not adding to clipboard because of wrong DataFlavor");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (event.isControlPressed() && event.isShiftPressed() && event.getVirtualKeyCode() >= 112 && event.getVirtualKeyCode() <= 123) {
                    int fkey = event.getVirtualKeyCode() - 112;
                    paste(fkey);
                }
                if (event.isControlPressed() && event.isShiftPressed() && event.getVirtualKeyCode() == 226) {
                    list();
                }
                if (event.isControlPressed() && event.isShiftPressed() && event.getVirtualKeyCode() == 8) {
                    pop();
                }
                if (event.isControlPressed() && event.isShiftPressed() && event.getVirtualKeyCode() == GlobalKeyEvent.VK_OEM_MINUS) {
                    clear();
                }
            }
        });

        try {
            while(run) Thread.sleep(128);
        } catch(InterruptedException e) { /* nothing to do here */ }
        finally { keyboardHook.shutdownHook(); }
    }

    public static void put(final String data) {
        if (store.size() >= 12) {
            System.err.println("Clipboard store is full!");
            return;
        }
        if (store.contains(data)) {
            System.out.println("Clipboard already holds " + data + " on #" + (store.indexOf(data)+1));
            return;
        }
        store.add(data);
        System.out.println("Added #" + store.size() + ": " + data);
    }

    public static void pop() {
        if (store.size() == 0) {
            System.err.println("Clipboard store is empty!");
            return;
        }
        String data = store.get(store.size()-1);
        store.remove(data);
        System.out.println("Removed #" + (store.size() + 1) + ": " + data);
    }

    public static void clear() {
        store.clear();
        System.out.println("Cleared Clipboards");
    }

    public static void paste(final int pos) {
        if (store.size() <= pos) {
            System.out.println("Clipboard # " + (pos + 1 ) + " is empty");
            return;
        }
        String string = store.get(pos);
        System.out.println("Paste #" + (pos + 1) + ": " + string);
        StringSelection selection = new StringSelection(string);
        clipboard.setContents(selection, selection);
        robot.keyRelease(KeyEvent.VK_SHIFT);
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_CONTROL);
        robot.keyRelease(KeyEvent.VK_V);
        runAfter(new Runnable() {
            public void run() {
                robot.keyPress(KeyEvent.VK_CONTROL);
                robot.keyPress(KeyEvent.VK_SHIFT);
            }
        });
    }

    public static void list() {
        System.out.println("Clipboard Bindings:");
        for (int i = 0; i < store.size(); i++) {
            String data = store.get(i);
            System.out.println("#" + i + ": " + data);
        }
    }

    public static void runAfter(final Runnable fun) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (fun != null) {
                    fun.run();
                }
            }
        }).start();
    }
}
