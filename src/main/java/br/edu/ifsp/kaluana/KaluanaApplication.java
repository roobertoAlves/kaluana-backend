package br.edu.ifsp.kaluana;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class KaluanaApplication {

    public static void main(String[] args) {
        SpringApplication.run(KaluanaApplication.class, args);
    }

}
