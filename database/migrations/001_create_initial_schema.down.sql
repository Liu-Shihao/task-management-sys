-- migrations/001_create_initial_schema.down.sql
-- Task Management System - Rollback Schema

BEGIN;

DROP TABLE IF EXISTS task_executions CASCADE;
DROP TABLE IF EXISTS tasks CASCADE;
DROP TABLE IF EXISTS release_rundowns CASCADE;
DROP TABLE IF EXISTS templates CASCADE;

DROP FUNCTION IF EXISTS update_updated_at_column();

COMMIT;
