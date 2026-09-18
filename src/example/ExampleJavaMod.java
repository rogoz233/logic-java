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
        // Безопасный запуск окна сразу после загрузки игры в v160.4
        Events.run(ClientLoadEvent.class, () -> {
            BaseDialog dialog = new BaseDialog("Java to Mlog");
            dialog.addCloseButton();
            
            var textArea = dialog.cont.field("", text -> {}).size(500, 300).get();
            textArea.setMessageText("int speed = 10;");

            dialog.cont.row();

            dialog.cont.button("Compile", () -> {
                String code = textArea.getText();
                String result = translate(code);
                Core.app.setClipboardText(result);
                Vars.ui.showInfoFade("Copied!");
            }).size(200, 50);

            dialog.show();
        });
    }

    private String translate(String javaCode) {
        StringBuilder mlog = new StringBuilder();
        String[] lines = javaCode.split("\n");

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("//")) continue;

            if (line.endsWith(";")) {
                line = line.substring(0, line.length() - 1).trim();
            }

            if (line.startsWith("int ") || line.startsWith("double ") || line.startsWith("float ")) {
                String sub = line.substring(line.indexOf(" ") + 1).trim();
                if (sub.contains("=")) {
                    String[] parts = sub.split("=");
                    mlog.append("set ").append(parts[0].trim()).append(" ").append(parts[1].trim()).append("\n");
                }
                continue;
            }

            if (line.contains("=") && (line.contains("+") || line.contains("-") || line.contains("*") || line.contains("/"))) {
                String[] parts = line.split("=");
                String target = parts[0].trim();
                String expr = parts[1].trim();

                String op = "";
                if (expr.contains("+")) op = "+";
                else if (expr.contains("-")) op = "-";
                else if (expr.contains("*")) op = "*";
                else if (expr.contains("/")) op = "/";

                if (!op.isEmpty()) {
                    String[] vars = expr.split("\\" + op);
                    String mlogOp = op.equals("+") ? "add" : op.equals("-") ? "sub" : op.equals("*") ? "mul" : "div";
                    mlog.append("op ").append(mlogOp).append(" ").append(target).append(" ").append(vars[0].trim()).append(" ").append(vars[1].trim()).append("\n");
                }
                continue;
            }

            if (line.contains("=")) {
                String[] parts = line.split("=");
                mlog.append("set ").append(parts[0].trim()).append(" ").append(parts[1].trim()).append("\n");
                continue;
            }
            mlog.append("# ").append(line).append("\n");
        }
        return mlog.toString();
    }
                        }
