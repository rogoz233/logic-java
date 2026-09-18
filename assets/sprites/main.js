Events.on(ClientLoadEvent, () => {
    Log.info("[Java Compiler] Initializing UI field for Mindcode...");

    // Функция, которая отправляет ваш высокоуровневый код на веб-сервер компилятора
    global.compileHighLevelCode = function(sourceCode, processorBuilding) {
        let Http = Packages.arc.util.Http;
        
        Http.post("https://herokuapp.com")
            .content("code=" + encodeURIComponent(sourceCode))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .submit(new Http.HttpCallback({
                handle: function(response) {
                    try {
                        let resultJson = response.getResultAsString();
                        // Вытаскиваем готовый mlog-код из ответа сервера
                        let mlogOutput = JSON.parse(resultJson).compiledCode; 
                        
                        // Безопасно загружаем скомпилированные инструкции в наш процессор
                        if (processorBuilding && mlogOutput) {
                            processorBuilding.setupCode(mlogOutput);
                            Log.info("[Java Compiler] Code compiled and loaded successfully!");
                        }
                    } catch(e) {
                        Log.err("[Java Compiler] Parsing error: " + e);
                    }
                },
                error: function(err) {
                    Log.err("[Java Compiler] Network error: " + err);
                }
            }));
    };

    Log.info("[Java Compiler] Extension successfully loaded!");
});
             
