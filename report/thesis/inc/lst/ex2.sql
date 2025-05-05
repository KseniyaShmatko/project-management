CREATE TABLE user_projects (
    user_id INTEGER REFERENCES users(user_id),
    project_id INTEGER REFERENCES projects(project_id),
    role VARCHAR(50),
    permission VARCHAR(50),
    PRIMARY KEY (user_id, project_id)
);