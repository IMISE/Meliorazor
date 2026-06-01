package eu.brunsch.terminology.annotator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulithic;

@Modulithic(
    sharedModules = {"eu.brunsch.terminology.annotator.useraccount"},
    useFullyQualifiedModuleNames = true
)
@SpringBootApplication
public class AnnotatorApplication {

    public static void main(final String[] args) {
        SpringApplication.run(AnnotatorApplication.class, args);
    }

}
