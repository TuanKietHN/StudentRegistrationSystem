DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'users' AND column_name = 'username' AND data_type = 'bytea'
  ) THEN
    ALTER TABLE users
      ALTER COLUMN username TYPE VARCHAR(255) USING convert_from(username, 'UTF8');
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'users' AND column_name = 'email' AND data_type = 'bytea'
  ) THEN
    ALTER TABLE users
      ALTER COLUMN email TYPE VARCHAR(255) USING convert_from(email, 'UTF8');
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'users' AND column_name = 'password' AND data_type = 'bytea'
  ) THEN
    ALTER TABLE users
      ALTER COLUMN password TYPE VARCHAR(255) USING convert_from(password, 'UTF8');
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'departments' AND column_name = 'code' AND data_type = 'bytea'
  ) THEN
    ALTER TABLE departments
      ALTER COLUMN code TYPE VARCHAR(20) USING convert_from(code, 'UTF8');
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'departments' AND column_name = 'name' AND data_type = 'bytea'
  ) THEN
    ALTER TABLE departments
      ALTER COLUMN name TYPE VARCHAR(255) USING convert_from(name, 'UTF8');
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'subjects' AND column_name = 'code' AND data_type = 'bytea'
  ) THEN
    ALTER TABLE subjects
      ALTER COLUMN code TYPE VARCHAR(255) USING convert_from(code, 'UTF8');
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'subjects' AND column_name = 'name' AND data_type = 'bytea'
  ) THEN
    ALTER TABLE subjects
      ALTER COLUMN name TYPE VARCHAR(255) USING convert_from(name, 'UTF8');
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'semesters' AND column_name = 'code' AND data_type = 'bytea'
  ) THEN
    ALTER TABLE semesters
      ALTER COLUMN code TYPE VARCHAR(255) USING convert_from(code, 'UTF8');
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'semesters' AND column_name = 'name' AND data_type = 'bytea'
  ) THEN
    ALTER TABLE semesters
      ALTER COLUMN name TYPE VARCHAR(255) USING convert_from(name, 'UTF8');
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'courses' AND column_name = 'code' AND data_type = 'bytea'
  ) THEN
    ALTER TABLE courses
      ALTER COLUMN code TYPE VARCHAR(255) USING convert_from(code, 'UTF8');
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'courses' AND column_name = 'name' AND data_type = 'bytea'
  ) THEN
    ALTER TABLE courses
      ALTER COLUMN name TYPE VARCHAR(255) USING convert_from(name, 'UTF8');
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'teachers' AND column_name = 'employee_code' AND data_type = 'bytea'
  ) THEN
    ALTER TABLE teachers
      ALTER COLUMN employee_code TYPE VARCHAR(50) USING convert_from(employee_code, 'UTF8');
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'students' AND column_name = 'student_code' AND data_type = 'bytea'
  ) THEN
    ALTER TABLE students
      ALTER COLUMN student_code TYPE VARCHAR(50) USING convert_from(student_code, 'UTF8');
  END IF;
END $$;

