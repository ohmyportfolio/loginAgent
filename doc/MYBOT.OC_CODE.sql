Insert into OC_CODE
   (TYPE_ID, CODE, NAME, USE_YN)
 Values
   ('resource', 'events', '사용 이력', 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, USE_YN)
 Values
   ('command', 'clearCache', '캐시 삭제', 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, USE_YN)
 Values
   ('resource', 'codes', '코드', 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, USE_YN, REG_DATE, 
    REG_USER_ID)
 Values
   ('user_status', 'leave', '휴직', 'y', TO_DATE('11/13/2019 16:19:12', 'MM/DD/YYYY HH24:MI:SS'), 
    'admin');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, SEQ, USE_YN)
 Values
   ('yna', 'na', 'N/A', 3, 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, SEQ, USE_YN)
 Values
   ('yna', 'y', 'O', 1, 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, SEQ, USE_YN)
 Values
   ('yna', 'n', 'X', 2, 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, USE_YN)
 Values
   ('user_type', 'admin', '관리자', 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, USE_YN)
 Values
   ('resource', 'users', '사용자', 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, USE_YN)
 Values
   ('resource', 'groups', '사용자', 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, USE_YN)
 Values
   ('resource', 'menus', '메뉴', 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, USE_YN)
 Values
   ('resource', 'assets', '점검 대상', 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, USE_YN)
 Values
   ('resource', 'assetTypes', '대상 유형', 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, USE_YN)
 Values
   ('resource', 'places', '장소', 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, USE_YN)
 Values
   ('resource', 'checks', '점검 실시', 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, USE_YN)
 Values
   ('resource', 'checkItems', '점검 항목', 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, USE_YN)
 Values
   ('resource', 'checklists', '체크리스트', 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, USE_YN)
 Values
   ('resource', 'checkCycles', '점검 계획', 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, USE_YN)
 Values
   ('resource', 'checkThemes', '점검 테마', 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, USE_YN)
 Values
   ('command', 'insert', '등록', 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, USE_YN)
 Values
   ('command', 'update', '수정', 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, USE_YN)
 Values
   ('command', 'delete', '삭제', 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, USE_YN)
 Values
   ('command', 'select', '목록', 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, USE_YN)
 Values
   ('command', 'get', '조회', 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, USE_YN)
 Values
   ('command', 'login', '로그인', 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, USE_YN)
 Values
   ('command', 'logout', '로그아웃', 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, USE_YN)
 Values
   ('command', 'ssoLogin', '로그인(SSO)', 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, USE_YN)
 Values
   ('command', 'loginForce', '로그인(강제)', 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, USE_YN)
 Values
   ('group_type', 'dept', '부서', 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, USE_YN)
 Values
   ('group_type', 'project', '프로젝트', 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, USE_YN)
 Values
   ('permit', '0', '상속', 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, USE_YN)
 Values
   ('permit', '1', '없음', 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, USE_YN)
 Values
   ('permit', '2', '읽기', 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, USE_YN)
 Values
   ('permit', '3', '쓰기', 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, USE_YN)
 Values
   ('permit', '4', '삭제', 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, USE_YN)
 Values
   ('user_type', 'employee', '임직원', 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, USE_YN)
 Values
   ('user_type', 'outsourcing', '외주', 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, USE_YN)
 Values
   ('user_status', 'normal', '재직', 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, USE_YN)
 Values
   ('user_status', 'retirement', '퇴직', 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, USE_YN, MOD_DATE, 
    MOD_USER_ID)
 Values
   ('yn', 'n', 'X', 'y', TO_DATE('12/12/2019 16:52:04', 'MM/DD/YYYY HH24:MI:SS'), 
    'admin');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, USE_YN, MOD_DATE, 
    MOD_USER_ID)
 Values
   ('yn', 'y', 'O', 'y', TO_DATE('12/12/2019 16:52:04', 'MM/DD/YYYY HH24:MI:SS'), 
    'admin');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, SEQ, USE_YN)
 Values
   ('week_of_month', '1', '1주차', 1, 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, SEQ, USE_YN)
 Values
   ('week_of_month', '2', '2주차', 2, 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, SEQ, USE_YN)
 Values
   ('week_of_month', '3', '3주차', 3, 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, SEQ, USE_YN)
 Values
   ('week_of_month', '4', '4주차', 4, 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, SEQ, USE_YN)
 Values
   ('day_of_week', 'mon', '월', 1, 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, SEQ, USE_YN)
 Values
   ('day_of_week', 'tue', '화', 2, 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, SEQ, USE_YN)
 Values
   ('day_of_week', 'wed', '수', 3, 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, SEQ, USE_YN)
 Values
   ('day_of_week', 'thu', '목', 4, 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, SEQ, USE_YN)
 Values
   ('day_of_week', 'fri', '금', 5, 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, SEQ, USE_YN)
 Values
   ('type_type', 'code', '코드', 1, 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, USE_YN)
 Values
   ('group_status', 'normal', '정상', 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, USE_YN)
 Values
   ('group_status', 'abolished', '폐지', 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, SEQ, USE_YN)
 Values
   ('site_type', 'uflix', '유플릭스', 2, 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, SEQ, USE_YN)
 Values
   ('site_type', 'wavve', '웨이브', 3, 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, SEQ, USE_YN)
 Values
   ('type_type', 'list', '리스트', 2, 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, SEQ, USE_YN)
 Values
   ('type_type', 'resource', '리소스', 3, 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, USE_YN)
 Values
   ('resource', 'types', '코드', 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, USE_YN)
 Values
   ('resource', 'columns', '컬럼', 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, USE_YN)
 Values
   ('resource', 'attachs', '첨부 파일', 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, SEQ, USE_YN)
 Values
   ('prop_type', 'string', '문자열', 1, 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, SEQ, USE_YN)
 Values
   ('prop_type', 'integer', '정수', 10, 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, SEQ, USE_YN)
 Values
   ('prop_type', 'float', '실수', 11, 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, SEQ, USE_YN)
 Values
   ('prop_type', 'date', '일자', 14, 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, SEQ, USE_YN)
 Values
   ('prop_type', 'datetime', '일시', 15, 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, SEQ, USE_YN)
 Values
   ('prop_type', 'code', '코드(콤보)', 4, 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, SEQ, USE_YN)
 Values
   ('prop_type', 'codeRadio', '코드(라디오)', 5, 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, SEQ, USE_YN)
 Values
   ('prop_type', 'resource', '리소스(콤보)', 6, 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, SEQ, USE_YN)
 Values
   ('prop_type', 'list', '목록(콤보)', 8, 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, SEQ, USE_YN)
 Values
   ('prop_type', 'listRadio', '목록(라디오)', 9, 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, SEQ, USE_YN)
 Values
   ('prop_type', 'textArea', '텍스트', 2, 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, SEQ, USE_YN)
 Values
   ('prop_type', 'checkBox', '체크박스', 3, 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, SEQ, USE_YN)
 Values
   ('prop_type', 'resourceTag', '리소스(태그)', 7, 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, SEQ, USE_YN)
 Values
   ('prop_type', 'fileSize', '파일크기', 12, 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, SEQ, USE_YN)
 Values
   ('prop_type', 'percent', '퍼센트', 13, 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, SEQ, USE_YN)
 Values
   ('prop_type', 'dateMonth', '년월', 16, 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, SEQ, USE_YN)
 Values
   ('prop_type', 'time', '시간', 17, 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, SEQ, USE_YN)
 Values
   ('site_type', 'netflix', '넷플릭스', 1, 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, SEQ, USE_YN)
 Values
   ('site_type', 'tving', 'Tving', 4, 'y');
Insert into OC_CODE
   (TYPE_ID, CODE, NAME, SEQ, USE_YN)
 Values
   ('site_type', 'comics', '만화', 5, 'y');
COMMIT;
