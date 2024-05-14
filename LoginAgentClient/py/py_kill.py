import psutil
from pywinauto.application import Application
from pywinauto.findwindows import find_window

def get_browser_url(process_id):
    try:
        app = Application(backend="uia").connect(process=process_id)
        dlg = app.window()
        # 주소 표시줄(Edit 컨트롤) 찾기
        url_bar = dlg.child_window(control_type="Edit", found_index=0)
        url = url_bar.get_value()
        if url:
            print(url)
            return url
    except Exception as e:
        print(f"Error: {e}")
        return None

def kill_account_page():
    target_urls = [
        "YourAccount", "uflix.co.kr/uws/web/mine/userInfo", "member.wavve.com/me", "tving.com/my/main",
        "tving.com/my/watch", "netflix.com/ManageProfiles", "edit-profiles", "profiles/manage", "profilesForEdit",
        "profileForEdit", "wavve.com/my", "wavve.com/voucher", "membership/tving", "app-settings",
        "help.disneyplus.com", "/edit-profile/", "passwords", "/account", "netflix.com/profiles/manage"
    ]

    # Edge와 Chrome 프로세스 리스트 생성
    processes = [p for p in psutil.process_iter(attrs=['pid', 'name']) if p.info['name'] in ('msedge.exe', 'chrome.exe')]

    for process in processes:
        try:
            url = get_browser_url(process.info['pid'])
            if url and any(target in url for target in target_urls):
                print(f"Killing process {process.info['name']} with URL {url}")
                process.kill()
        except Exception as e:
            print(f"Error in kill_account_page: {e}")

# 메인 실행 함수
if __name__ == "__main__":
    kill_account_page()
