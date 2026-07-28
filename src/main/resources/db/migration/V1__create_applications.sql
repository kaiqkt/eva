CREATE TABLE repositories (
    id          VARCHAR(26)  NOT NULL,
    url         VARCHAR(255) NOT NULL,
    CONSTRAINT pk_repositories PRIMARY KEY (id)
);

CREATE TABLE projects (
    id          VARCHAR(26)  NOT NULL,
    name        VARCHAR(50)  NOT NULL,
    slug        VARCHAR(50)  NOT NULL,
    description VARCHAR(255),
    CONSTRAINT pk_projects PRIMARY KEY (id),
    CONSTRAINT uk_projects_slug UNIQUE (slug)
);

CREATE TABLE applications (
    id            VARCHAR(26)  NOT NULL,
    name          VARCHAR(50)  NOT NULL,
    slug          VARCHAR(50)  NOT NULL,
    description   VARCHAR(255),
    repository_id VARCHAR(26)  NOT NULL,
    project_id    VARCHAR(26)  NOT NULL,
    CONSTRAINT pk_applications PRIMARY KEY (id),
    CONSTRAINT uk_applications_slug UNIQUE (slug),
    CONSTRAINT uk_applications_repository_id UNIQUE (repository_id),
    CONSTRAINT fk_applications_repository FOREIGN KEY (repository_id) REFERENCES repositories (id),
    CONSTRAINT fk_applications_project FOREIGN KEY (project_id) REFERENCES projects (id)
);
