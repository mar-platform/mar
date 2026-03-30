package ml2.mar.webserver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Map the route to the specific file path under static
        registry.addViewController("/modelgraph-ui").setViewName("forward:/modelgraph-ui/index.html");
        registry.addViewController("/modelgraph-ui/").setViewName("forward:/modelgraph-ui/index.html");
    }
}