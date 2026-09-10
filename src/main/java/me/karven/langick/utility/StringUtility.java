package me.karven.langick.utility;

import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtility {
    private static final Pattern placeholderPattern = Pattern.compile("[$][{](\\w+)}");

    public static @NonNull String format(final @NonNull String template, final @NonNull Map<@NonNull String, @NonNull Object> parameters) {
        final StringBuilder newTemplate = new StringBuilder(template);
        final List<Object> valueList = new ArrayList<>();

        final Matcher matcher = placeholderPattern.matcher(template);

        while (matcher.find()) {
            final String key = matcher.group(1);

            final String paramName = "${" + key + "}";
            int index = newTemplate.indexOf(paramName);
            if (index != -1) {
                newTemplate.replace(index, index + paramName.length(), "%s");
                valueList.add(parameters.get(key));
            }
        }

        return String.format(newTemplate.toString(), valueList.toArray());
    }

}
