package example;

import mindustry.mod.Mod;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.Vars;
import arc.Core;
import arc.Events;
import mindustry.game.EventType.ClientLoadEvent;

public class ExampleJavaMod extends Mod {
    @Override
    public void init() {
        Events.run(ClientLoadEvent.class, () -> {
            BaseDialog dialog = new BaseDialog("Java to Mlog Ultimate");
            dialog.addCloseButton();
            
            // Левое окно: Поле ввода Java-кода
            var inputArea = dialog.cont.field("", text -> {}).size(350, 400).get();
            inputArea.setMessageText(
                "// Control Units via Java\n" +
                "unit.bind(flare);\n" +
                "int targetX = 120;\n" +
                "int targetY = 180;\n" +
                "unit.move(targetX, targetY);"
            );

            // Правое окно: Поле вывода готового Mlog-кода (только для чтения)
            var outputArea = dialog.cont.field("", text -> {}).size(350, 400).get();
            outputArea.setMessageText("// Mlog code will appear here");
            outputArea.setDisabled(true); // Защищаем от случайного стирания на клавиатуре

            dialog.cont.row();

            // Кнопка компиляции
            dialog.cont.button("Compile & Copy", () -> {
                String code = inputArea.getText();
                String result = translateUltimate(code);
                
                // Выводим скомпилированный код на экран телефона!
                outputArea.setText(result);
                
                // Дублируем в буфер обмена
                Core.app.setClipboardText(result);
                Vars.ui.showInfoFade("Compiled and Copied!");
            }).size(250, 50);

            dialog.show();
        });
    }

    private String translateUltimate(String javaCode) {
        StringBuilder mlog = new StringBuilder();
        String[] lines = javaCode.split("\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty() || line.startsWith("//")) continue;

            if (line.endsWith(";")) {
                line = line.substring(0, line.length() - 1).trim();
            }

            // 1. Привязка юнита: unit.bind(flare); -> ubind @flare
            if (line.startsWith("unit.bind(") && line.endsWith(")")) {
                String unitType = line.substring(line.indexOf("unit.bind(") + 10, line.lastIndexOf(")")).trim();
                mlog.append("ubind @").append(unitType).append("\n");
                continue;
            }

            // 2. Движение юнита: unit.move(x, y); -> ucontrol move x y 0 0 0
            if (line.startsWith("unit.move(") && line.endsWith(")")) {
                String args = line.substring(line.indexOf("unit.move(") + 10, line.lastIndexOf(")")).trim();
                String[] coords = args.split(",");
                if (coords.length >= 2) {
                    mlog.append("ucontrol move ").append(coords[0].trim()).append(" ").append(coords[1].trim()).append(" 0 0 0\n");
                }
                continue;
            }

            // 3. Датчики построек: int copper = vault1.sensor(copper);
            if (line.contains(".sensor(")) {
                String varPart = line.substring(0, line.indexOf("=")).replace("int", "").replace("double", "").replace("float", "").trim();
                String callPart = line.substring(line.indexOf("=") + 1).trim();
                String building = callPart.substring(0, callPart.indexOf(".")).trim();
                String resource = callPart.substring(callPart.indexOf(".sensor(") + 8, callPart.indexOf(")")).trim();
                mlog.append("sensor ").append(varPart).append(" ").append(building).append(" @").append(resource).append("\n");
                continue;
            }

            // 4. Управление постройками: switch1.control(enabled);
            if (line.contains(".control(")) {
                String building = line.substring(0, line.indexOf(".")).trim();
                String state = line.substring(line.indexOf(".control(") + 9, line.indexOf(")")).trim();
                mlog.append("control enabled ").append(building).append(" ").append(state).append("\n");
                continue;
            }

            // 5. Математические вычисления (add, sub, mul, div)
            if (line.contains("=") && (line.contains("+") || line.contains("-") || line.contains("*") || line.contains("/"))) {
                String[] parts = line.split("=");
                String target = parts[0].trim();
                String expr = parts[1].trim();
                String op = expr.contains("+") ? "+" : expr.contains("-") ? "-" : expr.contains("*") ? "*" : "/";
                
                String[] vars = expr.split("\\" + op);
                String mlogOp = op.equals("+") ? "add" : op.equals("-") ? "sub" : op.equals("*") ? "mul" : "div";
                mlog.append("op ").append(mlogOp).append(" ").append(target).append(" ").append(vars[0].trim()).append(" ").append(vars[1].trim()).append("\n");
                continue;
            }

            // 6. Простое присваивание переменных
            if (line.contains("=")) {
                String[] parts = line.split("=");
                String varName = parts[0].replace("int", "").replace("double", "").replace("float", "").trim();
                mlog.append("set ").append(varName).append(" ").append(parts[1].trim()).append("\n");
                continue;
            }

            if (!line.equals("{") && !line.equals("}") && !line.equals("else {")) {
                mlog.append("# ").append(line).append("\n");
            }
        }
        return mlog.toString();
    }
                    }
                
