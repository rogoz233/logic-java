package example;

import mindustry.mod.*;
import arc.util.*;

public class ExampleJavaMod extends Mod {
    public ExampleJavaMod() {
        Log.info("Java core initialized.");
    }
    @Override
    public void init() {
        Log.info("Mod successfully loaded!");
    }
}
