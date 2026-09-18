package example;

import mindustry.mod.Mod;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.gen.Icon;
import mindustry.Vars;
import arc.Core;

public class ExampleJavaMod extends Mod {
    @Override
    public void init() {
        // Создаем кнопку в главном меню игры
        Vars.ui.menufrag.addButton("Java -> Mlog", Icon.code, () -> {
            showCompilerDialog();
        });
    }

    private void showCompilerDialog() {
        BaseDialog dialog = new BaseDialog("Транслятор Java в Mlog");
        dialog.addCloseButton();
        
        // Поле ввода кода на телефоне
        var textArea = dialog.cont.field("", text -> {}).size(500, 300).get();
        textArea.setMessageText("int speed = 10;");

        dialog.cont.row();

        // Кнопка запуска трансляции
        dialog.cont.button("Скомпилировать", () -> {
            String javaCode = textArea.getText();
            
            // ВЫЗОВ ВАШЕГО КЛАССА MlogTranslator
            String mlogResult = MlogTranslator.translate(javaCode);
            
            // Сохраняем полученный Mlog в буфер обмена телефона
            Core.app.setClipboardText(mlogResult);
            Vars.ui.showInfoFade("Mlog код скопирован в буфер обмена!");
        }).size(200, 50);

        dialog.show();
    }
}
