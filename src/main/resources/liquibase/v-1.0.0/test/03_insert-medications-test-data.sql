INSERT INTO MEDICATIONS(CODE, NAME, WEIGHT_GR, IMAGE)
VALUES ('med_code_1', 'Medication_1', 10, FILE_READ('classpath:liquibase/v-1.0.0/test/images/Medication.jpg'));

INSERT INTO MEDICATIONS(CODE, NAME, WEIGHT_GR, IMAGE)
VALUES ('med_code_2', 'Medication_2', 50, FILE_READ('classpath:liquibase/v-1.0.0/test/images/Medication.jpg'));

INSERT INTO MEDICATIONS(CODE, NAME, WEIGHT_GR, IMAGE)
VALUES ('med_code_3', 'Medication_3', 75, FILE_READ('classpath:liquibase/v-1.0.0/test/images/Medication.jpg'));

INSERT INTO MEDICATIONS(CODE, NAME, WEIGHT_GR, IMAGE)
VALUES ('med_code_4', 'Medication_4', 45, FILE_READ('classpath:liquibase/v-1.0.0/test/images/Medication.jpg'));

COMMIT;