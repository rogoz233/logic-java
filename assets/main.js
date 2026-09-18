Events.on(ClientLoadEvent, () => {
    Log.info("[V8 Logic] Initializing custom instructions...");

    try {
        // В v8 парсинг завязан на LogicIO и расширенные методы mlog
        // Регистрируем кастомное имя инструкции напрямую через глобальный реестр команд игры
        let logicParser = Packages.mindustry.logic.LAssemble;
        let LExecutor = Packages.mindustry.logic.LExecutor;

        if (logicParser && logicParser.instructions) {
            logicParser.instructions.put("hello", new Packages.arc.func.Func({
                get: function(args) {
                    let targetVar = args.length > 1 ? args[1] : null;
                    
                    return new LExecutor.LInstruction({
                        run: function(exec) {
                            if (targetVar != null) {
                                // Метод получения переменных в v8 был обновлен, обрабатываем безопасно
                                let variable = exec.getVar(targetVar);
                                let value = variable ? variable.numval : 0;
                                exec.textBuffer.append("Java v8 says: " + value + "\n");
                            } else {
                                exec.textBuffer.append("Hello from v8 Logic Extension!\n");
                            }
                        }
                    });
                }
            }));
            Log.info("[V8 Logic] Instruction 'hello' has been successfully injected!");
        } else {
            Log.err("[V8 Logic] Critical Error: Cannot find logic instructions registry.");
        }
    } catch(e) {
        Log.err("[V8 Logic] Failed to inject instruction: " + e);
    }
});
