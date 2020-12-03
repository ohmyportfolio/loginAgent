Insert into OC_SITE
   (ID, NAME, LOGIN_URL, ID_XPATH, PW_XPATH, 
    LOGIN_XPATH)
 Values
   ('netflix', '넷플릭스', 'https://www.netflix.com/kr/login', '//*[@id=''id_userLoginId'']', '//*[@id=''id_password'']', 
    '//*[@class=''btn login-button btn-submit btn-small'']');
Insert into OC_SITE
   (ID, NAME, LOGIN_URL, ID_XPATH, PW_XPATH, 
    LOGIN_XPATH)
 Values
   ('uflix', '유플릭스', 'https://uflix.co.kr/uws/login', '//*[@id=''j_username'']', '//*[@id=''j_password'']', 
    '//*[@id=''btnLogin'']');
Insert into OC_SITE
   (ID, NAME, LOGIN_URL, ID_XPATH, PW_XPATH, 
    LOGIN_XPATH)
 Values
   ('wavve', '웨이브', 'https://www.wavve.com/member/login', '//*[@title=''아이디'']', '//*[@title=''비밀번호'']', 
    '//*[@class=''btn-purple btn-purple-dark'']');
Insert into OC_SITE
   (ID, NAME, LOGIN_URL, ID_XPATH, PW_XPATH, 
    LOGIN_XPATH)
 Values
   ('tving', 'TVing', 'https://user.tving.com/pc/user/login.tving', '//*[@id=''a'']', '//*[@id=''b'']', 
    '//*[@id=''doLoginBtn'']');
COMMIT;
