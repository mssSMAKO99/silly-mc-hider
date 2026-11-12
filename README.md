# 👻 Window Hider Agent (Certified John Cena Simulator for Minecraft)

A goofy little Java agent that gaslights Windows into thinking your Minecraft window doesn't exist. OBS, Discord stream, and screenshots will see straight through it like an anime protagonist's plot armor. Also yeets the icon off your taskbar and `Alt+Tab` menu.

## 🧠 How this wizardry works
1. Slaps the magic `0x11` (`WDA_EXCLUDEFROMCAPTURE`) sticker onto your game window. Screen recorders literally cannot perceive it.
2. Replaces `WS_EX_APPWINDOW` with `WS_EX_TOOLWINDOW`. Windows goes *"understandable, have a nice day"* and vanishes it from the taskbar and `Alt+Tab`.
3. Runs an infinite background gremlin loop (`Thread.sleep(1000)`). If you smash `F11` and Minecraft recreates its window, the gremlin wakes up and re-applies the cloak of invisibility.

## 🛠️ How to cook it (in Eclipse)
1. Open this bad boy in Eclipse.
2. Right-click the project -> **Run As** -> **Maven build...**
3. Type `package` into the **Goals** box and smash **Run**.
4. Grab your shiny new `window-hider-agent-1.0.jar` from the `target/` folder.
5. Feel free to rename it to `totally_not_a_virus.jar` or `homework.jar`. Java doesn't care.

## 🚀 How to inject the juice
Drop this line into your JVM launch arguments (in your launcher or `args.txt`):

```text
-javaagent:C:\(\path\to\your\window-\)hider-agent-1.0.jar (or other name if me/you change .jar name)
```

*(Pro-tip: Keep Cyrillic/weird symbols out of the folder path unless you want JVM to have a mental breakdown).*

## 📋 Requirements
* Windows 10 (build 2004+) or Windows 11 (older Windows versions don't speak `0x11`).
* Java 21+ (ancient Java need not apply).
