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

class My_Chrome(uc.Chrome):
    def __del__(self):
        pass

def decrypt(cipher_text, key):
    cipher_text = b64decode(cipher_text)  # decode from Base64
    key = hashlib.sha256(key.encode()).digest()[:16]  # Hash the key
    cipher = AES.new(key, AES.MODE_CBC, key)  # Create a new cipher using key and IV
    plain_bytes = unpad(cipher.decrypt(cipher_text[AES.block_size:]), AES.block_size)  # Decrypt and unpad the result
    return plain_bytes.decode()  # Convert bytes to string

def main(xpath=None, xpath2=None, dkdlel=None, alqjs=None , loginPath=None):
    driver = My_Chrome()

    # If these are encrypted, you can use your decrypt function to decrypt them before use
    xpath_decrypted = decrypt(xpath, 'your_key_here') if xpath else None
    xpath2_decrypted = decrypt(xpath2, 'your_key_here') if xpath2 else None
    dkdlel_decrypted = decrypt(dkdlel, 'your_key_here') if dkdlel else None
    alqjs_decrypted = decrypt(alqjs, 'your_key_here') if alqjs else None
    loginPath_decrypted = decrypt(loginPath, 'your_key_here') if loginPath else None

    # Here you would use the decrypted arguments to set up your browser
    # For example:
    driver.get('https://www.youtube.com/account')

  
    input_element = driver.find_element(By.XPATH,xpath_decrypted)
    input_element.send_keys(dkdlel_decrypted)
    input_element.send_keys(Keys.ENTER)

       # wait for the password field to be visible
    WebDriverWait(driver, 10).until(EC.visibility_of_element_located((By.XPATH, xpath2_decrypted)))

    input_element2 = driver.find_element(By.XPATH,xpath2_decrypted)
    input_element2.send_keys(alqjs_decrypted)
    input_element2.send_keys(Keys.ENTER)

    WebDriverWait(driver, 10).until(EC.visibility_of_element_located((By.ID,loginPath_decrypted)))

    driver.get('https://www.youtube.com')
    


    # driver.find_element_by_xpath(xpath_decrypted)
    # ...
    # ...

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Decrypt args and launch browser.')
    parser.add_argument('--xpath', default=None, help='The encrypted xpath argument')
    parser.add_argument('--xpath2', default=None, help='The encrypted xpath2 argument')
    parser.add_argument('--dkdlel', default=None, help='The encrypted dkdlel argument')
    parser.add_argument('--alqjs', default=None, help='The encrypted alqjs argument')
    parser.add_argument('--loginPath',default=None, help='The encrypted loginPath argument')

    args = parser.parse_args()
    main(args.xpath, args.xpath2, args.dkdlel, args.alqjs , args.loginPath)
