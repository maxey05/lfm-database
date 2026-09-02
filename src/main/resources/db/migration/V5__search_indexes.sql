CREATE INDEX idx_person_first_name_trgm ON person USING gin (lower(first_name) gin_trgm_ops);
CREATE INDEX idx_person_middle_name_trgm ON person USING gin (lower(middle_name) gin_trgm_ops);
CREATE INDEX idx_person_last_name_trgm ON person USING gin (lower(last_name) gin_trgm_ops);
CREATE INDEX idx_person_nickname_trgm ON person USING gin (lower(nickname) gin_trgm_ops);
CREATE INDEX idx_person_email_trgm ON person USING gin (lower(email) gin_trgm_ops);
CREATE INDEX idx_person_contact_trgm ON person USING gin (lower(contact_number) gin_trgm_ops);
CREATE INDEX idx_person_facebook_trgm ON person USING gin (lower(facebook_name) gin_trgm_ops);

CREATE INDEX idx_person_archived ON person (archived);
CREATE INDEX idx_person_in_dgroup ON person (in_dgroup);
CREATE INDEX idx_person_gender ON person (gender);
CREATE INDEX idx_person_civil_status ON person (civil_status);
