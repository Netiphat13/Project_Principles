ALTER TABLE groups
ADD COLUMN invite_code VARCHAR(20);

CREATE UNIQUE INDEX uk_groups_invite_code
ON groups (invite_code);