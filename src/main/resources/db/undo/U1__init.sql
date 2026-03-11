-- Manual rollback script (Flyway Community does not run undo migrations automatically).
DROP TABLE IF EXISTS execution_log;
DROP TABLE IF EXISTS task;
DROP TABLE IF EXISTS release_rundown;
DROP TABLE IF EXISTS template;
DROP TABLE IF EXISTS project;
DROP TABLE IF EXISTS `user`;
