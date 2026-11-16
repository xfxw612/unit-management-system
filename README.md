# 单位管理系统（演示版）

一个可以直接跑起来、可以当场讲清楚的小型后台系统：人员 / 部门 / 权限三个模块，三级 RBAC，人员结构与部门绩效统计，列表分页与条件查询，以及 Excel / CSV 导出。

技术栈刻意选了最普通的一套（Spring Boot + MyBatis + MySQL），没有引入缓存、消息队列、注册中心这类这个体量用不上的东西。

---

## 1. 技术栈

| 层 | 选型 | 版本 |
| --- | --- | --- |
| 语言 | Java | 17（pom 里 `<java.version>17</java.version>`，本机用 JDK 21 编译运行也正常） |
| 框架 | Spring Boot | 3.3.5 |
| 持久层 | MyBatis（mapper 接口 + XML） | mybatis-spring-boot-starter 3.0.3 |
| 数据库 | MySQL 8（生产 profile） / H2 2.2（`h2` 开发 profile） | 见 `pom.xml` |
| 导出 | Apache POI（xlsx） / 手写 CSV（带 BOM） | poi-ooxml 5.2.5 |
| 前端 | 原生 HTML + CSS + JS（无构建、无框架），图形用 SVG 手绘 | — |
| 密码 | PBKDF2WithHmacSHA256，120000 次迭代，16 字节随机盐 | JDK 自带 |
| 构建 | Maven | 3.9.9 |

---

## 2. 目录结构

```
单位管理系统/
├─ pom.xml
├─ run.bat                          # Windows 双击启动（H2 模式）
├─ README.md
├─ sql/
│  └─ mysql-schema.sql              # MySQL 8 建库建表（含 CREATE DATABASE）
└─ src/main/
   ├─ java/com/example/unitsys/
   │  ├─ UnitSysApplication.java
   │  ├─ common/                    # ApiResponse、PageResult、BizException、全局异常处理
   │  ├─ auth/                      # LoginUser、@RequirePerm、AuthInterceptor
   │  ├─ config/                    # WebConfig（注册拦截器）
   │  ├─ controller/                # Auth/User/Dept/Role/Stats/Export
   │  ├─ service/                   # 业务逻辑 + 数据范围与越权判断
   │  ├─ mapper/                    # 接口
   │  ├─ entity/ dto/
   │  └─ util/                      # PasswordUtil、CsvUtil、ExcelUtil
   └─ resources/
      ├─ application.yml            # 两个 profile：h2（默认）/ mysql
      ├─ mapper/*.xml               # 4 个 Mapper 的 SQL
      ├─ db/h2-schema.sql           # H2 建表（MODE=MySQL）
      ├─ db/data.sql                # 初始数据（部门/角色/权限/32 人/87 条绩效）
      └─ static/                    # login.html / index.html / dept.html / roles.html / stats.html / app.js / app.css
```

---

## 3. 怎么跑

### 路线 A：H2 内存库（默认，不需要装任何数据库）

```bash
# 需要 JDK 17+ 和 Maven 3.8+
mvn spring-boot:run
```

或者：

```bash
mvn -DskipTests package
java -jar target/unit-sys.jar
```

Windows 上也可以直接双击 `run.bat`。

- 默认端口 **8088**，打开 <http://localhost:8088>
- `application.yml` 里 `spring.profiles.active` 默认为 `h2`，启动时自动执行 `db/h2-schema.sql` + `db/data.sql`
- **H2 是内存库，进程一停数据就没了**，重启后回到初始数据。演示够用，改数据不怕搞坏。
- 额外开了 H2 控制台：<http://localhost:8088/h2-console>，JDBC URL 填 `jdbc:h2:mem:unitsys;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1`，用户 `sa`，密码空。

### 路线 B：MySQL 8（生产 profile）

1. 建库建表 + 导入初始数据：

   ```bash
   mysql -uroot -p < sql/mysql-schema.sql
   mysql -uroot -p unit_sys < src/main/resources/db/data.sql
   ```

   > `data.sql` 是标准 INSERT，在 MySQL 和 H2(MODE=MySQL) 下都能执行。H2 启动时由 Spring 自动跑，MySQL 下由你自己跑。

2. 改连接信息：`src/main/resources/application.yml` 里 `mysql` profile 段

   ```yaml
   url: jdbc:mysql://localhost:3306/unit_sys?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
   username: root
   password: 123456
   ```

   （密码是本机示例值，换成你自己的。）

3. 用 mysql profile 启动：

   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=mysql
   # 或
   java -jar target/unit-sys.jar --spring.profiles.active=mysql
   ```

   `mysql` profile 下 `spring.sql.init.mode=never`，不会自动建表/灌数据，避免覆盖你自己的库。

> **关于本机**：交付验证时这台机器上**没有安装 MySQL**（3306 端口不通、无 mysqld 进程与服务），所以我没有偷偷把它换成别的数据库草草了事，而是保留 MySQL 8 作为正式方案、另给出 H2 开发 profile 兜底。所有实测结论都是在 H2 上跑出来的；MySQL 那条路是按标准写法准备的，但**没有在本机跑过**。面试时如果要现场演示 MySQL，建议提前在机器上装好 MySQL 8 并按上面的步骤导一次，避免临场等下载。

---

## 4. 默认账号

初始数据里有 32 个账号，三个层级各一个：

| 账号 | 密码 | 角色 | 级别 | 部门 | 能做什么 |
| --- | --- | --- | --- | --- | --- |
| `admin` | `admin123` | 系统管理员 ADMIN | 1 | 综合办公室 | 全部：人员、部门、角色矩阵、统计、导出 |
| `zhangwei` | `manager123` | 部门管理员 DEPT_MANAGER | 2 | 研发中心 | 只能看/管**本部门**人员，可导出，不能进部门管理、不能看角色矩阵 |
| `wangfang` | `123456` | 普通员工 STAFF | 3 | 研发中心 | 只能看本部门人员列表，不能增删改、不能看统计、不能导出 |

其余 29 个员工账号密码都是 `123456`，可在登录页用 `admin` 进去后在人员管理里看到账号名。另有 `tangning`、`fengyu` 两个离职账号（状态=离职），用来演示状态筛选。

> 密码在库里是 PBKDF2 摘要（`pbkdf2$120000$<salt>$<hash>`），不是明文。

---

## 5. 权限模型（三级 RBAC）

权限点是细粒度的字符串，挂在角色上（`sys_role_perm`），用户挂角色（`sys_user.role_id`），角色带 `level` 表示级别：1 管理员 / 2 部门管理员 / 3 普通员工。

9 个权限码与三级的对应关系：

| 权限码 | 含义 | ADMIN(1) | DEPT_MANAGER(2) | STAFF(3) |
| --- | --- | --- | :-: | :-: |
| `user:read` | 查看人员列表/详情 | ✔ | ✔ | ✔ |
| `user:write` | 新增/修改/删除人员、重置密码 | ✔ | ✔ | |
| `dept:read` | 查看部门列表 | ✔ | ✔ | ✔ |
| `dept:write` | 新增/修改/删除部门 | ✔ | | |
| `role:read` | 查看角色与权限矩阵 | ✔ | | |
| `role:write` | 调整人员所属角色 | ✔ | | |
| `stats:read` | 查看人员结构与部门绩效统计 | ✔ | ✔ | |
| `export:users` | 导出人员列表 CSV/Excel | ✔ | ✔ | |
| `export:stats` | 导出部门绩效 CSV/Excel | ✔ | ✔ | |

**越权是被拦住的，不是靠前端隐藏按钮**，拦在两层：

1. **接口级**：`AuthInterceptor` 拦 `/api/**`，先从 session 取登录用户（没有 → 401），再看方法/类上的 `@RequirePerm("xxx")`（没有该权限 → 403，请求进不到 Service）。
2. **数据级**：`UserService.applyScope` 强制把非管理员的查询条件收敛到本部门；显式传了别部门的 `deptId` 直接 403（`越权：只能查看本部门人员`）。写操作同理（`resolveWriteDept`），并且只能改级别比自己低的人（`checkTargetMutable`），不能把账号分配成级别 ≥ 自己的角色（`checkRoleAssignable`）。
3. 改角色单独一个权限点 `role:write`，只有管理员有。

前端 `app.js` 里也按 `data-perm` 隐藏了没权限的菜单和按钮，但那只是体验，安全性全在后端。

---

## 6. 接口列表

统一返回 `{ code, message, data }`；分页返回 `{ total, page, size, rows }`。所有接口都需要登录（session cookie `JSESSIONID`），下表「权限」列是方法上 `@RequirePerm` 的要求。

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| POST | `/api/auth/login` | — | 登录，body `{username,password}` |
| POST | `/api/auth/logout` | — | 退出 |
| GET | `/api/auth/me` | 登录即可 | 当前用户（含角色、级别、权限码集合） |
| GET | `/api/users` | `user:read` | 分页查询：`page`(默认1) `size`(默认10，上限100) `keyword` `deptId` `roleId` `status` `education` |
| GET | `/api/users/{id}` | `user:read` | 详情 |
| POST | `/api/users` | `user:write` | 新增（不传密码则默认 `123456`） |
| PUT | `/api/users/{id}` | `user:write` | 修改 |
| DELETE | `/api/users/{id}` | `user:write` | 删除 |
| POST | `/api/users/{id}/reset-password` | `user:write` | 重置为 `123456` |
| PUT | `/api/users/{id}/role` | `role:write` | 调整角色 |
| GET | `/api/depts` | `dept:read` | 部门列表（含在职人数；非管理员只看本部门） |
| POST | `/api/depts` | `dept:write` | 新增部门 |
| PUT | `/api/depts/{id}` | `dept:write` | 修改部门 |
| DELETE | `/api/depts/{id}` | `dept:write` | 删除部门（部门下有人 → 409） |
| GET | `/api/roles` | `role:read` | 角色 + 权限矩阵 |
| GET | `/api/roles/options` | `user:read` | 角色下拉选项（只给 id/name/code/level，给新增人员用） |
| GET | `/api/stats/overview` | `stats:read` | 汇总：`{scope, deptStats[], structure{byDept,byRole,byEducation,byStatus}, trend[]}` |
| GET | `/api/stats/dept-stats` | `stats:read` | 部门绩效明细 |
| GET | `/api/stats/structure` | `stats:read` | 人员结构四组分布 |
| GET | `/api/stats/trend` | `stats:read` | 绩效月度趋势 |
| GET | `/api/export/users` | `export:users` | 导出人员，`format=csv\|xlsx`（支持同样的筛选条件） |
| GET | `/api/export/dept-stats` | `export:stats` | 导出部门绩效，`format=csv\|xlsx` |

示例：

```bash
curl -c c.txt -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' http://localhost:8088/api/auth/login

curl -b c.txt "http://localhost:8088/api/users?page=1&size=5&deptId=2&status=1&keyword=王"
curl -b c.txt -o users.xlsx "http://localhost:8088/api/export/users?format=xlsx"
```

---

## 7. 数据模型

6 张表（表名避开 MySQL 保留字，`user`/`role` 一律加 `sys_` 前缀）：

- `sys_dept` 部门：id、code、name、leader、sort、remark
- `sys_role` 角色：id、code、name、level、remark
- `sys_perm` 权限点：id、code、name、module
- `sys_role_perm` 角色-权限：role_id、perm_id（联合主键）
- `sys_user` 人员：id、username、password、real_name、gender、phone、email、dept_id、role_id、education、hire_date、status、created_at
- `perf_record` 部门绩效：id、dept_id、period（如 `2025-10`）、score、total_tasks、done_tasks

初始数据：5 个部门、3 个角色、9 个权限点、32 个人员、3 个周期共 87 条部门绩效。

---

## 8. 实测结果（交付前实际跑过）

环境：Windows 10、JDK 21.0.12（`<java.version>17</java.version>` 编译）、Maven 3.9.9、`h2` profile。

| 项目 | 结果 |
| --- | --- |
| 编译打包 | `mvn -DskipTests package` 成功，`target/unit-sys.jar` ≈ 46 MB |
| 启动 | `java -jar unit-sys.jar --server.port=8088` → `Started UnitSysApplication in 2.5 seconds`，端口 **8088** |
| 接口自测脚本 | `unit-sys-test.ps1` 共 **37 项断言，全部通过**（匿名 401、分页 total=32、条件查询、CRUD、重名 409、重置密码后新密码可登录、CSV/Excel 下载、三级越权拦截等） |
| 分页查询耗时 | 32 条数据 `GET /api/users?page=1&size=10` 约 60 ms |
| 导出 CSV | 2472 字节，UTF-8 带 BOM，Excel 打开中文不乱码 |
| 导出 xlsx | 4006 字节，表头加粗、首行冻结 |
| 越权验证 | 部门管理员查别部门 → 403「越权：只能查看本部门人员」；给别部门加人 → 403；分配 ADMIN 角色 → 403「不能分配权限不低于自己的角色」；员工建人员 → 403 |
| 页面 | 登录页、人员管理、部门管理、角色权限、统计与导出 5 个页面均正常渲染，统计页 6 张图（部门人数 / 部门平均绩效 / 角色环图 / 学历分布 / 月度趋势 / 在职离职）+ 部门绩效明细表 |

---

## 9. 几个可以聊的设计取舍

- **为什么是 MyBatis 手写 XML 而不是 JPA**：这个项目最值得讲的恰恰是「列表查询要分页 + 多条件动态拼接 + 只查需要的列（不返回 password）」和「部门绩效统计要 join 又不能被绩效行数翻倍（用 `count(distinct u.id)`）」——这些写 SQL 才讲得清。JPA 也能做，但会把重点藏进注解里。
- **为什么用 session 而不是 JWT**：同源的单体后台，session 更简单，退出登录是真失效。`LoginUser` 里直接带权限码集合，鉴权不用每次查库。
- **权限为什么查两次（拦截器 + Service 数据范围）**：权限码只能回答「这个功能能不能用」，回答不了「这条数据是不是他的」。部门隔离必须在 Service 里按登录用户收敛查询条件，这层才是防越权的兜底。
- **前端为什么不用框架**：需求里就是表格、表单、六个图，原生实现反而代码量小、零构建、面试时每一行都能解释。图表用 SVG 手写（柱/环/折线三个函数，约 100 行）。
- **H2 只用 `MODE=MySQL`**：建表和 SQL 写法与 MySQL 保持一致（`limit ? offset ?`、`count(distinct ...)`），切换时不用改 SQL，这也是能一条命令切库的原因。

---

## 10. 已知边界

- H2 是内存库，重启数据重置（演示友好，别当生产用）。
- 没有做「部门树」（部门是平铺的，够这个体量）；没有审计日志、没有文件上传、没有多租户。
- `perf_record` 目前由初始数据提供，页面上**没有**录入绩效的界面——绩效是统计的输入，不是本系统的核心操作，这一点在 demo 时说明比留一个半成品录入页更好。
- 导出的 CSV 是 UTF-8 带 BOM（Excel 直接双击不乱码）；如果对方要求 GBK，改 `CsvUtil` 一处即可。
