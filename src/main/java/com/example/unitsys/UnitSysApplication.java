package com.example.unitsys;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.unitsys.mapper")
public class UnitSysApplication {

    public static void main(String[] args) {
        SpringApplication.run(UnitSysApplication.class, args);
    }
}
