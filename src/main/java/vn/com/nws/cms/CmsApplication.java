package vn.com.nws.cms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Arrays;

@SpringBootApplication
@EnableJpaAuditing
@Slf4j
public class CmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(CmsApplication.class, args);
    }

    /**
     * In thông tin kết nối và cấu hình ngay sau khi app khởi động xong.
     * ApplicationRunner chạy sau Flyway và DataSeeder — dùng để xác nhận
     * đang kết nối đúng DB và profile đúng.
     */
    @Bean
    ApplicationRunner logStartupInfo(Environment env, DataSource dataSource) {
        return args -> {
            String[] profiles = env.getActiveProfiles();
            log.info("╔══════════════════════════════════════════════╗");
            log.info("║              CMS APPLICATION READY           ║");
            log.info("╠══════════════════════════════════════════════╣");
            log.info("║  Active profiles : {}", profiles.length > 0
                    ? Arrays.toString(profiles)
                    : "[] ⚠ Không có profile — kiểm tra SPRING_PROFILES_ACTIVE");
            log.info("║  DB URL (config) : {}", env.getProperty("spring.datasource.url"));
            log.info("║  Flyway enabled  : {}", env.getProperty("spring.flyway.enabled"));
            log.info("║  Seed enabled    : {}", env.getProperty("cms.seed.enabled"));
            log.info("╠══════════════════════════════════════════════╣");
            try (Connection c = dataSource.getConnection()) {
                log.info("║  DB connected    : {}", c.getMetaData().getURL());
                log.info("║  DB product      : {} {}",
                        c.getMetaData().getDatabaseProductName(),
                        c.getMetaData().getDatabaseProductVersion());
            } catch (Exception e) {
                log.error("║  DB connected    : FAILED — {}", e.getMessage());
            }
            log.info("╚══════════════════════════════════════════════╝");
        };
    }
}