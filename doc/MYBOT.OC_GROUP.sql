Insert into OC_GROUP
   (ID, NAME, GROUP_TYPE, GROUP_STATUS, REG_DATE, 
    REG_USER_ID)
 Values
   ('admins', '시스템관리', 'dept', 'normal', TO_DATE('12/20/2019 10:08:34', 'MM/DD/YYYY HH24:MI:SS'), 
    'admin');
COMMIT;
