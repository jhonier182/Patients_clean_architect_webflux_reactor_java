package sura.pruebalegoback.sanitizer;

import java.lang.reflect.Field;
import org.springframework.stereotype.Component;
import org.apache.commons.text.StringEscapeUtils;

@Component
public class StringSanitizerService {

    /**
     * Sanitiza un string de entrada escapando caracteres HTML.
     *
     * @param input el string de entrada a sanitizar
     * @return el string sanitizado
     */
    public String sanitizeString(String input) {
        if (input == null) {
            return null;
        }
        return StringEscapeUtils.escapeHtml4(input);
    }

    /**
     * Sanitiza un arreglo de strings.
     *
     * @param inputs el arreglo de strings a sanitizar
     * @return un arreglo de strings sanitizados
     */
    public String[] sanitizeArray(String[] inputs) {
        if (inputs == null) {
            return null;
        }
        String[] sanitizedArray = new String[inputs.length];
        for (int i = 0; i < inputs.length; i++) {
            sanitizedArray[i] = StringEscapeUtils.escapeHtml4(inputs[i]);
        }
        return sanitizedArray;
    }

    public <T> T sanitizeDto(T dto) {
        if (dto == null)
            return null;

        try {
            Field[] fields = dto.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.getType().equals(String.class)) {
                    field.setAccessible(true);
                    String originalValue = (String) field.get(dto);
                    if (originalValue != null) {
                        String sanitizedValue = StringEscapeUtils.escapeHtml4(originalValue);
                        field.set(dto, sanitizedValue);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return dto;
    }

}