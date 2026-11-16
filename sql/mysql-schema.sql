-- ============================================================
-- 单位管理系统 · MySQL 8 建表脚本
-- 用法：mysql -u root -p < sql/mysql-schema.sql
--       mysql -u root -p unit_sys < src/main/resources/db/data.sql
-- ============================================================
CREATE DATABASE IF NOT EXISTS unit_sys DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE unit_sys;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS perf_record;
DROP TABLE IF EXISTS sys_user;
DROP TABLE IF EXISTS sys_role_perm;
DROP TABLE IF EXISTS sys_perm;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_dept;
SET FOREIGN_KEY_CHECKS = 1;

-- 部门（支持上级部门，当前演示数据是一级）
CREATE TABLE sys_dept (
  id        INT AUTO_INCREMENT PRIMARY KEY,
  code      VARCHAR(20)  NOT NULL,
  name      VARCHAR(50)  NOT NULL,
  parent_id INT          NULL,
  leader    VARCHAR(30)  NULL COMMENT '负责人',
  sort      INT          NOT NULL DEFAULT 0,
  UNIQUE KEY uk_dept_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门';

-- 角色：level 越小权限越高（1 系统管理员 / 2 部门管理员 / 3 普通员工）
CREATE TABLE sys_role (
  id    INT AUTO_INCREMENT PRIMARY KEY,
  code  VARCHAR(30) NOT NULL,
  name  VARCHAR(30) NOT NULL,
  level INT         NOT NULL COMMENT '1 系统管理员 / 2 部门管理员 / 3 普通员工',
  UNIQUE KEY uk_role_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色';

-- 权限点（RBAC 的 permission）
CREATE TABLE sys_perm (
  id     INT AUTO_INCREMENT PRIMARY KEY,
  code   VARCHAR(40) NOT NULL COMMENT '如 user:write',
  name   VARCHAR(40) NOT NULL,
  module VARCHAR(20) NOT NULL,
  UNIQUE KEY uk_perm_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限点';

-- 角色-权限
CREATE TABLE sys_role_perm (
  role_id INT NOT NULL,
  perm_id INT NOT NULL,
  PRIMARY KEY (role_id, perm_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联';

-- 人员
CREATE TABLE sys_user (
  id         INT AUTO_INCREMENT PRIMARY KEY,
  username   VARCHAR(40) NOT NULL,
  password   VARCHAR(160) NOT NULL COMMENT 'PBKDF2-HMAC-SHA256',
  real_name  VARCHAR(30) NOT NULL,
  gender     VARCHAR(4)  NULL,
  phone      VARCHAR(20) NULL,
  email      VARCHAR(80) NULL,
  dept_id    INT         NOT NULL,
  role_id    INT         NOT NULL,
  education  VARCHAR(10) NULL,
  hire_date  DATE        NULL,
  status     TINYINT     NOT NULL DEFAULT 1 COMMENT '1 在职 / 0 离职',
  created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_username (username),
  KEY idx_user_dept (dept_id),
  KEY idx_user_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='人员';

-- 绩效（每人每周期一条）
CREATE TABLE perf_record (
  id     INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  period  CHAR(7)      NOT NULL COMMENT '2025-11',
  score   DECIMAL(5,1) NOT NULL,
  tasks   INT          NOT NULL DEFAULT 0 COMMENT '任务数',
  done    INT          NOT NULL DEFAULT 0 COMMENT '完成数',
  UNIQUE KEY uk_perf_user_period (user_id, period),
  KEY idx_perf_period (period)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='绩效记录';
