Insert into OC_MENU
   (ID, NAME, USE_YN)
 Values
   ('root', 'Root', 'y');
Insert into OC_MENU
   (ID, NAME, PARENT_ID, ICON, SEQ, 
    USE_YN, MOD_DATE, MOD_USER_ID)
 Values
   ('system', '시스템 관리', 'root', 'preferences', 7, 
    'y', TO_DATE('12/20/2019 10:15:46', 'MM/DD/YYYY HH24:MI:SS'), 'admin');
Insert into OC_MENU
   (ID, NAME, PARENT_ID, ICON, PATH, 
    SEQ, USE_YN, MOD_DATE, MOD_USER_ID)
 Values
   ('home', '홈', 'root', 'home', '/', 
    1, 'y', TO_DATE('12/20/2019 09:32:02', 'MM/DD/YYYY HH24:MI:SS'), 'admin');
Insert into OC_MENU
   (ID, NAME, PARENT_ID, PATH, SEQ, 
    USE_YN, MOD_DATE, MOD_USER_ID)
 Values
   ('events', '사용 이력', 'system', '/events', 1, 
    'y', TO_DATE('12/19/2019 18:00:54', 'MM/DD/YYYY HH24:MI:SS'), 'admin');
Insert into OC_MENU
   (ID, NAME, PARENT_ID, PATH, SEQ, 
    USE_YN, MOD_DATE, MOD_USER_ID)
 Values
   ('menus', '메뉴 관리', 'system', '/menus', 4, 
    'y', TO_DATE('12/19/2019 18:00:54', 'MM/DD/YYYY HH24:MI:SS'), 'admin');
Insert into OC_MENU
   (ID, NAME, PARENT_ID, PATH, SEQ, 
    USE_YN)
 Values
   ('types', '코드 관리', 'system', '/types', 6, 
    'y');
Insert into OC_MENU
   (ID, NAME, PARENT_ID, PATH, SEQ, 
    USE_YN, MOD_DATE, MOD_USER_ID)
 Values
   ('users', '사용자 관리', 'system', '/users', 2, 
    'y', TO_DATE('12/19/2019 18:00:54', 'MM/DD/YYYY HH24:MI:SS'), 'admin');
Insert into OC_MENU
   (ID, NAME, PARENT_ID, PATH, SEQ, 
    USE_YN)
 Values
   ('groups', '그룹 관리', 'system', '/groups', 3, 
    'y');
Insert into OC_MENU
   (ID, NAME, PARENT_ID, PATH, SEQ, 
    USE_YN)
 Values
   ('acls', '권한 관리', 'system', '/acls', 5, 
    'y');
Insert into OC_MENU
   (ID, NAME, PARENT_ID, ICON, PATH, 
    SEQ, USE_YN, MOD_USER_ID)
 Values
   ('accountManagement', '계정 관리', 'root', 'edit', '/accounts', 
    2, 'y', 'admin');
Insert into OC_MENU
   (ID, NAME, PARENT_ID, ICON, PATH, 
    SEQ, USE_YN, MOD_USER_ID)
 Values
   ('siteManagement', '사이트 관리', 'root', 'edit', '/sites', 
    3, 'y', 'admin');
COMMIT;
