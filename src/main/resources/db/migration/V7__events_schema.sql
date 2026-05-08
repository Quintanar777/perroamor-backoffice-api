CREATE TABLE events (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    location    VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    start_date  DATE         NOT NULL,
    end_date    DATE         NOT NULL,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_events_dates CHECK (start_date <= end_date)
);

CREATE INDEX idx_events_dates     ON events (start_date, end_date);
CREATE INDEX idx_events_is_active ON events (is_active);
