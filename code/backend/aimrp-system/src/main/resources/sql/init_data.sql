-- 用户权限初始化数据
-- 默认管理员账号: admin / admin123

-- 插入默认用户 (密码: admin123, BCrypt 加密)
INSERT INTO m_user (username, password, real_name, email, status, created_by, created_at, updated_by, updated_at) 
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 'admin@aimrp.com', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP)
ON CONFLICT (username) DO NOTHING;

-- 插入默认角色
INSERT INTO m_role (role_code, role_name, description, status, created_by, created_at, updated_by, updated_at)
VALUES 
('ADMIN', '系统管理员', '拥有所有权限', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
('USER', '普通用户', '基本操作权限', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP)
ON CONFLICT (role_code) DO NOTHING;

-- 绑定用户角色
INSERT INTO m_user_role (user_id, role_id)
SELECT u.id, r.id FROM m_user u, m_role r WHERE u.username = 'admin' AND r.role_code = 'ADMIN'
ON CONFLICT DO NOTHING;

-- 插入默认权限
INSERT INTO m_permission (permission_code, permission_name, resource_type, resource_path, parent_id, created_at)
VALUES 
('system:user', '用户管理', 'MENU', '/system/user', NULL, CURRENT_TIMESTAMP),
('system:user:list', '用户列表', 'BUTTON', '/api/system/users', 1, CURRENT_TIMESTAMP),
('system:user:create', '创建用户', 'BUTTON', '/api/system/users', 1, CURRENT_TIMESTAMP),
('system:role', '角色管理', 'MENU', '/system/role', NULL, CURRENT_TIMESTAMP),
('demand:order', '需求订单', 'MENU', '/demand/order', NULL, CURRENT_TIMESTAMP),
('bom:manage', 'BOM管理', 'MENU', '/bom/manage', NULL, CURRENT_TIMESTAMP),
('inventory:manage', '库存管理', 'MENU', '/inventory/manage', NULL, CURRENT_TIMESTAMP),
('mrp:calculate', 'MRP计算', 'MENU', '/mrp/calculate', NULL, CURRENT_TIMESTAMP)
ON CONFLICT (permission_code) DO NOTHING;

-- 绑定角色权限
INSERT INTO m_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM m_role r, m_permission p WHERE r.role_code = 'ADMIN'
ON CONFLICT DO NOTHING;
