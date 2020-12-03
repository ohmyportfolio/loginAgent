Insert into OC_USER
   (ID, USER_ID, USER_NAME, ENCRYPTED_PASSWORD, SESSION_ID, 
    GROUP_ID, USER_TYPE, USER_STATUS, EMAIL, MOD_DATE, 
    MOD_USER_ID, NAME, POSITION, LOGIN_FAILURE_COUNT, PASSWORD_EXPIRATION_DATE, 
    LAST_LOGIN_DATE)
 Values
   ('admin', 'kingsman', '관리자', '$2a$10$x4DrMz3svExCyhd8mFpCDucoSiLiXa.A7pH0pmDSmnQerj2YNkgI.', 'JXntW4Kn4jbr4Rdo', 
    'admins', 'admin', 'normal', 'changhyun.nam@partner.samsung.com', TO_DATE('07/12/2020 06:20:07', 'MM/DD/YYYY HH24:MI:SS'), 
    'admin', '관리자/관리자', '관리자', 0, TO_DATE('10/12/2020 04:54:53', 'MM/DD/YYYY HH24:MI:SS'), 
    TO_DATE('07/12/2020 06:20:07', 'MM/DD/YYYY HH24:MI:SS'));
COMMIT;
