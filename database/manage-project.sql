DROP TABLE IF EXISTS `project_members`;
DROP TABLE IF EXISTS `project_sequences`;
DROP TABLE IF EXISTS `sprints`;
DROP TABLE IF EXISTS `issues`;
DROP TABLE IF EXISTS `issue_histories`;
DROP TABLE IF EXISTS `user`;
DROP TABLE IF EXISTS `role`;
DROP TABLE IF EXISTS `projects`;


CREATE TABLE IF NOT EXISTS `role` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `full_name` VARCHAR(255) NULL,
    `username` VARCHAR(100) NULL,
    `password` VARCHAR(255) NOT NULL,
    `email` VARCHAR(150) NOT NULL UNIQUE,
    `phone` VARCHAR(20) NULL,
    `avatar` VARCHAR(500) NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `status` VARCHAR(50) NULL DEFAULT 'ACTIVE',
    `gender` VARCHAR(20) NULL,
    `role_id` INT NULL,
    
    CONSTRAINT `fk_user_role` 
        FOREIGN KEY (`role_id`) 
        REFERENCES `role` (`id`) 
        ON DELETE SET NULL 
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    start_date DATE,
    deadline DATE,
    status VARCHAR(20) NOT NULL,
	is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS project_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role_in_project VARCHAR(20) NOT NULL,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
    
    CONSTRAINT fk_pm_project 
        FOREIGN KEY (project_id) REFERENCES projects(id) 
        ON DELETE CASCADE,
        
    CONSTRAINT fk_pm_user 
        FOREIGN KEY (user_id) REFERENCES user(id) 
        ON DELETE CASCADE,
        
    -- Ràng buộc Unique: Một user chỉ được xuất hiện 1 lần trong 1 dự án
    CONSTRAINT uk_project_user 
        UNIQUE (project_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS issues (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,          
    sprint_id BIGINT NULL,               
    issue_key VARCHAR(50) NOT NULL,     
    
    issue_type VARCHAR(20) NOT NULL,     
    title VARCHAR(255) NOT NULL,         
    description TEXT,                    
    
    status VARCHAR(50) DEFAULT 'TO_DO',  
    priority VARCHAR(20) DEFAULT 'MEDIUM',
    story_point INT NULL,                
    
    reporter_id BIGINT NOT NULL,         
    assignee_id BIGINT NULL,             
    due_date DATETIME NULL,              
    
    is_deleted BOOLEAN DEFAULT FALSE,    
    version INT DEFAULT 0,               
    
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_issue_key (issue_key), 
    
    CONSTRAINT `fk_issue_project` 
		FOREIGN KEY (`project_id`) REFERENCES `projects`(`id`) ON DELETE CASCADE,
	CONSTRAINT `fk_issue_reporter` 
		FOREIGN KEY (`reporter_id`) REFERENCES `user`(`id`) ON DELETE RESTRICT,
	CONSTRAINT `fk_issue_assignee` 
		FOREIGN KEY (`assignee_id`) REFERENCES `user`(`id`) ON DELETE SET NULL,
	CONSTRAINT `fk_issue_sprint`
		FOREIGN KEY (`sprint_id`) REFERENCES `sprints`(`id`) ON DELETE SET NULL,
      
    INDEX idx_project_sprint (project_id, sprint_id), 
    INDEX idx_assignee (assignee_id)    
);

CREATE TABLE IF NOT EXISTS project_sequences (
    project_id BIGINT PRIMARY KEY,       -- ID của Project
    current_value INT NOT NULL DEFAULT 0, -- Số đếm hiện tại
    CONSTRAINT `fk_seq_project` 
		FOREIGN KEY (`project_id`) REFERENCES `projects`(`id`) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS issue_histories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    issue_id BIGINT NOT NULL,            
    actor_id BIGINT NOT NULL,           
    
    field_name VARCHAR(50) NOT NULL,     
    old_value TEXT,                     
    new_value TEXT,                     
    
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT `fk_history_issue` 
		FOREIGN KEY (`issue_id`) REFERENCES `issues`(`id`) ON DELETE CASCADE,
	CONSTRAINT `fk_history_actor` 
		FOREIGN KEY (`actor_id`) REFERENCES `user`(`id`) ON DELETE RESTRICT,
    
    INDEX idx_issue_id (issue_id)        
);

CREATE TABLE IF NOT EXISTS `sprints` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `project_id` BIGINT NOT NULL,
    `name` VARCHAR(255) NOT NULL,
    `goal` TEXT,
    
    `start_date` DATETIME NULL,
    `end_date` DATETIME NULL,
    `status` VARCHAR(50) DEFAULT 'PENDING' NOT NULL, -- Enum: PENDING, ACTIVE, COMPLETED
    
    `is_deleted` BOOLEAN DEFAULT FALSE NOT NULL,
    
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT `fk_sprint_project` 
        FOREIGN KEY (`project_id`) REFERENCES `projects`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;