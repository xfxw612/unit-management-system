-- ============================================================
-- H2 建表脚本（本地默认 profile 用，内存库，MODE=MySQL）
-- 与 sql/mysql-schema.sql 保持同样的表结构，只是去掉 MySQL 专有语法。
-- ============================================================
DROP TABLE IF EXISTS perf_record;
DROP TABLE IF EXISTS sys_user;
DROP TABLE IF EXISTS sys_role_perm;
DROP TABLE IF EXISTS sys_perm;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_dept;

CREATE TABLE sys_dept (
  id        INT AUTO_INCREMENT PRIMARY KEY,
  code      VARCHAR(20) NOT NULL UNIQUE,
  name      VARCHAR(50) NOT NULL,
  parent_id INT,
  leader    VARCHAR(30),
  sort      INT NOT NULL DEFAULT 0
);

CREATE TABLE sys_role (
  id    INT AUTO_INCREMENT PRIMARY KEY,
  code  VARCHAR(30) NOT NULL UNIQUE,
  name  VARCHAR(30) NOT NULL,
  level INT NOT NULL
);

CREATE TABLE sys_perm (
  id     INT AUTO_INCREMENT PRIMARY KEY,
  code   VARCHAR(40) NOT NULL UNIQUE,
  name   VARCHAR(40) NOT NULL,
  module VARCHAR(20) NOT NULL
);

CREATE TABLE sys_role_perm (
  role_id INT NOT NULL,
  perm_id INT NOT NULL,
  PRIMARY KEY (role_id, perm_id)
);

CREATE TABLE sys_user (
  id         INT AUTO_INCREMENT PRIMARY KEY,
  username   VARCHAR(40) NOT NULL UNIQUE,
  password   VARCHAR(160) NOT NULL,
  real_name  VARCHAR(30) NOT NULL,
  gender     VARCHAR(4),
  phone      VARCHAR(20),
  email      VARCHAR(80),
  dept_id    INT NOT NULL,
  role_id    INT NOT NULL,
  education  VARCHAR(10),
  hire_date  DATE,
  status     TINYINT NOT NULL DEFAULT 1,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_dept ON sys_user (dept_id);
CREATE INDEX idx_user_role ON sys_user (role_id);

CREATE TABLE perf_record (
  id      INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  period  CHAR(7) NOT NULL,
  score   DECIMAL(5,1) NOT NULL,
  tasks   INT NOT NULL DEFAULT 0,
  done    INT NOT NULL DEFAULT 0,
  CONSTRAINT uk_perf_user_period UNIQUE (user_id, period)
);
