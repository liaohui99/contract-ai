package com.example.springaiapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import com.example.springaiapp.skills.SelfSkillsConfig;

@SpringBootApplication
@Import({SelfSkillsConfig.class})
public class TaoRunHuiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaoRunHuiApplication.class, args);
    }

}