chrome.runtime.onInstalled.addListener(() => {
    chrome.action.onClicked.addListener((tab) => {
        var port = chrome.runtime.connectNative('com.happy.loginAgent');
        port.postMessage({ url: tab.url });
        port.onMessage.addListener((msg) => {
            console.log("Received" + msg);
        });
        port.onDisconnect.addListener(() => {
            console.log("Disconnected");
        });
    });
});
