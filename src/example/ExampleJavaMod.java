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
        // Ждем загрузки клиента игры для безопасной модификации UI процессоров
        Events.run(ClientLoadEvent.class, () -> {
            
            // Находим стандартный интерфейс редактирования логики процессора и добавляем кнопку
            Vars.ui.logic.buttons.button("Java -> Mlog", () -> {
                showProcessorCompilerDialog();
            }).size(160, 45).pad(4);
            
        });
    }

    private void showProcessorCompilerDialog() {
        BaseDialog dialog = new BaseDialog("Java to Mlog Compiler");
        dialog.addCloseButton(); // Кнопка закрытия появится в самом низу

        // Поле ввода кода Java (высота 150 - идеально для мобильного экрана)
        var inputArea = dialog.cont.field("", text -> {}).size(450, 150).get();
        inputArea.setMessageText(
            "// Write Java code here\n" +
            "unit.bind(flare);\n" +
            "int targetX = 120;\n" +
            "int targetY = 180;\n" +
            "unit.move(targetX, targetY);"
        );

        dialog.cont.row();

        // Поле вывода скомпилированного Mlog кода
        var outputArea = dialog.cont.field("", text -> {}).size(450, 150).get();
        outputArea.setMessageText("// Mlog output");
        outputArea.setDisabled(true);

        // Пересобираем разметку элементов управления внутри диалогового окна
        dialog.cont.getCells().clear(); // Очищаем дефолтные отступы
        
        // Позиционируем элементы вертикально в столбик
        dialog.cont.add(inputArea).size(450, 140).pad(6).row();
        
        dialog.cont.button("Compile & Apply", () -> {
            String code = inputArea.getText();
            String result = translateUltimate(code);
            
            // Выводим результат в нижнее текстовое поле
            outputArea.setText(result);
            
            // Автоматически вставляем скомпилированный Mlog прямо в открытый процессор!
            Vars.ui.logic.setText(result);
            
            // Дублируем код в буфер обмена телефона
            Core.app.setClipboardText(result);
            Vars.ui.showInfoFade("Injected into Processor!");
        }).size(260, 45).pad(8).row();
        
        dialog.cont.add(outputArea).size(450, 140).pad(6);

        dialog.show();
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

            if (line.startsWith("unit.bind(") && line.endsWith(")")) {
                String unitType = line.substring(line.indexOf("unit.bind(") + 10, line.lastIndexOf(")")).trim();
                mlog.append("ubind @").append(unitType).append("\n");
                continue;
            }

            if (line.startsWith("unit.move(") && line.endsWith(")")) {
                String args = line.substring(line.indexOf("unit.move(") + 10, line.lastIndexOf(")")).trim();
                String[] coords = args.split(",");
                if (coords.length >= 2) {
                    mlog.append("ucontrol move ").append(coords[0].trim()).append(" ").append(coords[1].trim()).append(" 0 0 0\n");
                }
                continue;
            }

            if (line.contains(".sensor(")) {
                String varPart = line.substring(0, line.indexOf("=")).replace("int", "").replace("double", "").replace("float", "").trim();
                String callPart = line.substring(line.indexOf("=") + 1).trim();
                String building = callPart.substring(0, callPart.indexOf(".")).trim();
                String resource = callPart.substring(callPart.indexOf(".sensor(") + 8, callPart.indexOf(")")).trim();
                mlog.append("sensor ").append(varPart).append(" ").append(building).append(" @").append(resource).append("\n");
                continue;
            }

            if (line.contains(".control(")) {
                String building = line.substring(0, line.indexOf(".")).trim();
                String state = line.substring(line.indexOf(".control(") + 9, line.indexOf(")")).trim();
                mlog.append("control enabled ").append(building).append(" ").append(state).append("\n");
                continue;
            }

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
