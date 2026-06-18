package guru.springframework.spring7restmvc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.kafka.annotation.EnableKafka;

@EnableCaching
@EnableKafka
@SpringBootApplication
public class Spring7RestMvcApplication {

    public static void main(String[] args) {
        SpringApplication.run(Spring7RestMvcApplication.class, args);
    }

}
