package example;

import mindustry.mod.Mod;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.gen.Icon;
import mindustry.Vars;
import arc.Core;

public class ExampleJavaMod extends Mod {
    @Override
    public void init() {
        Vars.ui.menufrag.addButton("Java -> Mlog", Icon.code, () -> {
            showCompilerDialog();
        });
    }

    private void showCompilerDialog() {
        BaseDialog dialog = new BaseDialog("Java to Mlog");
        dialog.addCloseButton();
        
        var textArea = dialog.cont.field("", text -> {}).size(500, 300).get();
        textArea.setMessageText("int speed = 10;");

        dialog.cont.row();

        dialog.cont.button("Скомпилировать", () -> {
            String javaCode = textArea.getText();
            String mlogResult = MlogTranslator.translate(javaCode);
            
            Core.app.setClipboardText(mlogResult);
            Vars.ui.showInfoFade("Скопировано!");
        }).size(200, 50);

        dialog.show();
    }
}
