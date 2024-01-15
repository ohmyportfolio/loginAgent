import argparse
import undetected_chromedriver as uc
from Crypto.Cipher import AES
from Crypto.Util.Padding import unpad
from base64 import b64decode
import hashlib

from selenium.common.exceptions import WebDriverException
from selenium.webdriver.remote.webdriver import By
import selenium.webdriver.support.expected_conditions as EC  # noqa
from selenium.webdriver.support.wait import WebDriverWait
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.chrome.options import Options
import subprocess

def kill_existing_processes(process_name):
    try:
        subprocess.run(f"taskkill /f /im {process_name}", check=True, shell=True)
    except subprocess.CalledProcessError as e:
        print(f"Error killing existing processes: {e}")

class My_Chrome(uc.Chrome):

    def __init__(self, *args, **kwargs):
        chrome_options = Options()
        # Chrome 실행 파일의 위치 지정
        chrome_options.binary_location = "./GoogleChromePortable/App/Chrome-bin/chrome.exe"
        # 사용자 프로필 디렉토리 지정
        chrome_options.add_argument("--user-data-dir=./GoogleChromePortable/App/DefaultData/profile")
        chrome_options.add_argument("--incognito")
        
        # 추가적인 Chrome 옵션들이 필요하면 여기에 추가
        # 예: headless 모드, 창 크기 등
        # chrome_options.add_argument("--headless")
        # chrome_options.add_argument("--window-size=1920,1080")

        super().__init__(*args, options=chrome_options, **kwargs)
    def __del__(self):
        pass

def decrypt(encoded_text, key):
    decodedString = b64decode(encoded_text)  # decode from Base64
    cipher_text = decodedString.decode("utf-8")
    
    key = hashlib.sha256(key.encode()).digest()[:16]  # Hash the key
    cipher = AES.new(key, AES.MODE_CBC, key)  # Create a new cipher using key and IV
    plain_bytes = unpad(cipher.decrypt(cipher_text[AES.block_size:]), AES.block_size)  # Decrypt and unpad the result
    return plain_bytes.decode()  # Convert bytes to string

def main(url, xpath=None, xpath2=None, dkdlel=None, alqjs=None , loginPath=None):
    
    kill_existing_processes("chromedriver.exe")
    kill_existing_processes("chrome.exe")
    
    driver = My_Chrome()
    driver.creation_flags = 0x08000000
   

    offset = '!QAZxsw2';

    # If these are encrypted, you can use your decrypt function to decrypt them before use
    # xpath_decrypted = decrypt(xpath, offset) if xpath else None
    # xpath2_decrypted = decrypt(xpath2, offset) if xpath2 else None
    # dkdlel_decrypted = decrypt(dkdlel,offset) if dkdlel else None
    # alqjs_decrypted = decrypt(alqjs, offset) if alqjs else None
    
    xpath_decrypted = xpath 
    xpath2_decrypted = xpath2
    dkdlel_decrypted = dkdlel
    alqjs_decrypted = alqjs
    
    original_password_bytes = b64decode(alqjs)
    alqjs_decrypted = original_password_bytes.decode("utf-8")  # Assuming the password is UTF-8 encoded
    print(alqjs_decrypted)


    # Here you would use the decrypted arguments to set up your browser
    # For example:


    driver.get(url)
    
    WebDriverWait(driver, 10).until(EC.presence_of_element_located((By.XPATH, xpath_decrypted)))
            
    input_element = driver.find_element(By.XPATH,xpath_decrypted)
    input_element.send_keys(dkdlel_decrypted)
    input_element.send_keys(Keys.ENTER)
    

     # wait for the password field to be visible
    WebDriverWait(driver, 10).until(EC.visibility_of_element_located((By.XPATH, xpath2_decrypted)))
    

    input_element2 = driver.find_element(By.XPATH,xpath2_decrypted)
    input_element2.send_keys(alqjs_decrypted)
    input_element2.send_keys(Keys.ENTER)


if __name__ == '__main__':

    ## python py_you.py --url "https://www.disneyplus.com/ko-kr/login" --id_xpath "//*[@id='email']" --pw_xpath "//*[@id='password']" --dkdlel "아이디" --alqjs "삐뻔"
    parser = argparse.ArgumentParser(description='Decrypt args and launch browser.')
    parser.add_argument('--url', required=True, help='The website URL to visit')  # URL 인자 추가
    parser.add_argument('--id_xpath', default=None, help='The encrypted xpath argument')
    parser.add_argument('--pw_xpath', default=None, help='The encrypted xpath2 argument')
    parser.add_argument('--dkdlel', default=None, help='The encrypted dkdlel argument')
    parser.add_argument('--alqjs', default=None, help='The encrypted alqjs argument')

    args = parser.parse_args()
    main(args.url,args.id_xpath, args.pw_xpath, args.dkdlel, args.alqjs)
