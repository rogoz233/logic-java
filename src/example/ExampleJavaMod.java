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
            BaseDialog dialog = new BaseDialog("Java to Mlog Pro");
            dialog.addCloseButton();
            
            var textArea = dialog.cont.field("", text -> {}).size(560, 360).get();
            textArea.setMessageText(
                "// Example Code\n" +
                "int copper = vault1.sensor(copper);\n" +
                "if (copper < 500) {\n" +
                "    enabled = 1;\n" +
                "} else {\n" +
                "    enabled = 0;\n" +
                "}\n" +
                "switch1.control(enabled);"
            );

            dialog.cont.row();

            dialog.cont.button("Compile Pro", () -> {
                String code = textArea.getText();
                String result = translatePro(code);
                Core.app.setClipboardText(result);
                Vars.ui.showInfoFade("Mlog copied to clipboard!");
            }).size(220, 50);

            dialog.show();
        });
    }

    private String translatePro(String javaCode) {
        StringBuilder mlog = new StringBuilder();
        String[] lines = javaCode.split("\n");
        
        // Индексы для генерации меток jump в Mlog
        int lineCounter = 0;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty() || line.startsWith("//")) continue;

            if (line.endsWith(";")) {
                line = line.substring(0, line.length() - 1).trim();
            }

            // 1. Парсинг датчиков: int copper = vault1.sensor(copper);
            if (line.contains(".sensor(")) {
                // Извлекаем имя переменной
                String varPart = line.substring(0, line.indexOf("=")).replace("int", "").replace("double", "").trim();
                // Извлекаем имя постройки и тип ресурса
                String callPart = line.substring(line.indexOf("=") + 1).trim();
                String building = callPart.substring(0, callPart.indexOf(".")).trim();
                String resource = callPart.substring(callPart.indexOf(".sensor(") + 8, callPart.indexOf(")")).trim();

                mlog.append("sensor ").append(varPart).append(" ").append(building).append(" @").append(resource).append("\n");
                continue;
            }

            // 2. Парсинг команд управления: switch1.control(enabled);
            if (line.contains(".control(")) {
                String building = line.substring(0, line.indexOf(".")).trim();
                String state = line.substring(line.indexOf(".control(") + 9, line.indexOf(")")).trim();

                mlog.append("control enabled ").append(building).append(" ").append(state).append("\n");
                continue;
            }

            // 3. Упрощенный парсинг условных операторов if-else (базовый шаблон)
            if (line.startsWith("if ") && line.contains("(")) {
                String condition = line.substring(line.indexOf("(") + 1, line.indexOf(")")).trim();
                
                // Выделяем элементы условия (например, copper < 500)
                String[] condParts = condition.split(" ");
                if (condParts.length >= 3) {
                    String left = condParts[0];
                    String sign = condParts[1];
                    String right = condParts[2];

                    String mlogSign = "equal";
                    if (sign.equals("<")) mlogSign = "lessThan";
                    if (sign.equals(">")) mlogSign = "greaterThan";
                    if (sign.equals("==")) mlogSign = "equal";
                    if (sign.equals("!=")) mlogSign = "notEqual";

                    // Ищем тело if и else на следующих строках
                    String ifBody = "";
                    String elseBody = "";
                    
                    int j = i + 1;
                    if (j < lines.length && lines[j].trim().equals("{")) j++;
                    if (j < lines.length) ifBody = lines[j].replace(";", "").trim();
                    
                    j += 2; // Пропускаем закрывающую скобку и else {
                    if (j < lines.length && lines[j].trim().equals("else {")) j++;
                    if (j < lines.length && !lines[j].trim().equals("}")) elseBody = lines[j].replace(";", "").trim();

                    // Собираем Mlog-структуру для условного перехода jump
                    mlog.append("jump * ").append(mlogSign).append(" ").append(left).append(" ").append(right).append("\n");
                    mlog.append(elseBody).append("\n");
                    mlog.append("jump * always\n");
                    mlog.append(ifBody).append("\n");
                    
                    // Пропускаем обработанные строки блока в цикле
                    i = j + 1; 
                    continue;
                }
            }

            // 4. Обычные математические операции (из прошлой версии)
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

            // 5. Простое присваивание переменной
            if (line.contains("=")) {
                String[] parts = line.split("=");
                String varName = parts[0].replace("int", "").replace("double", "").trim();
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
