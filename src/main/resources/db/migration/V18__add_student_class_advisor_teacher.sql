DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'student_classes'
          AND column_name = 'advisor_teacher_id'
    ) THEN
        EXECUTE 'ALTER TABLE public.student_classes ADD COLUMN advisor_teacher_id bigint';
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints tc
        WHERE tc.constraint_schema = 'public'
          AND tc.table_name = 'student_classes'
          AND tc.constraint_name = 'fk_student_classes_advisor_teacher'
    ) THEN
        EXECUTE 'ALTER TABLE public.student_classes ADD CONSTRAINT fk_student_classes_advisor_teacher FOREIGN KEY (advisor_teacher_id) REFERENCES public.teachers(id)';
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM pg_indexes
        WHERE schemaname = 'public'
          AND tablename = 'student_classes'
          AND indexname = 'idx_student_classes_advisor_teacher_id'
    ) THEN
        EXECUTE 'CREATE INDEX idx_student_classes_advisor_teacher_id ON public.student_classes(advisor_teacher_id)';
    END IF;
END $$;

