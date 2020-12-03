import client from './client'
import notify from 'devextreme/ui/notify';
import { confirm } from 'devextreme/ui/dialog';

export default {
  authenticated() {
    return this.user && this.user.id;
  },
  async load() {
    let result = await client.request({ method: 'get', url: '/api/user'});
    if(result.id) {
      this.user = result;
      if(this.onLoaded)
        await this.onLoaded();
      return result;
    }
  },
  async login(login, password) {
    let data = await client.request({ method: 'post', url: '/api/login', data: { id: login, password: password }});
    if (data.success) {
      this.user = data.user;
      if(this.onLoaded)
        await this.onLoaded();
    } else {
      notify({message: data.message}, 'error', 10000);
    }
    return this.user;
  },
  logout() {
    this.user = null;
    client.request({ method: 'get', url: '/api/logout'});
  },
  async ssoLogin() {
    var ua = window.navigator.userAgent;
    var isIE = ua.indexOf('Trident/');

    if(isIE > 0) {
      if(EpAdmc !== undefined && EpAdmc.GetSecureBox !== undefined && EpAdmc.GetSecureBox !== null) {
        let ssoToken = EpAdmc.GetSecureBox();
        if (ssoToken === '' || ssoToken === null) {
          let confirmed = await confirm('KNOX 로그인이 되어있지 않거나 본 시스템이 신뢰할 수 있는 사이트에 추가되어 있지 않습니다.<br> "도구 > 인터넷 옵션 > 보안 > 신뢰할 수 있는 사이트 > 사이트 > 추가"를 클릭하여 본 시스엠을 신뢰할 수 있는 사이트로 추가해주세요. <br> KNOX 로그인 페이지로 이동하시겠습니까?', "확인");
          if(confirmed) {
            window.location.href = 'http://www.samsung.net';
          }
        } else {
          let data = await client.request({ method: 'post', url: '/api/ssoLogin', data: {ssoToken: ssoToken}});
          if (data.success) {
            this.user = data.user;
          } else {
            notify({message: data.message}, 'error', 10000);
          }
          return this.user;
        }
      } else {
        await alert('KNOX SSO 로그인을 위해서 본 시스템을 신뢰할 수 있는 사이트에 추가해주세요. (도구 > 인터넷 옵션 > 보안 > 신뢰할 수 있는 사이트 > 추가)', "확인");
      }
    }
  },
  async isResetPassword() {
    if(this.authenticated()) {
      let result = await client.request({ method: 'get', url: '/api/users/' + this.user.id, params: {select: 'is_password_use'}});
      return result.is_password_use ? false : true;
    } else {
      return false;
    }
  }

};
