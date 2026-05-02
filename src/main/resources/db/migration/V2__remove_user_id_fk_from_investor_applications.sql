/*
 Remove user_id as a foreign key from investor applications
 as the user is not created yet
 */

ALTER TABLE investor_applications
DROP CONSTRAINT fk_investor_applications_users CASCADE;

ALTER TABLE investor_applications
DROP COLUMN user_id;