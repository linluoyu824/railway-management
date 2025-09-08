-- SQL Script for creating Department and User tables
-- 推荐按顺序执行

-- 1. 创建 departments 表
-- 用于存储公司的组织架构，支持无限层级
CREATE TABLE `departments` (
                               `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '部门主键ID',
                               `name` VARCHAR(100) NOT NULL COMMENT '部门名称',
                               `level` INT NOT NULL COMMENT '部门层级',
                               `parent_id` BIGINT NULL COMMENT '上级部门ID，外键关联自身',
                               PRIMARY KEY (`id`),
    -- 添加外键约束，使得 parent_id 必须是 departments 表中已存在的 id
                               CONSTRAINT `fk_departments_parent`
                                   FOREIGN KEY (`parent_id`)
                                       REFERENCES `departments` (`id`)
                                       ON DELETE SET NULL -- 如果父部门被删除，子部门的parent_id设为NULL
                                       ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门信息表';


-- 2. 创建 users 表 (包含与 departments 的关联)
-- 如果您是首次创建，请使用此脚本。如果您已经有users表，请参考下面的 ALTER TABLE 脚本。
CREATE TABLE `users` (
                         `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户主键ID',
                         `username` VARCHAR(50) NOT NULL COMMENT '用户名',
                         `password` VARCHAR(255) NOT NULL COMMENT '密码 (存储哈希值)',
                         `full_name` VARCHAR(100) NULL COMMENT '姓名',
                         `employee_id` VARCHAR(20) NOT NULL COMMENT '员工工号',
                         `pinyin_code` VARCHAR(50) NULL COMMENT '姓名拼音码',
                         `department_id` BIGINT NULL COMMENT '所属部门ID，外键关联departments表',
                         `job_title` VARCHAR(50) NULL COMMENT '职位',
                         `driver_license_type` VARCHAR(20) NULL COMMENT '驾照类型 (C1, C2等)',
                         `mobile_phone` VARCHAR(20) NOT NULL COMMENT '手机号码',
                         `job_level` INT NULL COMMENT '员工职级',
                         `created_at` DATETIME(6) NOT NULL COMMENT '创建时间',
                         `created_by` VARCHAR(50) NULL COMMENT '创建人',
                         `updated_at` DATETIME(6) NULL COMMENT '最后更新时间',
                         `updated_by` VARCHAR(50) NULL COMMENT '最后更新人',
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `uk_users_username` (`username`),
                         UNIQUE KEY `uk_users_employee_id` (`employee_id`),
                         UNIQUE KEY `uk_users_mobile_phone` (`mobile_phone`),
    -- 添加外键约束，将 users 表与 departments 表关联起来
                         CONSTRAINT `fk_users_department`
                             FOREIGN KEY (`department_id`)
                                 REFERENCES `departments` (`id`)
                                 ON DELETE SET NULL -- 如果部门被删除，该用户的部门ID设为NULL
                                 ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户信息表';


-- ----------------------------------------------------------------------------------
-- **如果您已存在旧的 users 表**，请使用下面的 ALTER TABLE 脚本进行修改
-- ----------------------------------------------------------------------------------
/*
-- 1. 删除旧的扁平化部门字段
ALTER TABLE `users`
    DROP COLUMN `section`,
    DROP COLUMN `workshop`,
    DROP COLUMN `team`,
    DROP COLUMN `guidance_group`;

-- 2. 添加新的 department_id 字段
ALTER TABLE `users`
    ADD COLUMN `department_id` BIGINT NULL COMMENT '所属部门ID，外键关联departments表' AFTER `pinyin_code`;

-- 3. 添加外键约束
ALTER TABLE `users`
    ADD CONSTRAINT `fk_users_department`
        FOREIGN KEY (`department_id`)
        REFERENCES `departments` (`id`)
        ON DELETE SET NULL
        ON UPDATE CASCADE;
*/
-- --------------------------------------------------------------------------------
-- 铁路管理系统 - 权限、角色、职位及关联表示例脚本
-- --------------------------------------------------------------------------------

-- ----------------------------
-- 1. 创建 `permissions` 表 (全局权限表)
-- ----------------------------
-- 此表存储系统中所有可用的原子权限，不与任何租户（部门）关联。
DROP TABLE IF EXISTS `permissions`;
CREATE TABLE `permissions` (
                               `id` bigint NOT NULL AUTO_INCREMENT,
                               `name` varchar(100) NOT NULL COMMENT '权限名称 (e.g., "创建用户")',
                               `code` varchar(100) NOT NULL COMMENT '权限代码, 用于程序判断 (e.g., "user:create")',
                               `description` varchar(255) DEFAULT NULL COMMENT '权限描述',
                               PRIMARY KEY (`id`),
                               UNIQUE KEY `uk_permissions_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='全局权限定义表';

-- ----------------------------
-- 2. 创建 `positions` 表 (租户级职位表)
-- ----------------------------
-- 职位与部门（租户）关联，不同部门可以有相同名称但ID不同的职位。
DROP TABLE IF EXISTS `positions`;
CREATE TABLE `positions` (
                             `id` bigint NOT NULL AUTO_INCREMENT,
                             `name` varchar(100) NOT NULL COMMENT '职位名称',
                             `description` varchar(255) DEFAULT NULL COMMENT '职位描述',
                             `department_id` bigint DEFAULT NULL COMMENT '所属部门ID (租户ID)',
                             PRIMARY KEY (`id`),
                             KEY `fk_positions_department` (`department_id`),
                             CONSTRAINT `fk_positions_department` FOREIGN KEY (`department_id`) REFERENCES `departments` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='职位信息表（租户隔离）';

-- ----------------------------
-- 3. 创建 `roles` 表 (租户级角色表)
-- ----------------------------
-- 角色与部门（租户）关联，是权限的集合。
DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles` (
                         `id` bigint NOT NULL AUTO_INCREMENT,
                         `name` varchar(100) NOT NULL COMMENT '角色名称 (e.g., "部门管理员")',
                         `code` varchar(100) NOT NULL COMMENT '角色代码 (e.g., "ROLE_ADMIN_DEPT1")',
                         `department_id` bigint DEFAULT NULL COMMENT '所属部门ID (租户ID)',
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `uk_roles_code` (`code`),
                         KEY `fk_roles_department` (`department_id`),
                         CONSTRAINT `fk_roles_department` FOREIGN KEY (`department_id`) REFERENCES `departments` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色信息表（租户隔离）';

-- ----------------------------
-- 4. 创建关联表
-- ----------------------------
-- 角色-权限关联表 (多对多)
DROP TABLE IF EXISTS `roles_permissions`;
CREATE TABLE `roles_permissions` (
                                     `role_id` bigint NOT NULL,
                                     `permission_id` bigint NOT NULL,
                                     PRIMARY KEY (`role_id`,`permission_id`),
                                     KEY `fk_rp_permission` (`permission_id`),
                                     CONSTRAINT `fk_rp_permission` FOREIGN KEY (`permission_id`) REFERENCES `permissions` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
                                     CONSTRAINT `fk_rp_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色-权限关联表';

-- 用户-角色关联表 (多对多)
DROP TABLE IF EXISTS `users_roles`;
CREATE TABLE `users_roles` (
                               `user_id` bigint NOT NULL,
                               `role_id` bigint NOT NULL,
                               PRIMARY KEY (`user_id`,`role_id`),
                               KEY `fk_ur_role` (`role_id`),
                               CONSTRAINT `fk_ur_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
                               CONSTRAINT `fk_ur_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户-角色关联表';


-- --------------------------------------------------------------------------------
-- 插入示例数据
-- 假设 `departments` 表已存在 ID=1 (上海机务段) 和 ID=2 (南京机务段) 的记录
-- 假设 `users` 表已存在 ID=101 (张三, 属上海) 和 ID=201 (李四, 属南京) 的记录
-- --------------------------------------------------------------------------------

-- 1. 插入全局权限
INSERT INTO `permissions` (`id`, `name`, `code`, `description`) VALUES
                                                                    (1, '创建用户', 'user:create', '允许在当前部门下创建新用户'),
                                                                    (2, '读取用户', 'user:read', '允许查看当前部门的用户列表'),
                                                                    (3, '更新用户', 'user:update', '允许修改当前部门的用户信息'),
                                                                    (4, '删除用户', 'user:delete', '允许删除当前部门的用户'),
                                                                    (5, '分配角色', 'role:assign', '允许为当前部门的用户分配角色'),
                                                                    (6, '查看报表', 'report:view', '允许查看部门的运营报表'),
                                                                    (7, '编辑排班', 'schedule:edit', '允许编辑和发布排班信息');

-- 2. 插入职位 (按部门/租户隔离)
-- 上海机务段 (department_id = 1)
INSERT INTO `positions` (`id`, `name`, `description`, `department_id`) VALUES
                                                                           (1, '部门主管', '负责部门日常管理', 1),
                                                                           (2, '列车司机', '负责列车驾驶任务', 1);
-- 南京机务段 (department_id = 2)
INSERT INTO `positions` (`id`, `name`, `description`, `department_id`) VALUES
                                                                           (3, '电力工程师', '负责接触网维护', 2),
                                                                           (4, '安全监督员', '负责监督安全生产', 2);

-- 3. 插入角色 (按部门/租户隔离)
-- 上海机务段 (department_id = 1)
INSERT INTO `roles` (`id`, `name`, `code`, `department_id`) VALUES
                                                                (1, '部门管理员(沪)', 'ROLE_DEPT_ADMIN_SH', 1),
                                                                (2, '普通职员(沪)', 'ROLE_STAFF_SH', 1);
-- 南京机务段 (department_id = 2)
INSERT INTO `roles` (`id`, `name`, `code`, `department_id`) VALUES
                                                                (3, '部门主管(宁)', 'ROLE_DEPT_LEADER_NJ', 2),
                                                                (4, '技术员(宁)', 'ROLE_TECHNICIAN_NJ', 2);

-- 4. 为角色分配权限
-- "部门管理员(沪)" (role_id = 1) 拥有用户管理和角色分配权限
INSERT INTO `roles_permissions` (`role_id`, `permission_id`) VALUES
                                                                 (1, 1), -- user:create
                                                                 (1, 2), -- user:read
                                                                 (1, 3), -- user:update
                                                                 (1, 4), -- user:delete
                                                                 (1, 5); -- role:assign
-- "普通职员(沪)" (role_id = 2) 只能查看报表
INSERT INTO `roles_permissions` (`role_id`, `permission_id`) VALUES
    (2, 6); -- report:view
-- "部门主管(宁)" (role_id = 3) 能看报表和编辑排班
INSERT INTO `roles_permissions` (`role_id`, `permission_id`) VALUES
                                                                 (3, 6), -- report:view
                                                                 (3, 7); -- schedule:edit

-- 5. 为用户分配角色 (假设用户已存在)
-- 用户 张三 (user_id = 101) 是 "部门管理员(沪)"
INSERT INTO `users_roles` (`user_id`, `role_id`) VALUES
    (101, 1);
-- 用户 李四 (user_id = 201) 是 "技术员(宁)" (假设角色已创建)
-- INSERT INTO `users_roles` (`user_id`, `role_id`) VALUES (201, 4);

-- ----------------------------
-- Table structure for equipment_access_control门禁对应sql
-- ----------------------------
DROP TABLE IF EXISTS `equipment_access_control`;
CREATE TABLE `equipment_access_control` (
                                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                            `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '门禁设备名称',
                                            `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '安装位置',
                                            `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'IP地址',
                                            `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'ONLINE' COMMENT '设备状态 (ONLINE, OFFLINE, MAINTENANCE)',
                                            `department_id` bigint DEFAULT NULL COMMENT '所属部门ID',
                                            `department_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '部门层级路径',
                                            `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                            `created_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
                                            `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                            `updated_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
                                            PRIMARY KEY (`id`),
                                            KEY `idx_department_path` (`department_path`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='门禁设备表';

-- ----------------------------
-- Table structure for equipment_access_control_log
-- ----------------------------
DROP TABLE IF EXISTS `equipment_access_control_log`;
CREATE TABLE `equipment_access_control_log` (
                                                `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                                `access_control_id` bigint NOT NULL COMMENT '门禁设备ID',
                                                `user_id` bigint NOT NULL COMMENT '操作用户ID',
                                                `action` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作动作 (OPEN, CLOSE)',
                                                `timestamp` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
                                                `success` tinyint(1) DEFAULT '1' COMMENT '操作是否成功 (1:成功, 0:失败)',
                                                `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注信息',
                                                PRIMARY KEY (`id`),
                                                KEY `idx_access_control_id` (`access_control_id`),
                                                KEY `idx_timestamp` (`timestamp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='门禁操作记录表';




