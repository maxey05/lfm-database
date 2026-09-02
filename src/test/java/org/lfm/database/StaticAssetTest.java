package org.lfm.database;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class StaticAssetTest
{
    private static final Pattern WEBJAR_SRC = Pattern.compile("@\\{(/webjars/[^}]+)\\}");

    @Test
    void everyWebjarAssetTheLayoutReferencesActuallyExistsOnTheClasspath() throws IOException
    {
        String layout = new String(new ClassPathResource("templates/layout.html")
                .getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        Matcher matcher = WEBJAR_SRC.matcher(layout);
        int found = 0;

        while(matcher.find())
        {
            found++;
            String path = matcher.group(1);
            ClassPathResource resource = new ClassPathResource("META-INF/resources" + path);

            assertThat(resource.exists())
                    .withFailMessage("layout.html asks for %s but there is no such classpath resource; "
                            + "the browser would get a 404 and htmx would never load", path)
                    .isTrue();
        }

        assertThat(found).isPositive();
    }

    @Test
    void theApplicationJavascriptIsServedFromAPermittedPath() throws IOException
    {
        assertThat(new ClassPathResource("static/js/app.js").exists()).isTrue();
        assertThat(new ClassPathResource("static/css/app.css").exists()).isTrue();
    }
}
