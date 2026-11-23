package planetarium;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Читает manifest.json пакета: имя файла -> разрешение и эталонный SHA-256. */
public final class ManifestReader {

    /** Запись манифеста: разрешение и SHA-256, записанный при создании пакета. */
    public record Entry(int width, int height, String sha256) {}

    private ManifestReader() {}

    public static Map<String, Entry> read(Path file) throws IOException {
        Object root = new JsonParser(Files.readString(file, StandardCharsets.UTF_8)).parseValue();
        Map<String, Entry> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> item : asMap(root).entrySet()) {
            Map<String, Object> info = asMap(item.getValue());
            List<Object> res = asList(info.get("resolution"));
            result.put(item.getKey(), new Entry(
                    ((Long) res.get(0)).intValue(),
                    ((Long) res.get(1)).intValue(),
                    (String) info.get("sha256")));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object o) {
        if (o instanceof Map<?, ?> m) {
            return (Map<String, Object>) m;
        }
        throw new IllegalArgumentException("manifest.json: ожидался объект");
    }

    @SuppressWarnings("unchecked")
    private static List<Object> asList(Object o) {
        if (o instanceof List<?> l) {
            return (List<Object>) l;
        }
        throw new IllegalArgumentException("manifest.json: ожидался массив");
    }

    /** Минимальный разбор JSON: объекты, массивы, строки и целые числа. Этого хватает для manifest.json. */
    private static final class JsonParser {
        private final String s;
        private int pos;

        JsonParser(String s) {
            this.s = s;
        }

        Object parseValue() {
            skipWs();
            char c = peek();
            return switch (c) {
                case '{' -> parseObject();
                case '[' -> parseArray();
                case '"' -> parseString();
                default -> parseNumber();
            };
        }

        private Map<String, Object> parseObject() {
            Map<String, Object> m = new LinkedHashMap<>();
            expect('{');
            skipWs();
            if (peek() == '}') {
                pos++;
                return m;
            }
            while (true) {
                skipWs();
                String key = parseString();
                skipWs();
                expect(':');
                m.put(key, parseValue());
                skipWs();
                char c = next();
                if (c == '}') {
                    return m;
                }
                if (c != ',') {
                    throw error("ожидалась запятая или '}'");
                }
            }
        }

        private List<Object> parseArray() {
            List<Object> a = new ArrayList<>();
            expect('[');
            skipWs();
            if (peek() == ']') {
                pos++;
                return a;
            }
            while (true) {
                a.add(parseValue());
                skipWs();
                char c = next();
                if (c == ']') {
                    return a;
                }
                if (c != ',') {
                    throw error("ожидалась запятая или ']'");
                }
            }
        }

        private String parseString() {
            expect('"');
            StringBuilder sb = new StringBuilder();
            while (true) {
                char c = next();
                if (c == '"') {
                    return sb.toString();
                }
                if (c != '\\') {
                    sb.append(c);
                    continue;
                }
                char e = next();
                switch (e) {
                    case 'n' -> sb.append('\n');
                    case 't' -> sb.append('\t');
                    case 'u' -> {
                        if (pos + 4 > s.length()) {
                            throw error("обрезанная escape-последовательность");
                        }
                        sb.append((char) Integer.parseInt(s.substring(pos, pos + 4), 16));
                        pos += 4;
                    }
                    default -> sb.append(e);
                }
            }
        }

        private Long parseNumber() {
            int start = pos;
            while (pos < s.length() && "-0123456789".indexOf(s.charAt(pos)) >= 0) {
                pos++;
            }
            if (start == pos) {
                throw error("неожиданный символ '" + s.charAt(pos) + "'");
            }
            return Long.parseLong(s.substring(start, pos));
        }

        private char peek() {
            if (pos >= s.length()) {
                throw error("неожиданный конец текста");
            }
            return s.charAt(pos);
        }

        private char next() {
            char c = peek();
            pos++;
            return c;
        }

        private void expect(char ch) {
            if (next() != ch) {
                throw error("ожидался символ '" + ch + "'");
            }
        }

        private void skipWs() {
            while (pos < s.length() && Character.isWhitespace(s.charAt(pos))) {
                pos++;
            }
        }

        private IllegalArgumentException error(String msg) {
            return new IllegalArgumentException("manifest.json, позиция " + pos + ": " + msg);
        }
    }
}
