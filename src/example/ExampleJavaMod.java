package example;

import mindustry.mod.Mod;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.Vars;
import arc.Core;
import arc.util.Time;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.type.ItemStack;

public class ExampleJavaMod extends Mod {
    
    public static ItemTurret javaToMlogTurret;

    // Специальный метод для инициализации контента блоков без @Override
    // Это гарантирует, что текстуры турели загрузятся ДО открытия вкладки строительства
    public void load() {
        javaToMlogTurret = new ItemTurret("java-mlog-turret") {{
            localizedName = "Java Compiler Turret";
            description = "Кастомная турель из нашего Java мода. Стреляет кремнием и медью.";
            health = 1200;
            size = 2;
            range = 220f;
            reload = 15f;
            
            ammo(
                Items.copper, mindustry.content.Fx.instShoot,
                Items.silicon, mindustry.content.Fx.instBomb
            );
            
            requirements(Category.turret, new ItemStack[]{
                new ItemStack(Items.copper, 60),
                new ItemStack(Items.lead, 40)
            });
        }};
    }

    @Override
    public void init() {
        // Запускаем окно компилятора через безопасный таймер задержки
        Time.run(180f, () -> {
            showProcessorCompilerDialog();
        });
    }

    private void showProcessorCompilerDialog() {
        BaseDialog dialog = new BaseDialog("Java to Mlog Ultimate Pro");
        dialog.addCloseButton();

        var inputArea = dialog.cont.field("", text -> {}).get();
        inputArea.setMessageText(
            "// 1. Endless Loop Example\n" +
            "while(true) {\n" +
            "    int copper = vault1.sensor(copper);\n" +
            "    print(\"Vault copper count: \");\n" +
            "    print(copper);\n" +
            "    printFlush(message1);\n" +
            "}"
        );

        dialog.cont.row();

        var outputArea = dialog.cont.field("", text -> {}).get();
        outputArea.setMessageText("// Mlog output code here");
        outputArea.setDisabled(true);

        dialog.cont.getCells().clear();
        
        // Настройка адаптивного интерфейса по ширине дисплея (.growX)
        dialog.cont.add(inputArea).growX().height(150).pad(10).row();
        
        dialog.cont.button("Compile & Copy", () -> {
            String code = inputArea.getText();
            String result = translateEverything(code);
            outputArea.setText(result);
            Core.app.setClipboardText(result);
            Vars.ui.showInfoFade("Mlog compiled & copied!");
        }).size(250, 45).pad(6).row();
        
        dialog.cont.add(outputArea).growX().height(150).pad(10);

        dialog.show();
    }

    private String translateEverything(String javaCode) {
        StringBuilder mlog = new StringBuilder();
        String[] lines = javaCode.split("\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty() || line.startsWith("//")) continue;

            if (line.endsWith(";")) {
                line = line.substring(0, line.length() - 1).trim();
            }

            if (line.startsWith("while(true)") || line.startsWith("while (true)")) {
                continue;
            }

            if (line.startsWith("print(\"") && line.endsWith("\")")) {
                String txt = line.substring(line.indexOf("print(\"") + 7, line.lastIndexOf("\")"));
                mlog.append("print \"").append(txt).append("\"\n");
                continue;
            }

            if (line.startsWith("print(") && line.endsWith(")")) {
                String varName = line.substring(line.indexOf("print(") + 6, line.lastIndexOf(")")).trim();
                mlog.append("print ").append(varName).append("\n");
                continue;
            }

            if (line.startsWith("printFlush(") && line.endsWith(")")) {
                String msg = line.substring(line.indexOf("printFlush(") + 11, line.lastIndexOf(")")).trim();
                mlog.append("printflush ").append(msg).append("\n");
                continue;
            }

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

            if (line.contains("=")) {
                int eq = line.indexOf("=");
                String varName = line.substring(0, eq).replace("int", "").replace("double", "").replace("float", "").trim();
                String value = line.substring(eq + 1).trim();
                mlog.append("set ").append(varName).append(" ").append(value).append("\n");
                continue;
            }

            if (line.equals("}")) {
                mlog.append("jump 0 always\n");
                continue;
            }

            if (!line.equals("{") && !line.equals("else {")) {
                mlog.append("# ").append(line).append("\n");
            }
        }
        return mlog.toString();
    }
        }
