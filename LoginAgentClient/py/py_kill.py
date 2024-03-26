import configparser
import requests
import psutil

# config.ini 파일에서 URL 읽기
config = configparser.ConfigParser()
config.read('config.ini')
server_url = f"http://{config['server']['url']}/dist/urllist.txt"

# 서버에서 URL 리스트 다운로드
response = requests.get(server_url)
url_list = response.text.splitlines()

# 현재 실행 중인 프로세스 검색
for proc in psutil.process_iter(['name', 'cmdline']):
    try:
        # 프로세스의 명령줄 인수 중 하나가 URL 리스트에 있는지 확인
        if any(url in cmd for url in url_list for cmd in proc.info['cmdline']):
            proc.kill()  # URL 리스트에 해당하는 프로세스를 강제 종료
            print(f"Process {proc.pid} terminated.")
    except (psutil.NoSuchProcess, psutil.AccessDenied, psutil.ZombieProcess):
        pass
