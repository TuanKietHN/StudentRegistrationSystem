package vn.com.nws.cms.common.db;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Component
@Profile("dev")
@Order(0)
@RequiredArgsConstructor
@Slf4j
public class SchemaRepairRunner implements CommandLineRunner {

    private final DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            boolean repairedAdminClasses = false;
            boolean repairedSemesters = false;
            boolean repairedAcademicRenames = false;
            try (var st = conn.createStatement()) {
                if (tableExists(conn, "admin_classes")) {
                    repairedAdminClasses |= tryExecute(st, "ALTER TABLE admin_classes ALTER COLUMN code TYPE VARCHAR(50) USING convert_from(code, 'UTF8')");
                    repairedAdminClasses |= tryExecute(st, "ALTER TABLE admin_classes ALTER COLUMN name TYPE VARCHAR(255) USING convert_from(name, 'UTF8')");
                    repairedAdminClasses |= tryExecute(st, "ALTER TABLE admin_classes ALTER COLUMN program TYPE VARCHAR(255) USING convert_from(program, 'UTF8')");
                }

                if (tableExists(conn, "semesters")) {
                    repairedSemesters |= tryExecute(st, "ALTER TABLE semesters ADD COLUMN IF NOT EXISTS secondary_active BOOLEAN NOT NULL DEFAULT FALSE");
                }

                if (tableExists(conn, "cohorts")) {
                    tryExecute(st, "ALTER TABLE cohorts ADD COLUMN IF NOT EXISTS registration_enabled BOOLEAN NOT NULL DEFAULT TRUE");
                }

                if (tableExists(conn, "enrollments") && columnExists(conn, "enrollments", "course_id") && !columnExists(conn, "enrollments", "cohort_id")) {
                    repairedAcademicRenames |= tryExecute(st, "ALTER TABLE enrollments ADD COLUMN cohort_id BIGINT");
                    repairedAcademicRenames |= tryExecute(st, "UPDATE enrollments SET cohort_id = course_id WHERE cohort_id IS NULL");
                }

                if (tableExists(conn, "attendance_sessions") && columnExists(conn, "attendance_sessions", "course_id") && !columnExists(conn, "attendance_sessions", "cohort_id")) {
                    repairedAcademicRenames |= tryExecute(st, "ALTER TABLE attendance_sessions ADD COLUMN cohort_id BIGINT");
                    repairedAcademicRenames |= tryExecute(st, "UPDATE attendance_sessions SET cohort_id = course_id WHERE cohort_id IS NULL");
                }
            }

            if (repairedAdminClasses) {
                log.warn("Schema repaired: admin_classes columns converted from bytea to varchar");
            }
            if (repairedSemesters) {
                log.warn("Schema repaired: semesters.secondary_active added");
            }
            if (repairedAcademicRenames) {
                log.warn("Schema repaired: academic columns backfilled for new naming");
            }
        }
    }

    private boolean tableExists(Connection conn, String tableName) throws Exception {
        try (var ps = conn.prepareStatement("SELECT to_regclass(?) IS NOT NULL")) {
            ps.setString(1, tableName);
            try (var rs = ps.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }
        }
    }

    private boolean columnExists(Connection conn, String tableName, String columnName) throws Exception {
        try (var ps = conn.prepareStatement("""
                SELECT EXISTS (
                    SELECT 1
                    FROM information_schema.columns
                    WHERE table_name = ? AND column_name = ?
                )
                """)) {
            ps.setString(1, tableName);
            ps.setString(2, columnName);
            try (var rs = ps.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }
        }
    }

    private boolean tryExecute(java.sql.Statement st, String sql) {
        try {
            st.execute(sql);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }
}
