package com.zync.executorservice.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves {@code {{$.json.path}}} placeholders in template strings against a
 * webhook payload using Jayway JsonPath, enabling dynamic step configurations.
 */
@Slf4j
public class TemplateResolver {

    // Regex to find anything that looks like {{$.something}}
    private static final Pattern PATTERN = Pattern.compile("\\{\\{(\\$\\.[^}]+)}}");

    /**
     * Replaces all {@code {{$.path}}} expressions in the template with values
     * extracted from the payload. Unresolvable paths default to "UNKNOWN".
     *
     * @param template       the string containing optional placeholder expressions
     * @param webhookPayload the JSON payload to query against
     * @return the template with placeholders resolved, or the original if none found
     */
    public static String resolve(String template, JsonNode webhookPayload) {
        if (template == null || !template.contains("{{")) {
            return template;
        }

        Matcher matcher = PATTERN.matcher(template);
        StringBuffer resolvedString = new StringBuffer();

        // Convert the JsonNode to a String so Jayway can parse it once (Performance boost!)
        String jsonString = webhookPayload.toString();
        Object document = Configuration.defaultConfiguration().jsonProvider().parse(jsonString);

        while (matcher.find()) {
            String jsonPathExpr = matcher.group(1); // Grabs the "$.customer" part
            try {
                // Dynamically queries the JSON tree!
                Object extractedValue = JsonPath.read(document, jsonPathExpr);
                String replacement = extractedValue != null ? extractedValue.toString() : "";

                // Replaces {{$.customer}} with the actual value
                matcher.appendReplacement(resolvedString, Matcher.quoteReplacement(replacement));
            } catch (Exception e) {
                log.warn("Could not resolve JSONPath: {}. Replacing with UNKNOWN.", jsonPathExpr);
                matcher.appendReplacement(resolvedString, "UNKNOWN");
            }
        }
        matcher.appendTail(resolvedString);

        return resolvedString.toString();
    }
}