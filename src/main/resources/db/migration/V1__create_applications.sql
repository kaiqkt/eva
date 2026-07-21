CREATE TABLE applications (
    id          VARCHAR(26)  NOT NULL,
    name        VARCHAR(50)  NOT NULL,
    slug        VARCHAR(50)  NOT NULL,
    description VARCHAR(255),
    CONSTRAINT pk_applications PRIMARY KEY (id),
    CONSTRAINT uk_applications_slug UNIQUE (slug)
);
