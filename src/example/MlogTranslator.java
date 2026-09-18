package example;

public class MlogTranslator {

    public static String translate(String javaCode) {
        StringBuilder mlog = new StringBuilder();
        
        // Разбиваем весь текст на отдельные строки
        String[] lines = javaCode.split("\n");

        for (String line : lines) {
            line = line.trim(); // Удаляем лишние пробелы в начале и конце

            // Игнорируем пустые строки и комментарии
            if (line.isEmpty() || line.startsWith("//")) {
                continue;
            }

            // Убираем точку с запятой в конце строки для удобства парсинга
            if (line.endsWith(";")) {
                line = line.substring(0, line.length() - 1).trim();
            }

            // 1. Обработка создания переменных (Пример: int speed = 10)
            if (line.startsWith("int ") || line.startsWith("double ") || line.startsWith("float ")) {
                // Отрезаем тип данных (int, double, float)
                String безТипа = line.substring(line.indexOf(" ") + 1).trim();
                
                if (безТипа.contains("=")) {
                    String[] parts = безТипа.split("=");
                    String varName = parts[0].trim();
                    String value = parts[1].trim();
                    
                    // В Mlog запись переменной — это команда 'set'
                    mlog.append("set ").append(varName).append(" ").append(value).append("\n");
                }
                continue;
            }

            // 2. Обработка математических операций (Пример: x = a + b)
            if (line.contains("=") && (line.contains("+") || line.contains("-") || line.contains("*") || line.contains("/"))) {
                String[] parts = line.split("=");
                String targetVar = parts[0].trim(); // Куда записываем результат (x)
                String expression = parts[1].trim(); // Само выражение (a + b)

                // Ищем знак операции
                String op = "";
                if (expression.contains("+")) op = "+";
                else if (expression.contains("-")) op = "-";
                else if (expression.contains("*")) op = "*";
                else if (expression.contains("/")) op = "/";

                if (!op.isEmpty()) {
                    // Экранируем знак операции для разделения строки
                    String[] vars = expression.split("\\" + op);
                    String operand1 = vars[0].trim();
                    String operand2 = vars[1].trim();

                    // Конвертируем знак Java в название операции Mlog
                    String mlogOp = "add";
                    if (op.equals("-")) mlogOp = "sub";
                    if (op.equals("*")) mlogOp = "mul";
                    if (op.equals("/")) mlogOp = "div";

                    // В Mlog это пишется как: op [операция] [результат] [аргумент1] [аргумент2]
                    mlog.append("op ").append(mlogOp).append(" ").append(targetVar)
                        .append(" ").append(operand1).append(" ").append(operand2).append("\n");
                }
                continue;
            }

            // 3. Прямое присваивание существующей переменной (Пример: speed = 5)
            if (line.contains("=")) {
                String[] parts = line.split("=");
                String varName = parts[0].trim();
                String value = parts[1].trim();
                mlog.append("set ").append(varName).append(" ").append(value).append("\n");
                continue;
            }
            
            // Если строка не подошла ни под одно правило, оставляем её как комментарий об ошибке
            mlog.append("# [Ошибка парсинга]: ").append(line).append("\n");
        }

        return mlog.toString();
    }
                  }

