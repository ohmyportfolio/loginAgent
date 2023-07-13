import argparse
import undetected_chromedriver as uc
from pycryptodome.Cipher import AES
from pycryptodome.Util.Padding import unpad
from base64 import b64decode
import hashlib

class My_Chrome(uc.Chrome):
    def __del__(self):
        pass

def decrypt(cipher_text, key):
    cipher_text = b64decode(cipher_text)  # decode from Base64
    key = hashlib.sha256(key.encode()).digest()[:16]  # Hash the key
    cipher = AES.new(key, AES.MODE_CBC, key)  # Create a new cipher using key and IV
    plain_bytes = unpad(cipher.decrypt(cipher_text[AES.block_size:]), AES.block_size)  # Decrypt and unpad the result
    return plain_bytes.decode()  # Convert bytes to string

def main(xpath, xpath2, dkdlel, alqjs):
    driver = My_Chrome()

    # If these are encrypted, you can use your decrypt function to decrypt them before use
    xpath_decrypted = decrypt(xpath, 'your_key_here')
    xpath2_decrypted = decrypt(xpath2, 'your_key_here')
    dkdlel_decrypted = decrypt(dkdlel, 'your_key_here')
    alqjs_decrypted = decrypt(alqjs, 'your_key_here')

    # Here you would use the decrypted arguments to set up your browser
    # For example:
    driver.get('https://www.youtube.com/account')
    # driver.find_element_by_xpath(xpath_decrypted)
    # ...
    # ...

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Decrypt args and launch browser.')
    parser.add_argument('xpath', help='The encrypted xpath argument')
    parser.add_argument('xpath2', help='The encrypted xpath2 argument')
    parser.add_argument('dkdlel', help='The encrypted dkdlel argument')
    parser.add_argument('alqjs', help='The encrypted alqjs argument')

    args = parser.parse_args()
    main(args.xpath, args.xpath2, args.dkdlel, args.alqjs)
