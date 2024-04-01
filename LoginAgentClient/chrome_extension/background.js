chrome.tabs.onUpdated.addListener(function(tabId, changeInfo, tab) {
  if (changeInfo.status === 'complete' && tab.active) {
    // URL을 네이티브 애플리케이션으로 전송
    let message = { url: tab.url };
    chrome.runtime.sendNativeMessage('com.happy.loginAgent', message, function(response) {
      console.log(response.reply);
    });
  }
});
