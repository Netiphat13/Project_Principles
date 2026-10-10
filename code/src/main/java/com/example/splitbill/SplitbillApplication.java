package com.example.splitbill;

import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SplitbillApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(SplitbillApplication.class);
        // ค่าเริ่มต้นสำหรับอัปโหลดสลิป (application.properties ไม่ถูกเก็บใน git จึงตั้งไว้ที่นี่)
        // ถ้าตั้งค่าเดียวกันใน application.properties จะใช้ค่าจากไฟล์นั้นแทน
        app.setDefaultProperties(Map.of(
                "spring.servlet.multipart.max-file-size", "8MB",
                "spring.servlet.multipart.max-request-size", "10MB",
                // ใช้ cookie เก็บ session เท่านั้น ไม่ต่อ ;jsessionid= ท้าย URL
                "server.servlet.session.tracking-modes", "cookie",
                "app.upload-dir", "uploads/slips"));
        app.run(args);
    }
}
