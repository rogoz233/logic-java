package example;

import mindustry.mod.*;
import mindustry.logic.*;
import mindustry.logic.LAssemble;
import arc.util.*;

public class ExampleJavaMod extends Mod {

    public ExampleJavaMod() {
        Log.info("Loading Java Logic Extension...");
    }

    @Override
    public void init() {
        Log.info("Registering custom mlog instructions...");

        // Регистрируем команду "hello" в парсер игры с правильным регистром класса LAssemble
        LAssemble.instructions.put("hello", (args) -> {
            String targetVariable = args.length > 1 ? args : null;
            return new CustomHelloInstruction(targetVariable);
        });

        Log.info("Instruction 'hello' successfully added!");
    }

    public static class CustomHelloInstruction implements LExecutor.LInstruction {
        public String varName;

        public CustomHelloInstruction(String varName) {
            this.varName = varName;
        }

        @Override
        public void run(LExecutor exec) {
            if (varName != null) {
                // Читаем числовое значение переменной из процессора
                double value = exec.getVar(varName).numval; 
                exec.textBuffer.append("Java says: ").append(value).append("\n");
            } else {
                exec.textBuffer.append("Hello from Java Mod!\n");
            }
        }
    }
}
