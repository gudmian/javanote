package io.gudmian.javanote;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class JavanoteApplication {

    public static void main(String[] args) {
        SpringApplication.run(JavanoteApplication.class, args);
    }

}
