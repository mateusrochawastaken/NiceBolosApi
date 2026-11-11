package org.nicebolos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

@Configuration
@ComponentScan(basePackages = {"org.nicebolos","org.nicebolos.businesslayer"})
@EnableAutoConfiguration
@EnableWebMvc
public class SpringConfig implements WebMvcConfigurer {
    Document nicexml;
    public SpringConfig(){
        try {
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = df.newDocumentBuilder();
            nicexml = db.parse(classloader.getResource("nicebolos.xml").toString());
        }catch (ParserConfigurationException | IOException | SAXException e){
            throw new RuntimeException(e);
        }
    }

    public static void main() {
        SpringApplication application = new SpringApplication(SpringConfig.class);
        application.run();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry){
        registry.addResourceHandler("/thumbs/**").
                addResourceLocations("file:" + nicexml.getElementsByTagName("ThumbsPath").item(0).getTextContent());
    }
}
