package br.com.saveeditor.brasfoot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.shell.command.annotation.CommandScan;

@SpringBootApplication
@CommandScan
public class BrasfootEditorApplication {

    public static void main(String[] args) {
        // Run as a standard Spring Boot console application

        SpringApplication.run(BrasfootEditorApplication.class, args);
    }
}
