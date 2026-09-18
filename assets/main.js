Events.on(ClientLoadEvent, () => {
    Log.info("Registering custom mlog instructions via JS...");

    // Безопасный способ добавить команду напрямую в глобальный список инструкций игры
    let instructions = Packages.mindustry.logic.LAssemble.instructions;
    
    instructions.put("hello", new Packages.arc.func.Func({
        get: function(args) {
            let targetVariable = args.length > 1 ? args[1] : null;
            
            return new Packages.mindustry.logic.LExecutor.LInstruction({
                run: function(exec) {
                    if (targetVariable != null) {
                        let variable = exec.getVar(targetVariable);
                        let value = variable ? variable.numval : 0;
                        exec.textBuffer.append("Java says: " + value + "\n");
                    } else {
                        exec.textBuffer.append("Hello from Logic Extension!\n");
                    }
                }
            });
        }
    }));

    Log.info("Instruction 'hello' successfully added via JavaScript!");
});
          
