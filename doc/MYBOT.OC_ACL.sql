Insert into OC_ACL
   (ID, NAME, ADMIN_PERMIT, OWNER_PERMIT, GROUP_PERMIT, 
    OTHER_PERMIT, GUEST_PERMIT, USE_YN, MOD_DATE, MOD_USER_ID, 
    OWNER_GROUP_PERMIT)
 Values
   ('default', '기본', '4', '4', '4', 
    '1', '1', 'Y', TO_DATE('11/29/2019 10:38:42', 'MM/DD/YYYY HH24:MI:SS'), 'admin', 
    '1');
COMMIT;
