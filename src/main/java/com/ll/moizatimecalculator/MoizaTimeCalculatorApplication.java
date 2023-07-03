package com.ll.moizatimecalculator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class MoizaTimeCalculatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(MoizaTimeCalculatorApplication.class, args);
    }

}
