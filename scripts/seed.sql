-- Seed states and districts for local development
-- Replace sample values with authoritative codes before running in production

INSERT INTO states (name, code)
VALUES ('ANDHRA PRADESH', 'AP')
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO districts (name, code, state_id)
SELECT 'ANANTAPUR', 'ANANT', s.id
FROM states s
WHERE s.code = 'AP'
ON DUPLICATE KEY UPDATE name = VALUES(name);
