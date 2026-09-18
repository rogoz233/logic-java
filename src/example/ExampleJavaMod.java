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
            // Безопасно добавляем кнопку в панель кнопок редактора процессора
            if(Vars.ui != null && Vars.ui.logic != null && Vars.ui.logic.buttons != null){
                Vars.ui.logic.buttons.button("Java -> Mlog", () -> {
                    showProcessorCompilerDialog();
                }).size(160, 45).pad(4);
            }
        });
    }

    private void showProcessorCompilerDialog() {
        BaseDialog dialog = new BaseDialog("Java to Mlog Pro");
        dialog.addCloseButton();

        var inputArea = dialog.cont.field("", text -> {}).size(450, 140).get();
        inputArea.setMessageText(
            "// Write Java code here\n" +
            "unit.bind(flare);\n" +
            "int targetX = 120;\n" +
            "int targetY = 180;\n" +
            "unit.move(targetX, targetY);"
        );

        dialog.cont.row();

        var outputArea = dialog.cont.field("", text -> {}).size(450, 140).get();
        outputArea.setMessageText("// Mlog output");
        outputArea.setDisabled(true);

        dialog.cont.getCells().clear();
        dialog.cont.add(inputArea).size(450, 130).pad(4).row();
        
        dialog.cont.button("Compile & Apply", () -> {
            String code = inputArea.getText();
            String result = translateUltimate(code);
            
            outputArea.setText(result);
            
            if(Vars.ui != null && Vars.ui.logic != null){
                Vars.ui.logic.setText(result);
            }
            
            Core.app.setClipboardText(result);
            Vars.ui.showInfoFade("Injected into Processor!");
        }).size(250, 45).pad(6).row();
        
        dialog.cont.add(outputArea).size(450, 130).pad(4);

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

            // 1. Привязка юнитов
            if (line.startsWith("unit.bind(") && line.endsWith(")")) {
                String unitType = line.substring(line.indexOf("unit.bind(") + 10, line.lastIndexOf(")")).trim();
                mlog.append("ubind @").append(unitType).append("\n");
                continue;
            }

            // 2. Движение юнитов
            if (line.startsWith("unit.move(") && line.endsWith(")")) {
                String args = line.substring(line.indexOf("unit.move(") + 10, line.lastIndexOf(")")).trim();
                if (args.contains(",")) {
                    int comma = args.indexOf(",");
                    String cx = args.substring(0, comma).trim();
                    String cy = args.substring(comma + 1).trim();
                    mlog.append("ucontrol move ").append(cx).append(" ").append(cy).append(" 0 0 0\n");
                }
                continue;
            }

            // 3. Датчики ресурсов
            if (line.contains(".sensor(")) {
                int eq = line.indexOf("=");
                String varPart = line.substring(0, eq).replace("int", "").replace("double", "").replace("float", "").trim();
                String callPart = line.substring(eq + 1).trim();
                int dot = callPart.indexOf(".");
                String building = callPart.substring(0, dot).trim();
                String resource = callPart.substring(callPart.indexOf(".sensor(") + 8, callPart.indexOf(")")).trim();
                mlog.append("sensor ").append(varPart).append(" ").append(building).append(" @").append(resource).append("\n");
                continue;
            }

            // 4. Управление блоками
            if (line.contains(".control(")) {
                int dot = line.indexOf(".");
                String building = line.substring(0, dot).trim();
                String state = line.substring(line.indexOf(".control(") + 9, line.indexOf(")")).trim();
                mlog.append("control enabled ").append(building).append(" ").append(state).append("\n");
                continue;
            }

            // 5. Математические операции
            if (line.contains("=") && (line.contains("+") || line.contains("-") || line.contains("*") || line.contains("/"))) {
                int eq = line.indexOf("=");
                String target = line.substring(0, eq).trim();
                String expr = line.substring(eq + 1).trim();
                
                String op = expr.contains("+") ? "+" : expr.contains("-") ? "-" : expr.contains("*") ? "*" : "/";
                int opIdx = expr.indexOf(op);
                String v1 = expr.substring(0, opIdx).trim();
                String v2 = expr.substring(opIdx + 1).trim();
                
                String mlogOp = op.equals("+") ? "add" : op.equals("-") ? "sub" : op.equals("*") ? "mul" : "div";
                mlog.append("op ").append(mlogOp).append(" ").append(target).append(" ").append(v1).append(" ").append(v2).append("\n");
                continue;
            }

            // 6. Простое присваивание
            if (line.contains("=")) {
                int eq = line.indexOf("=");
                String varName = line.substring(0, eq).replace("int", "").replace("double", "").replace("float", "").trim();
                String value = line.substring(eq + 1).trim();
                mlog.append("set ").append(varName).append(" ").append(value).append("\n");
                continue;
            }

            if (!line.equals("{") && !line.equals("}") && !line.equals("else {")) {
                mlog.append("# ").append(line).append("\n");
            }
        }
        return mlog.toString();
    }
                        }
